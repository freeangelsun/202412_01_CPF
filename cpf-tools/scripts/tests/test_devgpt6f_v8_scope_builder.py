from pathlib import Path
import importlib.util
import json
import shutil

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/scripts/build-devgpt6f-v8-scope.py"
BASELINE = "09dd686c5ae0826594b9c5e1f871d95d95d3ce1c"


def load_module():
    spec = importlib.util.spec_from_file_location("devgpt6f_v8_scope", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def test_real_v8_scope_counts_and_direct_mapping(tmp_path):
    module = load_module()
    result = module.build(ROOT, BASELINE, tmp_path)
    assert result["status"] == "PASS"
    assert result["management_version"] == "V8"
    assert result["counts"] == {
        "work_items": 224,
        "canonical_requirements": 58,
        "cpf_fr": 5658,
        "cpf_sc": 7878,
        "engineering_gates": 21,
    }
    assert not any(result["missing_or_duplicate"].values())
    persisted = json.loads((tmp_path / "SCOPE_COVERAGE_VALIDATION.json").read_text(encoding="utf-8"))
    assert persisted["source_chain"].startswith("CPF_DEVELOPMENT_MANAGEMENT_V8/")
    assert persisted["cpf_fr_ids"][0].startswith("CPF-FR-")
    assert persisted["cpf_sc_ids"][0].startswith("CPF-SC-")


def test_duplicate_v8_development_item_fails_closed(tmp_path):
    module = load_module()
    fixture = tmp_path / "repo"
    shutil.copytree(ROOT / "cpf-docs/work/current", fixture / "cpf-docs/work/current")
    index = fixture / "cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/DEVELOPMENT_ITEM_INDEX.csv"
    lines = index.read_text(encoding="utf-8-sig").splitlines()
    index.write_text("\n".join(lines + [lines[1]]) + "\n", encoding="utf-8-sig")
    try:
        module.build(fixture, BASELINE, fixture / "out")
    except module.ScopeError as exc:
        assert "duplicate DEVELOPMENT_ITEM_INDEX IDs" in str(exc)
    else:
        raise AssertionError("duplicate V8 development item must fail closed")


def test_orphan_active_scope_id_fails_closed(tmp_path):
    module = load_module()
    fixture = tmp_path / "repo"
    shutil.copytree(ROOT / "cpf-docs/work/current", fixture / "cpf-docs/work/current")
    active = fixture / "cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/ACTIVE_DEVELOPMENT_SCOPE.csv"
    lines = active.read_text(encoding="utf-8-sig").splitlines()
    fields = lines[0].split(",")
    row = lines[1].split(",")
    row[fields.index("entity_id")] = "CPF-WP-ORPHAN-ACTIVE"
    active.write_text("\n".join(lines + [",".join(row)]) + "\n", encoding="utf-8-sig")
    try:
        module.build(fixture, BASELINE, fixture / "out")
    except module.ScopeError as exc:
        assert "active scope IDs absent from index" in str(exc)
    else:
        raise AssertionError("orphan active scope ID must fail closed")
