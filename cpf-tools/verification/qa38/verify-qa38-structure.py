from __future__ import annotations
from pathlib import Path
import csv, hashlib, json, re, sys

R = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parents[3]
errors: list[str] = []
warnings: list[str] = []
protected = (
    'cpf-docs/deliverables/',
    'cpf-docs/guides/',
    'cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/',
)

# File hygiene and protected paths.
files = [p for p in R.rglob('*') if p.is_file()]
for p in files:
    rel = p.relative_to(R).as_posix()
    if any(rel.startswith(prefix) for prefix in protected):
        errors.append(f'protected path included: {rel}')
    if rel.endswith(('.log', '.tmp', '.bak', '.orig', '.rej', '.pyc')):
        errors.append(f'generated/temporary file included: {rel}')
    if p.stat().st_size == 0:
        errors.append(f'empty file: {rel}')

# JSON/CSV syntax.
for p in R.rglob('*.json'):
    try:
        json.loads(p.read_text(encoding='utf-8-sig'))
    except Exception as exc:
        errors.append(f'json invalid: {p.relative_to(R)}: {exc}')
for p in R.rglob('*.csv'):
    try:
        with p.open(encoding='utf-8-sig', newline='') as fh:
            list(csv.reader(fh))
    except Exception as exc:
        errors.append(f'csv invalid: {p.relative_to(R)}: {exc}')

# Settings/project graph.
settings = (R / 'settings.gradle').read_text(encoding='utf-8')
settings_no_comments = '\n'.join(line for line in settings.splitlines() if not line.lstrip().startswith('//'))
includes: list[str] = []
for match in re.finditer(r"^\s*include\s+([^\n]+)", settings_no_comments, re.M):
    includes.extend(value.lstrip(':') for value in re.findall(r"'([^']+)'", match.group(1)))
include_set = set(includes)
if len(includes) != len(include_set):
    errors.append('duplicate settings include: ' + ', '.join(sorted(x for x in include_set if includes.count(x) > 1)))
mappings = {
    name.lstrip(':'): path
    for name, path in re.findall(r"project\('(:[^']+)'\)\.projectDir\s*=\s*file\('([^']+)'\)", settings_no_comments)
}
project_refs: list[tuple[str, str]] = []
for p in R.rglob('build.gradle'):
    text = p.read_text(encoding='utf-8', errors='replace')
    for ref in re.findall(r"project\('(:[^']+)'\)", text):
        project_refs.append((p.relative_to(R).as_posix(), ref.lstrip(':')))
for source, ref in project_refs:
    if ref not in include_set:
        errors.append(f'project reference not included: {source} -> {ref}')

# Required QA38 project mappings and physical dirs.
required_projects = {
    'cpf-starter-base': 'cpf-starters/base',
    'cpf-starter-persistence-jdbc': 'cpf-starters/persistence-jdbc',
    'cpf-starter-persistence-mybatis': 'cpf-starters/persistence-mybatis',
    'cpf-starter-aop-service-access': 'cpf-starters/aop-service-access',
    'cpf-starter-openapi-webmvc': 'cpf-starters/openapi-webmvc',
    'cpf-starter-security-resource-server': 'cpf-starters/security-resource-server',
    'cpf-starter-security-service-identity': 'cpf-starters/security-service-identity',
    'cpf-starter-messaging-reliability-jdbc': 'cpf-starters/messaging-reliability-jdbc',
    'cpf-starter-messaging-rabbitmq': 'cpf-starters/messaging-rabbitmq',
    'cpf-starter-messaging-jms': 'cpf-starters/messaging-jms',
    'cpf-starter-messaging-ibm-mq': 'cpf-starters/messaging-ibm-mq',
    'cpf-starter-integration-tcp': 'cpf-starters/integration-tcp',
    'cpf-starter-integration-iso8583': 'cpf-starters/integration-iso8583',
    'cpf-starter-integration-sftp': 'cpf-starters/integration-sftp',
    'cpf-starter-notification': 'cpf-starters/notification',
    'cpf-starter-notification-email': 'cpf-starters/notification-email',
    'cpf-notification-sms-spi': 'cpf-starters/notification-sms-spi',
    'cpf-starter-scheduler-quartz': 'cpf-starters/scheduler-quartz',
}
for project, path in required_projects.items():
    if project not in include_set:
        errors.append(f'required project not included: {project}')
    if mappings.get(project) != path:
        errors.append(f'required mapping mismatch: {project} -> {mappings.get(project)!r}, expected {path}')
    if not (R / path / 'build.gradle').is_file():
        errors.append(f'required module build missing: {path}')

# Starter substance and aggregate constraints.
legacy_with_baseline_source = {'cache', 'security', 'messaging-kafka', 'observability', 'resilience', 'featureflag', 'secret'}
aggregate_dirs = {'cache-aggregate', 'security-aggregate'}
for d in sorted(p for p in (R / 'cpf-starters').iterdir() if p.is_dir()):
    if d.name == 'profiles':
        continue
    java = list(d.rglob('src/main/java/**/*.java'))
    resources = list(d.rglob('src/main/resources/**/*'))
    if d.name in aggregate_dirs:
        if java or resources:
            errors.append(f'aggregate starter contains source/resources: {d.relative_to(R)}')
    elif d.name not in legacy_with_baseline_source and not java:
        errors.append(f'leaf starter has no implementation source: {d.relative_to(R)}')

