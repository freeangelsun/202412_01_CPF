from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
BATCH_RUNTIME = ROOT / "cpf-batch/runtime/build.gradle"
PUBLIC_BOM = ROOT / "cpf-tools/build/platform-bom/public-bom/build.gradle"


def test_public_release_coordinates_are_catalog_projected_only_in_public_mode():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "providers.gradleProperty('cpfPublicBinaryRepository').isPresent()" in text
    assert "cpfPublicRowsByProject" in text
    assert "nativeToPublic" in text
    assert "publication.groupId = primary.publicGroupId.toString()" in text
    assert "publication.artifactId = primary.artifactId.toString()" in text
    assert "alias.from(target.components.java)" in text
    assert "artifact.classifier in ['sources', 'javadoc']" in text
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
