@echo off
setlocal EnableExtensions EnableDelayedExpansion
set "ROOT=%~dp0..\..\.."
where py >nul 2>nul
if %ERRORLEVEL%==0 (
  py -3 "%ROOT%\cpf-tools\runtime\cli\cpf.py" --root "%ROOT%" %*
  exit /b !ERRORLEVEL!
)
where python >nul 2>nul
if %ERRORLEVEL%==0 (
  python "%ROOT%\cpf-tools\runtime\cli\cpf.py" --root "%ROOT%" %*
  exit /b !ERRORLEVEL!
)
echo CPF_CLI=FAIL Python 3 is required 1>&2
exit /b 127
