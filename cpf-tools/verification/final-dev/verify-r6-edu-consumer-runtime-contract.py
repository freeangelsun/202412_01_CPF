#!/usr/bin/env python3
from __future__ import annotations
import argparse, copy, json, re, sys
from collections import Counter
from pathlib import Path

EXPECTED_TYPES = {
    'JDBC_COMMAND': 51,
    'SPRING_BATCH': 30,
    'PROCESS': 17,
    'REFERENCE_GATEWAY': 14,
    'JDBC_QUERY': 6,
    'OUTBOX': 6,
    'FILE': 6,
    'HTTP': 5,
}

FILES = {
    'enum': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/EduConsumerType.java',
    'registry': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/EduBusinessConsumerRegistry.java',
    'runtime_config': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/configuration/EduRuntimeConfiguration.java',
    'execution_service': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/application/EduExecutionService.java',
    'controller': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/api/EduCapabilityController.java',
    'jdbc_command': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/jdbc/JdbcEduBusinessConsumer.java',
    'jdbc_query': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/jdbc/JdbcQueryEduBusinessConsumer.java',
    'file': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/file/FileEduBusinessConsumer.java',
    'http': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/http/HttpEduBusinessConsumer.java',
    'outbox': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/outbox/OutboxEduBusinessConsumer.java',
    'process': 'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/process/ProcessEduBusinessConsumer.java',
    'batch_consumer': 'cpf-reference/src/main/java/com/cpf/reference/batch/operation/SpringBatchEduBusinessConsumer.java',
    'batch_config': 'cpf-reference/src/main/java/com/cpf/reference/batch/config/ReferenceBatchRuntimeConfiguration.java',
    'gateway_consumer': 'cpf-reference/src/main/java/com/cpf/reference/optional/gateway/runtime/ReferenceGatewayBusinessConsumer.java',
    'gateway_config': 'cpf-reference/src/main/java/com/cpf/reference/optional/gateway/config/ReferenceGatewayRuntimeConfiguration.java',
    'process_script': 'cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1',
}

class ContractError(RuntimeError):
    pass

def require(ok: bool, message: str) -> None:
    if not ok:
        raise ContractError(message)

def load_text(root: Path, key: str, overrides: dict[str, str] | None = None) -> str:
    rel = FILES[key]
    if overrides and rel in overrides:
        return overrides[rel]
    p = root / rel
    require(p.is_file(), f'missing runtime source: {rel}')
    return p.read_text(encoding='utf-8')

