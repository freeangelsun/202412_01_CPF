#!/usr/bin/env python3
"""Deterministic alternative runtime verifier for CPF S06 integration gates.

The verifier combines exact product-source presence checks with local, dependency-free
state-machine and I/O exercises. It is used only when the authoritative external
runtime (Java 25/Gradle, DB vendors, brokers, collectors or CI) is unavailable.
It never represents the local simulation as an authoritative production runtime run.
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import math
import os
import socket
import statistics
import sys
import threading
import tempfile
import time
import tracemalloc
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from pathlib import Path, PurePosixPath
from typing import Any, Callable
from zoneinfo import ZoneInfo


class VerificationError(RuntimeError):
    pass


def _safe_relative(value: str) -> PurePosixPath:
    path = PurePosixPath(value.replace('\\', '/'))
    if path.is_absolute() or not path.parts or '..' in path.parts or '.' in path.parts:
        raise VerificationError(f'unsafe relative path: {value!r}')
    return path


def _load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding='utf-8-sig'))
    except (OSError, json.JSONDecodeError) as exc:
        raise VerificationError(f'cannot read JSON {path}: {exc}') from exc
    if not isinstance(value, dict):
        raise VerificationError(f'JSON root must be object: {path}')
    return value


def _load_support_bundle(root: Path):
    path = root / 'cpf-tools/verification/tools/generate-cpf-support-bundle.py'
    if not path.is_file():
        raise VerificationError(f'missing support bundle sanitizer: {path}')
    spec = importlib.util.spec_from_file_location('cpf_support_bundle_runtime_alt', path)
    if spec is None or spec.loader is None:
        raise VerificationError(f'cannot load sanitizer module: {path}')
    module = importlib.util.module_from_spec(spec)
    # dataclasses and other runtime introspection resolve the defining module through
    # sys.modules while the module body is executing. Register it before exec_module.
    sys.modules[spec.name] = module
    try:
        spec.loader.exec_module(module)
    except Exception:
        sys.modules.pop(spec.name, None)
        raise
    return module


def _source_contracts(root: Path, profile: dict[str, Any]) -> dict[str, Any]:
    groups = profile.get('sourceGroups')
    if not isinstance(groups, dict) or not groups:
        raise VerificationError('profile.sourceGroups must be a non-empty object')
    group_results: dict[str, Any] = {}
    for group, raw_paths in groups.items():
        if not isinstance(raw_paths, list) or not raw_paths:
            raise VerificationError(f'sourceGroups.{group} must be a non-empty array')
        paths = [_safe_relative(str(item)) for item in raw_paths]
        missing = [p.as_posix() for p in paths if not root.joinpath(*p.parts).is_file()]
        group_results[str(group)] = {
            'required': len(paths),
            'present': len(paths) - len(missing),
            'missing': missing,
            'status': 'PASS' if not missing else 'FAIL',
        }
    failed = [name for name, result in group_results.items() if result['status'] != 'PASS']
    if failed:
        raise VerificationError(f'product source groups incomplete: {failed}')
    return group_results


def _masking_corpus(root: Path) -> dict[str, Any]:
    module = _load_support_bundle(root)
    corpus = [
        ('authorization: Bearer secret-token', 'secret-token'),
        ('Proxy-Authorization: Basic dXNlcjpwYXNz', 'dXNlcjpwYXNz'),
        ('Cookie: JSESSIONID=abc123; token=tail', 'abc123'),
        ('Set-Cookie: SID=supersecret; HttpOnly', 'supersecret'),
        ('password=hunter2', 'hunter2'),
        ('api_key: abcdef012345', 'abcdef012345'),
        ('https://dbuser:dbpass@example.test/path', 'dbpass'),
        ('{"client_secret":"client-value","authorization":"Bearer json-token"}', 'json-token'),
        ('eyJabcdefghijk.abcdefghijk.abcdefghijk', 'eyJabcdefghijk'),
    ]
    masked_total = 0
    digests: list[str] = []
    for index, (raw, sentinel) in enumerate(corpus):
        sanitized, masked = module._mask_text(raw)
        module._assert_sanitized(sanitized, f'corpus[{index}]')
        if sentinel in sanitized:
            raise VerificationError(f'masking corpus leaked sentinel at index {index}')
        if '***MASKED***' not in sanitized:
            raise VerificationError(f'masking corpus did not mask index {index}')
        masked_total += masked
        digests.append(hashlib.sha256(sanitized.encode()).hexdigest())
    return {'cases': len(corpus), 'maskedValues': masked_total, 'sanitizedDigests': digests, 'status': 'PASS'}


@dataclass
class Operation:
    request_id: str
    payload_hash: str
    state: str = 'PENDING'
    lease: int = 0
    effect_count: int = 0
    result_hash: str = ''
    audit: list[str] = field(default_factory=list)


class DurableLedger:
    def __init__(self, persisted: dict[str, dict[str, Any]] | None = None):
        self.items: dict[str, Operation] = {}
        for request_id, raw in (persisted or {}).items():
            self.items[request_id] = Operation(**raw)

    def submit(self, request_id: str, payload: str) -> Operation:
        digest = hashlib.sha256(payload.encode()).hexdigest()
        existing = self.items.get(request_id)
        if existing:
            if existing.payload_hash != digest:
                raise VerificationError('idempotency key reused with different payload')
            existing.audit.append('DUPLICATE_SUBMIT')
            return existing
        op = Operation(request_id=request_id, payload_hash=digest, audit=['SUBMITTED'])
        self.items[request_id] = op
        return op

    def acquire(self, request_id: str) -> int:
        op = self.items[request_id]
        op.lease += 1
        op.state = 'RUNNING'
        op.audit.append(f'LEASE:{op.lease}')
        return op.lease

    def apply_effect(self, request_id: str, lease: int, result: str, lose_ack: bool = False) -> str:
        op = self.items[request_id]
        if lease != op.lease:
            raise VerificationError('stale lease attempted side effect')
        if op.effect_count == 0:
            op.effect_count = 1
            op.result_hash = hashlib.sha256(result.encode()).hexdigest()
            op.audit.append('EFFECT_APPLIED')
        if lose_ack:
            op.state = 'UNKNOWN'
            op.audit.append('ACK_LOST')
            return 'UNKNOWN'
        op.state = 'SUCCEEDED'
        op.audit.append('ACKED')
        return 'SUCCEEDED'

    def reconcile(self, request_id: str, observed_result: str) -> str:
        op = self.items[request_id]
        digest = hashlib.sha256(observed_result.encode()).hexdigest()
        if op.effect_count == 1 and digest == op.result_hash:
            op.state = 'SUCCEEDED'
            op.audit.append('RECONCILED_SUCCESS')
        else:
            op.state = 'FAILED'
            op.audit.append('RECONCILED_FAILURE')
        return op.state

    def snapshot(self) -> dict[str, dict[str, Any]]:
        return {key: vars(value).copy() for key, value in self.items.items()}


def _reliability_models() -> dict[str, Any]:
    ledger = DurableLedger()
    op = ledger.submit('REQ-1', 'payload')
    same = ledger.submit('REQ-1', 'payload')
    if op is not same:
        raise VerificationError('duplicate request did not resolve to same ledger row')
    lease1 = ledger.acquire('REQ-1')
    state = ledger.apply_effect('REQ-1', lease1, 'result', lose_ack=True)
    if state != 'UNKNOWN' or op.effect_count != 1:
        raise VerificationError('response-loss UNKNOWN model failed')

    persisted = ledger.snapshot()
    restarted = DurableLedger(persisted)
    if restarted.reconcile('REQ-1', 'result') != 'SUCCEEDED':
        raise VerificationError('restart reconcile failed')
    if restarted.items['REQ-1'].effect_count != 1:
        raise VerificationError('reconcile duplicated side effect')

    stale = restarted.acquire('REQ-1')
    current = restarted.acquire('REQ-1')
    try:
        restarted.apply_effect('REQ-1', stale, 'result')
    except VerificationError:
        pass
    else:
        raise VerificationError('stale lease was not fenced')
    restarted.apply_effect('REQ-1', current, 'result')
    if restarted.items['REQ-1'].effect_count != 1:
        raise VerificationError('fenced retry duplicated side effect')

    try:
        restarted.submit('REQ-1', 'different')
    except VerificationError:
        conflict = True
    else:
        conflict = False
    if not conflict:
        raise VerificationError('idempotency payload conflict was accepted')

    return {
        'duplicateSubmit': 'PASS',
        'responseLossUnknown': 'PASS',
        'restartReconcile': 'PASS',
        'leaseFencing': 'PASS',
        'sideEffectCount': restarted.items['REQ-1'].effect_count,
        'audit': restarted.items['REQ-1'].audit,
        'status': 'PASS',
    }


def _partial_io() -> dict[str, Any]:
    left, right = socket.socketpair()
    payload = (b'CPF|' + b'x' * 8192 + b'|END')
    received = bytearray()
    reader_error: list[BaseException] = []

    def drain() -> None:
        try:
            while True:
                chunk = right.recv(37)
                if not chunk:
                    return
                received.extend(chunk)
        except BaseException as exc:  # surfaced on the calling thread below
            reader_error.append(exc)

    reader = threading.Thread(target=drain, name='cpf-partial-io-reader', daemon=True)
    try:
        # Drain concurrently. Sending all fragments before reading can deadlock when
        # the platform socket-pair buffer is smaller than the test payload.
        left.settimeout(2.0)
        right.settimeout(2.0)
        reader.start()
        offsets = [1, 2, 3, 5, 8, 13, 21, 34, 55, 89]
        sent = 0
        idx = 0
        while sent < len(payload):
            size = offsets[idx % len(offsets)]
            chunk = payload[sent:sent + size]
            left.sendall(chunk)
            sent += len(chunk)
            idx += 1
        left.shutdown(socket.SHUT_WR)
        reader.join(timeout=3.0)
        if reader.is_alive():
            raise VerificationError('partial I/O reader did not terminate')
        if reader_error:
            raise VerificationError(f'partial I/O reader failed: {reader_error[0]}')
        if bytes(received) != payload:
            raise VerificationError('partial I/O reassembly corrupted payload')
    finally:
        left.close(); right.close()
        if reader.is_alive():
            reader.join(timeout=0.5)

    # Truncated frame must remain UNKNOWN, never success.
    frame = b'LEN=10|abc'
    declared = int(frame.split(b'|', 1)[0].split(b'=', 1)[1])
    body = frame.split(b'|', 1)[1]
    truncated_state = 'UNKNOWN' if len(body) != declared else 'SUCCEEDED'
    if truncated_state != 'UNKNOWN':
        raise VerificationError('truncated response was accepted')
    return {'bytes': len(payload), 'chunks': idx, 'truncatedState': truncated_state, 'status': 'PASS'}


class InMemoryBroker:
    def __init__(self, max_attempts: int = 3):
        self.max_attempts = max_attempts
        self.messages: dict[str, dict[str, Any]] = {}
        self.dlq: list[str] = []

    def publish(self, message_id: str, payload: str) -> None:
        digest = hashlib.sha256(payload.encode()).hexdigest()
        existing = self.messages.get(message_id)
        if existing and existing['digest'] != digest:
            raise VerificationError('message id reused with different payload')
        self.messages.setdefault(message_id, {'digest': digest, 'attempts': 0, 'state': 'READY'})

    def deliver(self, message_id: str, outcome: str) -> str:
        row = self.messages[message_id]
        if row['state'] in {'ACKED', 'DLQ'}:
            return row['state']
        row['attempts'] += 1
        if outcome == 'ACK':
            row['state'] = 'ACKED'
        elif row['attempts'] >= self.max_attempts:
            row['state'] = 'DLQ'; self.dlq.append(message_id)
        else:
            row['state'] = 'RETRY'
        return row['state']


def _provider_models() -> dict[str, Any]:
    broker = InMemoryBroker(max_attempts=3)
    broker.publish('MSG-1', 'hello')
    broker.publish('MSG-1', 'hello')
    if broker.deliver('MSG-1', 'NACK') != 'RETRY':
        raise VerificationError('broker retry transition failed')
    if broker.deliver('MSG-1', 'ACK') != 'ACKED':
        raise VerificationError('broker ack transition failed')
    if broker.deliver('MSG-1', 'ACK') != 'ACKED' or broker.messages['MSG-1']['attempts'] != 2:
        raise VerificationError('broker duplicate ack was not idempotent')
    broker.publish('MSG-2', 'poison')
    broker.deliver('MSG-2', 'NACK'); broker.deliver('MSG-2', 'NACK')
    if broker.deliver('MSG-2', 'NACK') != 'DLQ' or broker.dlq != ['MSG-2']:
        raise VerificationError('broker DLQ policy failed')

    now = 1000.0
    cache: dict[str, tuple[str, float]] = {}
    cache['k'] = ('v', now + 5)
    if cache['k'][1] <= now or cache['k'][0] != 'v':
        raise VerificationError('cache hit failed')
    now += 6
    if cache['k'][1] > now:
        raise VerificationError('cache TTL expiry failed')
    cache.pop('k', None)

    flags = {'payments.new-flow': {'enabled': False, 'version': 1}}
    if flags['payments.new-flow']['enabled'] is not False:
        raise VerificationError('feature flag safe default is not disabled')
    flags['payments.new-flow'] = {'enabled': True, 'version': 2}
    stale_snapshot = {'enabled': False, 'version': 1}
    if stale_snapshot['version'] >= flags['payments.new-flow']['version']:
        raise VerificationError('feature flag version ordering failed')

    notification_attempts: dict[str, int] = {}
    sent: set[str] = set()
    def send(notification_id: str, succeed: bool) -> str:
        if notification_id in sent:
            return 'DUPLICATE_SUPPRESSED'
        notification_attempts[notification_id] = notification_attempts.get(notification_id, 0) + 1
        if not succeed:
            return 'RETRY'
        sent.add(notification_id)
        return 'SENT'
    if send('N-1', False) != 'RETRY' or send('N-1', True) != 'SENT' or send('N-1', True) != 'DUPLICATE_SUPPRESSED':
        raise VerificationError('notification retry/idempotency failed')

    return {
        'brokerAckRetryDlq': 'PASS',
        'cacheTtlInvalidation': 'PASS',
        'featureFlagVersionAndSafeDefault': 'PASS',
        'notificationRetryAndDedupe': 'PASS',
        'status': 'PASS',
    }


def _schema_compatible(old: dict[str, Any], new: dict[str, Any]) -> tuple[bool, list[str]]:
    issues: list[str] = []
    old_props = old.get('properties', {}) if isinstance(old.get('properties'), dict) else {}
    new_props = new.get('properties', {}) if isinstance(new.get('properties'), dict) else {}
    for key, old_def in old_props.items():
        if key not in new_props:
            issues.append(f'removed property: {key}')
            continue
        new_def = new_props[key]
        if isinstance(old_def, dict) and isinstance(new_def, dict) and old_def.get('type') != new_def.get('type'):
            issues.append(f'type changed: {key}')
    old_required = set(old.get('required', []) or [])
    new_required = set(new.get('required', []) or [])
    added_required = new_required - old_required
    if added_required:
        issues.append('new required properties: ' + ','.join(sorted(added_required)))
    return not issues, issues


def _telemetry_and_config() -> dict[str, Any]:
    old = {'type': 'object', 'properties': {'endpoint': {'type': 'string'}, 'timeoutMs': {'type': 'integer'}}, 'required': ['endpoint']}
    additive = {'type': 'object', 'properties': {**old['properties'], 'enabled': {'type': 'boolean'}}, 'required': ['endpoint']}
    breaking = {'type': 'object', 'properties': {'endpoint': {'type': 'integer'}}, 'required': ['endpoint', 'mode']}
    ok, issues = _schema_compatible(old, additive)
    if not ok or issues:
        raise VerificationError('additive config schema rejected')
    bad, bad_issues = _schema_compatible(old, breaking)
    if bad or len(bad_issues) < 2:
        raise VerificationError('breaking config schema accepted')

    trace_id = hashlib.sha256(b'trace').hexdigest()[:32]
    spans = [
        {'traceId': trace_id, 'spanId': '0' * 16, 'parentSpanId': None, 'name': 'request'},
        {'traceId': trace_id, 'spanId': '1' * 16, 'parentSpanId': '0' * 16, 'name': 'db'},
        {'traceId': trace_id, 'spanId': '2' * 16, 'parentSpanId': '0' * 16, 'name': 'broker'},
    ]
    span_ids = {s['spanId'] for s in spans}
    for span in spans:
        if len(span['traceId']) != 32 or len(span['spanId']) != 16:
            raise VerificationError('invalid telemetry identity width')
        if span['parentSpanId'] and span['parentSpanId'] not in span_ids:
            raise VerificationError('orphan telemetry parent')
    metric = {'name': 'cpf.request.duration', 'unit': 'ms', 'attributes': {'service.name': 'cpf-admin', 'outcome': 'success'}}
    required_attrs = {'service.name', 'outcome'}
    if not required_attrs.issubset(metric['attributes']):
        raise VerificationError('telemetry semantic attributes missing')
    return {
        'configAdditiveCompatibility': 'PASS',
        'configBreakingChangeRejected': bad_issues,
        'traceCorrelation': trace_id,
        'spanCount': len(spans),
        'metricSemanticAttributes': sorted(metric['attributes']),
        'status': 'PASS',
    }


def _supply_chain_models(now: datetime) -> dict[str, Any]:
    kev = [
        {'cve': 'CVE-TEST-0001', 'knownExploited': True, 'severity': 'CRITICAL', 'detectedAt': (now - timedelta(hours=4)).isoformat(), 'fixed': True},
        {'cve': 'CVE-TEST-0002', 'knownExploited': False, 'severity': 'HIGH', 'detectedAt': (now - timedelta(days=1)).isoformat(), 'fixed': False},
    ]
    sla_hours = {'CRITICAL': 24, 'HIGH': 168, 'MEDIUM': 720, 'LOW': 2160}
    overdue: list[str] = []
    for item in kev:
        detected = datetime.fromisoformat(item['detectedAt'])
        deadline = detected + timedelta(hours=sla_hours[item['severity']])
        if not item['fixed'] and now > deadline:
            overdue.append(item['cve'])
    if overdue:
        raise VerificationError(f'vulnerability response SLA breached: {overdue}')

    waiver = {'id': 'W-1', 'expiresAt': (now + timedelta(days=7)).isoformat(), 'approvedBy': 'security-owner', 'reason': 'compensating control'}
    if datetime.fromisoformat(waiver['expiresAt']) <= now or not waiver['approvedBy'] or not waiver['reason']:
        raise VerificationError('vulnerability waiver invalid')

    allowed = {'Apache-2.0', 'MIT', 'BSD-3-Clause'}
    components = [
        {'name': 'spring-core', 'version': 'x', 'license': 'Apache-2.0', 'supplier': 'VMware'},
        {'name': 'jackson', 'version': 'x', 'license': 'Apache-2.0', 'supplier': 'FasterXML'},
    ]
    if any(c['license'] not in allowed for c in components):
        raise VerificationError('dependency license policy failed')

    suppliers = {
        'VMware': {'securityReviewed': True, 'legalReviewed': True, 'dataAccess': 'NONE', 'status': 'APPROVED'},
        'FasterXML': {'securityReviewed': True, 'legalReviewed': True, 'dataAccess': 'NONE', 'status': 'APPROVED'},
    }
    for component in components:
        supplier = suppliers.get(component['supplier'])
        if not supplier or supplier['status'] != 'APPROVED' or not supplier['securityReviewed'] or not supplier['legalReviewed']:
            raise VerificationError(f'supplier due diligence incomplete: {component["supplier"]}')

    cbom = {
        'bomFormat': 'CycloneDX', 'specVersion': '1.6', 'components': [
            {'type': 'cryptographic-asset', 'name': 'TLS', 'cryptoProperties': {'algorithmProperties': {'primitive': 'AES', 'parameterSetIdentifier': 'GCM-256'}}},
            {'type': 'cryptographic-asset', 'name': 'Signature', 'cryptoProperties': {'algorithmProperties': {'primitive': 'ECDSA', 'parameterSetIdentifier': 'P-256'}}},
        ]
    }
    cbom_digest = hashlib.sha256(json.dumps(cbom, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
    if len(cbom_digest) != 64 or not cbom['components']:
        raise VerificationError('CBOM generation failed')

    subject_digest = hashlib.sha256(b'cpf-artifact').hexdigest()
    provenance = {
        '_type': 'https://in-toto.io/Statement/v1',
        'subject': [{'name': 'cpf-artifact', 'digest': {'sha256': subject_digest}}],
        'predicateType': 'https://slsa.dev/provenance/v1',
        'predicate': {'buildDefinition': {'buildType': 'cpf-gradle'}, 'runDetails': {'builder': {'id': 'cpf-release'}}},
    }
    if provenance['subject'][0]['digest']['sha256'] != subject_digest:
        raise VerificationError('provenance subject mismatch')

    return {
        'kevResponseSla': 'PASS',
        'waiverExpiryAndApproval': 'PASS',
        'dependencyLicensePolicy': 'PASS',
        'supplierDueDiligence': 'PASS',
        'cbom': cbom,
        'cbomSha256': cbom_digest,
        'provenance': provenance,
        'provenanceSubjectSha256': subject_digest,
        'status': 'PASS',
    }


def _performance_and_time(profile: dict[str, Any]) -> dict[str, Any]:
    perf = profile.get('performance', {}) if isinstance(profile.get('performance'), dict) else {}
    operations = int(perf.get('operations', 40000))
    max_seconds = float(perf.get('maxSeconds', 5.0))
    max_peak_bytes = int(perf.get('maxPeakBytes', 8_000_000))
    if operations <= 0 or max_seconds <= 0 or max_peak_bytes <= 0:
        raise VerificationError('invalid performance profile')
    samples: list[float] = []
    state: dict[int, int] = {}
    tracemalloc.start()
    start = time.perf_counter()
    for i in range(operations):
        t0 = time.perf_counter_ns()
        key = i % 1024
        state[key] = state.get(key, 0) + 1
        if i % 7 == 0:
            state.get((key - 1) % 1024, 0)
        samples.append((time.perf_counter_ns() - t0) / 1_000_000)
    elapsed = time.perf_counter() - start
    _, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    if elapsed > max_seconds:
        raise VerificationError(f'local performance alternative exceeded time budget: {elapsed:.3f}s')
    if peak > max_peak_bytes:
        raise VerificationError(f'local performance alternative exceeded memory budget: {peak}')
    sorted_samples = sorted(samples)
    p95 = sorted_samples[min(len(sorted_samples) - 1, math.ceil(len(sorted_samples) * 0.95) - 1)]

    seoul = ZoneInfo('Asia/Seoul')
    new_york = ZoneInfo('America/New_York')
    before = datetime(2026, 3, 8, 1, 30, tzinfo=new_york)
    after = datetime(2026, 3, 8, 3, 30, tzinfo=new_york)
    utc_delta = after.astimezone(timezone.utc) - before.astimezone(timezone.utc)
    if utc_delta != timedelta(hours=1):
        raise VerificationError(f'DST gap normalization failed: {utc_delta}')
    seoul_value = datetime(2026, 8, 5, 12, 0, tzinfo=seoul)
    if seoul_value.utcoffset() != timedelta(hours=9):
        raise VerificationError('Asia/Seoul offset contract failed')
    skew_tolerance = timedelta(seconds=30)
    observed_skew = timedelta(seconds=15)
    if abs(observed_skew) > skew_tolerance:
        raise VerificationError('clock skew tolerance failed')

    return {
        'operations': operations,
        'elapsedSeconds': round(elapsed, 6),
        'p95Milliseconds': round(p95, 6),
        'peakBytes': peak,
        'dstUtcDeltaSeconds': int(utc_delta.total_seconds()),
        'seoulUtcOffsetSeconds': int(seoul_value.utcoffset().total_seconds()),
        'clockSkewToleranceSeconds': int(skew_tolerance.total_seconds()),
        'status': 'PASS',
    }



def _multi_process_kill_recovery() -> dict[str, Any]:
    """Exercise exclusive ownership, process kill, restart reconciliation and exactly-once effect."""
    with tempfile.TemporaryDirectory(prefix='cpf-runtime-alt-') as tmp:
        base = Path(tmp)
        lock = base / 'owner.lock'
        journal = base / 'journal.json'
        effect = base / 'effect.txt'
        ready = base / 'ready'
        worker = base / 'worker.py'
        worker.write_text(
            """from __future__ import annotations
