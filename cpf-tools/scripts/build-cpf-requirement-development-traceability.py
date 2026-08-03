#!/usr/bin/env python3
"""Build developer-owned Requirement/Work-Package traceability without touching QA/Codex state.

Work Package is an implementation/execution grouping. Requirement remains the minimum
status unit. Shared evidence is reused only after each Requirement row records its own
acceptance criteria, linked scenarios, actual source inventory, consumer, executed
test/runtime, evidence proof scope and uncovered acceptance scope.
"""
from __future__ import annotations

import argparse
import csv
import glob
import json
from collections import Counter, defaultdict
from pathlib import Path

BASE_SHA = "cb305fc5363263c9607e990ba640233c28668f01"
EVIDENCE_ROOT = "cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R4/"
TRACEABILITY_EVIDENCE = EVIDENCE_ROOT + "R4_REQUIREMENT_TRACEABILITY_FINAL.log"
SOURCE_REVIEW_EVIDENCE = EVIDENCE_ROOT + "R4_WORK_PACKAGE_SOURCE_REVIEW_FINAL.log"

GENERAL_SOURCE = {
    "ADM_UI": "cpf-admin/frontend;cpf-admin/src/main/java;cpf-admin/src/test",
    "BZA_UI": "cpf-biz-admin/frontend;cpf-biz-admin/src/main/java;cpf-biz-admin/src/test",
    "FRONTEND": "cpf-admin/frontend;cpf-biz-admin/frontend",
    "TEST": "cpf-tools/scripts;cpf-tools/scripts/tests;cpf-tools/verification",
    "QUALITY": "cpf-tools/scripts;cpf-docs/work/qa",
    "RELEASE": "build.gradle;settings.gradle;cpf-tools/release;deploy",
    "SECURITY": "cpf-core/src/main/java/com/cpf/core/api/security;cpf-admin/src/main/java;cpf-biz-admin/src/main/java",
    "PRODUCT": "cpf-core;cpf-common;cpf-admin;cpf-biz-admin;cpf-batch;cpf-tools/generator",
    "BATCH": "cpf-batch;cpf-admin/frontend/src/features/batch-runtime-control;cpf-tools/db/vendor",
    "DOC": "cpf-docs",
    "CORE": "cpf-core/src/main/java/com/cpf/core/api;cpf-core/src/main/java/com/cpf/core/spi;cpf-core/src/main/java/com/cpf/core/internal",
    "OPS": "cpf-admin/src/main/java/com/cpf/admin/opr;cpf-admin/frontend/src/features",
    "MESSAGING": "cpf-core/src/main/java/com/cpf/core/api/broker;cpf-starters/messaging",
    "MESSAGING_PROVIDER": "cpf-starters/messaging;cpf-tools/scripts",
    "STARTER": "cpf-starters;cpf-tools/generator/contracts/cpf-starter-catalog.json;settings.gradle",
    "GATEWAY": "cpf-gateway;cpf-admin/frontend/src/features",
    "EXTERNAL": "cpf-reference;cpf-core/src/main/java/com/cpf/core/api/servicecall",
    "RELIABILITY": "cpf-core/src/main/java/com/cpf/core/api/reliability;cpf-core/src/main/java/com/cpf/core/api/resilience;cpf-admin/frontend/src/features",
    "INTEGRATION": "cpf-core/src/main/java/com/cpf/core/api/http;cpf-core/src/main/java/com/cpf/core/api/servicecall;cpf-starters/integration",
    "OBSERVABILITY": "cpf-core/src/main/java/com/cpf/core/api/observability;cpf-core/src/main/java/com/cpf/core/api/logging;cpf-admin/frontend/src/features",
    "COMMON": "cpf-common;cpf-core/src/main/java/com/cpf/core/api",
    "RUNTIME_CONTROL": "cpf-core/src/main/java/com/cpf/core/api/runtimecontrol;cpf-admin/src/main/java/com/cpf/admin/opr;cpf-starters",
    "FILE": "cpf-core/src/main/java/com/cpf/core/api/filetransfer;cpf-core/src/main/java/com/cpf/core/api/attachment;cpf-admin/frontend/src/features",
    "API": "cpf-core/src/main/java/com/cpf/core/api;cpf-admin/frontend/openapi;cpf-biz-admin/frontend/openapi",
    "GOV": "cpf-docs/governance;cpf-docs/work/current",
}

