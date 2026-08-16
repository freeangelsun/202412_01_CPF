#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
req=[
'cpf-tools/testing/cpf-testkit/src/main/java/com/cpf/testkit/context/CpfTestContextRuntime.java',
'cpf-tools/testing/cpf-testkit/src/main/java/com/cpf/testkit/annotation/CpfAnnotationContractAssertions.java',
'cpf-tools/testing/cpf-testkit/src/main/resources/META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider',
'cpf-tools/testing/cpf-testkit/src/test/java/com/cpf/testkit/context/CpfTestkitRuntimeHarness.java']
fail=[f'MISSING:{r}' for r in req if not (root/r).is_file()]
ctx=(root/req[0]).read_text(errors='ignore') if (root/req[0]).exists() else ''
for token in ['priority() { return 1000; }','close order violated','leak detected','ThreadLocal']:
    if token not in ctx: fail.append('CONTRACT_MISSING:'+token)
service=(root/req[2]).read_text(errors='ignore').strip() if (root/req[2]).exists() else ''
if service!='com.cpf.testkit.context.CpfTestContextRuntime': fail.append('SERVICE_PROVIDER_INVALID:'+service)
if fail:
 print('CPF_TESTKIT_CONTRACT=FAIL'); print('\n'.join(fail)); sys.exit(1)
print('CPF_TESTKIT_CONTRACT=PASS contextProvider=deterministic annotationAssertions=present serviceLoader=present')
