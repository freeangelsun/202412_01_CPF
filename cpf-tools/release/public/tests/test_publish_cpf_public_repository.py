from pathlib import Path
import importlib.util, tempfile, subprocess
SCRIPT=Path(__file__).parents[1]/'publish-cpf-public-repository.py'
spec=importlib.util.spec_from_file_location('publisher',SCRIPT); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)

def test_remote_is_fail_closed():
    m.validate_remote('https://github.com/cpf-team/cpf-framework.git')
    try: m.validate_remote('https://github.com/example/wrong.git')
    except m.PublishError: pass
    else: raise AssertionError('wrong remote accepted')

def test_dirty_source_blocks_before_publication():
    with tempfile.TemporaryDirectory() as d:
        root=Path(d); subprocess.run(['git','init'],cwd=root,check=True,capture_output=True); subprocess.run(['git','config','user.email','t@example.test'],cwd=root,check=True); subprocess.run(['git','config','user.name','T'],cwd=root,check=True)
        (root/'a.txt').write_text('a'); subprocess.run(['git','add','.'],cwd=root,check=True); subprocess.run(['git','commit','-m','init'],cwd=root,check=True,capture_output=True)
        (root/'a.txt').write_text('dirty')
        try: m.require_clean_git(root)
        except m.PublishError as e: assert 'clean' in str(e)
        else: raise AssertionError('dirty source accepted')


def test_publisher_has_no_automatic_commit_or_push_path():
    text=SCRIPT.read_text(encoding='utf-8')
    assert "add_argument('--push'" not in text
    assert "git,'push'" not in text
    assert "git,'commit'" not in text
    assert 'publishToMavenLocal' not in text

def test_release_root_must_be_outside_private_root():
    with tempfile.TemporaryDirectory() as d:
        private=Path(d)/'private'; private.mkdir()
        try: m.release_root(private,str(private/'public'))
        except m.PublishError: pass
        else: raise AssertionError('release root inside private tree accepted')

def _fake_generator_distribution(directory: Path, version: str, classifier: str, payload: bytes = b'generator'):
    import hashlib, json
    directory.mkdir(parents=True, exist_ok=True)
    stem=f'cpf-generator-cli-{version}-{classifier}'
    archive=directory/(stem+'.zip')
    archive.write_bytes(payload+classifier.encode())
    digest=hashlib.sha256(archive.read_bytes()).hexdigest()
    (directory/(stem+'.zip.sha256')).write_text(digest+'\n',encoding='ascii')
    (directory/(stem+'.json')).write_text(json.dumps({
        'schemaVersion':1,'artifactId':'cpf-generator-cli','version':version,
        'classifier':classifier,'archive':archive.name,'sha256':digest,
        'canonicalEngine':'cpf-tools/generator/engine/cpf_domain_generator.py'
    })+'\n',encoding='utf-8')


def test_generator_distribution_requires_windows_and_linux_and_publishes_maven_layout(monkeypatch):
    with tempfile.TemporaryDirectory() as d:
        base=Path(d); prebuilt=base/'prebuilt'; repo=base/'repo'; native=base/'native'
        for classifier in m.REQUIRED_GENERATOR_CLASSIFIERS:
            _fake_generator_distribution(prebuilt,'1.2.3',classifier)
        monkeypatch.setenv('CPF_GENERATOR_NATIVE_BUILD_DIR',str(native))
        # Prebuilt matrix is complete, so no PyInstaller/native build is needed.
        result=m.publish_generator_distributions(base,repo,'1.2.3',prebuilt)
        assert result['classifiers']==list(m.REQUIRED_GENERATOR_CLASSIFIERS)
        target=repo/'com/cpf/tooling/cpf-generator-cli/1.2.3'
        for classifier in m.REQUIRED_GENERATOR_CLASSIFIERS:
            assert (target/f'cpf-generator-cli-1.2.3-{classifier}.zip').is_file()
            assert (target/f'cpf-generator-cli-1.2.3-{classifier}.zip.sha256').is_file()
            assert (target/f'cpf-generator-cli-1.2.3-{classifier}.json').is_file()


def test_generator_distribution_rejects_missing_cross_os_classifier(monkeypatch):
    with tempfile.TemporaryDirectory() as d:
        base=Path(d); prebuilt=base/'prebuilt'; repo=base/'repo'; native=base/'native'
        _fake_generator_distribution(prebuilt,'1.2.3','linux-x64')
        monkeypatch.setenv('CPF_GENERATOR_NATIVE_BUILD_DIR',str(native))
        # Prevent a native package build from hiding the missing matrix artifact.
        monkeypatch.setattr(m.platform if hasattr(m,'platform') else __import__('platform'), 'system', lambda:'Other')
        try:
            m.publish_generator_distributions(base,repo,'1.2.3',prebuilt)
        except m.PublishError as e:
            assert 'windows-x64' in str(e)
        else:
            raise AssertionError('incomplete generator matrix accepted')
