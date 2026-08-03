#!/usr/bin/env python3
from __future__ import annotations
import argparse, shutil, subprocess
from pathlib import Path

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', required=True)
    args = parser.parse_args()
    root = Path(args.root).resolve()
    base = root / 'cpf-tools/verification/java21/batch-abandon-runtime'
    out = base / 'build/classes'
    shutil.rmtree(base / 'build', ignore_errors=True)
    out.mkdir(parents=True)
    product_sources = [
        root / 'cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java',
        root / 'cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchExecutionException.java',
        root / 'cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchUnknownResultException.java',
        root / 'cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchAbandonCoordinator.java',
    ]
    # The harness owns only dependency stubs. Never compile a stub whose FQCN is
    # already supplied by an actual product source, otherwise javac can report a
    # duplicate class and hide the product behavior this harness is meant to test.
    product_fqcns = {
        'com.cpf.batch.api.BatchControlState',
        'com.cpf.batch.execution.CpfBatchExecutionException',
        'com.cpf.batch.execution.CpfBatchUnknownResultException',
        'com.cpf.batch.execution.CpfBatchAbandonCoordinator',
    }
    stub_sources = []
    for stub in sorted((base / 'stubs').rglob('*.java')):
        relative = stub.relative_to(base / 'stubs').with_suffix('')
        fqcn = '.'.join(relative.parts)
        if fqcn not in product_fqcns:
            stub_sources.append(stub)
    sources = [*product_sources, *stub_sources, *sorted((base / 'src').rglob('*.java'))]
    missing = [str(path) for path in sources if not path.is_file()]
    if missing:
        raise SystemExit('missing sources: ' + ', '.join(missing))
    compile_cmd = ['javac', '-encoding', 'UTF-8', '-source', '21', '-target', '21', '-d', str(out), *map(str, sources)]
    print('COMPILE', ' '.join(compile_cmd))
    subprocess.run(compile_cmd, check=True)
    run_cmd = ['java', '-cp', str(out), 'com.cpf.batch.execution.CpfBatchAbandonCoordinatorHarness']
    print('RUN', ' '.join(run_cmd))
    subprocess.run(run_cmd, check=True)
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
