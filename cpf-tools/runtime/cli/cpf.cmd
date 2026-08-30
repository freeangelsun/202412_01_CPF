@echo off
setlocal
chcp 65001 >nul 2>&1
set "SCRIPT_DIR=%~dp0"
set "CLI=%SCRIPT_DIR%lib\cpf-cli.jar"
if defined JAVA_HOME (set "JAVA_BIN=%JAVA_HOME%\bin\java.exe") else (set "JAVA_BIN=java.exe")
if not exist "%CLI%" (
  >&2 echo CPF_CLI=FAIL code=CPF-CLI-JAR-MISSING message=%CLI%
  exit /b 69
)
set "JAVA_VERSION="
rem for /f runs the command through cmd /c. A command starting with a quote hits the
rem cmd quote-stripping rule and fails to parse, so keep call in front of the quoted path.
for /f "tokens=3" %%V in ('call "%JAVA_BIN%" -version 2^>^&1 ^| findstr /b /c:"openjdk version" /c:"java version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~V"
for /f "tokens=1 delims=." %%M in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%M"
if not "%JAVA_MAJOR%"=="25" (
  >&2 echo CPF_CLI=FAIL code=CPF-CLI-JAVA-VERSION message=Java_25_required actual=%JAVA_VERSION%
  exit /b 69
)
"%JAVA_BIN%" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar "%CLI%" %*
exit /b %ERRORLEVEL%
