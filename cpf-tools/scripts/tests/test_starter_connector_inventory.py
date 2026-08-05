from pathlib import Path
import importlib.util,json,tempfile
ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/scripts/verify-cpf-starter-connector-inventory.py'
spec=importlib.util.spec_from_file_location('starter_connector',SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
INV=ROOT/'cpf-docs/evidence/development/DEVGPT-6F_faedf43/snapshot/starter_connector_inventory.json'
SHA='faedf43a7baffdad456bf40f8e46d622db9cfc76'
def test_exact_inventory_passes():
    assert m.verify(ROOT,INV,SHA)['moduleCount']==38
def test_stale_baseline_fails():
    data=json.loads(INV.read_text());data['baselineSha']='0'*40
    with tempfile.TemporaryDirectory() as d:
        p=Path(d)/'i.json';p.write_text(json.dumps(data))
        try:m.verify(ROOT,p,SHA)
        except m.GateError:return
    raise AssertionError('stale inventory accepted')
