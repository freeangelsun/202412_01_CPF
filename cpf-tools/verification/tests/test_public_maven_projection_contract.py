from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
BATCH_RUNTIME = ROOT / "cpf-batch/runtime/build.gradle"
PUBLIC_BOM = ROOT / "cpf-tools/build/platform-bom/public-bom/build.gradle"
PUBLIC_JAVA_POLICY = ROOT / "cpf-tools/release/public/cpf-public-java-publication-policy.json"
GRADLE_PLUGIN = ROOT / "cpf-tools/build/gradle-plugin/build.gradle"


def test_public_release_coordinates_are_catalog_projected_only_in_public_mode():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "providers.gradleProperty('cpfPublicBinaryRepository').isPresent()" in text
    assert "cpfPublicRowsByProject" in text
    assert "nativeToPublic" in text
    assert "publication.groupId = primary.publicGroupId.toString()" in text
    assert "publication.artifactId = primary.artifactId.toString()" in text
    assert "alias.from(target.components.java)" not in text
    assert "cpfProjectPublicRuntimeAliases" in text
    assert "project-cpf-public-runtime-aliases.py" in text
    assert "replacement = nativeToPublic" in text
    assert "dep.groupId[0].value = replacement.publicGroupId.toString()" in text
    assert "dep.artifactId[0].value = replacement.artifactId.toString()" in text


def test_batch_runtime_has_real_maven_publication_for_fresh_release_build():
    text = BATCH_RUNTIME.read_text(encoding="utf-8")
    assert "id 'maven-publish'" in text
    assert "artifactId = 'cpf-batch-runtime'" in text
    assert "from components.java" in text


def test_public_bom_is_owned_by_final_artifact_catalog_not_stale_batch_alias():
    text = PUBLIC_BOM.read_text(encoding="utf-8")
    assert "cpf-final-artifact-catalog.json" in text
    assert "publicArtifactCoordinates" in text
    assert "PUBLIC_RUNTIME" in text
    assert "cpf-batch-contract" not in text


def test_public_sources_and_javadoc_use_one_fail_closed_source_projection():
    import json

    convention = CONVENTION.read_text(encoding="utf-8")
    policy = json.loads(PUBLIC_JAVA_POLICY.read_text(encoding="utf-8"))
    source_policy = policy["sourceArtifactPolicy"]
    assert source_policy["excludedPathSegments"] == ["internal"]
    assert source_policy["forbiddenContentPatterns"]
    assert set(source_policy["projectionTargets"]) == {"sourcesJar", "javadoc"}
    assert "cpfIsProjectedPublicJavaSource" in convention
    assert "tasks.named('sourcesJar', Jar)" in convention
    assert "includeEmptyDirs = false" in convention
    assert "tasks.named('javadoc', Javadoc)" in convention
    assert "sourceSets.main.allJava.filter" in convention
    assert "inputs.file(cpfPublicJavaPublicationPolicyFile)" in convention
    assert "cpfDocumentationProjectDir.toPath()" in convention
    assert "project.relativePath(source)" not in convention
    assert "Xdoclint:${cpfPublicJavaPublicationPolicy.javadocPolicy.doclint}" in convention
    assert "addBooleanOption('Werror', true)" in convention


def test_public_tooling_plugin_does_not_publish_sources_in_public_mode():
    text = GRADLE_PLUGIN.read_text(encoding="utf-8")
    assert "!providers.gradleProperty('cpfPublicBinaryRepository').isPresent()" in text
    assert "withSourcesJar()" in text


def test_spring_boot_health_consumers_expose_optional_annotation_types_to_javac():
    health_imports = (
        "org.springframework.boot.health.contributor.Health",
        "org.springframework.boot.health.contributor.HealthIndicator",
        "org.springframework.boot.actuate.health.Health",
        "org.springframework.boot.actuate.health.HealthIndicator",
    )
    missing = []
    for source in (ROOT / "cpf-starters").rglob("src/main/java/**/*.java"):
        source_text = source.read_text(encoding="utf-8")
        if not any(value in source_text for value in health_imports):
            continue
        owner = source.parent
        while owner != ROOT and not (owner / "build.gradle").is_file():
            owner = owner.parent
        build = (owner / "build.gradle").read_text(encoding="utf-8")
        if "jackson-annotations" not in build and "jackson-databind" not in build:
            missing.append(source.relative_to(ROOT).as_posix())
    assert missing == []
