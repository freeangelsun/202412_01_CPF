#!/usr/bin/env python3
"""CPF Initial Operator Bootstrap current-contract validator.

문서만 currentize하고 Source/Consumer가 예전 profile switch 또는 가짜 Consumer login을 유지하는
false-green을 막는다. 이 validator는 secret 값 자체를 읽거나 출력하지 않는다.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


REQUIRED_FILES = (
    "cpf-tools/governance/cpf-product-surface-policy.json",
    "cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md",
    "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md",
    "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java",
    "cpf-admin/src/main/resources/application.yml",
    "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeBootstrapRunner.java",
    "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapRunner.java",
    "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java",
    "cpf-backoffice/online/src/main/resources/application.yml",
    "cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/security/BackofficeWebSecurityConfiguration.java",
    "cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/config/BackofficeWebProperties.java",
    "cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/client/BusinessApiHttpClient.java",
    "cpf-backoffice-web/src/main/resources/application.yml",
    "cpf-tools/release/open-git/verify_open_git_consumer_runtime.py",
    "cpf-tools/release/open-git/cpf_open_git.py",
    "cpf-tools/release/open-git/templates/README.md",
    "cpf-tools/db/vendor/mariadb/runtime/backoffice/repository/auth-bootstrap-operator-count.sql",
    "cpf-tools/db/vendor/postgresql/runtime/backoffice/repository/auth-bootstrap-operator-count.sql",
    "cpf-tools/db/vendor/oracle/runtime/backoffice/repository/auth-bootstrap-operator-count.sql",
)

PROFILE_BRANCH = re.compile(r"if\s*\([^\n]*(?:local|dev|stg|test|prod)[^\n]*\)", re.IGNORECASE)


def read(root: Path, relative: str, failures: list[str]) -> str:
    path = root / relative
    if not path.is_file():
        failures.append("MISSING:" + relative)
        return ""
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, label: str, failures: list[str]) -> None:
    if token not in text:
        failures.append("MISSING_CONTRACT:" + label + ":" + token)


def forbid(text: str, token: str, label: str, failures: list[str]) -> None:
    if token in text:
        failures.append("FORBIDDEN_CONTRACT:" + label + ":" + token)


def validate(root: Path) -> list[str]:
    failures: list[str] = []
    content = {relative: read(root, relative, failures) for relative in REQUIRED_FILES}

    policy_text = content["cpf-tools/governance/cpf-product-surface-policy.json"]
    try:
        policy = json.loads(policy_text)
    except json.JSONDecodeError as failure:
        return sorted(set(failures + ["INVALID_JSON:cpf-product-surface-policy.json:" + str(failure)]))
    contract = policy.get("initialOperatorBootstrapContract")
    if not isinstance(contract, dict):
        failures.append("MISSING_POLICY:initialOperatorBootstrapContract")
    else:
        if contract.get("profiles") != ["local", "dev", "stg", "test", "prod"]:
            failures.append("POLICY_PROFILES_MISMATCH:initialOperatorBootstrapContract")
        if contract.get("sameSecuritySemantics") is not True:
            failures.append("POLICY_SAME_SECURITY_SEMANTICS_REQUIRED")
        initial = contract.get("initialBootstrap")
        if not isinstance(initial, dict) or initial.get("oneTime") is not True:
            failures.append("POLICY_ONE_TIME_INITIAL_BOOTSTRAP_REQUIRED")
        elif initial.get("normalAdministrationRequiresMakerChecker") is not True:
            failures.append("POLICY_NORMAL_ADMIN_MAKER_CHECKER_REQUIRED")
        elif initial.get("auditAction") != "INITIAL_OPERATOR_BOOTSTRAP" or initial.get("auditActor") != "CPF_BOOTSTRAP":
            failures.append("POLICY_INITIAL_AUDIT_IDENTITY_MISMATCH")
        elif initial.get("secrets", {}).get("secretValueInSourceOrEvidence") is not False:
            failures.append("POLICY_SECRET_VALUE_BOUNDARY_REQUIRED")

    product = content["cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md"]
    harness = content["cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md"]
    require(product, "### 12.1 Initial Operator Bootstrap", "product", failures)
    require(product, "local/dev/stg/test/prod", "product", failures)
    require(harness, "## 34. 동일 Profile Initial Operator Bootstrap 계약", "harness", failures)
    require(harness, "authenticated business transaction", "harness", failures)
    require(harness, "verify_initial_operator_bootstrap_contract.py", "harness", failures)

    adm = content["cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java"]
    for token in ("PASSWORD_ENV", "hasAnyOperator()", "INITIAL_OPERATOR_BOOTSTRAP", "CPF_BOOTSTRAP"):
        require(adm, token, "adm-initial-runner", failures)
    for token in ("allowProd", "isEnabled(", "getPassword(", "getActiveProfiles("):
        forbid(adm, token, "adm-initial-runner", failures)
    if PROFILE_BRANCH.search(adm):
        failures.append("FORBIDDEN_PROFILE_SECURITY_BRANCH:adm-initial-runner")
    adm_yml = content["cpf-admin/src/main/resources/application.yml"]
    for token in ("operator-id: ${CPF_ADM_BOOTSTRAP_OPERATOR_ID:}", "operator-name: ${CPF_ADM_BOOTSTRAP_OPERATOR_NAME:}"):
        require(adm_yml, token, "adm-config", failures)
    forbid(adm_yml, "CPF_ADM_BOOTSTRAP_ENABLED", "adm-config", failures)
    forbid(adm_yml, "password: ${CPF_ADM_BOOTSTRAP", "adm-config", failures)

    normal_mbw = content["cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeBootstrapRunner.java"]
    for token in ("hasAnyOperator()", "MBW_INITIAL_OPERATOR_BOOTSTRAP_REQUIRED", "MBW_INITIAL_AND_APPROVED_BOOTSTRAP_CANNOT_RUN_TOGETHER"):
        require(normal_mbw, token, "mbw-normal-approval-runner", failures)
    initial_runner = content["cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapRunner.java"]
    initial_service = content["cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java"]
    for token in ("CPF_MBW_BOOTSTRAP_PASSWORD", "bootstrapSecretProvided", "MBW_INITIAL_AND_APPROVED_BOOTSTRAP_CANNOT_RUN_TOGETHER"):
        require(initial_runner, token, "mbw-initial-runner", failures)
    for token in ("withAuditChainLock", "hasAnyOperator()", "INITIAL_OPERATOR_BOOTSTRAP", "CPF_BOOTSTRAP", "Arrays.fill"):
        require(initial_service, token, "mbw-initial-service", failures)
    if PROFILE_BRANCH.search(initial_runner) or PROFILE_BRANCH.search(initial_service):
        failures.append("FORBIDDEN_PROFILE_SECURITY_BRANCH:mbw-initial-bootstrap")
    mbw_yml = content["cpf-backoffice/online/src/main/resources/application.yml"]
    for token in ("initial-operator:", "login-id: ${CPF_MBW_INITIAL_OPERATOR_LOGIN_ID:}",
                  "operator-name: ${CPF_MBW_INITIAL_OPERATOR_NAME:}",
                  "role-code: ${CPF_MBW_INITIAL_OPERATOR_ROLE_CODE:}",
                  "code: ${CPF_ENVIRONMENT_CODE:local}"):
        require(mbw_yml, token, "mbw-initial-config", failures)
    forbid(mbw_yml, "initial-operator:\n      password:", "mbw-initial-config", failures)

    # Channel Front는 self SystemCode를 만들지 않는다. issuer는 ChannelCode, original/caller
    # business metadata는 최초 MBW owner로 구성한다.
    bff_security = content["cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/security/BackofficeWebSecurityConfiguration.java"]
    require(bff_security, '"/api/v1/backoffice/security/csrf").permitAll()', "bff-pre-login-csrf", failures)
    bff_properties = content["cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/config/BackofficeWebProperties.java"]
    bff_client = content["cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/client/BusinessApiHttpClient.java"]
    bff_yml = content["cpf-backoffice-web/src/main/resources/application.yml"]
    for text, token, label in ((bff_properties, "String channelCode", "bff-properties"),
                               (bff_client, "properties.channelCode()", "bff-client"),
                               (bff_yml, "channel-code: MBW", "bff-config"),
                               (bff_yml, "target-system-code: MBW", "bff-config")):
        require(text, token, label, failures)
    for text, token, label in ((bff_properties, "callerSystemCode", "bff-properties"),
                               (bff_yml, "caller-system-code", "bff-config"),
                               (bff_yml, "MBW_WEB_SYSTEM_CODE", "bff-config")):
        forbid(text, token, label, failures)

    consumer = content["cpf-tools/release/open-git/verify_open_git_consumer_runtime.py"]
    for token in ("REQUIRED_CREDENTIAL_ENV", "CPF_MBW_JWT_SECRET", "security/csrf", "CookieJar()",
                  'login.get("authenticated") is not True', "backoffice/organizations",
                  "ADM authenticated operation did not succeed", "Fresh Consumer Runtime environment"):
        require(consumer, token, "open-git-consumer-verifier", failures)
    for token in ("cpf-consumer-probe", "login_status >= 500", "CPF_ADM_BOOTSTRAP_ENABLED"):
        forbid(consumer, token, "open-git-consumer-verifier", failures)
    release = content["cpf-tools/release/open-git/cpf_open_git.py"]
    require(release, "CPF_MBW_INITIAL_OPERATOR_LOGIN_ID", "open-git-release-entry", failures)
    readme = content["cpf-tools/release/open-git/templates/README.md"]
    for token in ("CPF_ADM_BOOTSTRAP_OPERATOR_NAME", "CPF_MBW_INITIAL_OPERATOR_LOGIN_ID",
                  "CPF_MBW_JWT_SECRET", "MBW_WEB_MODE", "CSRF token"):
        require(readme, token, "open-git-readme", failures)
    forbid(readme, "CPF_ADM_BOOTSTRAP_ENABLED", "open-git-readme", failures)

    for relative in REQUIRED_FILES[-3:]:
        sql = content[relative].upper()
        if "SELECT COUNT(*)" not in sql or "MBW_ADMIN_USER" not in sql:
            failures.append("INVALID_MBW_OPERATOR_COUNT_QUERY:" + relative)

    return sorted(set(failures))


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate Initial Operator Bootstrap current contract")
    parser.add_argument("--root", default=Path(__file__).resolve().parents[2], type=Path)
    args = parser.parse_args()
    failures = validate(args.root.resolve())
    if failures:
        for failure in failures:
            print("[CPF][INITIAL-OPERATOR] FAIL " + failure)
        return 1
    print("[CPF][INITIAL-OPERATOR] PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
