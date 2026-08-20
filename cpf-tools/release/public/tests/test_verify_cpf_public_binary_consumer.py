from pathlib import Path
import importlib.util, tempfile
SCRIPT=Path(__file__).parents[1]/'verify-cpf-public-binary-consumer.py'
spec=importlib.util.spec_from_file_location('consumer',SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
def test_workspace_scan_rejects_maven_local():
    with tempfile.TemporaryDirectory() as d:
        p=Path(d);(p/'settings.gradle').write_text('repositories { mavenLocal() }')
        try:m.scan_workspace(p)
        except m.ConsumerError as e: assert 'mavenLocal' in str(e)
        else: raise AssertionError('expected failure')
def test_workspace_scan_accepts_public_repository_only():
    with tempfile.TemporaryDirectory() as d:
        p=Path(d);(p/'settings.gradle').write_text('repositories { maven { url = uri(System.getenv("CPF_MAVEN_REPOSITORY_URL")) }; mavenCentral() }')
        m.scan_workspace(p)
