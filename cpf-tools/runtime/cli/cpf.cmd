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
"%JAVA_BIN%" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar "%CLI%" %*
exit /b %ERRORLEVEL%
