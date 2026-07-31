#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', default='.')
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failed: list[str] = []
    for module, registry in [('cpf-admin', 'admRouterRecords'), ('cpf-biz-admin', 'bzaRouterRecords')]:
        cfg = (root / module / 'frontend/playwright.config.ts').read_text(encoding='utf-8-sig')
        spec = (root / module / 'frontend/e2e/route-quality.spec.ts').read_text(encoding='utf-8-sig')
        sec = (root / module / 'frontend/e2e/bff-security.spec.ts').read_text(encoding='utf-8-sig')
        for token in ['Desktop Chrome', 'Desktop Firefox', 'Desktop Safari', 'CPF_FRONTEND_URL',
                      'CPF_E2E_AUTH_STATE', 'CPF_E2E_ROUTE_MATRIX', 'CPF_E2E_FAILURE_MATRIX',
                      'CPF_E2E_SECURITY_FIXTURE']:
            if token not in cfg:
                failed.append(f'{module}:config:{token}')
        for token in [registry, 'for(const path of routes)', 'release route matrix covers',
                      'expect(injected', '401,403,409,429,500,503', 'server-side failure matrix',
                      'riskConfirmationSelector', 'horizontal overflow']:
            if token not in spec:
                failed.append(f'{module}:route:{token}')
        for forbidden in ['slice(0, 40)', "test.skip(release,'Release mode uses real backend failure scenarios"]:
            if forbidden in spec:
                failed.append(f'{module}:false-green:{forbidden}')
        for token in ['CPF_E2E_PRIVILEGED_ENDPOINTS', '[401, 403]', 'localStorage', 'sessionStorage',
                      'session fixation', 'logout did not revoke session', 'write without CSRF token',
                      'untrusted Origin', 'firstSessionAfterSecondLoginStatuses']:
            if token not in sec:
                failed.append(f'{module}:security:{token}')
    if failed:
        raise SystemExit('browser contract failed: ' + ', '.join(failed))
    print('CPF 3-browser full-route/BFF contract: PASS')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