# A profile only proves the explicitly described scope. It never auto-completes a Requirement.
PROFILES = [
    {
        "name": "batch_abandon",
        "predicate": lambda e, r: "Abandon" in r.get("feature", "") or (e["work_package_id"] == "P10-BATCH-SPRING-BATCH" and r.get("feature") == "Abandon"),
        "source": "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java;cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchAbandonCoordinator.java;cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchControlState.java;cpf-tools/db/vendor",
        "call_path": "ADM Batch UI/Controller → BAT compatibility API → CpfSpringBatchExecutionControl → CpfBatchAbandonCoordinator → BatchExecutionLedgerPort/JobOperator",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_BATCH_ABANDON_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_RUNTIME_COMMAND_FINAL.log;" + EVIDENCE_ROOT + "gates/db_vendor_semantic_final.log",
        "proof": "ABANDONING 선점, 동시 외부 부작용 1회, 응답 유실·Ledger 확정 실패 UNKNOWN, 3 Vendor V99/R99 정합성을 실행 검증",
    },
    {
        "name": "runtime_command",
        "predicate": lambda e, r: e["work_package_id"] == "P09-ADM-UI-BATCH" or "Runtime Command" in (r.get("requirement", "") + r.get("feature", "")),
        "source": "cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java;cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandExecutionException.java;cpf-batch/control-server/src/test/java/com/cpf/batch/control/RuntimeCommandExecutorFailureClassificationTest.java",
        "call_path": "ADM Batch UI/Controller → RuntimeControlController → RuntimeCommandExecutor → RuntimeRegistry/RuntimeCommandRepository/RuntimeLifecycleService",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_RUNTIME_COMMAND_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_CONTROLLER_FINAL.log",
        "proof": "사전 검증 실패 FAILED, dispatch 이후 불명확 UNKNOWN, 부분 성공, Evidence 저장 실패, 중복 Target 차단과 Secret 마스킹을 실행 검증",
    },
    {
        "name": "frontend_workflow",
        "predicate": lambda e, r: r.get("feature") in {"Break-glass", "Attachment", "Session", "운영자 Session"} or e["work_package_id"] in {"P10-RUNTIME-CONTROL-BREAK-GLASS-POLICY", "P10-STARTER-ATTACHMENT", "P10-RUNTIME-CONTROL-SESSION-POLICY"},
        "source": "cpf-admin/frontend/src/features/break-glass;cpf-biz-admin/frontend/src/features/attachments;cpf-biz-admin/frontend/src/features/sessions;cpf-admin/frontend/src/components/DangerousActionDialog.vue",
        "call_path": "ADM/BZA Workflow Page → typed workflow operation contract → cpfApi/orval mutator → BFF",
        "evidence": EVIDENCE_ROOT + "R4_FRONTEND_WORKFLOW_RUNTIME_FINAL.log;" + EVIDENCE_ROOT + "R4_FRONTEND_API_BZA_OWNER_FINAL.log",
        "proof": "위험조치 사유·확인·상태·접근성·operationId와 Browser actor override 차단을 TypeScript Runtime으로 검증",
    },
    {
        "name": "frontend_structure",
        "predicate": lambda e, r: r.get("requirement_group") in {"ADM_UI", "BZA_UI", "FRONTEND"},
        "source": "cpf-admin/frontend;cpf-biz-admin/frontend",
        "call_path": "Route/Menu/Feature Component → generated operation wrapper/cpfApi → ADM/BZA BFF",
        "evidence": EVIDENCE_ROOT + "R4_FRONTEND_TSC_BZA_OWNER_FINAL.log;" + EVIDENCE_ROOT + "R4_FRONTEND_API_BZA_OWNER_FINAL.log;" + EVIDENCE_ROOT + "gates/frontend_closure_final.log;" + EVIDENCE_ROOT + "gates/operator_trust_rerun.log",
        "proof": "현재 Snapshot의 ADM/BZA TypeScript 전체 Compile, import/operation consumer closure, same-origin/CSRF/actor trust boundary를 검증; Browser·실제 BFF·DB 동작은 증명하지 않음",
    },
    {
        "name": "transaction",
        "predicate": lambda e, r: r["requirement_id"] == "CPF-FR-011988" or e["work_package_id"] in {"P10-CORE-HEADER", "P10-CORE-CPF-HEADER", "P10-CORE-CPF-TXID"},
        "source": "cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java;cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java;cpf-admin/frontend/src/shared/transaction.ts;cpf-biz-admin/frontend/src/shared/transaction.ts",
        "call_path": "HTTP request → TransactionHeaderValidationInterceptor → CpfInboundHeaderValidator → Product Controller; Frontend transaction generator → cpfApi header",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_TRANSACTION_FINAL.log;" + EVIDENCE_ROOT + "gates/transaction_id.log",
        "proof": "실제 Interceptor/Validator Java 21 호출과 누락·부적합 transactionId fail-closed, legacy ID 부재를 검증",
    },
    {
        "name": "network",
        "predicate": lambda e, r: "endpoint" in (r.get("feature", "") + r.get("capability", "")).lower() or e["work_package_id"] == "P10-RUNTIME-CONTROL-EXTERNAL-ENDPOINT",
        "source": "cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java;cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java;cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfPinnedHttpConnectorFactory.java;cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java;cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java",
        "call_path": "Service/Gateway/Agent consumer → endpoint registry/policy → pinned connector/transport → resolved remote address",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_NETWORK_FINAL.log;" + EVIDENCE_ROOT + "gates/network_consumers_final.log",
        "proof": "DNS rebinding·mixed address·CIDR·configured pin과 실제 5개 Consumer 연결을 검증",
    },
    {
        "name": "db_less",
        "predicate": lambda e, r: "DB-less" in (r.get("requirement", "") + r.get("acceptance_criteria", "")) or e["work_package_id"] in {"P10-STARTER-JDBC", "P10-STARTER-MYBATIS"},
        "source": "cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java;cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java;cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java",
        "call_path": "Spring configuration → runtime-mode/product condition → DataSource/MyBatis Bean graph → ADM/Common consumer context",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_DB_LESS_FINAL.log;" + EVIDENCE_ROOT + "gates/db_less_after_fix.log",
        "proof": "DB-less Bean 미생성, Product fail-closed 정책, Context Test와 의미 기반 Gate를 실행 검증",
    },
    {
        "name": "starter_structure",
        "predicate": lambda e, r: r.get("requirement_group") == "STARTER",
        "source": "cpf-starters;cpf-tools/generator/contracts/cpf-starter-catalog.json;settings.gradle",
        "call_path": "Canonical Starter Catalog → settings.gradle physical include → AutoConfiguration imports → Product profile consumer",
        "evidence": EVIDENCE_ROOT + "R4_PYTHON_GATE_TESTS_FINAL.log;" + EVIDENCE_ROOT + "R4_WORK_PACKAGE_SOURCE_REVIEW_FINAL.log",
        "proof": "Starter Catalog/Owner Gate의 Positive·Negative Unit Test와 physical Source inventory만 연결; 실제 전체 Git checkout Gate는 미검증",
        "implementation": False,
    },
    {
        "name": "db_vendor",
        "predicate": lambda e, r: r.get("function_type") in {"DB_VENDOR", "DB_PARITY", "DB_MIGRATION", "ROLLBACK", "INSTALL", "UPGRADE"} or "3-VENDOR" in e["work_package_id"] or e["work_package_id"].startswith("P10-DB-"),
        "source": "cpf-tools/db/canonical;cpf-tools/db/vendor/mariadb;cpf-tools/db/vendor/postgresql;cpf-tools/db/vendor/oracle;cpf-tools/scripts/generate-official-db-vendor-source.ps1",
        "call_path": "Canonical schema → Vendor generator → Source/Install/Migration/Rollback/Verify → Runtime repository",
        "evidence": EVIDENCE_ROOT + "gates/db_vendor_semantic_final.log;" + EVIDENCE_ROOT + "R4_DB_VENDOR_GATE_TESTS.log",
        "proof": "200 Table과 공식 3 Vendor lifecycle/금지 타입/V97~V100 Positive·Negative Parser 검증; 실제 DB Server 실행은 증명하지 않음",
    },
    {
        "name": "audit",
        "predicate": lambda e, r: e["work_package_id"] in {"P10-SECURITY-SEC-AUDIT", "P11-SECURITY-SEC-AUDIT", "P10-RUNTIME-CONTROL-AUDIT-DELIVERY"},
        "source": "cpf-admin/src/test/java/com/cpf/admin/opr/service/AdmAuditLogServiceFailClosedTest.java;cpf-tools/verification/java21/audit-runtime",
        "call_path": "Audit write/read consumer → fail-closed repository/runtime → durable store → two JVM verifier",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_AUDIT_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_AUDIT_UNIT_FINAL.log",
        "proof": "2 JVM 동시 저장·kill -9·지속·재기동·220건/중복0/유실0·write/read fail-closed·trace/masking을 검증",
    },
    {
        "name": "batch_structure",
        "predicate": lambda e, r: r.get("requirement_group") == "BATCH",
        "source": "cpf-batch;cpf-admin/src/main/java/com/cpf/admin/opr/batch;cpf-admin/frontend/src/features/batch-runtime-control",
        "call_path": "ADM operation consumer → BAT Control Server → Spring Batch execution/scheduler/worker/agent/center-cut runtime",
        "evidence": EVIDENCE_ROOT + "R4_JAVA21_CONTROLLER_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_RUNTIME_COMMAND_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_BATCH_ABANDON_FINAL.log",
        "proof": "Batch Controller 오류 분류, Runtime Command, Spring Batch abandon 핵심 경로를 실행 검증; 모든 Batch Job/Step/Worker/DB topology는 증명하지 않음",
    },
    {
        "name": "security_structure",
        "predicate": lambda e, r: r.get("requirement_group") == "SECURITY",
        "source": "cpf-core/src/main/java/com/cpf/core/api/security;cpf-admin/src/main/java;cpf-biz-admin/src/main/java;cpf-admin/frontend;cpf-biz-admin/frontend",
        "call_path": "Browser/Product consumer → authentication/authorization/permission/audit/masking boundary",
        "evidence": EVIDENCE_ROOT + "gates/operator_trust_rerun.log;" + EVIDENCE_ROOT + "R4_FRONTEND_API_BZA_OWNER_FINAL.log;" + EVIDENCE_ROOT + "R4_JAVA21_AUDIT_FINAL.log",
        "proof": "Browser actor trust·Bearer 차단·Audit fail-closed/masking 대체 Runtime을 검증; MFA/OIDC/mTLS/실제 서버 권한 전체는 증명하지 않음",
    },
    {
        "name": "quality_test_structure",
        "predicate": lambda e, r: r.get("requirement_group") in {"TEST", "QUALITY"},
        "source": "cpf-tools/scripts;cpf-tools/scripts/tests;cpf-tools/verification",
        "call_path": "Developer/QA command → fail-closed Gate/Harness → Source/Runtime/Evidence result",
        "evidence": EVIDENCE_ROOT + "R4_PYTHON_GATE_TESTS_FINAL.log;" + EVIDENCE_ROOT + "R4_RUNTIME_EXIT_SUMMARY_FINAL.csv",
        "proof": "Gate/Harness Positive·Negative Test와 현재 실행 가능한 Java21/Node/Python Runtime 묶음을 검증; 모든 제품 Capability별 Test는 증명하지 않음",
    },
]


