from pathlib import Path
import importlib.util, json, tempfile

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/governance/tools/verify-cpf-starter-connector-inventory.py"
spec = importlib.util.spec_from_file_location("starter_connector", SCRIPT)
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
SHA = "a" * 40
TREE = "b" * 40

def build_inventory(path: Path):
    catalog = json.loads((ROOT / "cpf-tools/generator/contracts/cpf-starter-catalog.json").read_text(encoding="utf-8"))
    modules = []
    for row in catalog["modules"]:
        owner = row.get("ownerPath") or row.get("physicalPath") or row.get("path")
        modules.append({"ownerPath": owner, "buildFile": f"{owner}/build.gradle", "blobSha": "c" * 40})
    data = {
        "status": "PASS",
        "baselineSha": SHA,
        "starterTreeSha": TREE,
        "catalogModuleCount": len(catalog["modules"]),
        "inventoryModuleCount": len(modules),
        "modules": modules,
        "provenance": {"tree_unchanged": True, "current_baseline_sha": SHA, "inventory_tree_sha": TREE, "current_tree_sha": TREE},
    }
    path.write_text(json.dumps(data), encoding="utf-8")

def test_exact_inventory_passes():
    with tempfile.TemporaryDirectory() as d:
        inv = Path(d) / "starter_connector_inventory.json"; build_inventory(inv)
        assert m.verify(ROOT, inv, SHA)["moduleCount"] > 0

def test_stale_baseline_fails():
    with tempfile.TemporaryDirectory() as d:
        inv = Path(d) / "starter_connector_inventory.json"; build_inventory(inv)
        data = json.loads(inv.read_text()); data["baselineSha"] = "0" * 40; inv.write_text(json.dumps(data))
        try: m.verify(ROOT, inv, SHA)
        except m.GateError: return
    raise AssertionError("stale inventory accepted")
