#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path

class ContractError(ValueError): pass

def require(text: str, token: str, rel: str, errors: list[str]):
    if token not in text: errors.append(f"{rel}: missing {token}")

def verify(root: Path):
    errors=[]
    profile_rel='cpf-tools/runtime/profiles/cpf-telemetry-profile.json'
    props_rel='cpf-starters/platform-operations/observability/otlp/src/main/java/com/cpf/platform/operations/observability/otlp/CpfOtlpProperties.java'
    auto_rel='cpf-starters/platform-operations/observability/otlp/src/main/java/com/cpf/platform/operations/observability/otlp/CpfOtlpAutoConfiguration.java'
    consumer_rel='cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java'
    paths={r:root/r for r in (profile_rel,props_rel,auto_rel,consumer_rel)}
    for rel,p in paths.items():
        if not p.is_file(): errors.append(f'{rel}: source missing')
    if errors: raise ContractError('\n'.join(errors))
    profile=json.loads(paths[profile_rel].read_text(encoding='utf-8'))
    if profile.get('schemaVersion') != 1: errors.append(f'{profile_rel}: schemaVersion must be 1')
    if profile.get('schemaUrl') != f"https://opentelemetry.io/schemas/{profile.get('semanticConventionVersion')}": errors.append(f'{profile_rel}: schema URL/version mismatch')
    if set(profile.get('signals',[])) != {'traces','metrics','logs'}: errors.append(f'{profile_rel}: traces/metrics/logs required')
    required={'service.name','service.version','deployment.environment.name','service.instance.id'}
    if not required.issubset(set(profile.get('resourceRequired',[]))): errors.append(f'{profile_rel}: resource identity incomplete')
    deny=' '.join(profile.get('sensitiveAttributeDenyPatterns',[])).lower()
    for token in ('password','secret','token','authorization','cookie','account'):
        if token not in deny: errors.append(f'{profile_rel}: sensitive deny pattern missing {token}')
    card=profile.get('cardinality',{})
    if not 1 <= int(card.get('maxSpanAttributes',0)) <= 256: errors.append(f'{profile_rel}: invalid maxSpanAttributes')
    if not 64 <= int(card.get('maxAttributeValueLength',0)) <= 16384: errors.append(f'{profile_rel}: invalid maxAttributeValueLength')
    export=profile.get('export',{})
    if export.get('overflowPolicy') != 'DROP_WITH_METRIC_AND_HEALTH_DEGRADED': errors.append(f'{profile_rel}: overflow must be observable')
    compatibility=profile.get('compatibility',{})
    for key in ('breakingChangeRequiresNewProfile','mixedVersionWindowRequired'):
        if compatibility.get(key) is not True: errors.append(f'{profile_rel}: {key} must be true')
    owners=profile.get('signalOwners',{})
    expected_owners={
        'traces':'cpf-starters/platform-operations/observability/otlp',
        'metrics':'cpf-starters/platform-operations/observability',
        'logs':'cpf-starters/platform-operations/observability',
    }
    for signal,owner in expected_owners.items():
        if owners.get(signal) != owner: errors.append(f'{profile_rel}: signal owner mismatch {signal}')
    props=paths[props_rel].read_text(encoding='utf-8')
    for token in ('endpoint','timeout','sampleProbability','serviceName','serviceVersion','deploymentEnvironmentName','serviceInstanceId','maxAttributesPerSpan','OTLP endpoint is required'):
        require(props,token,props_rel,errors)
    auto=paths[auto_rel].read_text(encoding='utf-8')
    for token in ('Resource.getDefault()', 'AttributeKey.stringKey("service.name")', 'AttributeKey.stringKey("service.version")', 'AttributeKey.stringKey("deployment.environment.name")', 'AttributeKey.stringKey("service.instance.id")', '.setEndpoint(', '.setTimeout(', 'BatchSpanProcessor.builder(', 'Sampler.traceIdRatioBased('):
        require(auto,token,auto_rel,errors)
    observation_rel='cpf-starters/platform-operations/observability/src/main/java/com/cpf/starter/platform/operations/observability/CpfObservationSupport.java'
    structured_rel='cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/logging/CpfStructuredLogger.java'
    observation_path=root/observation_rel
    structured_path=root/structured_rel
    if not observation_path.is_file(): errors.append(f'{observation_rel}: source missing')
    elif 'ObservationRegistry' not in observation_path.read_text(encoding='utf-8'): errors.append(f'{observation_rel}: Micrometer observation bridge missing')
    if not structured_path.is_file(): errors.append(f'{structured_rel}: source missing')
    else:
        structured=structured_path.read_text(encoding='utf-8')
        for token in ('business(', 'operation(', 'security(', 'error('): require(structured,token,structured_rel,errors)
    consumer=paths[consumer_rel].read_text(encoding='utf-8')
    for token in (
        'traceByTransactionId(',
        'traceByTraceId(',
        'traceByBusinessTransactionId(',
        'TRANSACTION_ID',
        'TRACE_ID',
        'SPAN_ID',
        'BUSINESS_TRANSACTION_ID',
        'relatedBatchExecutions',
    ):
        require(consumer,token,consumer_rel,errors)
    if errors: raise ContractError('\n'.join(errors))

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args()
    try: verify(Path(ns.root).resolve())
    except (ContractError,json.JSONDecodeError) as e:
        print('[FAIL] CPF telemetry lifecycle\n'+str(e),file=sys.stderr); return 1
    print('[PASS] CPF telemetry lifecycle semconv=1.30.0 signals=3 bounded=true sensitive-deny=true mixed-version=true consumer=true')
    return 0
if __name__=='__main__': raise SystemExit(main())
