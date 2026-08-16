from pathlib import Path
import subprocess,sys

ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/verification/verify_integration_closure_contract.py'

def test_current_integration_closure_gate_passes_real_repository():
    result=subprocess.run([sys.executable,str(SCRIPT),'--root',str(ROOT)],capture_output=True,text=True)
    assert result.returncode==0, result.stdout+result.stderr
    assert 'canonicalOrval=true' in result.stdout

def test_gate_no_longer_requires_retired_integration_closure_generated_file():
    text=SCRIPT.read_text(encoding='utf-8')
    assert 'src/generated/integrationClosureApi.ts' not in text
    assert 'src/generated/orval/cpf-api.ts' in text
    assert 'features/integration-closure/integrationClosureApi.ts' in text
