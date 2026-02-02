# Claude Code Telegram Bot

## Project Overview
텔레그램 봇을 통해 WSL2의 Claude Code를 원격 제어하는 시스템입니다.

## Development Environment
- **OS**: Windows (WSL2 배포 대상)
- **Build Tool**: Gradle
- **Package**: `kr.yonggeon.claudebot`

## Tech Stack
- **Framework**: Spring Boot 3.5.0
- **Language**: Java 21 LTS
- **ORM**: MyBatis 3.0.4
- **Bot Library**: TelegramBots 9.0.0
- **Database**: MySQL 8.0

## Project Structure
```
src/main/java/kr/yonggeon/claudebot/
├── ClaudeCodeTelegramBotApplication.java  # Main entry point
├── config/
│   ├── TelegramBotConfig.java             # Bot configuration & TelegramClient bean
│   └── BotStartupLogger.java              # Bot startup logging
├── domain/
│   ├── User.java                          # User entity
│   ├── Project.java                       # Project entity with status enum
│   └── UserSession.java                   # Session entity for state management
├── mapper/
│   ├── UserMapper.java                    # User MyBatis mapper
│   ├── ProjectMapper.java                 # Project MyBatis mapper
│   └── UserSessionMapper.java             # Session MyBatis mapper
├── service/
│   ├── UserService.java                   # User business logic
│   ├── ProjectService.java                # Project CRUD operations
│   └── SessionService.java                # Session & state management
├── bot/
│   ├── ClaudeCodeBot.java                 # Main bot handler
│   ├── command/
│   │   ├── CommandHandler.java            # Command interface
│   │   ├── CommandRouter.java             # Routes commands to handlers
│   │   ├── StartCommand.java              # /start
│   │   ├── HelpCommand.java               # /help
│   │   ├── NewProjectCommand.java         # /newproject
│   │   ├── ProjectsCommand.java           # /projects
│   │   ├── SelectCommand.java             # /select
│   │   ├── CurrentCommand.java            # /current
│   │   ├── ArchiveCommand.java            # /archive
│   │   └── DeleteCommand.java             # /delete
│   ├── callback/
│   │   └── CallbackHandler.java           # Inline button callbacks
│   └── keyboard/
│       └── InlineKeyboardFactory.java     # Keyboard builder utility
├── util/
│   └── MessageSender.java                 # Telegram message utilities
└── exception/
    ├── BotException.java                  # Base exception
    ├── ProjectNotFoundException.java      # Project not found
    └── DuplicateProjectException.java     # Duplicate project name

src/main/resources/
├── application.yml                        # Main config
├── application-dev.yml                    # Dev environment config
├── db/
│   └── schema.sql                         # Database schema
└── mapper/
    ├── UserMapper.xml                     # User SQL mappings
    ├── ProjectMapper.xml                  # Project SQL mappings
    └── UserSessionMapper.xml              # Session SQL mappings
```

## Environment Variables
```bash
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_ALLOWED_USERS=123456789,987654321  # Optional: comma-separated user IDs
DB_USERNAME=root
DB_PASSWORD=password
```

## Commands (Phase 1)
| Command | Description |
|---------|-------------|
| `/start` | Start bot and see welcome message |
| `/help` | Show available commands |
| `/newproject <name>` | Create a new project |
| `/projects` | List all projects |
| `/select <name>` | Select a project |
| `/current` | Show current project info |
| `/archive <name>` | Archive a project |
| `/delete <name>` | Delete a project (with confirmation) |

## Database Schema
- **user**: Telegram user info (telegram_id, username, first_name, etc.)
- **project**: User projects (name, description, working_directory, status)
- **user_session**: User session state (current_project_id, state, state_data)

## Running the Application
```bash
# Set environment variables
export TELEGRAM_BOT_TOKEN=your_token
export TELEGRAM_BOT_USERNAME=your_bot_username
export DB_USERNAME=root
export DB_PASSWORD=password

# Run with dev profile (Gradle)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Build JAR
./gradlew build
```

## Key Design Decisions
1. **User Authentication**: Based on Telegram User ID with optional allowlist
2. **Soft Delete**: Projects use status (ACTIVE/ARCHIVED/DELETED) instead of hard delete
3. **Auto-select**: New projects are automatically selected after creation
4. **Session Management**: Each user has a session tracking current project and conversation state

## Git Workflow
**master 브랜치에 직접 커밋하지 않고, 반드시 PR을 통해 merge합니다.**

```bash
# 1. feature 브랜치 생성
git checkout -b feature/작업명

# 2. 작업 & 커밋
git add 파일들
git commit -m "커밋 메시지"

# 3. 브랜치 push
git push -u origin feature/작업명

# 4. PR 생성 (gh CLI 사용)
gh pr create --title "PR 제목" --body "PR 설명"
```

### Branch Naming Convention
- `feature/기능명` - 새 기능 개발
- `fix/버그명` - 버그 수정
- `refactor/대상` - 리팩토링
- `docs/문서명` - 문서 작업
