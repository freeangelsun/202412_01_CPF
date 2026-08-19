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
