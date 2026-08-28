@echo off
setlocal
call "%~dp0bin\cpf.cmd" %*
exit /b %ERRORLEVEL%
