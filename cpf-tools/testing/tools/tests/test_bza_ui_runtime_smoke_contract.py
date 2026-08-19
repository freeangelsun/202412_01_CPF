from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SMOKE=ROOT/"cpf-tools/verification/tools/smoke-bza-ui.ps1"
FULL=ROOT/"cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"

def test_bza_smoke_uses_external_reference_surface():
    text=SMOKE.read_text(encoding="utf-8-sig")
    for token in ("cpf-biz-frontend","VITE_BZA_CHANNEL_BASE_URL","fourFeatureRoutes","channelOnly","browserOwnsNoCpfHeader"):
        assert token in text
    assert "cpf-biz-admin/frontend" not in text

def test_bza_browser_smoke_has_no_local_login_secret_contract():
    text=SMOKE.read_text(encoding="utf-8-sig")
    assert "CPF_BZA_FRONTEND_URL" in text
    assert "BzaPassword" not in text and "로그인 ID" not in text
    assert "BZA reference navigation" in text

def test_full_local_does_not_target_embedded_bza_frontend():
    text=FULL.read_text(encoding="utf-8-sig")
    assert "cpf-biz-admin\\frontend" not in text
