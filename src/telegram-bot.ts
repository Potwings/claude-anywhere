/**
 * Telegram Bot + Claude Agent SDK 연동
 * 실행: npm run dev:bot
 */
import { query, type Query, type SDKMessage } from "@anthropic-ai/claude-agent-sdk";
import { Telegraf, type Context } from "telegraf";
import { readFileSync, writeFileSync, existsSync } from "fs";
import { join } from "path";
import "dotenv/config";

// ============================================================
// 로깅 유틸
// ============================================================
function ts() {
  return new Date().toISOString();
}

function log(tag: string, ...args: unknown[]) {
  console.log(`[${ts()}][${tag}]`, ...args);
}

function logError(tag: string, ...args: unknown[]) {
  console.error(`[${ts()}][${tag}]`, ...args);
}

// ============================================================
// 세션 영속화 (JSON 파일)
// ============================================================
type SessionRecord = {
  sessionId: string;
  prompt: string;
  cwd: string;
  createdAt: string;
  status: "running" | "done" | "error" | "cancelled";
};

type SessionStore = {
  [userId: string]: SessionRecord[];
};

const SESSION_FILE = join(process.cwd(), "sessions.json");

function loadSessions(): SessionStore {
  if (!existsSync(SESSION_FILE)) return {};
  try {
    return JSON.parse(readFileSync(SESSION_FILE, "utf-8"));
  } catch (error) {
    console.error(`Failed to load sessions from ${SESSION_FILE}:`, error);
    return {};
  }
}

function saveSessions(store: SessionStore) {
  writeFileSync(SESSION_FILE, JSON.stringify(store, null, 2));
}

function addSession(userId: number, record: SessionRecord) {
  const store = loadSessions();
  const key = String(userId);
  if (!store[key]) store[key] = [];
  // 같은 sessionId가 있으면 업데이트
  const idx = store[key].findIndex((s) => s.sessionId === record.sessionId);
  if (idx >= 0) {
    store[key][idx] = record;
  } else {
    store[key].push(record);
  }
  saveSessions(store);
}

function updateSessionStatus(userId: number, sessionId: string, status: SessionRecord["status"]) {
  const store = loadSessions();
  const key = String(userId);
  const session = store[key]?.find((s) => s.sessionId === sessionId);
  if (session) {
    session.status = status;
    saveSessions(store);
  }
}

function getUserSessions(userId: number): SessionRecord[] {
  const store = loadSessions();
  return store[String(userId)] ?? [];
}

// ============================================================
// 텔레그램 전송 (rate limit 대응 + 재시도 + 타이밍 로그)
// ============================================================
async function sendLog(ctx: Context, text: string) {
  if (!text.trim()) return;
  const truncated = text.length > 4000 ? text.slice(0, 3997) + "..." : text;
  for (let attempt = 0; attempt < 3; attempt++) {
    const start = Date.now();
    try {
      await ctx.reply(truncated);
      log("reply", `ok (${Date.now() - start}ms)`);
      return;
    } catch (err: any) {
      const elapsed = Date.now() - start;
      const code = err?.response?.error_code;
      const desc = err?.response?.description;
      const retryAfter = err?.response?.parameters?.retry_after;

      logError("reply", `FAIL attempt=${attempt + 1} elapsed=${elapsed}ms code=${code} desc=${desc} retryAfter=${retryAfter} name=${err.name} message=${err.message}`);

      if (retryAfter) {
        await new Promise((r) => setTimeout(r, retryAfter * 1000));
      } else if (attempt < 2) {
        await new Promise((r) => setTimeout(r, 2000));
      }
    }
  }
}