# Profile aggregate constraints.
profile_root = R / 'cpf-starters/profiles'
for d in profile_root.iterdir():
    if not d.is_dir():
        continue
    if (d / 'src').exists():
        errors.append(f'capability profile contains source: {d.relative_to(R)}')
    build = d / 'build.gradle'
    if not build.is_file() or "id 'java-library'" not in build.read_text(encoding='utf-8'):
        errors.append(f'capability profile build invalid: {d.relative_to(R)}')

# Duplicate FQCN and package declaration.
fqcn_to_path: dict[str, str] = {}
for p in R.rglob('*.java'):
    text = p.read_text(encoding='utf-8', errors='replace')
    package_match = re.search(r'^\s*package\s+([\w.]+)\s*;', text, re.M)
    type_match = re.search(r'\b(?:public\s+)?(?:final\s+|abstract\s+)?(?:class|interface|record|enum)\s+(\w+)', text)
    if not package_match or not type_match:
        errors.append(f'java package/type missing: {p.relative_to(R)}')
        continue
    fqcn = package_match.group(1) + '.' + type_match.group(1)
    previous = fqcn_to_path.setdefault(fqcn, p.relative_to(R).as_posix())
    if previous != p.relative_to(R).as_posix():
        errors.append(f'duplicate FQCN: {fqcn}: {previous}, {p.relative_to(R)}')

# Simple duplicate declaration guard caused by non-idempotent generation.
for p in R.rglob('src/main/java/**/*.java'):
    text = p.read_text(encoding='utf-8', errors='replace')
    fields = re.findall(r'\b(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?[\w<>?,.\[\]]+\s+(\w+)\s*(?:=[^;]*)?;', text)
    duplicates = sorted(name for name in set(fields) if fields.count(name) > 1)
    # Inner helper classes can legitimately repeat these names.
    if duplicates and p.name not in {'CpfSftpClient.java'}:
        errors.append(f'duplicate field declaration: {p.relative_to(R)}: {duplicates}')

# AutoConfiguration imports targets. Existing exact-SHA baseline classes are allowlisted only after GitHub inspection.
baseline_auto_configurations = {
    'com.cpf.starter.kafka.CpfKafkaAutoConfiguration',
    'com.cpf.core.config.CpfExceptionAutoConfiguration',
    'com.cpf.core.config.CpfTransactionMetaAutoConfiguration',
    'com.cpf.core.config.CpfPublicBoundaryAutoConfiguration',
    'com.cpf.core.config.CpfSagaAutoConfiguration',
}
for imports in R.rglob('org.springframework.boot.autoconfigure.AutoConfiguration.imports'):
    for line in imports.read_text(encoding='utf-8').splitlines():
        fqcn = line.strip()
        if not fqcn:
            continue
        if fqcn not in fqcn_to_path and fqcn not in baseline_auto_configurations:
            errors.append(f'AutoConfiguration target missing: {imports.relative_to(R)} -> {fqcn}')

# Core runtime extraction.
core_build = (R / 'cpf-core/build.gradle').read_text(encoding='utf-8')
for forbidden in (
    'mybatis-spring-boot-starter',
    'spring-boot-starter-aspectj',
    'springdoc-openapi-starter',
    'opentelemetry-sdk',
    'opentelemetry-exporter-otlp',
    'commons-compress',
):
    for scope in ('implementation', 'api', 'runtimeOnly'):
        if re.search(rf"\b{scope}\s+['\"][^'\"]*{re.escape(forbidden)}", core_build):
            errors.append(f'cpf-core runtime dependency remains: {scope} {forbidden}')
core_imports = (R / 'cpf-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports').read_text(encoding='utf-8')
for moved in (
    'CpfDataSourceConfig', 'CpfMyBatisConfig', 'CpfAopConfig', 'CpfOpenApiAutoConfiguration',
    'CpfSecurityAutoConfiguration', 'CpfRuntimeControlAutoConfiguration', 'CpfServiceCallAutoConfiguration',
    'CpfAttachmentAutoConfiguration', 'CpfLogPolicyAutoConfiguration',
):
    if moved in core_imports:
        errors.append(f'cpf-core auto-configuration still imports moved runtime: {moved}')

# cpf-common must retain business-common contracts only, not provider runtimes.
common_build = (R / 'cpf-common/build.gradle').read_text(encoding='utf-8')
for forbidden in ('mybatis-spring-boot-starter', 'spring-data-redis', 'lettuce-core', 'caffeine', 'poi-ooxml', 'hibernate-validator'):
    pattern = r"(?m)^\s*(?:api|implementation|runtimeOnly)\s+['\"][^'\"]*" + re.escape(forbidden)
    if re.search(pattern, common_build):
        errors.append(f'cpf-common technical runtime dependency remains: {forbidden}')

