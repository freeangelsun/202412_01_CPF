#!/usr/bin/env python3
from __future__ import annotations
import argparse
import shutil
import sys
import tempfile
from pathlib import Path


def check(root: Path) -> list[str]:
    errors: list[str] = []
    frontend = root / 'cpf-admin/frontend'
    page = frontend / 'src/features/approvals/ApprovalsPage.vue'
    client = frontend / 'src/generated/orval/cpf-api.ts'
    gate = frontend / 'scripts/verify-operation-consumer.mjs'
    models = {
        'PolicyCommand': frontend / 'src/generated/orval/model/policyCommand.ts',
        'RequestCommand': frontend / 'src/generated/orval/model/requestCommand.ts',
        'DecisionCommand': frontend / 'src/generated/orval/model/decisionCommand.ts',
        'AdmApprovalReconcileParams': frontend / 'src/generated/orval/model/admApprovalReconcileParams.ts',
    }
    for path in [page, client, gate, *models.values()]:
        if not path.exists():
            errors.append(f'missing:{path.relative_to(root)}')
    if errors:
        return errors
    page_text = page.read_text(encoding='utf-8')
    client_text = client.read_text(encoding='utf-8')
    gate_text = gate.read_text(encoding='utf-8')
    if 'from "../../generated/orval/cpf-api"' not in page_text:
        errors.append('approval-page-not-orval')
    if 'from "../../generated/cpf-api"' in page_text:
        errors.append('approval-page-compatibility-client')
    if 'admApprovalRequest<Record<string,unknown>>' in page_text.replace(' ', ''):
        errors.append('approval-page-generic-request')
    required_calls = [
        'admApprovalPolicySave({',
        'admApprovalRequest({',
        'admApprovalDecision(Number(this.approvalEngine.requestId),{',
        'admApprovalExecute(Number(this.approvalEngine.requestId),{reason:',
        'admApprovalReconcile(Number(this.approvalEngine.requestId),{reason:',
    ]
    for token in required_calls:
        if token not in page_text.replace(' ', '') if ' ' not in token else token not in page_text:
            errors.append('approval-page-call-missing:' + token)
    signatures = [
        'admApprovalPolicySave = async (data: PolicyCommand',
        'admApprovalRequest = async (data: RequestCommand',
        'admApprovalDecision = async (id: number, data: DecisionCommand',
        'admApprovalExecute = async (id: number, params: { reason: string }',
        'params: AdmApprovalReconcileParams',
    ]
    for token in signatures:
        if token not in client_text:
            errors.append('generated-signature-missing:' + token)
    for name, path in models.items():
        text = path.read_text(encoding='utf-8')
        if 'Record<string, unknown>' in text or '= unknown' in text:
            errors.append(f'generic-model:{name}')
        if f'interface {name}' not in text:
            errors.append(f'non-interface-model:{name}')
    if '!typedGeneratedConsumed.has(operation.operationId)' not in gate_text:
        errors.append('gate-does-not-require-typed-generated')
    # Direct model inspection + mutation below reject generic/unknown approval models.
    # The shared consumer gate must additionally reject raw/generic HIGH/CRITICAL bypasses
    # and require imported concrete Orval operations.
    if 'high-risk mutation raw/generic bypass is forbidden' not in gate_text:
        errors.append('gate-does-not-reject-raw-generic-bypass')
    if 'high-risk mutation must call concrete typed Orval generated API/model' not in gate_text:
        errors.append('gate-does-not-require-concrete-typed-orval')
    return errors


def self_test(root: Path) -> list[str]:
    failures: list[str] = []
    with tempfile.TemporaryDirectory(prefix='cpf-typed-client-mutation-') as tmp:
        fixture = Path(tmp) / 'repo'
        shutil.copytree(root / 'cpf-admin', fixture / 'cpf-admin')
        shutil.copytree(root / 'cpf-tools', fixture / 'cpf-tools')
        page = fixture / 'cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue'
        original = page.read_text(encoding='utf-8')
        page.write_text(original.replace('../../generated/orval/cpf-api', '../../generated/cpf-api'), encoding='utf-8')
        if not check(fixture):
            failures.append('mutation-compatibility-import-survived')
        page.write_text(original, encoding='utf-8')
        model = fixture / 'cpf-admin/frontend/src/generated/orval/model/policyCommand.ts'
        model_original = model.read_text(encoding='utf-8')
        model.write_text('export type PolicyCommand = Record<string, unknown>;\n', encoding='utf-8')
        if not check(fixture):
            failures.append('mutation-generic-model-survived')
        model.write_text(model_original, encoding='utf-8')
    return failures


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', required=True)
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    root = Path(args.root).resolve()
    errors = check(root)
    if not errors and args.self_test:
        errors.extend(self_test(root))
    if errors:
        print('[CPF][ADM-APPROVAL-TYPED][FAIL]')
        print('\n'.join(errors))
        return 1
    print('[CPF][ADM-APPROVAL-TYPED][PASS] typedOrval=true genericBody=false mutationKilled=' + str(args.self_test).lower())
    return 0

if __name__ == '__main__':
    sys.exit(main())
