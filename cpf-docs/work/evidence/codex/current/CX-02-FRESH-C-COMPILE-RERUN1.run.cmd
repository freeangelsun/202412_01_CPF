@echo off
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" -p "C:\Users\fly10\AppData\Local\CPF\validation\generated-domains\cx02-20260822-1947\workspace\cpf-cxgamma" --no-daemon :online:compileJava :batch:compileJava --rerun-tasks -PcpfProductCompositeRoot="C:\dev\projects\jck\202412_01_CPF" -PcpfDbVendor=postgresql 1>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-C-COMPILE-RERUN1.stdout.log" 2>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-C-COMPILE-RERUN1.stderr.log"
set cpf_rc=%errorlevel%
>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-C-COMPILE-RERUN1.exit.txt" echo %cpf_rc%
exit /b %cpf_rc%