def read_parts(pattern: str) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for path in sorted(glob.glob(pattern)):
        with open(path, encoding="utf-8-sig", newline="") as handle:
            rows.extend({key: (value or "").strip() for key, value in row.items()} for row in csv.DictReader(handle))
    return rows


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        return [{key: (value or "").strip() for key, value in row.items()} for row in csv.DictReader(handle)]


def unique_semicolon(values: list[str]) -> str:
    return ";".join(dict.fromkeys(value for raw in values for value in raw.split(";") if value))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--execution-glob", required=True)
    parser.add_argument("--requirement-glob", required=True)
    parser.add_argument("--scenario-glob", required=True)
    parser.add_argument("--work-package-review", required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--start-row", type=int, default=20_001)
    parser.add_argument("--expected-count", type=int, default=10_558)
    parser.add_argument("--expected-work-packages", type=int, default=291)
    parser.add_argument("--baseline-sha", default=BASE_SHA)
    args = parser.parse_args()

    execution = read_parts(args.execution_glob)
    requirements = {row["requirement_id"]: row for row in read_parts(args.requirement_glob)}
    scenarios: dict[str, list[dict[str, str]]] = defaultdict(list)
    for row in read_parts(args.scenario_glob):
        scenarios[row["linked_requirement_id"]].append(row)
    review_rows = read_csv(Path(args.work_package_review))
    work_package_review = {row["work_package_id"]: row for row in review_rows}
    scope = execution[args.start_row - 1 :]
    if len(scope) != args.expected_count:
        raise SystemExit(f"scope count mismatch expected={args.expected_count} actual={len(scope)}")
    if len(work_package_review) != args.expected_work_packages:
        raise SystemExit(f"work package review mismatch expected={args.expected_work_packages} actual={len(work_package_review)}")

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    rows: list[dict[str, str]] = []
    by_work_package: dict[str, list[dict[str, str]]] = defaultdict(list)

    for execution_row in scope:
        requirement = requirements[execution_row["requirement_id"]]
        linked = scenarios[execution_row["requirement_id"]]
        source_review = work_package_review[execution_row["work_package_id"]]
        sources = [source_review.get("actual_source_files", ""), GENERAL_SOURCE.get(requirement.get("requirement_group", ""), "")]
        call_path = f"{requirement.get('actual_consumer', '')} → {requirement.get('owner_module', '')}::{requirement.get('owner_package', '')}"
        evidence = [TRACEABILITY_EVIDENCE, SOURCE_REVIEW_EVIDENCE]
        proof = [
            "Requirement 원문·Acceptance Criteria·Scenario·Owner·Consumer·Work Package·actual source inventory를 개별 행으로 연결",
            "Work Package Source Review는 실제 Snapshot 파일을 연결하지만 기능 Runtime 완료 자체를 증명하지 않음",
        ]
        profiles: list[str] = []
        for profile in PROFILES:
            if profile["predicate"](execution_row, requirement):
                profiles.append(profile["name"])
                sources.append(profile["source"])
                call_path = profile["call_path"]
                evidence.append(profile["evidence"])
                proof.append(profile["proof"])
        implementation_profiled = any(profile.get("implementation", True) for profile in PROFILES if profile["name"] in profiles)
        source = unique_semicolon(sources)
        executed_evidence = unique_semicolon(evidence)
        scenario_ids = ";".join(row["scenario_id"] for row in linked)
        uncovered_parts = []
        source_uncovered = source_review.get("uncovered_aspects", "")
        if source_uncovered:
            uncovered_parts.append("Source Review 미확인 Aspect=" + source_uncovered)
        if implementation_profiled:
            uncovered_parts.append("Profile Evidence가 증명하지 않은 Requirement 고유 Acceptance Criteria")
        else:
            uncovered_parts.append("Requirement 고유 Source 본문·실제 호출·Positive/Negative/Integration/Runtime/Fault Evidence")
        uncovered_parts.append("전체 Git checkout exact-HEAD·Java25 Gradle·실제 3 Vendor DB·실제 Browser/BFF 중 해당 항목")
        uncovered = " | ".join(uncovered_parts)
        development_status = "부분 구현" if implementation_profiled else "미검증"
        execution_result = (
            "R4_RUNTIME_EXIT_SUMMARY_FINAL.csv의 관련 Harness/Gate와 Requirement Traceability/Source Review 실행"
            if implementation_profiled
            else "Requirement Traceability Closure와 Work Package Source Review 실행; 기능 Runtime은 미연결"
        )
        row = {
            "execution_order": execution_row["execution_order"],
            "requirement_id": execution_row["requirement_id"],
            "work_package_id": execution_row["work_package_id"],
            "phase_id": execution_row["phase_id"],
            "requirement_group": requirement["requirement_group"],
            "capability": requirement["capability"],
            "feature": requirement["feature"],
            "function_type": requirement["function_type"],
            "priority": requirement["priority"],
            "owner_module": requirement["owner_module"],
            "owner_package": requirement["owner_package"],
            "actual_consumer": requirement["actual_consumer"],
            "requirement": requirement["requirement"],
            "acceptance_criteria": requirement["acceptance_criteria"],
            "verification_method": requirement["verification_method"],
            "completion_prohibited_when": requirement["completion_prohibited_when"],
            "scenario_count": str(len(linked)),
            "scenario_ids": scenario_ids,
            "scenario_titles": " || ".join(row["title"] for row in linked),
            "scenario_expected_results": " || ".join(row["expected_result"] for row in linked),
            "scenario_failure_criteria": " || ".join(row["failure_criteria"] for row in linked),
            "evidence_level": "IMPLEMENTATION_SUBSTITUTE_RUNTIME" if implementation_profiled else "TRACEABILITY_ONLY",
            "evidence_profiles": ";".join(profiles),
            "source_resolution": source_review.get("source_resolution", ""),
            "source_review_selected_file_count": source_review.get("selected_file_count", ""),
            "source_review_uncovered_aspects": source_uncovered,
            "actual_source": source,
            "actual_call_path": call_path,
            "executed_test_runtime_evidence": executed_evidence,
            "evidence_proves": " | ".join(proof),
            "uncovered_acceptance": uncovered,
            "development_status": development_status,
            "verification_status": "미검증",
            "개발GPT_수행여부": "예",
            "개발GPT_상태": "미완료",
            "개발GPT_수행내용": "Requirement 원문·Scenario·actual source·Consumer·실행 Evidence를 개별 연결" + ("하고 관련 공통 구현/대체 Runtime을 수행" if implementation_profiled else ""),
            "개발GPT_미완료사유": uncovered,
            "개발GPT_실행및검증": execution_result,
            "개발GPT_필요환경및권한": "전체 Git Working Tree; Java25+Gradle; Oracle/PostgreSQL/MariaDB 실제 DB 계정; Chromium/Playwright; 실제 Product topology 중 해당 항목",
            "개발GPT_evidence": executed_evidence,
            "개발GPT_자체검수여부": "예",
            "개발GPT_자체검수상태": "미완료",
            "개발GPT_자체검수결과": (
                "공통 구현·대체 Runtime PASS 범위를 개별 연결했으나 Requirement 고유 Acceptance 전체는 미완료"
                if implementation_profiled
                else "개별 Traceability·Source Review 완료, 기능 구현·Runtime Evidence 미연결"
            ),
            "verifiedAgainstSha": args.baseline_sha,
            "baseline_sha": args.baseline_sha,
        }
        rows.append(row)
        by_work_package[execution_row["work_package_id"]].append(row)

    with (output_dir / "REQUIREMENT_STATUS.csv").open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)

    work_packages: list[dict[str, object]] = []
    for work_package_id, requirement_rows in by_work_package.items():
        review = work_package_review[work_package_id]
        work_packages.append({
            "work_package_id": work_package_id,
            "first_execution_order": requirement_rows[0]["execution_order"],
            "last_execution_order": requirement_rows[-1]["execution_order"],
            "requirement_count": len(requirement_rows),
            "scenario_count": sum(int(row["scenario_count"]) for row in requirement_rows),
            "owner_modules": ";".join(sorted({row["owner_module"] for row in requirement_rows})),
            "requirement_groups": ";".join(sorted({row["requirement_group"] for row in requirement_rows})),
            "capabilities": ";".join(sorted({row["capability"] for row in requirement_rows})),
            "source_resolution": review.get("source_resolution", ""),
            "selected_source_files": review.get("actual_source_files", ""),
            "source_review_uncovered_aspects": review.get("uncovered_aspects", ""),
            "implementation_profiled_requirements": sum(row["evidence_level"] == "IMPLEMENTATION_SUBSTITUTE_RUNTIME" for row in requirement_rows),
            "traceability_only_requirements": sum(row["evidence_level"] == "TRACEABILITY_ONLY" for row in requirement_rows),
            "developer_complete": 0,
            "partial": sum(row["development_status"] == "부분 구현" for row in requirement_rows),
            "unverified": sum(row["development_status"] == "미검증" for row in requirement_rows),
            "evidence_profiles": ";".join(sorted({profile for row in requirement_rows for profile in row["evidence_profiles"].split(";") if profile})),
            "executed_evidence": unique_semicolon([row["executed_test_runtime_evidence"] for row in requirement_rows]),
            "development_result": "Developer implementation/substitute runtime linked" if any(row["evidence_level"] == "IMPLEMENTATION_SUBSTITUTE_RUNTIME" for row in requirement_rows) else "Individual traceability/source inventory only",
            "next_required_action": "REQUIREMENT_STATUS.csv의 uncovered_acceptance를 Requirement별 실제 Source/Consumer/Runtime으로 닫고 개발GPT 컬럼만 갱신",
            "baseline_sha": args.baseline_sha,
        })
    with (output_dir / "WORK_PACKAGE_STATUS.csv").open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(work_packages[0]))
        writer.writeheader()
        writer.writerows(work_packages)

    summary = {
        "requirements": len(rows),
        "workPackages": len(work_packages),
        "linkedScenarios": sum(int(row["scenario_count"]) for row in rows),
        "sourceResolvedRequirements": sum(row["source_resolution"] == "EXACT_SNAPSHOT_FILES" for row in rows),
        "implementationEvidenceRequirements": sum(row["evidence_level"] == "IMPLEMENTATION_SUBSTITUTE_RUNTIME" for row in rows),
        "traceabilityOnlyRequirements": sum(row["evidence_level"] == "TRACEABILITY_ONLY" for row in rows),
        "developerComplete": 0,
        "partial": sum(row["development_status"] == "부분 구현" for row in rows),
        "unverified": sum(row["development_status"] == "미검증" for row in rows),
        "baselineSha": args.baseline_sha,
        "profileCounts": dict(Counter(profile for row in rows for profile in row["evidence_profiles"].split(";") if profile)),
    }
    (output_dir / "TRACEABILITY_SUMMARY.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
