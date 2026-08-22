from __future__ import annotations
import subprocess, sys, tempfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'


def run(*args:str):
    return subprocess.run([sys.executable,str(CLI),'--root',str(ROOT),*args],cwd=ROOT,text=True,capture_output=True)


def test_setup_materializes_multiple_business_features_and_preserves_them_on_sync():
    (ROOT/'build').mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-business-feature-',dir=ROOT/'build') as td:
        out=Path(td)/'cpf-qafeature'
        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--preset','custom',
               '--persistence','none','--no-http-client','--no-resilience','--no-sample-transaction',
               '--business-feature','customer','--business-feature','profile','--output',str(out))
        assert cp.returncode==0,cp.stderr+cp.stdout
        definition=(out/'cpf-domain.yaml').read_text(encoding='utf-8')
        assert 'businessFeatures:\n  - customer\n  - profile\n' in definition
        assert (out/'online/src/main/java/qafeature/customer/operation/QafeatureDomainPingOperation.java').is_file()
        assert (out/'online/src/main/java/qafeature/profile/operation/ProfileFeatureScaffold.java').is_file()
        assert not (out/'online/src/main/java/qafeature/online/qafeature').exists()
        assert not (out/'online/src/main/java/qafeature/qafeature').exists()

        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--sync','--output',str(out))
        assert cp.returncode==0,cp.stderr+cp.stdout
        preserved=(out/'cpf-domain.yaml').read_text(encoding='utf-8')
        assert 'businessFeatures:\n  - customer\n  - profile\n' in preserved


def test_setup_rejects_domain_name_as_business_feature():
    (ROOT/'build').mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-business-feature-invalid-',dir=ROOT/'build') as td:
        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--preset','custom',
               '--persistence','none','--business-feature','qafeature','--output',str(Path(td)/'cpf-qafeature'))
        assert cp.returncode!=0
        assert 'Business Feature' in (cp.stderr+cp.stdout)
