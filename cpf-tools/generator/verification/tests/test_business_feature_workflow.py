from __future__ import annotations
import subprocess, sys, tempfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'


def run(*args:str):
    return subprocess.run([sys.executable,str(CLI),'--root',str(ROOT),*args],cwd=ROOT,text=True,encoding='utf-8',errors='replace',capture_output=True)


def test_setup_materializes_multiple_business_features_and_preserves_them_on_sync():
    test_root=ROOT/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/tests'
    test_root.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-business-feature-',dir=test_root) as td:
        out=Path(td)/'cpf-qafeature'
        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--preset','custom',
               '--persistence','none','--no-http-client','--no-resilience','--no-sample-transaction',
               '--business-feature','customer','--business-feature','profile','--output',str(out))
        assert cp.returncode==0,cp.stderr+cp.stdout
        contract=(out/'gradle.properties').read_text(encoding='utf-8')
        assert 'cpf.domain.businessFeatures=customer,profile\n' in contract
        assert not (out/'cpf-domain.yaml').exists()
        assert not (out/'cpf-generator.lock.json').exists()
        assert (out/'online/src/main/java/qafeature/customer/operation/QafeatureDomainPingOperation.java').is_file()
        ping=(out/'online/src/main/java/qafeature/customer/operation/QafeatureDomainPingOperation.java').read_text(encoding='utf-8')
        assert '@Qualifier("cpfStarterClock") Clock clock' in ping
        assert (out/'online/src/main/java/qafeature/profile/operation/ProfileFeatureScaffold.java').is_file()
        assert not (out/'online/src/main/java/qafeature/online/qafeature').exists()
        assert not (out/'online/src/main/java/qafeature/qafeature').exists()

        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--sync','--output',str(out))
        assert cp.returncode==0,cp.stderr+cp.stdout
        preserved=(out/'gradle.properties').read_text(encoding='utf-8')
        assert 'cpf.domain.businessFeatures=customer,profile\n' in preserved


def test_setup_rejects_domain_name_as_business_feature():
    test_root=ROOT/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/tests'
    test_root.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-business-feature-invalid-',dir=test_root) as td:
        cp=run('domain','setup','--name','qafeature','--system-code','QAF','--preset','custom',
               '--persistence','none','--business-feature','qafeature','--output',str(Path(td)/'cpf-qafeature'))
        assert cp.returncode!=0
        assert 'Business Feature' in (cp.stderr+cp.stdout)


def test_sample_consumers_select_the_universal_runtime_clock():
    test_root=ROOT/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/tests'
    test_root.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-sample-clock-',dir=test_root) as td:
        out=Path(td)/'cpf-qaclock'
        cp=run('domain','setup','--name','qaclock','--system-code','QAC','--preset','custom',
               '--persistence','mybatis','--vendor','mariadb','--sample-transaction',
               '--database-name','qaclock_db','--schema-name','qaclock',
               '--no-http-client','--no-resilience','--output',str(out))
        assert cp.returncode==0,cp.stderr+cp.stdout
        sources=[
            out/'online/src/main/java/qaclock/sample/operation/QaclockDomainPingOperation.java',
            out/'online/src/main/java/qaclock/sample/service/SampleTransactionService.java',
        ]
        for source in sources:
            text=source.read_text(encoding='utf-8')
            assert 'import org.springframework.beans.factory.annotation.Qualifier;' in text
            assert '@Qualifier("cpfStarterClock") Clock clock' in text
        service=sources[1].read_text(encoding='utf-8')
        assert service.count('@CpfTransactional(transactionManager="cpfDomainTransactionManager")')==4
        assert service.count('@CpfTransactional(transactionManager="cpfDomainTransactionManager", readOnly=true)')==3
        assert '@CpfTransactional\n' not in service
        operations=sorted((out/'online/src/main/java/qaclock/sample/operation').glob('Sample*DomainOperation.java'))
        assert len(operations)==6
        for operation in operations:
            text=operation.read_text(encoding='utf-8')
            assert ' implements CpfDomainOperation<' in text
            assert 'public final class ' not in text
            assert 'public class ' in text
