#!/usr/bin/env python3
from pathlib import Path
import re,sys,tempfile
root=Path(__file__).resolve().parents[2]
core=root/'cpf-core/src/main/java'
errors=[]
# Forbidden runtime technologies in Core source.
for f in core.rglob('*.java'):
    s=f.read_text(errors='ignore'); rel=f.relative_to(root).as_posix()
    for token,code in [
        ('io.opentelemetry','CORE_OTEL_RUNTIME'),('jakarta.servlet','CORE_SERVLET_RUNTIME'),
        ('org.springframework.security','CORE_SECURITY_RUNTIME'),('javax.sql','CORE_DB_RUNTIME'),
        ('java.sql.','CORE_DB_RUNTIME'),('JdbcTemplate','CORE_DB_RUNTIME'),('OpenFeature','CORE_FEATURE_PROVIDER')]:
        if token in s: errors.append((code,rel))
# Positive owner witnesses
witnesses={
 'WEB_SECURITY_FILTER': [
   root/'cpf-starters/security/session/jdbc/src/main/java/com/cpf/security/session/jdbc/CpfBffSessionBridgeFilter.java',
   root/'cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfResourceServerAutoConfiguration.java'],
 'PERSISTENCE_RUNTIME': [
   root/'cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/runtime/CpfRepositoryPolicyBeanPostProcessor.java',
   root/'cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/runtime/CpfRepositoryPolicyAutoConfiguration.java'],
 'OBSERVABILITY_CONTRACT': [root/'cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/CpfTelemetry.java'],
}
for kind,paths in witnesses.items():
    if not any(p.exists() for p in paths): errors.append(('OWNER_WITNESS_MISSING',kind))
# Feature provider and OTLP leaf are intentionally not required for PASS here: this gate proves Core ownership removal,
# while their physical provider portfolio is checked by the canonical verify_starter_catalog.py gate.
if errors:
    print(f'CPF_OWNER_BOUNDARY=FAIL errors={len(errors)}')
    for e in errors: print(','.join(map(str,e)))
    sys.exit(1)
print('CPF_OWNER_BOUNDARY=PASS coreOtel=0 coreFeatureProvider=0 coreServletFilter=0 corePersistentRuntime=0 ownerWitnesses=3')
