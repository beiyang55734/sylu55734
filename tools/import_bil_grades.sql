SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `bil_grades`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `bil_grades`;

DROP TABLE IF EXISTS `choose`;
DROP TABLE IF EXISTS `grade`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NULL,
  `password` VARCHAR(255) NULL,
  `name` VARCHAR(255) NULL,
  `phone` VARCHAR(255) NULL,
  `email` VARCHAR(255) NULL,
  `address` VARCHAR(255) NULL,
  `avatar` VARCHAR(500) NULL,
  `sex` VARCHAR(50) NULL,
  `age` VARCHAR(50) NULL,
  `infos` TEXT NULL,
  `role` VARCHAR(50) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `course` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `content` TEXT NULL,
  `teacher` VARCHAR(255) NULL,
  `times` VARCHAR(255) NULL,
  `intervals` VARCHAR(255) NULL,
  `address` VARCHAR(255) NULL,
  `img` VARCHAR(500) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notice` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `content` LONGTEXT NULL,
  `create_date` VARCHAR(255) NULL,
  `user_id` VARCHAR(255) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `grade` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `studentname` VARCHAR(255) NULL,
  `studentid` INT NULL,
  `courseid` INT NULL,
  `score` DOUBLE NULL,
  `comment` TEXT NULL,
  `feedback` TEXT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `choose` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `studentid` INT NULL,
  `courseid` INT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOAD DATA LOCAL INFILE 'E:/计算机设计大赛/项目二/数据库/user.txt'
INTO TABLE `user`
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@id, @username, @password, @name, @phone, @email, @address, @avatar, @sex, @age, @infos, @role)
SET
  `id` = NULLIF(@id, ''),
  `username` = NULLIF(@username, ''),
  `password` = NULLIF(@password, ''),
  `name` = NULLIF(@name, ''),
  `phone` = NULLIF(@phone, ''),
  `email` = NULLIF(@email, ''),
  `address` = NULLIF(@address, ''),
  `avatar` = NULLIF(@avatar, ''),
  `sex` = NULLIF(@sex, ''),
  `age` = NULLIF(@age, ''),
  `infos` = NULLIF(@infos, ''),
  `role` = NULLIF(@role, '');

LOAD DATA LOCAL INFILE 'E:/计算机设计大赛/项目二/数据库/course.txt'
INTO TABLE `course`
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@id, @name, @content, @teacher, @times, @intervals, @address, @img)
SET
  `id` = NULLIF(@id, ''),
  `name` = NULLIF(@name, ''),
  `content` = NULLIF(@content, ''),
  `teacher` = NULLIF(@teacher, ''),
  `times` = NULLIF(@times, ''),
  `intervals` = NULLIF(@intervals, ''),
  `address` = NULLIF(@address, ''),
  `img` = NULLIF(@img, '');

LOAD DATA LOCAL INFILE 'E:/计算机设计大赛/项目二/数据库/notice.txt'
INTO TABLE `notice`
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@id, @name, @content, @create_date, @user_id)
SET
  `id` = NULLIF(@id, ''),
  `name` = NULLIF(@name, ''),
  `content` = NULLIF(@content, ''),
  `create_date` = NULLIF(@create_date, ''),
  `user_id` = NULLIF(@user_id, '');

LOAD DATA LOCAL INFILE 'E:/计算机设计大赛/项目二/数据库/grade.txt'
INTO TABLE `grade`
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@id, @name, @studentname, @studentid, @courseid, @score, @comment, @feedback)
SET
  `id` = NULLIF(@id, ''),
  `name` = NULLIF(@name, ''),
  `studentname` = NULLIF(@studentname, ''),
  `studentid` = NULLIF(@studentid, ''),
  `courseid` = NULLIF(@courseid, ''),
  `score` = NULLIF(@score, ''),
  `comment` = NULLIF(@comment, ''),
  `feedback` = NULLIF(@feedback, '');

LOAD DATA LOCAL INFILE 'E:/计算机设计大赛/项目二/数据库/choose.txt'
INTO TABLE `choose`
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@id, @name, @studentid, @courseid)
SET
  `id` = NULLIF(@id, ''),
  `name` = NULLIF(@name, ''),
  `studentid` = NULLIF(@studentid, ''),
  `courseid` = NULLIF(@courseid, '');

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'user' AS table_name, COUNT(*) AS row_count FROM `user`
UNION ALL
SELECT 'course' AS table_name, COUNT(*) AS row_count FROM `course`
UNION ALL
SELECT 'notice' AS table_name, COUNT(*) AS row_count FROM `notice`
UNION ALL
SELECT 'grade' AS table_name, COUNT(*) AS row_count FROM `grade`
UNION ALL
SELECT 'choose' AS table_name, COUNT(*) AS row_count FROM `choose`;
