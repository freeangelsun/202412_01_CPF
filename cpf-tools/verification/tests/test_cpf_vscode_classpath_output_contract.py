from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
FULL = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"


def test_every_java_project_keeps_canonical_gradle_compile_output():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "non-canonical-compile-output" in text
    assert "classes/java/main" in text
    assert "compileJava.destinationDirectory.set(" not in text
    assert "fake API or" in text
    assert "source-empty-canonical-output-missing" in text


def test_no_ide_only_output_directory_is_introduced():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "ide-only-output-directory-present" in text
    assert ".cpf-ide/main" not in text
    assert "cpfIdeClasspathRoot" not in text
    assert "cpf-ide-classpath/" not in text
    assert "cpfIdeClasspathMaterializedOnConfiguration" not in text
    assert "target.sourceSets.main.java.files.empty" in text
    assert "output.mkdirs()" in text


def test_ide_model_gate_runs_before_explicit_repair_and_after_build():
    text = FULL.read_text(encoding="utf-8")
    projects = text.index("GRADLE_PROJECTS")
    model = text.index("GRADLE_IDE_CLASSPATH_MODEL")
    repair = text.index("GRADLE_IDE_CLASSPATH'")
    build = text.index("GRADLE_FULL_BUILD_QUALITY")
    model_after = text.index("GRADLE_IDE_CLASSPATH_MODEL_AFTER_BUILD")
    assert projects < model < repair < build < model_after
    assert "cpfVerifyIdeClasspathModel" in text
    assert "cpfPrepareIdeClasspath" in text


def test_ide_classpath_repair_is_discovery_driven_and_does_not_override_dependencies():
    text = CONVENTION.read_text(encoding="utf-8")
    start = text.index("// VS Code Build Server/JDT resolves")
    end = text.index("allprojects {", start)
    block = text[start:end]
    assert "subprojects.findAll { it.plugins.hasPlugin('java') }" in block
    assert "sourceSets.main.java.files.empty" in block
    assert "tasks.named('compileJava')" in block
    assert "project(" not in block
    assert "dependencies {" not in block
    assert "fake API or" in block
    assert "sourceEmpty.each" in block
