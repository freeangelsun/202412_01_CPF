@echo off
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" --no-daemon :cpf-generated-cpf-member:online:generateCpfBusinessOperationManifest :cpf-generated-cpf-external:online:generateCpfBusinessOperationManifest --rerun-tasks -PcpfIncludeGeneratedDomains=true -PcpfDbVendor=postgresql 1>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN3.stdout.log" 2>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN3.stderr.log"
if errorlevel 1 goto finished
call "C:\dev\projects\jck\202412_01_CPF\gradlew.bat" -p "C:\dev\projects\jck\202412_01_CPF\cpf-backoffice" --no-daemon :online:generateCpfBusinessOperationManifest --rerun-tasks -PcpfProductCompositeRoot="C:\dev\projects\jck\202412_01_CPF" -PcpfDbVendor=postgresql 1>>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN3.stdout.log" 2>>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN3.stderr.log"
:finished
set cpf_rc=%errorlevel%
>"C:\dev\projects\jck\202412_01_CPF\cpf-docs\work\evidence\codex\current\CX-02-OPERATION-MANIFEST-IMPACT-RERUN3.exit.txt" echo %cpf_rc%
exit /b %cpf_rc%
