from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SMOKE = ROOT / "cpf-tools/verification/tools/smoke-bza-ui.ps1"
FULL = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"


def test_bza_smoke_uses_current_vue_auth_route_generated_surfaces():
    text = SMOKE.read_text(encoding="utf-8-sig")
    assert "src/features/console.ts" in text and "legacyConsoleRemoved" in text
    assert "src/features/auth/session.ts" in text
    assert "src/app/routes.ts" in text
    assert "src/generated/orval/cpf-api.ts" in text
    assert "src/shared/orval-mutator.ts" in text
    assert "x-cpf-openapi-operation-count" in text
    assert "productionBuild" not in text


def test_bza_browser_smoke_is_real_and_secret_not_cli_logged():
    text = SMOKE.read_text(encoding="utf-8-sig")
    for token in ("BzaPassword", "BrowserClick", "RequireBrowserClick", "BZA_UI_SMOKE_PASSWORD"):
        assert token in text
    assert 'getByRole("heading", { name: "BZA Backoffice" })' in text
    assert 'getByLabel("로그인 ID")' in text
    assert 'getByLabel("비밀번호")' in text
    assert 'getByRole("navigation", { name: "업무 백오피스 메뉴" })' in text
    # Password is injected into process env; it must not be appended to Playwright CLI arguments.
    assert "--password" not in text.lower()


def test_full_local_passes_current_bza_frontend_url_contract():
    text = FULL.read_text(encoding="utf-8-sig")
    assert "CPF_BZA_FRONTEND_URL='http://127.0.0.1:8080/bza/'" in text