def validate_catalog(features: list[dict]) -> None:
    require(len(features) == 135, f'expected 135 EDU requirements, found {len(features)}')
    ids = [f.get('requirementId') for f in features]
    require(len(set(ids)) == 135 and all(ids), 'requirementId must be unique/nonblank for all 135 features')
    counts = Counter((f.get('consumerBinding') or {}).get('type') for f in features)
    require(counts == Counter(EXPECTED_TYPES), f'consumer type distribution drift: {dict(counts)}')

    for f in features:
        rid = f['requirementId']
        b = f.get('consumerBinding') or {}
        typ = b.get('type')
        require(b.get('ownerModule') == 'cpf-reference', f'{rid}: ownerModule must be cpf-reference')
        require(b.get('runtimeCommand') == f'POST /api/reference/edu-capabilities/{rid}/executions', f'{rid}: runtimeCommand drift')
        require(isinstance(b.get('timeoutSeconds'), int) and b['timeoutSeconds'] > 0, f'{rid}: positive timeoutSeconds required')
        args = b.get('argumentFields')
        require(isinstance(args, list) and len(args) > 0 and len(args) == len(set(args)), f'{rid}: concrete unique argumentFields required')
        require(set(args).issubset(set(f.get('requiredFields') or [])), f'{rid}: consumer argumentFields must be requiredFields subset')
        require(str(b.get('operation') or '').strip(), f'{rid}: operation required')
        require(str(b.get('entryPoint') or '').strip(), f'{rid}: entryPoint required')
        require(str(b.get('publicContract') or '').strip(), f'{rid}: publicContract required')

        if typ == 'PROCESS':
            require(b['entryPoint'] == FILES['process_script'], f'{rid}: PROCESS entryPoint must be allowlisted EDU script')
            require(b.get('configurationKey') == 'cpf.repository-root', f'{rid}: PROCESS repository-root config required')
            require(b['timeoutSeconds'] == 600, f'{rid}: PROCESS timeout must be 600s')
        elif typ in {'JDBC_COMMAND', 'JDBC_QUERY'}:
            require(b['entryPoint'] == 'CPF_EDU_BUSINESS_RECORD', f'{rid}: JDBC must use CPF_EDU_BUSINESS_RECORD')
            require('REF DB' in b['publicContract'], f'{rid}: JDBC public contract drift')
            require(b['timeoutSeconds'] == 60, f'{rid}: JDBC timeout must be 60s')
        elif typ == 'HTTP':
            require(re.fullmatch(r'/external/\d{2}', b['entryPoint']) is not None, f'{rid}: HTTP entryPoint must be /external/NN')
            require(b.get('configurationKey') == 'cpf.edu.counterparty.base-url', f'{rid}: HTTP base URL property required')
            require('HTTP contract' in b['publicContract'], f'{rid}: HTTP public contract drift')
            require(b['timeoutSeconds'] == 60, f'{rid}: HTTP timeout must be 60s')
        elif typ == 'OUTBOX':
            require(b['entryPoint'] == 'CPF_EDU_OUTBOX', f'{rid}: OUTBOX must use durable CPF_EDU_OUTBOX')
            require('outbox/inbox' in b['publicContract'], f'{rid}: durable OUTBOX contract required')
            require(b['timeoutSeconds'] == 60, f'{rid}: OUTBOX timeout must be 60s')
        elif typ == 'FILE':
            require(b['entryPoint'] == 'cpf-reference EDU file store', f'{rid}: FILE entryPoint drift')
            require(b.get('configurationKey') == 'cpf.edu.business-file-root', f'{rid}: FILE root config required')
            require('checksum' in b['publicContract'].lower(), f'{rid}: FILE checksum contract required')
            require(b['timeoutSeconds'] == 120, f'{rid}: FILE timeout must be 120s')
        elif typ == 'SPRING_BATCH':
            m = re.fullmatch(r'EDU-BAT-(\d{2})', rid)
            require(m is not None, f'{rid}: SPRING_BATCH only valid for EDU-BAT')
            require(b['entryPoint'] == f"eduBat{m.group(1)}Job", f'{rid}: Spring Batch job entryPoint drift')
            require(b.get('configurationKey') == 'cpf.reference.features.batch.enabled', f'{rid}: batch feature config required')
            require('Spring Batch Job/Step' in b['publicContract'], f'{rid}: Spring Batch public contract required')
            require(b['timeoutSeconds'] == 3600, f'{rid}: Spring Batch timeout must be 3600s')
        elif typ == 'REFERENCE_GATEWAY':
            require(rid.startswith('EDU-GW-'), f'{rid}: REFERENCE_GATEWAY only valid for EDU-GW')
            require(b['entryPoint'] == 'CPF_EDU_BUSINESS_RECORD', f'{rid}: gateway simulator must persist via REF DB')
            require(b.get('configurationKey') == 'cpf.reference.features.gateway.enabled', f'{rid}: gateway feature config required')
            require('Gateway simulator' in b['publicContract'], f'{rid}: gateway simulator contract required')
            require(b['timeoutSeconds'] == 60, f'{rid}: gateway timeout must be 60s')
        else:
            raise ContractError(f'{rid}: unsupported consumer type {typ!r}')

