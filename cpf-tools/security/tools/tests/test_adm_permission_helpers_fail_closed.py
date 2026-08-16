from pathlib import Path

def test_adm_permission_helpers_fail_closed():
    root = Path(__file__).resolve().parents[4]
    source = (root / "cpf-admin/frontend/src/features/core/methods.ts").read_text(encoding="utf-8")
    assert "deleteAllowed === true" in source
    assert "deleteAllowed !== false" not in source
    assert "if (!this.buttonsLoaded) return false" in source
    assert "return menuId ? this.canWrite(menuId) : false" not in source
