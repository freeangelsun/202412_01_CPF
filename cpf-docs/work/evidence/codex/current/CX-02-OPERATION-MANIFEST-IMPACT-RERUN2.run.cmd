@echo off
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" --no-daemon :apps:backoffice:generateCpfBusinessOperationManifest :cpf-generated-cpf-member:online:generateCpfBusinessOperationManifest :cpf-generated-cpf-external:online:generateCpfBusinessOperationManifest --rerun-tasks -PcpfIncludeGeneratedDomains=true -PcpfDbVendor=postgresql 1>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN2.stdout.log" 2>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN2.stderr.log"
set cpf_rc=%errorlevel%
>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN2.exit.txt" echo %cpf_rc%
exit /b %cpf_rc%