def validate_sources(root: Path, overrides: dict[str, str] | None = None) -> None:
    enum = load_text(root, 'enum', overrides)
    for typ in EXPECTED_TYPES:
        require(re.search(rf'\b{re.escape(typ)}\b', enum) is not None, f'consumer enum missing {typ}')

    source_type_tokens = {
        'jdbc_command': 'EduConsumerType.JDBC_COMMAND',
        'jdbc_query': 'EduConsumerType.JDBC_QUERY',
        'file': 'EduConsumerType.FILE',
        'http': 'EduConsumerType.HTTP',
        'outbox': 'EduConsumerType.OUTBOX',
        'process': 'EduConsumerType.PROCESS',
        'batch_consumer': 'EduConsumerType.SPRING_BATCH',
        'gateway_consumer': 'EduConsumerType.REFERENCE_GATEWAY',
    }
    for key, token in source_type_tokens.items():
        text = load_text(root, key, overrides)
        require('implements EduBusinessConsumer' in text, f'{FILES[key]} must be concrete EduBusinessConsumer')
        require(token in text, f'{FILES[key]} must bind exact type {token}')
        require('EduBusinessConsumerResult' in text, f'{FILES[key]} must return concrete consumer result')

    registry = load_text(root, 'registry', overrides)
    for token in ['new EnumMap<>(EduConsumerType.class)', 'value.type()', 'Duplicate EDU consumer', 'consumers.get(binding.type())', 'No concrete product consumer registered', 'c.invoke(binding,command,fencingToken)']:
        require(token in registry.replace(' ', '' ) if token == 'c.invoke(binding,command,fencingToken)' else token in registry, f'registry missing fail-closed token: {token}')

    runtime = load_text(root, 'runtime_config', overrides)
    for bean in ['JdbcEduBusinessConsumer', 'JdbcQueryEduBusinessConsumer', 'FileEduBusinessConsumer', 'HttpEduBusinessConsumer', 'ProcessEduBusinessConsumer', 'OutboxEduBusinessConsumer']:
        require(bean in runtime, f'mandatory runtime configuration missing {bean}')
    require('List<EduBusinessConsumer> consumers' in runtime and 'new EduBusinessConsumerRegistry(consumers)' in runtime, 'runtime registry must consume concrete Spring beans')

    batch_cfg = load_text(root, 'batch_config', overrides)
    require(re.search(r'@Bean\s+SpringBatchEduBusinessConsumer\s+\w+\s*\(', batch_cfg) is not None, 'batch runtime must register SpringBatchEduBusinessConsumer bean')
    require('cpf.reference.features.batch.enabled' in batch_cfg, 'batch consumer must be feature-bound')
    gateway_cfg = load_text(root, 'gateway_config', overrides)
    require(re.search(r'@Bean\s+ReferenceGatewayBusinessConsumer\s+\w+\s*\(', gateway_cfg) is not None, 'gateway runtime must register ReferenceGatewayBusinessConsumer bean')
    require('cpf.reference.features.gateway.enabled' in gateway_cfg, 'gateway consumer must be feature-bound')

    service = load_text(root, 'execution_service', overrides)
    for token in ['EduBusinessConsumerRegistry consumers', 'handler.consumerBinding()', 'consumers.invoke(binding, command, fence)', 'binding.type().name()', 'consumerResult.code()', 'consumerResult.data()']:
        require(token in service, f'ExecutionService missing real consumer path token: {token}')
    require('binding.type() == EduConsumerType.HTTP' in service and 'binding.type() == EduConsumerType.OUTBOX' in service, 'ExecutionService must model external/unknown consumer effects')

    controller = load_text(root, 'controller', overrides)
    require('/api/reference/edu-capabilities' in controller and '@RequestMapping' in controller, 'EDU canonical REST base route drift')
    require('@PostMapping("/{requirementId}/executions")' in controller and 'service.execute(requirementId,c)' in controller, 'runtimeCommand must reach EduExecutionService')
    for operation in ['retry(', 'reconcile(', 'compensate(', 'cancel(']:
        require(operation in controller, f'EDU operational recovery endpoint missing {operation}')

    process_script = load_text(root, 'process_script', overrides)
    require('CPF_EDU_' in process_script, 'PROCESS allowlisted script must consume EDU execution environment')

