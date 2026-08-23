#!/usr/bin/env python3
from pathlib import Path
import shutil, subprocess, tempfile, sys, os

root = Path(__file__).resolve().parents[3]
base = Path(__file__).resolve().parent


def resolve_typescript_command() -> list[str]:
    local_bin = root / 'cpf-admin' / 'frontend' / 'node_modules' / '.bin'
    candidates = [
        local_bin / ('tsc.cmd' if os.name == 'nt' else 'tsc'),
        Path(found) if (found := shutil.which('tsc')) else None,
    ]
    compiler = next((candidate.resolve() for candidate in candidates if candidate and candidate.is_file()), None)
    if compiler is None:
        raise SystemExit(
            'TypeScript compiler missing: run the canonical frontend dependency setup so '
            'cpf-admin/frontend/node_modules/.bin/tsc is available'
        )
    if compiler.suffix.lower() in {'.cmd', '.bat'}:
        command_processor = os.environ.get('COMSPEC') or shutil.which('cmd.exe')
        if not command_processor:
            raise SystemExit('Windows command processor is unavailable for the repository-local TypeScript compiler')
        return [command_processor, '/d', '/s', '/c', str(compiler)]
    return [str(compiler)]


typescript_command = resolve_typescript_command()

with tempfile.TemporaryDirectory(prefix='cpf-frontend-runtime-') as td:
    t = Path(td)

    # ADM: compile the actual protected-header/client/mutator implementation with minimal stubs.
    adm = t / 'adm'
    adm_src = adm / 'src'
    shutil.copytree(base / 'adm-stubs', adm_src)
    shutil.copy2(base / 'adm-tsconfig.json', adm / 'tsconfig.json')
    adm_actual = root / 'cpf-admin/frontend/src/shared'
    for name in ('cpfApi.ts', 'clientHeaders.ts', 'orval-mutator.ts'):
        actual = adm_actual / name
        if not actual.is_file():
            raise SystemExit(f'missing actual ADM source: {actual}')
        (adm_src / 'shared').mkdir(parents=True, exist_ok=True)
        shutil.copy2(actual, adm_src / 'shared' / name)
    c = subprocess.run([*typescript_command, '-p', str(adm / 'tsconfig.json')], text=True, capture_output=True)
    if c.returncode:
        print(c.stdout + c.stderr)
        raise SystemExit(c.returncode)

    # Backoffice Web: compile the actual channel client. import.meta.env is Vite-owned, so only
    # the environment expression is substituted in the temporary instrumentation copy; request
    # construction/security behavior remains the product implementation.
    bo = t / 'backoffice'
    bo_src = bo / 'src/shared/api'
    bo_src.mkdir(parents=True, exist_ok=True)
    actual_bo = root / 'cpf-backoffice-web/frontend/src/shared/api/channelHttpClient.ts'
    if not actual_bo.is_file():
        raise SystemExit(f'missing actual Backoffice Web client source: {actual_bo}')
    text = actual_bo.read_text(encoding='utf-8')
    text = text.replace("(import.meta.env.VITE_MBW_WEB_BASE_URL ?? '')", "('')")
    (bo_src / 'channelHttpClient.ts').write_text(text, encoding='utf-8')
    shutil.copy2(base / 'backoffice-tsconfig.json', bo / 'tsconfig.json')
    c = subprocess.run([*typescript_command, '-p', str(bo / 'tsconfig.json')], text=True, capture_output=True)
    if c.returncode:
        print(c.stdout + c.stderr)
        raise SystemExit(c.returncode)

    adm_api = adm / 'dist/shared/cpfApi.js'
    adm_mutator = adm / 'dist/shared/orval-mutator.js'
    bo_client = bo / 'dist/shared/api/channelHttpClient.js'
    env = os.environ.copy()
    env['NODE_PATH'] = str(adm / 'dist')
    r = subprocess.run(
        ['node', str(base / 'harness.cjs'), str(adm_api), str(adm_mutator), str(bo_client)],
        env=env, text=True, capture_output=True
    )
    print(r.stdout, end='')
    print(r.stderr, end='', file=sys.stderr)
    raise SystemExit(r.returncode)
