import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
FRONTENDS=(ROOT/"cpf-admin/frontend/package-lock.json",ROOT/"cpf-backoffice-web/frontend/package-lock.json")
def test_orval_optional_prettier_peer_is_resolved_from_install_root_for_both_frontends():
    for lock in FRONTENDS:
        packages=json.loads(lock.read_text(encoding="utf-8"))["packages"]
        assert packages["node_modules/orval"]["peerDependencies"]["prettier"]==">=3.0.0"
        assert packages["node_modules/orval"]["peerDependenciesMeta"]["prettier"]["optional"] is True
        assert packages["node_modules/prettier"]["version"]=="3.9.6"
        assert "node_modules/orval/node_modules/prettier" not in packages
