$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

# Build paths relative to repo root (two levels up from this script)
$scriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
$projectRoot = Resolve-Path (Join-Path $scriptDir '..\..')
if (-not $projectRoot) { throw "Project root not found from: $scriptDir" }
$dataDir = Join-Path $projectRoot '数据库'

$sqlSrc = Join-Path $projectRoot 'code-scaffold\tools\import_bil_grades.sql'
$sqlTmp = Join-Path $env:TEMP ("import_bil_grades_runtime_{0}{1}.sql" -f (Get-Random), (Get-Random))

if (!(Test-Path $sqlSrc)) { throw "SQL not found: $sqlSrc" }
if (!(Test-Path $dataDir)) { throw "Data folder not found: $dataDir" }

# Build map for data files
$map = @{
  user   = 'user.txt'
  course = 'course.txt'
  notice = 'notice.txt'
  grade  = 'grade.txt'
  choose = 'choose.txt'
}

# Replace LOAD DATA paths with local absolute paths
$content = Get-Content -Raw -Encoding UTF8 $sqlSrc
foreach ($k in $map.Keys) {
  $fp = (Join-Path $dataDir $map[$k]) -replace '\\', '/'
  if (!(Test-Path ($fp -replace '/', '\'))) { throw "Data file not found: $fp" }
  $content = $content -replace "LOAD DATA LOCAL INFILE '.*?/$k\.txt'", "LOAD DATA LOCAL INFILE '$fp'"
}
Set-Content -Encoding UTF8 -NoNewline $sqlTmp $content

# Prompt for MySQL user/password
$defaultUser = 'root'
$defaultPwd = '55734'
$mysqlUser = Read-Host "MySQL user (default $defaultUser)"
if ([string]::IsNullOrWhiteSpace($mysqlUser)) { $mysqlUser = $defaultUser }
$mysqlPwd = Read-Host "MySQL password (default $defaultPwd)"
if ([string]::IsNullOrWhiteSpace($mysqlPwd)) { $mysqlPwd = $defaultPwd }

Write-Host "Importing database bil_grades..."
Get-Content -Raw -Encoding UTF8 $sqlTmp | & mysql --local-infile=1 -u"$mysqlUser" -p"$mysqlPwd"
if ($LASTEXITCODE -ne 0) {
  throw "MySQL import failed. Check user/password and MySQL service."
}

Remove-Item -Force $sqlTmp
Write-Host "Done."