def validate(root: Path, catalog: dict, overrides: dict[str, str] | None = None) -> None:
    features = catalog.get('features') if isinstance(catalog, dict) else None
    require(isinstance(features, list), 'manual-135-catalog.json features array required')
    validate_catalog(features)
    validate_sources(root, overrides)

def run_mutations(root: Path, catalog: dict) -> int:
    mutations: list[tuple[str, dict, dict[str,str] | None]] = []

    def cat_mut(name, fn):
        c=copy.deepcopy(catalog); fn(c['features']); mutations.append((name,c,None))
    cat_mut('unknown-consumer-type', lambda fs: fs[0]['consumerBinding'].__setitem__('type','MOCK'))
    cat_mut('runtime-command-id-drift', lambda fs: fs[0]['consumerBinding'].__setitem__('runtimeCommand','POST /api/reference/edu-capabilities/EDU-DEV-99/executions'))
    cat_mut('batch-entrypoint-drift', lambda fs: next(f for f in fs if f['requirementId']=='EDU-BAT-01')['consumerBinding'].__setitem__('entryPoint','eduBat02Job'))
    cat_mut('http-config-missing', lambda fs: next(f for f in fs if f['consumerBinding']['type']=='HTTP')['consumerBinding'].__setitem__('configurationKey',''))
    cat_mut('gateway-owner-drift', lambda fs: next(f for f in fs if f['consumerBinding']['type']=='REFERENCE_GATEWAY')['consumerBinding'].__setitem__('ownerModule','cpf-core'))

    service_rel=FILES['execution_service']; service=(root/service_rel).read_text(encoding='utf-8')
    mutations.append(('consumer-invoke-removed', catalog, {service_rel: service.replace('consumers.invoke(binding, command, fence)','/* mutation */ null')}))
    batch_rel=FILES['batch_config']; batch=(root/batch_rel).read_text(encoding='utf-8')
    mutations.append(('batch-bean-removed', catalog, {batch_rel: batch.replace('@Bean SpringBatchEduBusinessConsumer','SpringBatchEduBusinessConsumer')}))
    gateway_rel=FILES['gateway_config']; gateway=(root/gateway_rel).read_text(encoding='utf-8')
    mutations.append(('gateway-bean-removed', catalog, {gateway_rel: gateway.replace('@Bean ReferenceGatewayBusinessConsumer','ReferenceGatewayBusinessConsumer')}))

    passed=0
    for name, c, overrides in mutations:
        try:
            validate(root,c,overrides)
        except ContractError:
            passed+=1
            continue
        raise ContractError(f'mutation false-green: {name}')
    return passed

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    ap.add_argument('--catalog', default='cpf-reference/src/main/resources/edu/manual-135-catalog.json')
    ap.add_argument('--self-test', action='store_true')
    ns=ap.parse_args()
    root=Path(ns.root).resolve()
    catalog_path=(root/ns.catalog) if not Path(ns.catalog).is_absolute() else Path(ns.catalog)
    require(catalog_path.is_file(), f'missing catalog: {catalog_path}')
    catalog=json.loads(catalog_path.read_text(encoding='utf-8'))
    validate(root,catalog)
    mutations=run_mutations(root,catalog) if ns.self_test else 0
    print(f'[CPF][R6I][EDU-CONSUMER][PASS] features=135 types=8 mutations={mutations}')
    return 0

if __name__ == '__main__':
    try:
        raise SystemExit(main())
    except ContractError as e:
        print(f'[CPF][R6I][EDU-CONSUMER][FAIL] {e}', file=sys.stderr)
        raise SystemExit(1)