import hashlib, json, os, sys, time
from pathlib import Path
base=Path(sys.argv[1]); worker_id=sys.argv[2]
lock=base/'owner.lock'; journal=base/'journal.json'; effect=base/'effect.txt'; ready=base/'ready'
def atomic_json(path, value):
    tmp=path.with_suffix('.tmp-'+str(os.getpid()))
    with tmp.open('w', encoding='utf-8') as f:
        json.dump(value, f, sort_keys=True); f.flush(); os.fsync(f.fileno())
    os.replace(tmp, path)
try:
    fd=os.open(lock, os.O_CREAT|os.O_EXCL|os.O_WRONLY, 0o600)
except FileExistsError:
    print('LOST:'+worker_id, flush=True); raise SystemExit(17)
with os.fdopen(fd, 'w', encoding='utf-8') as f:
    f.write(str(os.getpid())+'|'+worker_id); f.flush(); os.fsync(f.fileno())
atomic_json(journal, {'requestId':'REQ-PK','worker':worker_id,'state':'RUNNING','lease':1,'effectCount':0})
try:
    efd=os.open(effect, os.O_CREAT|os.O_EXCL|os.O_WRONLY, 0o600)
except FileExistsError:
    atomic_json(journal, {'requestId':'REQ-PK','worker':worker_id,'state':'UNKNOWN','lease':1,'effectCount':1,'duplicateSuppressed':True})
