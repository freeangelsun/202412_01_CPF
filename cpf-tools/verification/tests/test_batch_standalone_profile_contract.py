from pathlib import Path
import subprocess, sys
ROOT=Path(__file__).resolve().parents[3]
def test_actual_batch_standalone_profile_contract_passes():
    p=subprocess.run([sys.executable,str(ROOT/'cpf-tools/verification/tools/verify-cpf-batch-standalone-profile.py'),'--root',str(ROOT)],capture_output=True,text=True)
    assert p.returncode==0,p.stdout+p.stderr
