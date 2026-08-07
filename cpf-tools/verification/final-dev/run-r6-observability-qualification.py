#!/usr/bin/env python3
from __future__ import annotations
import argparse, hashlib, json, os, re, sys, urllib.parse, urllib.request, uuid
from pathlib import Path
from typing import Any

SHA40 = re.compile(r'^[0-9a-f]{40}$')
STORES = ('metric', 'log', 'trace', 'alert', 'audit')
RECORD_KEYS = ('records', 'items', 'results', 'events')
class ObsError(RuntimeError): pass

def request_json(url: str, method='GET', body=None, token='') -> Any:
    u = urllib.parse.urlparse(url)
    if u.scheme not in {'http', 'https'} or not u.hostname:
        raise ObsError('observability URL must be http/https')
    if u.scheme != 'https' and u.hostname not in {'127.0.0.1', 'localhost', '::1'}:
        raise ObsError('non-local observability endpoint must use https')
    data = None if body is None else json.dumps(body, separators=(',', ':')).encode()
    headers = {'Accept': 'application/json'}
    if data is not None: headers['Content-Type'] = 'application/json'
    if token: headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=float(os.getenv('CPF_R6_OBSERVABILITY_TIMEOUT_SECONDS', '60'))) as r:
            raw = r.read(4 * 1024 * 1024); status = r.status
    except Exception as e:
        raise ObsError(f'{type(e).__name__}: {url}') from e
    if not 200 <= status < 300: raise ObsError(f'HTTP {status}: {url}')
    try: return json.loads(raw.decode())
    except Exception as e: raise ObsError(f'endpoint must return JSON: {url}') from e

def field(record: dict[str, Any], *names: str) -> Any:
    normalized = {re.sub(r'[^a-z0-9]', '', str(k).lower()): v for k, v in record.items()}
    for name in names:
        key = re.sub(r'[^a-z0-9]', '', name.lower())
        if key in normalized: return normalized[key]
    return None

def records_from(payload: Any, store: str) -> list[dict[str, Any]]:
    # PASS/observed booleans are deliberately ignored: only concrete records count.
    if isinstance(payload, list): source = payload
    elif isinstance(payload, dict):
        source = None
        for key in RECORD_KEYS:
            if isinstance(payload.get(key), list): source = payload[key]; break
        if source is None and isinstance(payload.get('data'), list): source = payload['data']
        if source is None: raise ObsError(f'{store} endpoint must return concrete records, not self-attested booleans')
    else:
        raise ObsError(f'{store} endpoint returned unsupported JSON shape')
    records = [item for item in source if isinstance(item, dict)]
    if not records: raise ObsError(f'{store} endpoint returned no concrete records')
    return records

def correlated(record: dict[str, Any], qualification_id: str, transaction_id: str, trace_id: str, source_sha: str) -> bool:
    q = str(field(record, 'qualificationId', 'qualification_id') or '')
    tx = str(field(record, 'transactionId', 'transaction_id') or '')
    tr = str(field(record, 'traceId', 'trace_id') or '')
    sha = str(field(record, 'sourceSha', 'source_sha', 'gitSha', 'revision') or '').lower()
    # A store record must carry the canonical transaction plus at least one independent qualification/trace key.
    if tx != transaction_id: return False
    if q not in {'', qualification_id} or tr not in {'', trace_id}: return False
    if not (q == qualification_id or tr == trace_id): return False
    if sha and sha != source_sha: return False
    return True

