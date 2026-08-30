from pathlib import Path
import importlib.util
import tempfile
import zipfile
import xml.etree.ElementTree as ET

SCRIPT = Path(__file__).parents[1] / 'verify-cpf-public-binary-repository.py'
ROOT = SCRIPT.parents[3]
spec = importlib.util.spec_from_file_location('public_binary_verifier', SCRIPT)
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)


def test_gradle_plugin_marker_is_catalog_derived_and_requires_exact_implementation_dependency():
    markers = m._load_gradle_plugin_markers(ROOT)
    marker = ('com.cpf.platform-conventions', 'com.cpf.platform-conventions.gradle.plugin')
    assert markers == {marker: ('com.cpf.gradle', 'cpf-gradle-plugin')}
    assert m._gradle_plugin_marker_findings(
        *marker,
        '1.2.3',
        [('com.cpf.gradle', 'cpf-gradle-plugin', '1.2.3')],
        '1.2.3',
        markers,
    ) == []
    findings = m._gradle_plugin_marker_findings(
        *marker,
        '1.2.3',
        [('com.cpf.gradle', 'cpf-gradle-plugin', '9.9.9')],
        '1.2.3',
        markers,
    )
    assert any('dependency mismatch' in finding for finding in findings)


def _jar(path: Path, entries: dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, 'w') as zf:
        for name, text in entries.items():
            zf.writestr(name, text)


def test_sources_jar_rejects_internal_package_path():
    with tempfile.TemporaryDirectory() as d:
        jar = Path(d) / 'x-sources.jar'
        _jar(jar, {'com/cpf/example/internal/Hidden.java': 'package com.cpf.example.internal;'})
        findings = m._scan_zip_leakage(jar, javadoc=False)
        assert findings and 'internal path' in findings[0]


def test_javadoc_jar_rejects_internal_signature_reference():
    with tempfile.TemporaryDirectory() as d:
        jar = Path(d) / 'x-javadoc.jar'
        _jar(jar, {'com/cpf/api/PublicApi.html': '<code>com.cpf.security.internal.SecretPort</code>'})
        findings = m._scan_zip_leakage(jar, javadoc=True)
        assert findings and 'forbidden content' in findings[0]


def test_sources_and_javadoc_allow_public_surface():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        sources = root / 'x-sources.jar'
        docs = root / 'x-javadoc.jar'
        _jar(sources, {'com/cpf/api/PublicApi.java': '/** CPF public contract. */ public interface PublicApi {}'})
        _jar(docs, {'com/cpf/api/PublicApi.html': '<html>CPF public contract</html>'})
        assert m._scan_zip_leakage(sources, javadoc=False) == []
        assert m._scan_zip_leakage(docs, javadoc=True) == []


def _fake_generator_set(repository: Path, version: str) -> None:
    import hashlib, json
    base = repository / 'com/cpf/tooling/cpf-generator-cli' / version
    base.mkdir(parents=True, exist_ok=True)
    for classifier in m.REQUIRED_GENERATOR_CLASSIFIERS:
        stem = f'cpf-generator-cli-{version}-{classifier}'
        archive = base / f'{stem}.zip'
        archive.write_bytes(b'generator-' + classifier.encode())
        digest = hashlib.sha256(archive.read_bytes()).hexdigest()
        (base / f'{stem}.zip.sha256').write_text(digest + '\n', encoding='ascii')
        (base / f'{stem}.json').write_text(json.dumps({
            'artifactId': 'cpf-generator-cli', 'version': version,
            'classifier': classifier, 'sha256': digest
        }) + '\n', encoding='utf-8')


def test_generator_distribution_verifier_requires_both_public_os_classifiers():
    with tempfile.TemporaryDirectory() as d:
        repo = Path(d)
        _fake_generator_set(repo, '1.2.3')
        assert m._verify_generator_distributions(repo, '1.2.3') == []
        missing = repo / 'com/cpf/tooling/cpf-generator-cli/1.2.3/cpf-generator-cli-1.2.3-windows-x64.zip'
        missing.unlink()
        findings = m._verify_generator_distributions(repo, '1.2.3')
        assert any('windows-x64' in finding and 'missing generator distribution' in finding for finding in findings)


def test_generator_distribution_verifier_rejects_checksum_drift():
    with tempfile.TemporaryDirectory() as d:
        repo = Path(d)
        _fake_generator_set(repo, '1.2.3')
        archive = repo / 'com/cpf/tooling/cpf-generator-cli/1.2.3/cpf-generator-cli-1.2.3-linux-x64.zip'
        archive.write_bytes(b'tampered')
        findings = m._verify_generator_distributions(repo, '1.2.3')
        assert any('checksum mismatch classifier=linux-x64' in finding for finding in findings)


def test_snapshot_java_publication_resolves_timestamped_artifacts_from_metadata():
    with tempfile.TemporaryDirectory() as d:
        repository = Path(d)
        group = 'com.cpf.sample'
        artifact = 'cpf-sample'
        version = '1.2.3-SNAPSHOT'
        resolved = '1.2.3-20260830.010203-4'
        base = repository / 'com/cpf/sample/cpf-sample' / version
        base.mkdir(parents=True)
        files = {
            ('pom', ''): base / f'{artifact}-{resolved}.pom',
            ('jar', ''): base / f'{artifact}-{resolved}.jar',
            ('jar', 'sources'): base / f'{artifact}-{resolved}-sources.jar',
            ('jar', 'javadoc'): base / f'{artifact}-{resolved}-javadoc.jar',
        }
        files[('pom', '')].write_text(
            '<project><modelVersion>4.0.0</modelVersion><groupId>com.cpf.sample</groupId>'
            '<artifactId>cpf-sample</artifactId><version>1.2.3-SNAPSHOT</version></project>',
            encoding='utf-8',
        )
        files[('jar', '')].write_bytes(b'main')
        _jar(files[('jar', 'sources')], {'com/cpf/sample/Public.java': 'public interface Public {}'})
        _jar(files[('jar', 'javadoc')], {'com/cpf/sample/Public.html': '<html>Public</html>'})
        metadata = ET.Element('metadata', {'modelVersion': '1.1.0'})
        ET.SubElement(metadata, 'groupId').text = group
        ET.SubElement(metadata, 'artifactId').text = artifact
        ET.SubElement(metadata, 'version').text = version
        versioning = ET.SubElement(metadata, 'versioning')
        snapshot_versions = ET.SubElement(versioning, 'snapshotVersions')
        for extension, classifier in files:
            item = ET.SubElement(snapshot_versions, 'snapshotVersion')
            if classifier:
                ET.SubElement(item, 'classifier').text = classifier
            ET.SubElement(item, 'extension').text = extension
            ET.SubElement(item, 'value').text = resolved
        ET.ElementTree(metadata).write(base / 'maven-metadata.xml', encoding='utf-8', xml_declaration=True)

        for (extension, classifier), expected in files.items():
            assert m._published_artifact_path(repository, group, artifact, version, extension, classifier) == expected
        findings, sources, javadocs = m._verify_java_publication_set(
            repository,
            version,
            {artifact: {'artifactId': artifact}},
            {artifact: group},
        )
        assert findings == []
        assert sources == 1
        assert javadocs == 1
