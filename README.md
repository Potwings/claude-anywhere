# Claude Anywhere

Telegram bot for remote control of Claude Code on WSL2 - code anywhere from your phone.

텔레그램 메신저를 통해 집 PC(WSL2)의 Claude Code를 원격 제어하는 봇입니다.

## Features

### Phase 1 (Current)
- `/start` - 봇 시작 및 환영 메시지
- `/help` - 도움말 표시
- `/newproject <name>` - 새 프로젝트 생성
- `/projects` - 프로젝트 목록 조회
- `/select <name>` - 프로젝트 선택
- `/current` - 현재 프로젝트 확인
- `/archive <name>` - 프로젝트 아카이브
- `/delete <name>` - 프로젝트 삭제

### Upcoming
- **Phase 2**: Claude Code 연동 + 실시간 로그 스트리밍
- **Phase 3**: 결과 확인 (changes, diff, download)
- **Phase 4**: Git 연동 (commit, push, branch)
- **Phase 5**: 사용자 설정 + 에러 처리

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.5.0 |
| Language | Java 21 LTS |
| Bot Library | TelegramBots 8.0.0 |
| ORM | MyBatis 3.0.4 |
| Database | MariaDB / MySQL 8.0 |
| Build Tool | Gradle |

## Project Structure

```
src/main/java/kr/yonggeon/claudebot/
├── ClaudeCodeTelegramBotApplication.java
├── config/           # Bot configuration
├── domain/           # User, Project, UserSession entities
├── mapper/           # MyBatis mappers
├── service/          # Business logic
├── bot/
│   ├── command/      # Command handlers (8 commands)
│   ├── callback/     # Inline button callbacks
│   └── keyboard/     # Inline keyboard factory
├── util/             # MessageSender utility
└── exception/        # Custom exceptions
```

## Setup

### Prerequisites
- Java 21
- MariaDB or MySQL 8.0
- Telegram Bot Token (from [@BotFather](https://t.me/BotFather))

### Environment Variables

```bash
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_ALLOWED_USERS=123456789,987654321  # Optional
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
```

### Database Setup

```sql
CREATE DATABASE claude_bot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Run the schema: `src/main/resources/db/schema.sql`

### Run

```bash
# Development
./gradlew bootRun --args='--spring.profiles.active=dev'

# Build
./gradlew build

# Run JAR
java -jar build/libs/claude-code-telegram-bot-0.0.1-SNAPSHOT.jar
```

## Usage Scenarios

### Scenario 1: Start a project on the subway
```
1. /start - Start the bot
2. /newproject blog-api - Create a new project
3. Send prompt: "Spring Boot로 블로그 REST API 만들어줘"
4. Receive real-time logs
5. Check results with [변경사항] button
```

### Scenario 2: Quick bug fix from cafe
```
1. /select my-project - Select project
2. Send: "NullPointerException 버그 수정해줘"
3. Review changes
4. /commit "Fix NPE bug"
5. /push
```

## License

MIT License