// ============================================================
// SDK 메시지에서 로그 텍스트 추출
// ============================================================
function formatLogMessage(message: SDKMessage): string | null {
  switch (message.type) {
    case "system":
      if (message.subtype === "init") {
        return `[init] model: ${message.model} | tools: ${message.tools.join(", ")}`;
      }
      return null;

    case "assistant":
      if (!message.message?.content) return null;
      const parts: string[] = [];
      for (const block of message.message.content) {
        if ("text" in block && block.text) {
          parts.push(block.text);
        } else if ("type" in block && block.type === "tool_use") {
          const b = block as { name: string; input: unknown };
          const inputStr = JSON.stringify(b.input);
          const short = inputStr.length > 200 ? inputStr.slice(0, 197) + "..." : inputStr;
          parts.push(`[tool_call] ${b.name}(${short})`);
        }
      }
      return parts.length > 0 ? parts.join("\n") : null;

    case "result":
      return null;

    default:
      return null;
  }
}

// ============================================================
// Bot 초기화
// ============================================================
if (!process.env.TELEGRAM_BOT_TOKEN) {
  console.error("TELEGRAM_BOT_TOKEN is not set. Check your .env file.");
  process.exit(1);
}

const bot = new Telegraf(process.env.TELEGRAM_BOT_TOKEN, {
  handlerTimeout: Infinity,
});

