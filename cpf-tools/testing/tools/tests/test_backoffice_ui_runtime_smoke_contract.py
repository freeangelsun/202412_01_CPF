from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SMOKE=ROOT/"cpf-tools/verification/tools/smoke-backoffice-ui.ps1"
FULL=ROOT/"cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"

def test_backoffice_smoke_uses_external_reference_surface():
    text=SMOKE.read_text(encoding="utf-8-sig")
    for token in ("cpf-backoffice-web/frontend","VITE_MBW_WEB_BASE_URL","fourFeatureRoutes","channelOnly","browserOwnsNoCpfHeader"):
        assert token in text
    assert "cpf-backoffice/online/frontend" not in text

def test_backoffice_browser_smoke_has_no_local_login_secret_contract():
    text=SMOKE.read_text(encoding="utf-8-sig")
    assert "CPF_BACKOFFICE_FRONTEND_URL" in text
    assert "BackofficePassword" not in text and "로그인 ID" not in text
    assert "BACKOFFICE reference navigation" in text

def test_full_local_does_not_target_embedded_backoffice_frontend():
    text=FULL.read_text(encoding="utf-8-sig")
    assert "cpf-backoffice/online\\frontend" not in text
