@echo off
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" -p "C:\Users\fly10\AppData\Local\CPF\validation\generated-domains\cx02-20260822-1947\workspace\cpf-cxalpha" --no-daemon :online:compileJava --rerun-tasks -PcpfProductCompositeRoot="C:\dev\projects\jck\202412_01_CPF" 1>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-A-COMPILE.stdout.log" 2>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-A-COMPILE.stderr.log"
set cpf_rc=%errorlevel%
>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-A-COMPILE.exit.txt" echo %cpf_rc%
exit /b %cpf_rc%