# Capability profile catalog.
profile_catalog_path = R / 'cpf-tools/generator/contracts/capability-profiles.json'
profile_catalog = json.loads(profile_catalog_path.read_text(encoding='utf-8'))
profiles = profile_catalog.get('profiles', [])
if len(profiles) < 10:
    errors.append(f'capability profiles incomplete: {len(profiles)}')
artifact_ids = set()
for p in R.rglob('build.gradle'):
    text = p.read_text(encoding='utf-8', errors='replace')
    artifact_ids.update(re.findall(r"artifactId\s*=\s*'([^']+)'", text))
for profile in profiles:
    pid = profile.get('profileId')
    resolved = profile.get('resolvedStarters') or []
    if not pid or not resolved:
        errors.append(f'empty capability profile: {pid!r}')
    for artifact in resolved:
        if artifact not in artifact_ids and artifact not in {
            'cpf-starter-resilience', 'cpf-starter-featureflag', 'cpf-starter-secret',
            'cpf-starter-observability', 'cpf-starter-messaging-kafka',
        }:
            errors.append(f'profile references unpublished artifact: {pid} -> {artifact}')

# Artifact catalog alignment for all QA38 projects.
catalog = json.loads((R / 'cpf-tools/release/cpf-final-artifact-catalog.json').read_text(encoding='utf-8'))
qa38_entries = catalog.get('qa38StarterArtifacts', [])
qa38_by_project = {entry.get('projectPath', '').lstrip(':'): entry for entry in qa38_entries}
for project, path in mappings.items():
    if project.startswith('cpf-starter-') or project in {'cpf-integration-fixedlength-core', 'cpf-notification-sms-spi'}:
        entry = qa38_by_project.get(project)
        if not entry:
            # Existing baseline artifacts may live in the original artifacts array.
            if not any(a.get('artifactId') == project for a in catalog.get('artifacts', [])):
                errors.append(f'artifact catalog missing project: {project}')
        elif entry.get('ownerPath') != path:
            errors.append(f'artifact owner mismatch: {project} -> {entry.get("ownerPath")}, expected {path}')

# DB vendor parity.
official_vendors = {'mariadb', 'postgresql', 'oracle'}
for module in sorted(p for p in (R / 'cpf-starters').iterdir() if p.is_dir()):
    db = module / 'src/main/resources/db'
    if not db.exists():
        continue
    vendors = {p.name.lower() for p in db.iterdir() if p.is_dir()}
    if vendors != official_vendors:
        errors.append(f'DB vendor set mismatch: {module.relative_to(R)}: {sorted(vendors)}')
        continue
    layouts: dict[str, set[str]] = {}
    for vendor in official_vendors:
        layouts[vendor] = {
            p.relative_to(db / vendor).as_posix()
            for p in (db / vendor).rglob('*.sql')
        }
        for sql in (db / vendor).rglob('*.sql'):
            text = sql.read_text(encoding='utf-8', errors='replace').strip()
            if not text:
                errors.append(f'empty SQL: {sql.relative_to(R)}')
            if re.search(r'\b(H2|MSSQL|SQLSERVER|MYSQL)\b', text, re.I):
                errors.append(f'unsupported vendor token in SQL: {sql.relative_to(R)}')
    baseline_layout = layouts['mariadb']
    for vendor in ('postgresql', 'oracle'):
        if layouts[vendor] != baseline_layout:
            errors.append(f'DB script parity mismatch: {module.relative_to(R)} mariadb={sorted(baseline_layout)} {vendor}={sorted(layouts[vendor])}')

# Placeholder/fake implementation patterns. Test fixtures may deliberately throw unsupported
# operations, while product sources may catch provider-specific unsupported filesystem modes.
placeholder_pattern = re.compile(r'\b(?:TODO|FIXME|PLANNED|not\s+implemented|placeholder|dummy)\b', re.I)
for p in files:
    rel = p.relative_to(R).as_posix()
    product_source = '/src/main/' in '/' + rel
    generator_source = rel.startswith('cpf-tools/generator/') and p.suffix.lower() in {'.ps1', '.sh', '.py'}
    if not (product_source or generator_source):
        continue
    if p.suffix.lower() not in {'.java', '.kt', '.groovy', '.ps1', '.sh', '.py', '.sql'}:
        continue
    text = p.read_text(encoding='utf-8', errors='replace')
    if placeholder_pattern.search(text):
        errors.append(f'placeholder marker: {rel}')
    if product_source and re.search(r'throw\s+new\s+UnsupportedOperationException\s*\(', text):
        errors.append(f'unsupported product implementation: {rel}')

print(f'FILES={len(files)} INCLUDES={len(include_set)} PROJECT_REFS={len(project_refs)} JAVA={len(list(R.rglob("*.java")))} PROFILES={len(profiles)}')
if warnings:
    for warning in warnings:
        print('WARN', warning)
if errors:
    for error in errors:
        print('ERROR', error)
    sys.exit(1)
print('QA38_STATIC_VALIDATION_PASS')
