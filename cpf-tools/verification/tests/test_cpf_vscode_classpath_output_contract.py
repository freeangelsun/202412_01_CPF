from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
FULL = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"


def test_source_empty_java_projects_materialize_canonical_output_during_configuration():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "project.afterEvaluate {" in text
    assert "sourceSets.main.java.files.empty" in text
    assert "cpfIdeClasspathMaterializedOnConfiguration" in text
    assert "compileJava.destinationDirectory.set(stableOutputDir)" in text
    assert "gradle.gradleUserHomeDir" in text
    assert "cpf-ide-classpath/" in text
    assert "Never add fake Java API/classes" in text


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


def test_ide_classpath_fix_is_discovery_driven_and_does_not_override_dependencies():
    text = CONVENTION.read_text(encoding="utf-8")
    start = text.index("// VS Code Buildship/JDT resolves")
    end = text.index("allprojects {", start)
    block = text[start:end]
    assert "subprojects.findAll { it.plugins.hasPlugin('java') }" in block
    assert "sourceSets.main.java.files.empty" in text
    assert "project(" not in block
    assert "dependencies {" not in block
    assert "sourceEmptyJavaProjects" in block
    assert "stable=${stable}" in text
    assert "fake Java API/classes" in block
    assert "gradle user home" in text.lower()
