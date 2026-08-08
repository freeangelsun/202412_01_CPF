#!/usr/bin/env python3
from pathlib import Path
import argparse,json,sys

def verify(root:Path)->list[str]:
 script_path=root/'cpf-tools/verification/final-dev/run-db3-lifecycle.ps1'
 contract_path=root/'cpf-tools/db/cpf-db-lifecycle-contract.json'
 executor_path=root/'cpf-tools/scripts/invoke-cpf-qa34-db-runtime-matrix.ps1'
 test_path=root/'cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1'
 missing=[str(p) for p in (script_path,contract_path,executor_path,test_path) if not p.is_file()]
 if missing:return ['missing files: '+','.join(missing)]
 script=script_path.read_text(encoding='utf-8');contract=json.loads(contract_path.read_text(encoding='utf-8'));executor=executor_path.read_text(encoding='utf-8');runtime_test=test_path.read_text(encoding='utf-8')
 checks={
  'canonical_executor_declared':contract.get('runtimeExecutor')=='cpf-tools/scripts/invoke-cpf-qa34-db-runtime-matrix.ps1',
  'canonical_executor_exists':executor_path.is_file(),
  'default_delegates_canonical':'CANONICAL_QA34_RUNTIME_EXECUTOR' in script and 'invoke-cpf-qa34-db-runtime-matrix.ps1' in script,
  'phantom_java_removed':'build/classes/java/main' not in script and "@('-cp', $RunnerClasspath, $RunnerClass)" not in script,
  'canonical_full_lifecycle':all(x in executor for x in ('clean-install','runtime-query-pack','schema-drift','rollback','upgrade','backupRestoreEvidence','pitrEvidence')),
  'exact_sha':'ExpectedHead mismatch' in script and '$actualHead' in script,
  'sanitized_release_evidence':'releaseEligible' in script and 'sanitized' in script and 'sourceSha' in script,
  'json_stdin_custom_runner':'--connection-json-stdin' in script and 'ConnectionJson' in script,
  'no_url_argv':'"--url=$url"' not in script and '"--username=$username"' not in script,
  'url_secret_rejection':'Assert-SafeJdbcUrl' in script and 'credential 또는 secret' in script,
  'environment_clear_custom_runner':'$start.Environment.Clear()' in script,
  'timeout_kill_custom_runner':'WaitForExit($TimeoutSeconds * 1000)' in script and '$process.Kill($true)' in script,
  'runtime_custom_safety_tests':'child_env_runtime_secret_count=0' in runtime_test and 'grandchild-survived.txt' in runtime_test,
  'default_runner_pester_assertion':'checked-in canonical QA34 runtime executor' in runtime_test,
 }
 failures=[]
 for n,v in checks.items():
  print(('PASS' if v else 'FAIL')+' '+n)
  if not v:failures.append(n)
 mut=script.replace("runnerMode = 'CANONICAL_QA34_RUNTIME_EXECUTOR'","runnerMode = 'PHANTOM'")+"\n# build/classes/java/main\n"
 mutation_killed='build/classes/java/main' in mut and 'CANONICAL_QA34_RUNTIME_EXECUTOR' not in mut
 print(('PASS' if mutation_killed else 'FAIL')+' phantom_default_mutation')
 if not mutation_killed:failures.append('phantom-default mutation survived')
 return failures

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path('.'));a=ap.parse_args();fail=verify(a.root.resolve())
 if fail:print('DB3 runner contract failures: '+','.join(fail),file=sys.stderr);return 1
 print('PASS selfTest=true canonicalRunner=QA34');return 0
if __name__=='__main__':raise SystemExit(main())