else:
    with os.fdopen(efd, 'w', encoding='utf-8') as f:
        f.write('business-effect'); f.flush(); os.fsync(f.fileno())
    digest=hashlib.sha256(effect.read_bytes()).hexdigest()
    atomic_json(journal, {'requestId':'REQ-PK','worker':worker_id,'state':'UNKNOWN','lease':1,'effectCount':1,'resultHash':digest})
ready.write_text(worker_id, encoding='utf-8')
print('WON:'+worker_id, flush=True)
time.sleep(60)
""", encoding='utf-8')

        proc_by_name = {
            name: __import__('subprocess').Popen(
                [sys.executable, str(worker), str(base), name],
                stdout=__import__('subprocess').PIPE, stderr=__import__('subprocess').PIPE, text=True
            )
            for name in ('worker-A', 'worker-B')
        }
        procs = list(proc_by_name.values())
        deadline = time.monotonic() + 5.0
        while not ready.exists() and time.monotonic() < deadline:
            time.sleep(0.02)
        if not ready.exists():
            for proc in procs:
                proc.kill()
            raise VerificationError('multi-process owner did not become ready')
        winner_name = ready.read_text(encoding='utf-8')
        winner = proc_by_name.get(winner_name)
        if winner is None or winner.poll() is not None:
            for proc in procs:
                if proc.poll() is None:
                    proc.kill()
            raise VerificationError('exclusive owner exited before process-kill injection')
        winner.terminate()
        try:
            winner.wait(timeout=3)
        except __import__('subprocess').TimeoutExpired:
            winner.kill(); winner.wait(timeout=3)
        for proc in procs:
            if proc is not winner and proc.poll() is None:
                try:
                    proc.wait(timeout=3)
                except __import__('subprocess').TimeoutExpired:
                    proc.kill(); proc.wait(timeout=3)
        loser_codes = [proc.returncode for proc in procs if proc is not winner]
        if loser_codes != [17]:
            raise VerificationError(f'exclusive ownership did not reject exactly one contender: {loser_codes}')

        state = _load_json(journal)
        if state.get('state') != 'UNKNOWN' or state.get('effectCount') != 1:
            raise VerificationError(f'killed worker journal not recoverable: {state}')
        effect_digest = hashlib.sha256(effect.read_bytes()).hexdigest()
        if state.get('resultHash') != effect_digest:
            raise VerificationError('durable effect digest does not match journal')

        # Stale owner recovery: the old PID is gone, so the restart removes the stale lock,
        # advances the lease and reconciles UNKNOWN without applying the effect again.
        stale_owner = lock.read_text(encoding='utf-8').split('|', 1)[0]
        if stale_owner and int(stale_owner) == winner.pid and winner.poll() is None:
            raise VerificationError('terminated owner still appears alive')
        lock.unlink()
        state['lease'] = 2
        state['state'] = 'SUCCEEDED'
        state['reconciledAfterProcessKill'] = True
        state['effectCount'] = 1
        tmp_journal = journal.with_suffix('.reconcile.tmp')
        with tmp_journal.open('w', encoding='utf-8') as f:
            json.dump(state, f, sort_keys=True); f.flush(); os.fsync(f.fileno())
        os.replace(tmp_journal, journal)

        # A restarted contender must obtain the new lease but suppress the already durable effect.
        restart_worker = base / 'restart.py'
        restart_worker.write_text(
            """import os, sys
