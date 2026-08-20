from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
LEGACY=ROOT/'cpf-tools/verification/tools/smoke-bza-ui.ps1'
CURRENT=ROOT/'cpf-tools/verification/tools/smoke-backoffice-ui.ps1'

def test_legacy_bza_smoke_is_current_backoffice_delegation():
 text=LEGACY.read_text(encoding='utf-8-sig')
 assert 'smoke-backoffice-ui.ps1' in text
 assert 'cpf-biz-frontend' not in text and 'VITE_BZA_CHANNEL_BASE_URL' not in text
 assert 'CPF_BACKOFFICE_FRONTEND_URL' in text

def test_current_backoffice_smoke_owns_mbw_surface():
 text=CURRENT.read_text(encoding='utf-8-sig')
 for token in ('cpf-backoffice-web/frontend','VITE_MBW_WEB_BASE_URL','fourFeatureRoutes','channelOnly','browserOwnsNoCpfHeader'):
  assert token in text
