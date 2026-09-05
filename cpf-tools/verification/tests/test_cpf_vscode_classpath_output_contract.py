from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
FULL = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"
VSCODE_SETTINGS = ROOT / ".vscode/settings.json"
GRADLE_PLUGIN_BUILD = ROOT / "cpf-tools/build/gradle-plugin/build.gradle"


def _uses_isolated_jdt_output(text: str) -> bool:
    return all(marker in text for marker in (
        "eclipseClasspath.baseSourceOutputDir.set(target.layout.buildDirectory.dir('ide/classes'))",
        "eclipseClasspath.defaultOutputDir = target.layout.buildDirectory.dir('ide/classes/default').get().asFile",
        "JDT output must also be isolated from Gradle's",
        "ide-output-missing",
    ))


def test_every_java_project_keeps_canonical_gradle_compile_output():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "non-canonical-compile-output" in text
    assert "classes/java/main" in text
    assert "compileJava.destinationDirectory.set(" not in text
    assert "fake API or" in text
    assert "source-empty-canonical-output-missing" in text


def test_jdt_output_is_explicitly_isolated_from_gradle_outputs():
    text = CONVENTION.read_text(encoding="utf-8")
    assert _uses_isolated_jdt_output(text)
    assert "build/ide/classes" in text
    assert "ide-output-missing" in text
    assert "CPF IDE-isolated output could not be created" in text
    assert ".cpf-ide/main" not in text
    assert "cpf-ide-classpath/" not in text
    assert "target.sourceSets.main.java.files.empty" in text
    assert "output.mkdirs()" in text


def test_mutation_shared_jdt_and_gradle_output_is_rejected():
    """A future simplification must not restore the class-output race seen in Open Git Release."""
    text = CONVENTION.read_text(encoding="utf-8")
    mutated = text.replace("dir('ide/classes')", "dir('classes/java')")
    assert not _uses_isolated_jdt_output(mutated)


def test_stale_pre_isolation_jdt_resources_are_removed_only_when_identical_to_gradle_resources():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "def cpfPurgeStaleJdtResourceCopies" in text
    assert "Files.mismatch(candidate.toPath(), canonicalResource.toPath()) == -1L" in text
    assert "candidate.name.endsWith('.class')" in text
    assert "target.tasks.withType(Jar).configureEach" in text
    assert "javaProjects.each { target -> cpfPurgeStaleJdtResourceCopies(target) }" in text
    assert "duplicatesStrategy = DuplicatesStrategy.EXCLUDE" not in text


def test_gradle_buildship_model_never_targets_product_bin_source():
    """Buildship/JDT default bin/* must be redirected through the Gradle Eclipse model."""
    text = CONVENTION.read_text(encoding="utf-8")
    assert "import org.gradle.plugins.ide.eclipse.model.EclipseModel" in text
    assert "target.pluginManager.apply('eclipse')" in text
    assert "cpfConfigureEclipseOutput(rootProject)" in text
    assert "eclipseClasspath.baseSourceOutputDir.set(target.layout.buildDirectory.dir('ide/classes'))" in text
    assert "eclipseClasspath.defaultOutputDir = target.layout.buildDirectory.dir('ide/classes/default').get().asFile" in text
    assert "non-canonical-eclipse-source-output" in text
    assert "non-canonical-eclipse-default-output" in text
    included_build = GRADLE_PLUGIN_BUILD.read_text(encoding="utf-8")
    assert "id 'eclipse'" in included_build
    assert "baseSourceOutputDir.set(layout.buildDirectory.dir('ide/classes'))" in included_build
    assert "defaultOutputDir = layout.buildDirectory.dir('ide/classes/default').get().asFile" in included_build


def test_vscode_uses_the_gradle_importer_that_honors_the_canonical_eclipse_model():
    """Do not select the Build Server path while it ignores Gradle's Eclipse output model."""
    settings = json.loads(VSCODE_SETTINGS.read_text(encoding="utf-8"))
    assert settings["java.gradle.buildServer.enabled"] == "off"


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