def validate_store(store: str, records: list[dict[str, Any]], qid: str, txid: str, traceid: str, head: str) -> dict[str, Any]:
    matched = [r for r in records if correlated(r, qid, txid, traceid, head)]
    if not matched: raise ObsError(f'{store} store has no independently correlated record')
    if store == 'metric':
        qualified = [r for r in matched if field(r, 'metricName', 'name') and isinstance(field(r, 'value', 'metricValue'), (int, float))]
        if not qualified: raise ObsError('metric store lacks named numeric sample')
    elif store == 'log':
        qualified = [r for r in matched if field(r, 'message', 'eventName', 'eventType') and field(r, 'level', 'severity')]
        if not qualified: raise ObsError('log store lacks event/severity record')
    elif store == 'trace':
        qualified = [r for r in matched if str(field(r, 'traceId') or '') == traceid and field(r, 'spanId', 'span_id')]
        if not qualified: raise ObsError('trace store lacks correlated span')
    elif store == 'alert':
        states = {str(field(r, 'state', 'status', 'alertState') or '').upper() for r in matched}
        fired = states & {'FIRING', 'FIRED', 'ACTIVE', 'OPEN', 'TRIGGERED'}
        resolved = states & {'RESOLVED', 'CLOSED', 'RECOVERED', 'CLEARED'}
        if not fired or not resolved: raise ObsError(f'alert store must prove fired+resolved lifecycle, states={sorted(states)}')
        qualified = matched
    elif store == 'audit':
        qualified = [r for r in matched if field(r, 'action', 'eventName', 'eventType') and field(r, 'outcome', 'result', 'status')]
        if not qualified: raise ObsError('audit store lacks action/outcome record')
    else: raise ObsError('unsupported store: ' + store)
    ids = []
    for r in qualified:
        eid = str(field(r, 'evidenceId', 'recordId', 'eventId', 'spanId', 'id') or '').strip()
        if eid: ids.append(hashlib.sha256(eid.encode()).hexdigest())
    if not ids: raise ObsError(f'{store} concrete record identifier missing')
    return {'recordCount': len(qualified), 'recordIdSha256': ids[:20]}

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--expected-head', required=True)
    ap.add_argument('--output-json', required=True, type=Path)
    a = ap.parse_args()
    head = a.expected_head.lower().strip()
    if not SHA40.fullmatch(head): raise ObsError('expected head must be a 40-char SHA')
    probe = os.getenv('CPF_R6_OBSERVABILITY_PROBE_URL', '').strip()
    token = os.getenv('CPF_R6_OBSERVABILITY_PROBE_TOKEN', '').strip()
    if not probe: raise ObsError('CPF_R6_OBSERVABILITY_PROBE_URL is required')
    rid = str(uuid.uuid4())
    proof = request_json(probe, 'POST', {'requestId': rid, 'sourceSha': head, 'scenario': 'known-traffic-and-failure'}, token)
    if not isinstance(proof, dict): raise ObsError('probe must return identifier object')
    if proof.get('sourceSha') not in {None, head}: raise ObsError('probe sourceSha mismatch')
    if proof.get('requestId') not in {None, rid}: raise ObsError('probe requestId mismatch')
    qualification_id = str(proof.get('qualificationId', '')).strip()
    transaction_id = str(proof.get('transactionId', '')).strip()
    trace_id = str(proof.get('traceId', '')).strip()
    if min(map(len, (qualification_id, transaction_id, trace_id))) < 8:
        raise ObsError('probe must return qualificationId/transactionId/traceId')
    evidence = {}
    for store in STORES:
        env = 'CPF_R6_OBSERVABILITY_' + store.upper() + '_QUERY_URL'
        base = os.getenv(env, '').strip()
        if not base: raise ObsError(env + ' is required')
        sep = '&' if '?' in base else '?'
        url = base + sep + urllib.parse.urlencode({'qualificationId': qualification_id, 'transactionId': transaction_id, 'traceId': trace_id, 'sourceSha': head})
        payload = request_json(url, 'GET', None, token)
        evidence[store] = validate_store(store, records_from(payload, store), qualification_id, transaction_id, trace_id, head)
    result = {
        'schemaVersion': 3, 'protocol': 'CPF-R6J-OBSERVABILITY-AUTHORITATIVE-RECORDS', 'sourceSha': head,
        'status': 'PASS', 'qualificationIdSha256': hashlib.sha256(qualification_id.encode()).hexdigest(),
        'transactionIdSha256': hashlib.sha256(transaction_id.encode()).hexdigest(),
        'traceIdSha256': hashlib.sha256(trace_id.encode()).hexdigest(), 'stores': evidence
    }
    a.output_json.parent.mkdir(parents=True, exist_ok=True)
    a.output_json.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(f'[CPF][R6J][OBS][PASS] sourceSha={head} stores={len(STORES)}'); return 0

if __name__ == '__main__':
    try: raise SystemExit(main())
    except ObsError as e:
        print(f'[CPF][R6J][OBS][FAIL] {e}', file=sys.stderr); raise SystemExit(1)
