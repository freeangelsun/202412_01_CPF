@echo off
setlocal
chcp 65001 >nul 2>&1
set "ROOT=%~dp0.."
set "CLI=%ROOT%\bin\lib\cpf-cli.jar"
if not exist "%CLI%" (
  >&2 echo [CPF][WRAPPER] cpf-cli.jar missing: %CLI%
  exit /b 69
)
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar "%CLI%" %*
exit /b %ERRORLEVEL%
