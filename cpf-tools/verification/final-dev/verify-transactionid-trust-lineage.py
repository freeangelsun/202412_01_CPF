#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path
import shutil
import tempfile

POLICY = Path('cpf-core/src/main/java/com/cpf/core/common/transaction/CpfInboundTransactionIdPolicy.java')
FILTER = Path('cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java')
TEST = Path('cpf-core/src/test/java/com/cpf/core/common/transaction/CpfInboundTransactionIdPolicyTest.java')
PROP = Path('cpf-core/src/main/java/com/cpf/core/common/header/CpfHeaderPropagator.java')
BROKER = Path('cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfBrokerHeaderPolicy.java')
GEN = Path('cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java')


def text(root: Path, rel: Path, required: bool = True) -> str:
    p = root / rel
    if not p.exists():
        if required:
            raise AssertionError(f'missing required source: {rel.as_posix()}')
        return ''
    return p.read_text(encoding='utf-8')


def verify(root: Path) -> None:
    policy = text(root, POLICY)
    flt = text(root, FILTER)
    tests = text(root, TEST)
    gen = text(root, GEN)
    prop = text(root, PROP, required=False)
    broker = text(root, BROKER, required=False)

    required_policy = [
        'AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE',
        'authoritativeStarterSystem(request)',
        'authoritativePropagatingSystem(request)',
        'guardStartReplay(request, incoming, encodedOrigin)',
        'return new Resolution(incoming, true, start',
        'Channel metadata (CHANNEL_CODE / ORIGINAL_CHANNEL_CODE) is not a system identity',
    ]
    for needle in required_policy:
        assert needle in policy, f'transactionId trust policy missing semantic: {needle}'
    assert 'transactionIdPolicy.resolve(request, transactionIdGenerator).transactionId()' in flt, \
        'inbound filter must delegate valid inbound transactionId to trust policy'
    assert 'transactionIdGenerator.generateOrUse(' not in flt, \
        'filter must not accept transactionId based only on syntax'
    assert 'CpfTransactionIds.isCanonical(transactionId)' in gen and 'DEFAULT_SEQUENCE_DIGITS = 7' in gen, \
        'canonical 34-character transactionId generator contract must remain intact'

    # Channel metadata must never be accepted as a System identity.
    starter_body = policy.split('private static String authoritativeStarterSystem', 1)[1].split('private static String authoritativePropagatingSystem', 1)[0]
    assert 'getHeader(CpfHeaderNames.CHANNEL_CODE)' not in starter_body \
        and 'getHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE)' not in starter_body, \
        'starter System provenance must not be inferred from ChannelCode headers'

    test_semantics = [
        'officialTrustedChannelStartPreservesItsCanonicalTransactionId',
        'trustedInternalHopPreservesOriginalTransactionIdWithoutCreatingNewStartClaim',
        'unauthenticatedPublicClientCannotInjectAValidInternalTransactionId',
        'originSystemMismatchIsRejectedEvenWhenTransportIsTrusted',
        'differentStartRequestCannotReplayAnExistingTransactionId',
        'trustedChannelMetadataAloneCannotAssertSystemProvenance',
        '"MOBILE", "ADM", "CHANNEL"',
        '"MOBILE", "CMN", "INTERNAL"',
    ]
    for needle in test_semantics:
        assert needle in tests, f'transactionId trust regression missing: {needle}'

    if prop:
        assert 'TransactionContext.getOrCreateTransactionId()' in prop, \
            'outbound propagation must use the current transactionId'
        assert 'headers, CpfHeaderNames.TRANSACTION_ID, transactionId' in prop, \
            'outbound propagation must emit the same transactionId'
        assert 'PARENT_TRANSACTION_SEGMENT_ID' in prop, \
            'sub-call identity must use segment lineage rather than a replacement transactionId'
    if broker:
        assert 'request.transactionId()' in broker, \
            'broker publish validation must preserve the transactionId field'


def self_test(root: Path) -> None:
    with tempfile.TemporaryDirectory(prefix='cpf-txid-mutation-') as tmp:
        mroot = Path(tmp)
        for rel in (POLICY, FILTER, TEST, GEN, PROP, BROKER):
            src = root / rel
            if src.exists():
                dst = mroot / rel
                dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(src, dst)
        # Mutation 1: regenerate a trusted inbound transactionId.
        p = mroot / POLICY
        s = p.read_text(encoding='utf-8').replace(
            'return new Resolution(incoming, true, start,',
            'return new Resolution(generator.generate(), true, start,', 1)
        p.write_text(s, encoding='utf-8')
        try:
            verify(mroot)
        except AssertionError:
            pass
        else:
            raise AssertionError('mutation survived: trusted inbound transactionId regeneration')

        # restore, then mutation 2: treat ChannelCode as System provenance.
        shutil.copy2(root / POLICY, p)
        s = p.read_text(encoding='utf-8').replace(
            'String serverAuthenticated = text(request.getAttribute(AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE));',
            'String serverAuthenticated = text(request.getHeader(CpfHeaderNames.CHANNEL_CODE));', 1)
        p.write_text(s, encoding='utf-8')
        try:
            verify(mroot)
        except AssertionError:
            pass
        else:
            raise AssertionError('mutation survived: ChannelCode used as System identity')


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', required=True)
    ap.add_argument('--self-test', action='store_true')
    args = ap.parse_args()
    root = Path(args.root).resolve()
    verify(root)
    if args.self_test:
        self_test(root)
    print('[CPF][TXID][PASS] canonical34=true trustedStarterPreserved=true trustedPropagationPreserved=true '
          f'spoofReplayGuard=true channelSystemSeparated=true selfTest={str(args.self_test).lower()}')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
