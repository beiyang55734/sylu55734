$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$workspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$dataRoot = (Get-ChildItem $workspaceRoot -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'user.txt') } | Select-Object -First 1 -ExpandProperty FullName)

if (-not $dataRoot) {
    throw 'Could not locate the data directory that contains user.txt'
}

$dbFiles = @{
    user   = Join-Path $dataRoot 'user.txt'
    course = Join-Path $dataRoot 'course.txt'
    notice = Join-Path $dataRoot 'notice.txt'
    grade  = Join-Path $dataRoot 'grade.txt'
    choose = Join-Path $dataRoot 'choose.txt'
}

foreach ($entry in $dbFiles.GetEnumerator()) {
    if (-not (Test-Path $entry.Value)) {
        throw "Missing data file: $($entry.Value)"
    }
}

function Escape-SqlValue {
    param([string]$Value)

    if ([string]::IsNullOrEmpty($Value)) {
        return 'NULL'
    }

    $escaped = $Value.Replace('\', '\\').Replace("'", "''").Replace("`r", '\r').Replace("`n", '\n')
    return "'$escaped'"
}

function To-IntSqlValue {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return 'NULL'
    }

    return [int]$Value
}

function To-DoubleSqlValue {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return 'NULL'
    }

    return ([double]$Value).ToString([System.Globalization.CultureInfo]::InvariantCulture)
}

$userRows = Import-Csv $dbFiles.user -Delimiter "`t"
$courseRows = Import-Csv $dbFiles.course -Delimiter "`t"
$noticeRows = Import-Csv $dbFiles.notice -Delimiter "`t"
$gradeRows = Import-Csv $dbFiles.grade -Delimiter "`t"
$chooseRows = Import-Csv $dbFiles.choose -Delimiter "`t"

$sql = New-Object System.Collections.Generic.List[string]
$sql.Add('SET NAMES utf8mb4;')
$sql.Add('SET FOREIGN_KEY_CHECKS = 0;')
$sql.Add('CREATE DATABASE IF NOT EXISTS `bil_grades` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;')
$sql.Add('USE `bil_grades`;')
$sql.Add('DROP TABLE IF EXISTS `choose`;')
$sql.Add('DROP TABLE IF EXISTS `grade`;')
$sql.Add('DROP TABLE IF EXISTS `notice`;')
$sql.Add('DROP TABLE IF EXISTS `course`;')
$sql.Add('DROP TABLE IF EXISTS `user`;')

$sql.Add('CREATE TABLE `user` (`id` INT NOT NULL AUTO_INCREMENT, `username` VARCHAR(255) NULL, `password` VARCHAR(255) NULL, `name` VARCHAR(255) NULL, `phone` VARCHAR(255) NULL, `email` VARCHAR(255) NULL, `address` VARCHAR(255) NULL, `avatar` VARCHAR(500) NULL, `sex` VARCHAR(50) NULL, `age` VARCHAR(50) NULL, `infos` TEXT NULL, `role` VARCHAR(50) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')
$sql.Add('CREATE TABLE `course` (`id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NULL, `content` TEXT NULL, `teacher` VARCHAR(255) NULL, `times` VARCHAR(255) NULL, `intervals` VARCHAR(255) NULL, `address` VARCHAR(255) NULL, `img` VARCHAR(500) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')
$sql.Add('CREATE TABLE `notice` (`id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NULL, `content` LONGTEXT NULL, `create_date` VARCHAR(255) NULL, `user_id` VARCHAR(255) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')
$sql.Add('CREATE TABLE `grade` (`id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NULL, `studentname` VARCHAR(255) NULL, `studentid` INT NULL, `courseid` INT NULL, `score` DOUBLE NULL, `comment` TEXT NULL, `feedback` TEXT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')
$sql.Add('CREATE TABLE `choose` (`id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NULL, `studentid` INT NULL, `courseid` INT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')

foreach ($row in $userRows) {
    $sql.Add("INSERT INTO user (id,username,password,name,phone,email,address,avatar,sex,age,infos,role) VALUES ($([int]$row.id),$(Escape-SqlValue $row.username),$(Escape-SqlValue $row.password),$(Escape-SqlValue $row.name),$(Escape-SqlValue $row.phone),$(Escape-SqlValue $row.email),$(Escape-SqlValue $row.address),$(Escape-SqlValue $row.avatar),$(Escape-SqlValue $row.sex),$(Escape-SqlValue $row.age),$(Escape-SqlValue $row.infos),$(Escape-SqlValue $row.role));")
}

foreach ($row in $courseRows) {
    if ([string]::IsNullOrWhiteSpace($row.id)) { continue }
    $sql.Add("INSERT INTO course (id,name,content,teacher,times,intervals,address,img) VALUES ($([int]$row.id),$(Escape-SqlValue $row.name),$(Escape-SqlValue $row.content),$(Escape-SqlValue $row.teacher),$(Escape-SqlValue $row.times),$(Escape-SqlValue $row.intervals),$(Escape-SqlValue $row.address),$(Escape-SqlValue $row.img));")
}

foreach ($row in $noticeRows) {
    $sql.Add("INSERT INTO notice (id,name,content,create_date,user_id) VALUES ($([int]$row.id),$(Escape-SqlValue $row.name),$(Escape-SqlValue $row.content),$(Escape-SqlValue $row.time),$(Escape-SqlValue $row.user_id));")
}

foreach ($row in $gradeRows) {
    $sql.Add("INSERT INTO grade (id,name,studentname,studentid,courseid,score,comment,feedback) VALUES ($([int]$row.id),$(Escape-SqlValue $row.name),$(Escape-SqlValue $row.studentname),$(To-IntSqlValue $row.studentid),$(To-IntSqlValue $row.courseid),$(To-DoubleSqlValue $row.score),$(Escape-SqlValue $row.comment),$(Escape-SqlValue $row.feedback));")
}

foreach ($row in $chooseRows) {
    $sql.Add("INSERT INTO choose (id,name,studentid,courseid) VALUES ($([int]$row.id),$(Escape-SqlValue $row.name),$(To-IntSqlValue $row.studentid),$(To-IntSqlValue $row.courseid));")
}

$sql.Add('SET FOREIGN_KEY_CHECKS = 1;')
$sql.Add("SELECT 'user' AS table_name, COUNT(*) AS row_count FROM user UNION ALL SELECT 'course', COUNT(*) FROM course UNION ALL SELECT 'notice', COUNT(*) FROM notice UNION ALL SELECT 'grade', COUNT(*) FROM grade UNION ALL SELECT 'choose', COUNT(*) FROM choose;")

$sql -join "`n"