// 허용된 Telegram 유저 ID (쉼표 구분)
const adminIds = new Set(
  (process.env.TELEGRAM_ADMIN_IDS ?? "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean)
    .map(Number)
);

if (adminIds.size > 0) {
  log("bot", `Admin whitelist: ${[...adminIds].join(", ")}`);
  bot.use((ctx, next) => {
    const userId = ctx.from?.id;
    if (!userId || !adminIds.has(userId)) {
      log("bot", `Blocked user ${userId}`);
      return;
    }
    return next();
  });
} else {
  log("bot", "No TELEGRAM_ADMIN_IDS set — all users allowed");
}

// 현재 활성 세션 (메모리)
const activeSessionIds = new Map<number, string>();
const userWorkDirs = new Map<number, string>();
const runningQueries = new Map<number, Query>();

// ============================================================
// 커맨드 핸들러
// ============================================================
bot.command("start", (ctx) => {
  ctx.reply(
    "Claude Code Bot (TypeScript)\n\n" +
      "Commands:\n" +
      "/setdir <path> - Set working directory\n" +
      "/sessions - List previous sessions\n" +
      "/resume <number> - Resume a session\n" +
      "/status - Show current session info\n" +
      "/reset - Start new session\n" +
      "/cancel - Cancel running task\n\n" +
      "Send any message to execute Claude Code."
  );
});

bot.command("setdir", (ctx) => {
  const dir = ctx.message.text.replace("/setdir", "").trim();
  if (!dir) {
    ctx.reply("Usage: /setdir /path/to/project");
    return;
  }
  userWorkDirs.set(ctx.from.id, dir);
  ctx.reply(`Working directory set: ${dir}`);
});

bot.command("status", (ctx) => {
  const sessionId = activeSessionIds.get(ctx.from.id);
  const workDir = userWorkDirs.get(ctx.from.id);
  const isRunning = runningQueries.has(ctx.from.id);

  ctx.reply(
    `Session: ${sessionId ?? "none"}\n` +
      `Directory: ${workDir ?? "not set"}\n` +
      `Running: ${isRunning ? "yes" : "no"}`
  );
});

bot.command("reset", (ctx) => {
  activeSessionIds.delete(ctx.from.id);
  ctx.reply("Session cleared. Next message will start a new session.");
});

bot.command("cancel", async (ctx) => {
  const running = runningQueries.get(ctx.from.id);
  if (!running) {
    ctx.reply("No running task.");
    return;
  }

  const sessionId = activeSessionIds.get(ctx.from.id);
  try {
    await running.interrupt();
  } catch (error) {
    console.error(`Failed to interrupt query for user ${ctx.from.id}:`, error);
    if (sessionId) updateSessionStatus(ctx.from.id, sessionId, "error");
    runningQueries.delete(ctx.from.id);
    ctx.reply("Cancel failed, but task has been cleaned up.");
    return;
  }

  if (sessionId) updateSessionStatus(ctx.from.id, sessionId, "cancelled");
  runningQueries.delete(ctx.from.id);
  ctx.reply("Task cancelled.");
});

// /sessions - 이전 세션 목록
bot.command("sessions", (ctx) => {
  const sessions = getUserSessions(ctx.from.id);
  if (sessions.length === 0) {
    ctx.reply("No saved sessions.");
    return;
  }

  const currentId = activeSessionIds.get(ctx.from.id);
  const lines = sessions.map((s, i) => {
    const isCurrent = s.sessionId === currentId ? " [active]" : "";
    const prompt = s.prompt.length > 40 ? s.prompt.slice(0, 37) + "..." : s.prompt;
    const date = s.createdAt.slice(0, 16).replace("T", " ");
    return `${i + 1}. [${s.status}]${isCurrent} ${date}\n   ${prompt}\n   cwd: ${s.cwd}`;
  });

  ctx.reply(`Sessions:\n\n${lines.join("\n\n")}\n\nUse /resume <number> to resume.`);
});

// /resume <number> - 세션 이어가기
bot.command("resume", (ctx) => {
  const arg = ctx.message.text.replace("/resume", "").trim();
  const sessions = getUserSessions(ctx.from.id);

  if (!arg) {
    // 인자 없으면 마지막 세션 resume
    if (sessions.length === 0) {
      ctx.reply("No sessions to resume. Use /sessions to check.");
      return;
    }
    const last = sessions[sessions.length - 1];
    activeSessionIds.set(ctx.from.id, last.sessionId);
    if (last.cwd) userWorkDirs.set(ctx.from.id, last.cwd);
    ctx.reply(
      `Resumed last session:\n` +
        `ID: ${last.sessionId.slice(0, 8)}...\n` +
        `Prompt: ${last.prompt.slice(0, 50)}\n` +
        `CWD: ${last.cwd}\n\n` +
        `Send a message to continue.`
    );
    return;
  }

  const num = parseInt(arg, 10);
  if (isNaN(num) || num < 1 || num > sessions.length) {
    ctx.reply(`Invalid number. Use 1~${sessions.length}. See /sessions.`);
    return;
  }

  const selected = sessions[num - 1];
  activeSessionIds.set(ctx.from.id, selected.sessionId);
  if (selected.cwd) userWorkDirs.set(ctx.from.id, selected.cwd);
  ctx.reply(
    `Resumed session #${num}:\n` +
      `ID: ${selected.sessionId.slice(0, 8)}...\n` +
      `Prompt: ${selected.prompt.slice(0, 50)}\n` +
      `CWD: ${selected.cwd}\n\n` +
      `Send a message to continue.`
  );
});

// ============================================================
// 메인 메시지 핸들러
// ============================================================
bot.on("text", async (ctx) => {
  const userId = ctx.from.id;
  const prompt = ctx.message.text;
  const workDir = userWorkDirs.get(userId) ?? process.cwd();

  if (runningQueries.has(userId)) {
    ctx.reply("A task is already running. Use /cancel to stop it.");
    return;
  }

  log("handler", `user=${userId} prompt="${prompt.slice(0, 50)}..." cwd=${workDir}`);

  const statusMsg = await ctx.reply("Processing...");
  const handlerStart = Date.now();

  let newSessionId: string | undefined;

  try {
    const sessionId = activeSessionIds.get(userId);

    log("sdk", `query start (resume=${sessionId ?? "new"})`);
    const queryStart = Date.now();

    const q = query({
      prompt,
      options: {
        allowedTools: ["Read", "Edit", "Write", "Bash", "Glob", "Grep"],
        permissionMode: "bypassPermissions",
        allowDangerouslySkipPermissions: true,
        cwd: workDir,
        ...(sessionId ? { resume: sessionId } : {}),
      },
    });

    runningQueries.set(userId, q);

    let resultText = "";
    let msgCount = 0;

    for await (const message of q) {
      msgCount++;
      const elapsed = Date.now() - queryStart;
      log("sdk", `msg #${msgCount} type=${message.type}${("subtype" in message && message.subtype) ? `/` + message.subtype : ""} elapsed=${elapsed}ms`);

      // 세션 ID 캡처
      if (message.type === "system" && message.subtype === "init") {
        newSessionId = message.session_id;
        // 세션 저장 (신규) 또는 상태 업데이트 (resume)
        addSession(userId, {
          sessionId: message.session_id,
          prompt: prompt.slice(0, 200),
          cwd: workDir,
          createdAt: new Date().toISOString(),
          status: "running",
        });
        activeSessionIds.set(userId, message.session_id);
      }

      // 중간 진행 로그 전송
      const logText = formatLogMessage(message);
      if (logText) {
        await sendLog(ctx, logText);
      }

      // 최종 결과
      if (message.type === "result") {
        if (message.subtype === "success") {
          resultText = message.result;
          if (newSessionId) updateSessionStatus(userId, newSessionId, "done");
        } else {
          const errorDetail = [
            `[Error] subtype: ${message.subtype}`,
            `stop_reason: ${message.stop_reason ?? "null"}`,
            `num_turns: ${message.num_turns}`,
            `duration: ${message.duration_ms}ms`,
            `cost: $${message.total_cost_usd}`,
          ];
          if ("errors" in message && message.errors?.length) {
            errorDetail.push(`errors: ${message.errors.join(" | ")}`);
          }
          if ("permission_denials" in message && message.permission_denials?.length) {
            const denials = message.permission_denials.map(
              (d) => `${d.tool_name}(${JSON.stringify(d.tool_input).slice(0, 100)})`
            );
            errorDetail.push(`permission_denied: ${denials.join(", ")}`);
          }
          resultText = errorDetail.join("\n");
          logError("sdk-result", resultText);
          if (newSessionId) updateSessionStatus(userId, newSessionId, "error");
        }

        // (활성 세션은 init 시점에 이미 설정됨)
      }
    }

    log("sdk", `query done msgs=${msgCount} elapsed=${Date.now() - queryStart}ms`);

    // 에러 결과만 별도 전송 (성공 시 assistant 메시지로 이미 전송됨)
    if (resultText && resultText.startsWith("[Error]")) {
      await sendLog(ctx, resultText);
    }
  } catch (error: any) {
    const errMsg = error.message || "unknown";
    const errStack = error.stack || "";
    const errName = error.name || error.constructor?.name || "Error";
    const elapsed = Date.now() - handlerStart;
    const detail = `[${errName}] ${errMsg} (elapsed=${elapsed}ms)`;
    logError("catch", `${detail}\n${errStack}`);
    await sendLog(ctx, `Error:\n${detail}`);
    if (newSessionId) {
      try {
        updateSessionStatus(userId, newSessionId, "error");
      } catch (statusErr: any) {
        logError("catch", `Failed to update session status: ${statusErr.message}`);
      }
    }
  } finally {
    runningQueries.delete(userId);
    log("handler", `done user=${userId} total=${Date.now() - handlerStart}ms`);
    ctx.telegram
      .deleteMessage(ctx.chat.id, statusMsg.message_id)
      .catch(() => {});
  }
});

// ============================================================
// 에러 핸들링
// ============================================================
bot.catch((err: any, ctx) => {
  logError("telegraf", `updateType=${ctx.updateType} name=${err.name} message=${err.message}`);
  logError("telegraf", err.stack);
});

process.on("uncaughtException", (err) => {
  logError("UNCAUGHT", `name=${err.name} message=${err.message}`);
  logError("UNCAUGHT", err.stack);
});

process.on("unhandledRejection", (reason: any) => {
  logError("UNHANDLED_REJECTION", `type=${typeof reason} name=${reason?.name} message=${reason?.message}`);
  if (reason?.stack) logError("UNHANDLED_REJECTION", reason.stack);
});

// ============================================================
// 시작
// ============================================================
bot.launch({
  dropPendingUpdates: true,
  allowedUpdates: ["message"],
});
log("boot", "Telegram bot started.");

process.once("SIGINT", () => bot.stop("SIGINT"));
process.once("SIGTERM", () => bot.stop("SIGTERM"));
