#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(__file__).resolve().parents[3]
script=(root/'cpf-tools/verification/final-dev/run-db3-lifecycle.ps1').read_text(encoding='utf-8')
runtime_test=(root/'cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1').read_text(encoding='utf-8')
checks={
 'json_stdin': "--connection-json-stdin" in script and 'ConnectionJson' in script,
 'no_url_argv': '"--url=$url"' not in script and '"--username=$username"' not in script,
 'url_secret_rejection': 'Assert-SafeJdbcUrl' in script and 'credential 또는 secret' in script,
 'environment_clear': '$start.Environment.Clear()' in script,
 'environment_allowlist': "@('PATH','JAVA_HOME','SystemRoot','WINDIR','TEMP','TMP','LANG','LC_ALL')" in script,
 'timeout': 'WaitForExit($TimeoutSeconds * 1000)' in script and 'ExitCode = 124' in script,
 'kill_tree': '$process.Kill($true)' in script,
 'redaction': 'Protect-Text' in script and '***REDACTED***' in script,
 'runtime_child_environment_test': 'child_env_runtime_secret_count=0' in runtime_test and 'intentional-secret-echo' in runtime_test,
 'runtime_tree_kill_test': 'grandchild-survived.txt' in runtime_test and 'Should -BeFalse' in runtime_test,
 'runtime_timeout_assertion': "UNKNOWN_TIMEOUT" in runtime_test and '-TimeoutSeconds 1' in runtime_test,
}
failed=[name for name,ok in checks.items() if not ok]
for name,ok in checks.items(): print(f"{'PASS' if ok else 'FAIL'} {name}")
if failed: print('DB3 runner contract failures: '+','.join(failed),file=sys.stderr);sys.exit(1)
print(f'PASS checks={len(checks)}')