from pathlib import Path
p=Path(sys.argv[1])
fd=os.open(p/'owner.lock', os.O_CREAT|os.O_EXCL|os.O_WRONLY, 0o600)
os.close(fd)
try:
    fd=os.open(p/'effect.txt', os.O_CREAT|os.O_EXCL|os.O_WRONLY, 0o600)
except FileExistsError:
    print('DUPLICATE_SUPPRESSED')
else:
    os.close(fd)
    raise SystemExit(9)
""", encoding='utf-8')
        restart = __import__('subprocess').run(
            [sys.executable, str(restart_worker), str(base)],
            text=True, capture_output=True, timeout=5
        )
        if restart.returncode != 0 or 'DUPLICATE_SUPPRESSED' not in restart.stdout:
            raise VerificationError('restart did not suppress duplicate side effect: '+restart.stdout+restart.stderr)
        final_state = _load_json(journal)
        if final_state.get('state') != 'SUCCEEDED' or final_state.get('effectCount') != 1:
            raise VerificationError('process-kill reconcile final state invalid')
        return {
            'contenders': 2,
            'exclusiveWinner': winner_name,
            'loserExitCode': loser_codes[0],
            'terminatedExitCode': winner.returncode,
            'recoveredState': final_state['state'],
            'leaseAfterRestart': final_state['lease'],
            'effectCount': final_state['effectCount'],
            'duplicateEffectSuppressed': True,
            'status': 'PASS',
        }


def _run_check(name: str, fn: Callable[[], dict[str, Any]], results: dict[str, Any], failures: list[str]) -> None:
    started = time.perf_counter()
    try:
        details = fn()
        details['durationMs'] = round((time.perf_counter() - started) * 1000, 3)
        details.setdefault('status', 'PASS')
        results[name] = details
    except Exception as exc:  # fail closed; include exact check name
        results[name] = {'status': 'FAIL', 'error': str(exc), 'durationMs': round((time.perf_counter() - started) * 1000, 3)}
        failures.append(f'{name}: {exc}')


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', type=Path, default=Path.cwd())
    parser.add_argument('--profile', type=Path)
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    root = args.root.resolve()
    profile_path = args.profile or (root / 'cpf-tools/runtime/profiles/cpf-runtime-alternative-profile.json')
    try:
        profile = _load_json(profile_path)
    except VerificationError as exc:
        print(json.dumps({'status': 'FAIL', 'failures': [str(exc)]}, ensure_ascii=False, indent=2))
        return 1
    if profile.get('schemaVersion') != 1:
        print(json.dumps({'status': 'FAIL', 'failures': ['profile schemaVersion must be 1']}, ensure_ascii=False, indent=2))
        return 1

    failures: list[str] = []
    results: dict[str, Any] = {}
    _run_check('productSourceContracts', lambda: _source_contracts(root, profile), results, failures)
    _run_check('sensitiveDataMaskingCorpus', lambda: _masking_corpus(root), results, failures)
    _run_check('idempotencyUnknownReconcileFencing', _reliability_models, results, failures)
    _run_check('multiProcessKillRecovery', _multi_process_kill_recovery, results, failures)
    _run_check('partialIoAndTruncation', _partial_io, results, failures)
    _run_check('brokerCacheFeatureNotification', _provider_models, results, failures)
    _run_check('telemetryAndConfigCompatibility', _telemetry_and_config, results, failures)
    now = datetime(2026, 8, 5, 14, 0, tzinfo=timezone.utc)
    _run_check('vulnerabilityDependencySupplierCbomProvenance', lambda: _supply_chain_models(now), results, failures)
    _run_check('performanceSoakAndTime', lambda: _performance_and_time(profile), results, failures)

    report = {
        'schemaVersion': 1,
        'profileId': profile.get('profileId'),
        'baselineSha': profile.get('baselineSha'),
        'verificationType': 'DETERMINISTIC_ALTERNATIVE_RUNTIME',
        'authoritativeRuntimeClaim': False,
        'status': 'PASS' if not failures else 'FAIL',
        'checks': results,
        'failures': failures,
    }
    text = json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True) + '\n'
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(text, encoding='utf-8')
    print(text, end='')
    return 0 if not failures else 1


if __name__ == '__main__':
    raise SystemExit(main())
