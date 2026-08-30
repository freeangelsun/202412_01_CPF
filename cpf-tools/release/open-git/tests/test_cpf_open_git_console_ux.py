from pathlib import Path
import importlib.util
ROOT=Path(__file__).resolve().parents[4]
MODULE_PATH=ROOT/'cpf-tools/release/open-git/cpf_open_git.py'
spec=importlib.util.spec_from_file_location('cpf_open_git_console_ux',MODULE_PATH);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)

def test_release_stages_are_korean_user_facing_and_gradle_task_spam_is_filtered():
    source=MODULE_PATH.read_text(encoding='utf-8')
    assert 'Framework Binary 생성·Publication' in source
    assert 'Generator Windows/Linux 배포본 생성' in source
    assert '[CPF][OPEN-GIT][진행]' in source
    assert 'Gradle 내부 작업' in source
    assert '상세 명령/전체 출력: cpf-release/logs/open-git-release.log' in source
    assert m._should_echo_gradle_line('> Task :framework:core:compileJava\n') is False
    assert m._should_echo_gradle_line('Execution failed for task :apps:admin:frontendInstall\n') is True

def test_console_labels_hide_low_level_command_as_primary_ux():
    assert m._command_console_label(['gradlew.bat','clean'])=='Gradle 빌드/검증/Publication'
    assert 'Frontend 의존성 설치' in m._command_console_label(['npm.cmd','ci'])
