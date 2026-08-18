import json, subprocess, sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/verification/verify_cpf_capability_management_contract.py'
CATALOG=ROOT/'cpf-tools/generator/contracts/cpf-starter-catalog.json'

def run(*args):
    return subprocess.run([sys.executable,str(SCRIPT),'--root',str(ROOT),*args],capture_output=True,text=True,timeout=30)

def test_capability_management_contract_positive():
    result=run()
    assert result.returncode==0, result.stdout+result.stderr
    data=json.loads(CATALOG.read_text(encoding='utf-8')); expected=sum(1 for m in data['modules'] if m.get('visibility')=='public'); assert f'publicStarters={expected}' in result.stdout

def test_new_public_starter_without_management_is_fail_closed(tmp_path):
    data=json.loads(CATALOG.read_text(encoding='utf-8'))
    data['modules'].append({
        'projectPath':':starters:example:new-provider',
        'artifactId':'cpf-starter-example-new-provider',
        'visibility':'public',
        'ownerGroup':'integration',
        'usageLevel':'capability'
    })
    bad=tmp_path/'bad-catalog.json'; bad.write_text(json.dumps(data),encoding='utf-8')
    result=run('--catalog',str(bad))
    assert result.returncode!=0
    assert 'management contract missing' in result.stdout
