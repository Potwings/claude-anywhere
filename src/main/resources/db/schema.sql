-- Claude Code Telegram Bot Database Schema
-- MySQL 8.0

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS claude_telegram_bot
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE claude_telegram_bot;

-- User table
-- Stores Telegram user information
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `telegram_id` BIGINT NOT NULL UNIQUE COMMENT 'Telegram User ID',
    `username` VARCHAR(255) NULL COMMENT 'Telegram Username',
    `first_name` VARCHAR(255) NULL COMMENT 'Telegram First Name',
    `last_name` VARCHAR(255) NULL COMMENT 'Telegram Last Name',
    `language_code` VARCHAR(10) NULL COMMENT 'User Language Code',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'User Active Status',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_telegram_id` (`telegram_id`),
    INDEX `idx_user_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Project table
-- Stores user projects
CREATE TABLE IF NOT EXISTS `project` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'Reference to user.id',
    `name` VARCHAR(255) NOT NULL COMMENT 'Project Name',
    `description` TEXT NULL COMMENT 'Project Description',
    `working_directory` VARCHAR(1024) NULL COMMENT 'Project Working Directory Path',
    `status` ENUM('ACTIVE', 'ARCHIVED', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Project Status',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_project_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_project_user_name` (`user_id`, `name`),
    INDEX `idx_project_user_id` (`user_id`),
    INDEX `idx_project_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Session table
-- Stores user session state (currently selected project, etc.)
CREATE TABLE IF NOT EXISTS `user_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT 'Reference to user.id',
    `current_project_id` BIGINT NULL COMMENT 'Currently selected project',
    `state` VARCHAR(100) NULL COMMENT 'Current conversation state',
    `state_data` JSON NULL COMMENT 'Additional state data',
    `last_activity_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_session_project` FOREIGN KEY (`current_project_id`) REFERENCES `project`(`id`) ON DELETE SET NULL,
    INDEX `idx_session_user_id` (`user_id`),
    INDEX `idx_session_last_activity` (`last_activity_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
