@echo off
setlocal
chcp 65001 >nul 2>&1
set "ROOT=%~dp0.."
set "CLI=%ROOT%\bin\lib\cpf-cli.jar"
if not exist "%CLI%" (
  >&2 echo [CPF][WRAPPER] cpf-cli.jar missing: %CLI%
  exit /b 69
)
set "JAVA_VERSION="
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /b /c:"openjdk version" /c:"java version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~V"
for /f "tokens=1 delims=." %%M in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%M"
if not "%JAVA_MAJOR%"=="25" (
  >&2 echo CPF_CLI=FAIL code=CPF-CLI-JAVA-VERSION message=Java_25_required actual=%JAVA_VERSION%
  exit /b 69
)
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar "%CLI%" %*
exit /b %ERRORLEVEL%
