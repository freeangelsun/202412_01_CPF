import hashlib,importlib.util,json,zipfile
from pathlib import Path
import pytest

SCRIPT=Path(__file__).parents[1]/'verify-devgpt6f-deliverable.py'
spec=importlib.util.spec_from_file_location('v6fzip',SCRIPT);mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)

def test_rejects_path_traversal(tmp_path):
    z=tmp_path/'bad.zip'
    with zipfile.ZipFile(z,'w') as out:out.writestr('../escape.txt','x')
    with pytest.raises(SystemExit):mod.validate(str(z))

def test_rejects_manifest_hash_mismatch(tmp_path):
    z=tmp_path/'bad-hash.zip'; mp=mod.BASE+'PACKAGE_MANIFEST.json'; p='cpf-tools/scripts/x.py'
    manifest={'baselineSha':'09dd686c5ae0826594b9c5e1f871d95d95d3ce1c','files':[{'path':p,'size':1,'sha256':'0'*64}]}
    with zipfile.ZipFile(z,'w') as out:
        out.writestr(p,b'x');out.writestr(mp,json.dumps(manifest))
    with pytest.raises(SystemExit):mod.validate(str(z))
