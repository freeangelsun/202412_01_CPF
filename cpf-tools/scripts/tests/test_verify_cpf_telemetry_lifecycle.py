from __future__ import annotations
import importlib.util, json, tempfile
from pathlib import Path
import pytest
SCRIPT=Path(__file__).parents[1]/'verify-cpf-telemetry-lifecycle.py'
spec=importlib.util.spec_from_file_location('telemetry_lifecycle',SCRIPT); module=importlib.util.module_from_spec(spec); spec.loader.exec_module(module)

def fixture():
    root=Path(tempfile.mkdtemp())
    files={
      'cpf-tools/observability/cpf-telemetry-profile.json': json.dumps({
        'schemaVersion':1,'semanticConventionVersion':'1.30.0','schemaUrl':'https://opentelemetry.io/schemas/1.30.0','signals':['traces','metrics','logs'],
        'resourceRequired':['service.name','service.version','deployment.environment.name','service.instance.id'],
        'sensitiveAttributeDenyPatterns':['password','secret','token','authorization','cookie','account'],
        'cardinality':{'maxSpanAttributes':128,'maxAttributeValueLength':4096},
        'export':{'overflowPolicy':'DROP_WITH_METRIC_AND_HEALTH_DEGRADED'},
        'compatibility':{'breakingChangeRequiresNewProfile':True,'mixedVersionWindowRequired':True}}),
      'cpf-starters/platform-operations/otlp/src/main/java/com/cpf/starter/platform/operations/otlp/CpfOtlpProperties.java':'schemaUrl serviceName deploymentEnvironment maxQueueSize maxExportBatchSize maxAttributes maxAttributeValueLength official OpenTelemetry schema-url is required',
      'cpf-starters/platform-operations/otlp/src/main/java/com/cpf/starter/platform/operations/otlp/CpfOtlpAutoConfiguration.java':'Resource.create( properties.getSchemaUrl() service.name deployment.environment.name .setMaxQueueSize( .setMaxExportBatchSize( .setScheduleDelay( .setExporterTimeout( SpanLimits.builder() .setMaxNumberOfAttributes( .setMaxAttributeValueLength(',
      'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java':'traceByTransactionId( traceByTraceId( traceByBusinessTransactionId( TRANSACTION_ID TRACE_ID SPAN_ID BUSINESS_TRANSACTION_ID relatedBatchExecutions'}
    for rel,text in files.items(): p=root/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(text,encoding='utf-8')
    return root

def test_valid(): module.verify(fixture())
def test_missing_sensitive_pattern_fails():
    r=fixture(); p=r/'cpf-tools/observability/cpf-telemetry-profile.json'; d=json.loads(p.read_text()); d['sensitiveAttributeDenyPatterns'].remove('token'); p.write_text(json.dumps(d));
    with pytest.raises(module.ContractError): module.verify(r)
def test_unbounded_queue_fails():
    r=fixture(); p=r/'cpf-starters/platform-operations/otlp/src/main/java/com/cpf/starter/platform/operations/otlp/CpfOtlpAutoConfiguration.java'; p.write_text(p.read_text().replace('.setMaxQueueSize(',''));
    with pytest.raises(module.ContractError): module.verify(r)
def test_schema_drift_fails():
    r=fixture(); p=r/'cpf-tools/observability/cpf-telemetry-profile.json'; d=json.loads(p.read_text()); d['schemaUrl']='https://opentelemetry.io/schemas/1.29.0'; p.write_text(json.dumps(d));
    with pytest.raises(module.ContractError): module.verify(r)

def test_missing_span_id_consumer_contract_fails():
    r=fixture()
    p=r/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java'
    p.write_text(p.read_text().replace('SPAN_ID',''), encoding='utf-8')
    with pytest.raises(module.ContractError):
        module.verify(r)

def test_missing_trace_lookup_consumer_contract_fails():
    r=fixture()
    p=r/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java'
    p.write_text(p.read_text().replace('traceByTraceId(',''), encoding='utf-8')
    with pytest.raises(module.ContractError):
        module.verify(r)
