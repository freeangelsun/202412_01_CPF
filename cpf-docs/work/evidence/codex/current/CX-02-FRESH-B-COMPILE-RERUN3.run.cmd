@echo off
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" -p "C:\Users\fly10\AppData\Local\CPF\validation\generated-domains\cx02-20260822-1947\workspace\cpf-cxbeta" --no-daemon :online:compileJava --rerun-tasks -PcpfProductCompositeRoot="C:\dev\projects\jck\202412_01_CPF" -PcpfIncludeGeneratedDomains=true 1>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-B-COMPILE-RERUN3.stdout.log" 2>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-B-COMPILE-RERUN3.stderr.log"
set cpf_rc=%errorlevel%
>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-FRESH-B-COMPILE-RERUN3.exit.txt" echo %cpf_rc%
exit /b %cpf_rc%
