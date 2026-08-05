from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "verify-cpf-generator-upgrade-ownership.py"
spec = spec_from_file_location("upgrade_gate", SCRIPT)
module = module_from_spec(spec); assert spec.loader; spec.loader.exec_module(module)


def test_current_upgrade_is_manifest_driven_and_path_contained():
    target = Path(__file__).parents[2] / "generator" / "upgrade-domain.ps1"
    assert module.validate(target) == []


def test_broad_source_tree_ownership_is_rejected(tmp_path):
    target = tmp_path / "upgrade-domain.ps1"
    target.write_text("\n".join(module.REQUIRED) + "\n$userOwnedPrefixes = @('src/main/java/', 'src/test/java/', 'ui/')\n" + "$userOwnedFiles = @('README.md', 'config/cpf-approved-exceptions.csv')\n$oldOwned = @{}\n$tempRoot = Join-Path\n$ownedSha = if ($oldOwned.ContainsKey($relative)) {}\n$currentSha = Get-Sha $target\n", encoding="utf-8")
    assert any("forbidden broad ownership" in error for error in module.validate(target))


def test_owned_path_semantic_mirror_rejects_escape_and_windows_ambiguity():
    for value in (
        "../outside.txt",
        "manifest/../../outside.txt",
        "/absolute.txt",
        r"C:\\outside.txt",
        r"manifest\\owned.txt",
        "manifest//owned.txt",
        "manifest/./owned.txt",
        "manifest/owned.txt/",
        "manifest/trailing.",
        " manifest/owned.txt",
    ):
        assert module.validate_owned_relative_path(value), value
    assert module.validate_owned_relative_path("src/main/java/com/example/Owned.java") == []


def test_owned_sha_semantic_mirror_requires_exact_sha256():
    assert module.validate_sha256("a" * 64) == []
    for value in ("", "a" * 63, "a" * 65, "g" * 64, "sha256:" + "a" * 64):
        assert module.validate_sha256(value), value
