from pathlib import Path
import importlib.util

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/verification/verify-cpf-gradle-logical-tree.py'

def test_gate_script_is_git_independent_and_fail_closed():
    text=SCRIPT.read_text(encoding='utf-8')
    assert 'subprocess' not in text.lower()
    assert 'rev-parse' not in text.lower()
    assert 'retired Gradle project/task reference' in text
    assert "catalogModuleCount" in text
    assert "project(':runtime:batch').projectDir = file('cpf-batch')" in text

def test_current_tree_uses_five_logical_roots_and_no_flat_run_aliases():
    settings=(ROOT/'settings.gradle').read_text(encoding='utf-8')
    convention=(ROOT/'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    for root_group in ('apps','runtime','framework','starters','internal'):
        assert f"project(':{root_group}').projectDir" in settings
    for legacy in (':cpf-local-runtime:bootRun',':cpf-admin:bootRun',':cpf-backoffice:bootRun',':cpf-gateway:bootRun',':cpf-local-batch-runtime:bootRun',':cpf-education:bootRun'):
        assert legacy not in convention

def test_framework_web_declares_its_direct_data_api_dependency():
    web=(ROOT/'cpf-starters/web/build.gradle').read_text(encoding='utf-8')
    aspect=(ROOT/'cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfDtoValidationAspect.java').read_text(encoding='utf-8')
    assert 'import com.cpf.data.api.CpfDto;' in aspect
    assert "implementation project(':framework:data')" in web

def test_integration_resilience_declares_direct_data_and_spring_dependencies():
    build=(ROOT/'cpf-starters/integration/resilience/build.gradle').read_text(encoding='utf-8')
    worker=(ROOT/'cpf-starters/integration/resilience/src/main/java/com/cpf/platform/operations/reconciliation/CpfReconciliationWorker.java').read_text(encoding='utf-8')
    repository=(ROOT/'cpf-starters/integration/resilience/src/main/java/com/cpf/platform/operations/reconciliation/JdbcCpfReconciliationRepository.java').read_text(encoding='utf-8')
    assert 'import com.cpf.data.lock.api.CpfLockManager;' in worker
    assert 'import org.springframework.jdbc.core.JdbcTemplate;' in repository
    assert "api project(':framework:data')" in build
    assert "api 'org.springframework:spring-jdbc'" in build
    assert "implementation project(':framework:data:persistence')" in build
    assert "implementation 'org.springframework:spring-tx'" in build

def test_testkit_fault_aspect_uses_canonical_online_transaction_accessor():
    aspect=(ROOT/'cpf-tools/testing/cpf-testkit/src/main/java/com/cpf/testkit/fault/CpfFaultInjectionAspect.java').read_text(encoding='utf-8')
    assert 'transaction.operationId()' in aspect
    assert 'transaction.id()' not in aspect

def test_cpf_common_declares_direct_logging_and_lifecycle_dependencies():
    build=(ROOT/'cpf-common/build.gradle').read_text(encoding='utf-8')
    logging=(ROOT/'cpf-common/src/main/java/com/cpf/common/message/service/CmnLoggingCommonManagementAuditSink.java').read_text(encoding='utf-8')
    lifecycle=(ROOT/'cpf-common/src/main/java/com/cpf/common/message/service/CmnErrorCatalogCache.java').read_text(encoding='utf-8')
    assert 'import org.slf4j.Logger;' in logging
    assert 'import jakarta.annotation.PostConstruct;' in lifecycle
    assert "implementation 'org.slf4j:slf4j-api'" in build
    assert "compileOnly 'jakarta.annotation:jakarta.annotation-api'" in build

def test_public_base_starter_composes_mandatory_common_runtime_entry():
    build=(ROOT/'cpf-starters/base/build.gradle').read_text(encoding='utf-8')
    assert "api project(':starters:common')" in build
    assert "api project(':cpf-common')" not in build

def test_local_runtime_declares_adm_annotation_compile_classpath():
    build=(ROOT/'cpf-tools/runtime/cpf-local-runtime/build.gradle').read_text(encoding='utf-8')
    modules=(ROOT/'cpf-tools/runtime/cpf-local-runtime/src/main/java/com/cpf/local/runtime/CpfLocalRuntimeModules.java').read_text(encoding='utf-8')
    assert 'import com.cpf.admin.AdmApplication;' in modules
    assert 'springdoc-openapi-starter-common:${rootProject.ext.cpfSpringdocVersion}' in build
    assert 'compileOnly "org.springdoc:springdoc-openapi-starter-common:' in build

def test_generated_domain_composite_uses_the_actual_generated_group():
    settings=(ROOT/'settings.gradle').read_text(encoding='utf-8')
    assert "def packageName = properties.getProperty('cpf.domain.packageName')" in settings
    assert "properties.getProperty('cpf.domain.contractVersion') != '1'" in settings
    assert 'packageName == null || definitionDomainName == null' in settings
    assert 'definitionDomainName != domainName' in settings
    assert 'substitute module("${packageName}:online")' in settings
    assert 'substitute module("${packageName}:batch")' in settings
    assert 'packageMatch' not in settings
    assert '"com.cpf.${domainMatch.group(1)' not in settings

def test_composite_module_identity_collisions_are_catalog_owned_and_fail_closed():
    convention=(ROOT/'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    for token in (
        'cpfCompositeIdentityCollisions',
        'cpfCompositeGroupByProject',
        "gradle.afterProject",
        "target.base.archivesName.set(module.artifactId.toString())",
        "target.group = compositeGroup",
        "groupId = module.groupId.toString()",
        "compositeModuleIdentityGate",
        "wrongArchiveBaseNames",
        "duplicateArchiveFileNames",
        "CPF_COMPOSITE_IDENTITY_READY",
    ):
        assert token in convention
