"""Development Master Release 보존과 Open Git Fresh 재생성 계약.

핵심은 두 축이 독립이라는 것이다.

- "이 Asset 을 Development Master Git 에 보존할 것인가" (masterTracked)
- "다음 Open Git Release 를 만들 때 다시 Fresh 생성해야 하는가" (freshRegenerationRequired)

Master 에 보존한다고 해서 다음 Release 의 생성 입력(releaseInputAuthority)이 되지 않는다.
generated / binary 라는 이유만으로 Master 보존을 막지 않고, Master 보존 여부로 공개 여부를 추론하지도 않는다.

정본 metadata: cpf-tools/release/open-git/open-git-surface-policy.json 의 releaseAssetPolicy
정본 Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §39
"""

from __future__ import annotations

import json
import os
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(os.environ.get("CPF_RELEASE_ASSET_ROOT") or Path(__file__).resolve().parents[3])

POLICY = REPO_ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"
ENGINE = REPO_ROOT / "cpf-tools/release/open-git/cpf_open_git.py"
GITIGNORE = REPO_ROOT / ".gitignore"
GITATTRIBUTES = REPO_ROOT / ".gitattributes"
RUNTIME_CATALOG = REPO_ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"
LFS_VERIFIER = REPO_ROOT / "cpf-tools/release/open-git/verify_release_lfs_contract.py"
HARNESS = REPO_ROOT / "cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md"
WORK_ITEM_REGISTRY = REPO_ROOT / "cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv"

CANONICAL = "CANONICAL_RELEASE_SOURCE"
TRACKED = "TRACKED_VERIFIED_RELEASE_RESULT"
UNTRACKED = "UNTRACKED_RELEASE_RESULT"
TRANSIENT = "TRANSIENT_RELEASE_OUTPUT"
LARGE_BINARY = "LARGE_RELEASE_BINARY"
BY_SURFACE = "BY_SURFACE_POLICY"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def policy() -> dict:
    return json.loads(read(POLICY))


def authority() -> dict:
    value = policy().get("releaseAssetPolicy")
    if not value:
        raise AssertionError("Release Asset 분류 정본(releaseAssetPolicy)이 없다")
    return value


def rules() -> list[dict]:
    model = policy()
    return list(model.get("sourceRules", [])) + list(model.get("templateRules", []))


def asset_class(rule: dict) -> str:
    """규칙의 Release Asset 부류. 파일명이 아니라 metadata 에서만 파생한다."""
    model = authority()
    override = str(rule.get(model.get("ruleOverrideKey", "releaseAssetClass"), "")).strip()
    if override:
        return override
    mapping = model["classificationMapping"]
    classification = str(rule.get("classification", ""))
    if classification not in mapping:
        raise AssertionError(f"Release Asset 부류가 정해지지 않은 classification: {classification}")
    return mapping[classification]


def binary_runtime_artifact_ids() -> list[str]:
    catalog = json.loads(read(RUNTIME_CATALOG))
    return sorted(str(runtime["artifactId"]) for runtime in catalog["runtimes"]
                  if runtime.get("provision") == "binary")


class AssetAuthorityShape(unittest.TestCase):
    """분류와 축이 정본 metadata 로 존재한다."""

    def test_asset_classes_are_declared(self) -> None:
        classes = authority()["classes"]
        for name in (CANONICAL, TRACKED, UNTRACKED, TRANSIENT, LARGE_BINARY):
            self.assertIn(name, classes, f"Release Asset 부류 선언이 없다: {name}")

    def test_every_class_declares_all_four_axes(self) -> None:
        axes = set(authority()["axes"])
        for required in ("masterTracked", "publicRelease", "releaseInputAuthority",
                         "freshRegenerationRequired", "transport"):
            self.assertIn(required, axes, f"Release Asset 축 선언이 없다: {required}")
        missing: list[str] = []
        for name, contract in authority()["classes"].items():
            for axis in axes:
                if axis not in contract:
                    missing.append(f"{name}.{axis}")
        self.assertEqual([], missing, f"부류가 축을 선언하지 않았다: {missing}")

    def test_every_rule_resolves_to_a_declared_class(self) -> None:
        declared = set(authority()["classes"])
        unresolved: list[str] = []
        for rule in rules():
            try:
                value = asset_class(rule)
            except AssertionError as failure:
                unresolved.append(str(failure))
                continue
            if value not in declared:
                unresolved.append(f"{rule.get('target')} -> {value}")
        self.assertEqual([], unresolved, f"부류가 정해지지 않은 투영 규칙: {unresolved}")

    def test_classification_mapping_covers_every_allowed_classification(self) -> None:
        allowed = set(policy()["allowedClassifications"])
        mapped = set(authority()["classificationMapping"])
        missing = sorted(allowed - mapped)
        self.assertEqual([], missing, f"Release Asset 부류가 없는 classification: {missing}")

    def test_classification_is_not_derived_from_path_or_extension(self) -> None:
        """경로나 확장자로 분류하면 파일을 옮기거나 형식을 바꾸는 순간 계약이 깨진다."""
        for key in authority()["classificationMapping"]:
            self.assertNotIn("/", key, f"경로로 분류한 항목이 있다: {key}")
            self.assertFalse(key.startswith("."), f"확장자로 분류한 항목이 있다: {key}")
            self.assertFalse(key.endswith((".json", ".md", ".sh", ".ps1", ".jar")),
                             f"파일명으로 분류한 항목이 있다: {key}")


class ProducedAssetCoverage(unittest.TestCase):
    """엔진이 만들어 내는 자산도 분류 밖에 두지 않는다.

    투영 규칙만 분류하면 Binary Repository, CLI 실행물, Checksum/Manifest 처럼 실제로 배포되는
    자산이 "무엇을 보존하고 무엇을 다시 만들 것인가" 판단 밖에 남는다.
    """

    def test_produced_assets_are_declared(self) -> None:
        assets = policy().get("releaseProducedAssets", {}).get("assets", [])
        self.assertTrue(assets, "Release 엔진 생성 자산 선언이 없다")

    def test_every_produced_asset_has_a_declared_class(self) -> None:
        declared = set(authority()["classes"])
        offenders = [str(asset.get("name")) for asset in
                     policy()["releaseProducedAssets"]["assets"]
                     if str(asset.get("releaseAssetClass")) not in declared]
        self.assertEqual([], offenders, f"부류가 없는 생성 자산: {offenders}")

    def test_produced_assets_are_never_release_inputs(self) -> None:
        classes = authority()["classes"]
        offenders = [str(asset.get("name")) for asset in
                     policy()["releaseProducedAssets"]["assets"]
                     if classes[str(asset["releaseAssetClass"])]["releaseInputAuthority"]]
        self.assertEqual([], offenders,
                         f"엔진 생성 자산이 다음 Release 의 입력 권한을 갖는다: {offenders}")

    def test_every_produced_asset_names_its_producer(self) -> None:
        missing = [str(asset.get("name")) for asset in
                   policy()["releaseProducedAssets"]["assets"]
                   if not str(asset.get("producedBy", "")).strip()]
        self.assertEqual([], missing, f"생성 주체가 없는 자산: {missing}")

    def test_binary_repository_and_cli_binary_are_covered(self) -> None:
        paths = {str(asset.get("path")) for asset in policy()["releaseProducedAssets"]["assets"]}
        for required in ("binary-repository", "bin/lib/cpf-cli.jar"):
            self.assertIn(required, paths, f"전수 분류에서 빠진 배포 자산: {required}")


class AxisIndependence(unittest.TestCase):
    """저장과 생성, 저장과 공개는 서로 다른 문제다."""

    def test_independence_rules_are_declared(self) -> None:
        text = " ".join(authority()["independenceRules"])
        for phrase in ("masterTracked=true 가 releaseInputAuthority=true",
                       "masterTracked=false 가 publicRelease=false",
                       "generated", "binary"):
            self.assertIn(phrase, text, f"독립성 규칙이 빠졌다: {phrase}")

    def test_tracked_result_is_never_a_release_input(self) -> None:
        contract = authority()["classes"][TRACKED]
        self.assertTrue(contract["masterTracked"], "현재 Release 결과를 Master 에 보존하지 않는 계약이다")
        self.assertFalse(contract["releaseInputAuthority"],
                         "Master 에 보존된 Release 결과를 다음 Release 의 입력으로 쓴다")
        self.assertTrue(contract["freshRegenerationRequired"],
                        "Master 에 보존된 Release 결과가 Fresh 재생성 의무를 면제받는다")
        self.assertEqual("FRESH_BUILD_CACHE", contract["neverUsedAs"],
                         "tracked 결과의 금지 용도가 선언되지 않았다")

    def test_only_canonical_source_carries_release_input_authority(self) -> None:
        holders = [name for name, contract in authority()["classes"].items()
                   if contract["releaseInputAuthority"]]
        self.assertEqual([CANONICAL], holders,
                         f"Canonical Source 밖에서 Release 입력 권한을 갖는다: {holders}")

    def test_public_surface_is_not_inferred_from_master_tracking(self) -> None:
        """공개 여부는 Surface Policy 가 정한다. Master 보존 여부로 추론하지 않는다."""
        for name in (CANONICAL, TRACKED):
            self.assertEqual(BY_SURFACE, authority()["classes"][name]["publicRelease"],
                             f"{name} 의 공개 여부를 Master 보존 여부로 정한다")

    def test_untracked_result_can_still_be_public(self) -> None:
        contract = authority()["classes"][UNTRACKED]
        self.assertFalse(contract["masterTracked"])
        self.assertTrue(contract["publicRelease"],
                        "Master 미보존이 공개 배포 제외로 연결된다")
        self.assertTrue(contract["trackingExceptionReasonRequired"],
                        "Master 보존 제외에 이유를 요구하지 않는다")

    def test_tracking_exception_requires_measured_evidence(self) -> None:
        evidence = set(authority()["classes"][UNTRACKED]["trackingExceptionEvidence"])
        for required in ("assetCurrentSize", "repositoryGrowth", "clonePullImpact",
                         "hostingCapability", "alternatives"):
            self.assertIn(required, evidence, f"보존 제외 근거 항목이 빠졌다: {required}")

    # 실측 근거를 적는 자리와 판정 기준을 적는 자리는 다르다. 근거는 남기고 기준은 금지한다.
    EVIDENCE_FIELDS = ("trackingExceptionReason", "note", "measuredAt", "reevaluation",
                       "evidence", "reason", "meaning", "intent", "scopeConflictNote",
                       "operatorEditableNote", "disabledRowNote", "unknownArtifactNote")
    THRESHOLD_KEY_HINTS = ("threshold", "maxsize", "sizelimit", "limitbytes", "cutoff")

    def test_no_size_threshold_is_hardcoded(self) -> None:
        """임의 용량 기준을 정책에 숫자로 박지 않는다.

        실측 근거(예: "2026-09-05 실측 111.3MB")는 금지 대상이 아니다. 금지하는 것은
        "50MB 이상 제외" 같은 판정 기준이다. 기준을 숫자로 박으면 실측 없이 결론이 나온다.
        """
        offenders: list[str] = []

        def walk(node, path: str) -> None:
            if isinstance(node, dict):
                for key, value in node.items():
                    if any(hint in key.lower() for hint in self.THRESHOLD_KEY_HINTS):
                        offenders.append(f"{path}.{key}")
                    walk(value, f"{path}.{key}")
            elif isinstance(node, list):
                for index, value in enumerate(node):
                    walk(value, f"{path}[{index}]")
            elif isinstance(node, str):
                field = path.rsplit(".", 1)[-1].split("[")[0]
                if field in self.EVIDENCE_FIELDS:
                    return
                if re.search(r"\d+\s*(?:MB|GB|mb|gb)\b", node):
                    offenders.append(f"{path}: {node[:60]}")

        walk(authority(), "releaseAssetPolicy")
        self.assertEqual([], offenders, f"임의 용량 기준이 하드코딩됐다: {offenders}")

    def test_size_evidence_is_recorded_where_an_exception_exists(self) -> None:
        """예외를 두었다면 그 근거가 실측으로 남아 있어야 한다."""
        classification = authority().get("artifactClassification")
        if not classification:
            self.skipTest("artifact 분류가 아직 없다")
        for rule in classification["rules"]:
            if rule.get("masterTracked") is not False:
                continue
            reason = str(rule.get("trackingExceptionReason", ""))
            self.assertTrue(reason.strip(), f"{rule['id']}: 예외 사유가 없다")
            self.assertRegex(reason, r"\d",
                             f"{rule['id']}: 예외 사유에 실측치가 없다")
            self.assertTrue(str(rule.get("evidence", "")).strip(),
                            f"{rule['id']}: 실측 근거 파일을 가리키지 않는다")


class CanonicalSourceContract(unittest.TestCase):
    """사람이 작성한 정본은 Release 가 새로 만들지 않는다. 그러나 매번 새로 투영한다."""

    def test_canonical_source_is_projected_not_regenerated(self) -> None:
        contract = authority()["classes"][CANONICAL]
        self.assertEqual("FRESH_PROJECTION_FROM_CANONICAL", contract["projectionPolicy"])
        self.assertFalse(contract["freshRegenerationRequired"],
                         "사람이 작성한 정본을 Release 가 다시 코드 생성한다")
        self.assertTrue(contract["mustNotBeGeneratedByReleaseEngine"],
                        "Release 엔진이 정본을 문자열로 생성해도 되는 계약이다")

    def test_canonical_source_never_reuses_previous_release_output(self) -> None:
        """다시 생성하지 않는 것과 지난 Release 출력을 재사용하는 것은 다르다."""
        self.assertFalse(authority()["classes"][CANONICAL]["reusePreviousReleaseOutput"],
                         "지난 Release tree 의 파일을 그대로 이어 쓴다")

    def test_canonical_source_is_still_verified_every_release(self) -> None:
        checks = set(authority()["classes"][CANONICAL]["verificationPolicy"])
        for required in ("projectionCompleteness", "exactByteSha256", "executableBit",
                         "lineEnding", "readmeCliParity", "windowsLinuxParity",
                         "freshConsumerExecution"):
            self.assertIn(required, checks, f"매 Release 검증 항목이 빠졌다: {required}")

    def test_user_launcher_is_authored_canonical_source(self) -> None:
        launcher_rules = [rule for rule in rules()
                          if str(rule.get("target", "")).startswith("bin/cpf")]
        self.assertTrue(launcher_rules, "공개 launcher 투영 규칙을 찾지 못했다")
        for rule in launcher_rules:
            self.assertEqual(CANONICAL, asset_class(rule),
                             f"공개 launcher 가 정본 Source 부류가 아니다: {rule.get('target')}")
            origin = rule.get("source") or rule.get("pattern")
            self.assertTrue(origin, f"공개 launcher 의 정본 위치가 없다: {rule.get('target')}")
            self.assertTrue((REPO_ROOT / str(origin)).exists(),
                            f"공개 launcher 정본 파일이 없다: {origin}")

    def test_release_engine_never_writes_launcher_content_inline(self) -> None:
        code = "\n".join(line for line in read(ENGINE).splitlines()
                         if not line.strip().startswith("#"))
        for marker in ("#!/usr/bin/env sh", "@echo off", "$LASTEXITCODE"):
            self.assertNotIn(marker, code,
                             f"Release 엔진이 launcher 본문을 생성한다: {marker}")

    def test_release_engine_does_not_decide_policy_by_extension(self) -> None:
        """확장자로 보존/공개 정책을 추론하면 metadata 가 정본이 아니게 된다.

        Maven 좌표의 artifact type 판정처럼 확장자가 곧 의미인 경우는 다르다. 금지하는 것은
        확장자로 tracking/publishing 결정을 내리는 코드다.
        """
        lines = [line for line in read(ENGINE).splitlines()
                 if not line.strip().startswith("#")]
        decisions = ("IGNORE", "TRACK", "masterTracked", "publicRelease", "releaseAssetClass")
        offenders: list[str] = []
        for index, line in enumerate(lines):
            if not re.search(r"endswith\([\"']", line):
                continue
            window = "\n".join(lines[index:index + 4])
            if any(token in window for token in decisions):
                offenders.append(line.strip()[:80])
        self.assertEqual([], offenders,
                         f"Release 엔진이 확장자로 보존/공개 정책을 정한다: {offenders}")


class EngineFreshnessContract(unittest.TestCase):
    """엔진이 이전 Release 산출물을 입력으로 삼지 않는다.

    정책 문서만으로는 부족하다. 엔진이 실제로 어떤 순서로 무엇을 읽는지 고정해야
    "Master 에 보존하지만 Fresh Release 는 재생성" 이 성립한다.
    """

    @staticmethod
    def _build_release_body() -> str:
        text = read(ENGINE)
        parts = text.split("def build_release(", 1)
        if len(parts) != 2:
            raise AssertionError("build_release 진입점을 찾지 못했다")
        return parts[1].split("\ndef ", 1)[0]

    def test_release_root_is_cleaned_before_anything_is_generated(self) -> None:
        body = self._build_release_body()
        cleanup = body.find("clean_release_root(")
        self.assertGreaterEqual(cleanup, 0, "Release 재생성 지점을 찾지 못했다")
        for producer in ("private_build_and_publication(", "_prepare_workspace(",
                         "sanitize_binary_repository("):
            position = body.find(producer)
            if position < 0:
                continue
            self.assertLess(cleanup, position,
                            f"이전 Release 잔여물을 지우기 전에 생성한다: {producer}")

    def test_prerequisites_resolve_before_the_previous_release_is_deleted(self) -> None:
        body = self._build_release_body()
        cleanup = body.find("clean_release_root(")
        remote = body.find("canonical_remote(")
        self.assertGreaterEqual(remote, 0, "remote 전제조건 확인이 없다")
        self.assertLess(remote, cleanup,
                        "전제조건을 확인하기 전에 직전 Release 산출물을 삭제한다")

    def test_engine_never_reads_a_previous_release_result_as_input(self) -> None:
        """지난 Release tree 를 읽어 새 Release 를 채우면 Fresh 재생성이 아니다."""
        body = self._build_release_body()
        offenders: list[str] = []
        for line in body.splitlines():
            stripped = line.strip()
            if stripped.startswith("#"):
                continue
            if "previous_release" in stripped or "last_release" in stripped:
                offenders.append(stripped[:80])
            # 삭제(clean)를 제외하면 release 디렉터리는 출력 전용이어야 한다.
            if "copytree(" in stripped and "release /" in stripped:
                offenders.append(stripped[:80])
        self.assertEqual([], offenders,
                         f"이전 Release 결과를 입력으로 읽는다: {offenders}")

    def test_publication_targets_an_isolated_staging_repository(self) -> None:
        body = self._build_release_body()
        self.assertIn("raw_repo = work /", body,
                      "발행이 격리 staging repository 를 쓰지 않는다")
        self.assertIn("sanitize_binary_repository(", read(ENGINE),
                      "공개 저장소가 투영 함수의 결과로 만들어지지 않는다")

    def test_lfs_contract_runs_for_candidate_staging_and_fresh_open_git_tree(self) -> None:
        body = self._build_release_body()
        calls = [match.start() for match in re.finditer(r"verify_release_lfs_contract\(", body)]
        self.assertEqual(3, len(calls),
                         "candidate binary, public staging, fresh Open Git tree의 LFS 검증이 모두 필요하다")
        self.assertLess(body.find("candidate_lfs_result"), body.find("staging_lfs_result"))
        self.assertLess(body.find("staging_lfs_result"), body.find("open_git_lfs_result"))


class GeneratedAndArtifactContract(unittest.TestCase):
    def test_generated_asset_declares_its_generator_input(self) -> None:
        key = authority().get("generatorInputKey", "generatorInput")
        declared = [rule for rule in rules() if str(rule.get(key, "")).strip()]
        self.assertTrue(declared, "생성 입력을 선언한 자산이 하나도 없다")
        missing = sorted({str(rule.get(key)) for rule in declared
                          if not (REPO_ROOT / str(rule.get(key))).exists()})
        self.assertEqual([], missing, f"존재하지 않는 생성 입력: {missing}")

    def test_promotion_happens_only_after_verification(self) -> None:
        contract = authority()["classes"][TRACKED]
        self.assertEqual("PROMOTE_AFTER_VERIFICATION", contract["promotionPolicy"])
        order = contract["currentizationOrder"]
        self.assertLess(order.index("verify"), order.index("currentizeTrackedSnapshot"),
                        "검증 전에 tracked 결과를 덮어쓴다")
        self.assertLess(order.index("generate"), order.index("verify"))

    def test_failed_artifact_is_not_a_current_release(self) -> None:
        states = authority()["classes"][TRACKED]["promotionStates"]
        for state in ("GENERATED", "STAGED", "VERIFIED", "PROMOTED_CURRENT_RELEASE"):
            self.assertIn(state, states, f"승격 단계 선언이 빠졌다: {state}")
        self.assertEqual("PROMOTED_CURRENT_RELEASE", states[-1],
                         "최종 승격 상태가 마지막이 아니다")

    def test_artifact_set_is_currentized_atomically(self) -> None:
        self.assertTrue(authority()["classes"][TRACKED]["atomicArtifactSet"],
                        "새 JAR 과 옛 SBOM 이 섞이는 반쪽 상태를 허용한다")


class CleanWorkspaceContract(unittest.TestCase):
    """Fresh Release 의 Clean 은 git clean 이 아니다."""

    def test_clean_targets_are_release_scoped(self) -> None:
        clean = authority()["cleanWorkspace"]
        for target in ("approvedGeneratedReleaseRoot", "isolatedStagingWorkspace"):
            self.assertIn(target, clean["allowedTargets"], f"허용 Clean 대상이 없다: {target}")

    def test_broad_destructive_git_commands_are_forbidden(self) -> None:
        forbidden = set(authority()["cleanWorkspace"]["forbiddenCommands"])
        for command in ("git clean", "git reset --hard", "git restore ."):
            self.assertIn(command, forbidden, f"광범위 destructive 명령이 금지되지 않았다: {command}")

    def test_protected_paths_cover_repository_authority(self) -> None:
        protected = set(authority()["cleanWorkspace"]["protectedPaths"])
        for path in (".git", ".github", ".gitignore", ".gitattributes", ".editorconfig"):
            self.assertIn(path, protected, f"보호 경로가 빠졌다: {path}")

    def test_previous_release_residue_is_not_allowed(self) -> None:
        self.assertFalse(authority()["cleanWorkspace"]["previousResidueAllowed"],
                         "이전 Release 잔여물이 새 Release 에 살아남는 것을 허용한다")

    def test_prerequisites_run_before_destruction(self) -> None:
        self.assertTrue(authority()["cleanWorkspace"]["prerequisitesBeforeDestruction"],
                        "전제조건 확인 전에 직전 Release 를 지우는 계약이다")

    def test_publisher_never_targets_a_tracked_or_public_tree(self) -> None:
        routing = authority()["publicationRouting"]
        for target in ("trackedReleaseTree", "openGitWorkingTree"):
            self.assertIn(target, routing["forbiddenDirectTargets"],
                          f"발행 대상 금지 목록에 없다: {target}")
        self.assertEqual(["isolatedStaging", "verification", "promotionOrProjection"],
                         routing["canonicalOrder"], "발행 경로 순서가 정본과 다르다")


class GitIgnoreContract(unittest.TestCase):
    """generated/binary 라는 이유만으로 일괄 제외하지 않는다."""

    def test_gitignore_never_blanket_excludes_release_artifacts(self) -> None:
        if not GITIGNORE.is_file():
            self.skipTest(".gitignore 가 없다")
        offenders: list[str] = []
        for raw in read(GITIGNORE).splitlines():
            line = raw.strip()
            if not line or line.startswith("#") or line.startswith("!"):
                continue
            normalized = line.rstrip("/").lstrip("/")
            if line in {"*.jar", "**/*.jar", "*.zip", "**/*.zip"}:
                offenders.append(line)
            elif normalized in {"cpf-release", "cpf-release/**",
                                "cpf-release/binary-repository", "cpf-release/binary-repository/**",
                                "cpf-release/reports", "cpf-release/reports/**"}:
                # 뿌리째 제외하면 POM/checksum/manifest 같은 Current Release Metadata 까지 사라진다.
                offenders.append(line)
        self.assertEqual([], offenders,
                         f"Current Verified Release Artifact 까지 일괄 제외한다: {offenders}")

    def test_transient_output_is_declared(self) -> None:
        examples = authority()["classes"][TRANSIENT]["examples"]
        for name in ("build", "cache", "temporary staging"):
            self.assertIn(name, examples, f"Transient 예시가 빠졌다: {name}")


class ReleaseGateStaging(unittest.TestCase):
    def test_development_and_final_gates_are_separated(self) -> None:
        model = authority()
        self.assertEqual("IMPACT_TARGETED", model["developmentGate"],
                         "개발 중에도 전체 Gate 를 반복하는 계약이다")
        self.assertEqual("FULL_FRESH_ONCE", model["finalReleaseCandidateGate"],
                         "최종 Release Candidate 의 전체 Fresh 계약이 없다")

    def test_final_order_builds_before_projection_and_ends_with_evidence(self) -> None:
        order = authority()["finalReleaseCandidateOrder"]
        for stage in ("sourceFreeze", "cleanReleaseWorkspace", "freshBuild", "freshPublication",
                      "canonicalPublicSourceProjection", "freshBinaryRepository", "freshSbom",
                      "freshChecksum", "freshManifest", "leakageZero",
                      "gitLfsMaterializationAndManifestCorrelation",
                      "trackedResultComparisonAndCurrentization", "freshConsumer", "evidence"):
            self.assertIn(stage, order, f"최종 순서에 단계가 없다: {stage}")
        self.assertLess(order.index("cleanReleaseWorkspace"), order.index("freshBuild"),
                        "Clean Workspace 확보보다 Build 가 먼저다")
        self.assertLess(order.index("freshBuild"), order.index("canonicalPublicSourceProjection"),
                        "투영이 Fresh Build 보다 먼저 온다")
        self.assertLess(order.index("freshConsumer"),
                        order.index("evidence"), "Evidence 가 Consumer 실행보다 먼저다")
        self.assertLess(order.index("trackedResultComparisonAndCurrentization"),
                        order.index("freshConsumer"),
                        "tracked 비교/현행화가 Consumer 뒤로 밀렸다")

    def test_both_acceptance_statements_must_hold(self) -> None:
        acceptance = authority()["acceptance"]
        self.assertTrue(acceptance["bothMustHold"],
                        "두 Acceptance 중 하나만 만족해도 되는 계약이다")
        self.assertIn("Development Master checkout",
                      acceptance["masterCheckoutShowsCurrentDeliverable"])
        self.assertIn("완전히 배제해도", acceptance["freshReleaseWithoutTrackedResult"])

    def test_working_tree_keeps_only_the_current_release(self) -> None:
        forbidden = authority()["currentOnly"]["forbiddenSiblingPatterns"]
        self.assertTrue(forbidden, "과거 Release 사본 금지 패턴이 없다")
        present = [name for name in forbidden if (REPO_ROOT / name).exists()]
        self.assertEqual([], present, f"과거 Release 사본이 Working Tree 에 있다: {present}")


class ArtifactClassificationContract(unittest.TestCase):
    """분류 없는 Artifact 는 통과시키지 않는다."""

    def classification(self) -> dict:
        model = authority().get("artifactClassification")
        if not model:
            raise AssertionError("Release Artifact 분류 정본이 없다")
        return model

    def test_unknown_artifact_fails_closed(self) -> None:
        self.assertEqual("FAIL_CLOSED", self.classification()["unknownArtifact"],
                         "분류가 없는 Artifact 를 통과시킨다")

    def test_classification_is_derived_from_runtime_metadata(self) -> None:
        derived = self.classification()["derivedFrom"]
        self.assertIn("cpf-runtime-target-catalog.json", derived["authority"],
                      "분류가 canonical Runtime metadata 에서 파생되지 않는다")
        self.assertIn("provision=binary", derived["selector"],
                      "Public Binary Runtime 실행물이라는 성격으로 분류하지 않는다")

    def test_tracked_manifest_records_every_required_field(self) -> None:
        columns = set(authority()["artifactClassification"]["trackedManifestColumns"])
        for field in ("artifact_path", "coordinate", "version", "size_bytes", "sha256",
                      "source_identity", "public_release", "asset_class", "transport",
                      "lfs_required"):
            self.assertIn(field, columns, f"tracked manifest 항목이 빠졌다: {field}")

    def test_untracked_artifact_is_still_publicly_delivered(self) -> None:
        """Master 미보존이 공개 배포 누락으로 이어지면 Consumer 가 실행물을 얻지 못한다."""
        for rule in self.classification()["rules"]:
            if rule.get("assetClass") != "UNTRACKED_RELEASE_RESULT":
                continue
            self.assertTrue(rule.get("publicRelease"),
                            f"{rule['id']}: Master 미보존이 공개 배포 제외로 연결된다")

    def test_git_lfs_is_canonical_transport_for_catalog_binary_runtimes(self) -> None:
        lfs = authority()["artifactClassification"]["gitLfs"]
        self.assertTrue(lfs["adopted"], "확정된 Git LFS transport를 정본화하지 않았다")
        self.assertEqual(LARGE_BINARY, lfs["largeArtifactClass"])
        self.assertEqual(".gitattributes", lfs["attributeFile"])
        self.assertEqual("git-lfs", lfs["requiredCommand"])
        self.assertTrue(lfs["materializationRequiredBeforeRuntime"])
        for code in ("GIT_LFS_NOT_AVAILABLE", "LFS_OBJECT_NOT_MATERIALIZED",
                     "LFS_DOWNLOAD_FAILED", "LFS_HASH_MISMATCH",
                     "RELEASE_MANIFEST_MISMATCH", "RUNTIME_ARTIFACT_INVALID"):
            self.assertIn(code, lfs["failClosedCodes"], f"LFS failure code가 없다: {code}")

        rule = next(rule for rule in self.classification()["rules"]
                    if rule["id"] == "publicBinaryRuntimeExecutable")
        self.assertEqual(LARGE_BINARY, rule["assetClass"],
                         "binary runtime must use the canonical GIT_LFS class")
        self.assertIs(True, rule["masterTracked"], "GIT_LFS runtime must remain Master tracked")
        self.assertIs(True, rule["publicRelease"], "GIT_LFS runtime must remain publicly delivered")
        self.assertEqual("GIT_LFS", rule["transport"])
        self.assertEqual("runtimeTargetCatalog.binaryProvisionArtifactIds", rule["selection"])

    def test_lfs_attributes_are_exactly_catalog_derived_and_metadata_stays_regular_git(self) -> None:
        self.assertTrue(GITATTRIBUTES.is_file(), ".gitattributes가 없다")
        lines = [line.strip() for line in read(GITATTRIBUTES).splitlines()
                 if line.strip() and not line.lstrip().startswith("#")]
        lfs_lines = [line for line in lines if "filter=lfs" in line]
        expected = {
            f"cpf-release/binary-repository/com/cpf/runtime/{artifact_id}/*/{artifact_id}-*.jar "
            "filter=lfs diff=lfs merge=lfs -text"
            for artifact_id in binary_runtime_artifact_ids()
        }
        self.assertEqual(expected, set(lfs_lines),
                         "LFS scope는 runtime catalog executable만 exact하게 따라야 한다")
        forbidden = {"*.jar", "**/*.jar", "cpf-release/binary-repository/**/*.jar"}
        lfs_patterns = {line.split(maxsplit=1)[0] for line in lfs_lines}
        self.assertEqual(set(), lfs_patterns.intersection(forbidden),
                         "전역/경로 glob LFS가 library 또는 metadata까지 넓힌다")
        self.assertFalse(any(".pom" in line or ".json" in line or ".sha" in line
                             for line in lfs_lines),
                         "POM/manifest/checksum/report는 regular Git이어야 한다")

    def test_lfs_validator_is_a_release_gate_not_a_document_only_rule(self) -> None:
        self.assertTrue(LFS_VERIFIER.is_file(), "LFS attribute/materialization validator가 없다")
        text = read(LFS_VERIFIER)
        for required in ("GIT_LFS_NOT_AVAILABLE", "LFS_OBJECT_NOT_MATERIALIZED",
                         "LFS_HASH_MISMATCH", "RELEASE_MANIFEST_MISMATCH",
                         "RUNTIME_ARTIFACT_INVALID", "git-lfs"):
            self.assertIn(required, text, f"LFS validator의 fail-closed contract가 빠졌다: {required}")


class PayloadCompositionContract(unittest.TestCase):
    """Release 크기는 payload composition 으로 판정한다. 파일 크기 하나로 결론내지 않는다."""

    COMPOSITION_TOOL = "cpf-tools/release/open-git/report_release_payload_composition.py"
    REQUIRED_FIELDS = (
        "cpfAuthoredBinaryBytes", "ossSeparatelyVendoredBytes", "ossEmbeddedInFatJarBytes",
        "duplicateEmbeddedDependencyBytes", "metadataBytes", "docsBytes",
    )

    def test_composition_tool_exists(self) -> None:
        self.assertTrue((REPO_ROOT / self.COMPOSITION_TOOL).is_file(),
                        "payload composition 측정 도구가 없다")

    def test_tool_separates_authored_from_embedded_and_vendored(self) -> None:
        text = read(REPO_ROOT / self.COMPOSITION_TOOL)
        for field in self.REQUIRED_FIELDS:
            self.assertIn(field, text, f"보고 항목이 빠졌다: {field}")

    def test_tool_measures_duplicate_embedded_dependencies(self) -> None:
        text = read(REPO_ROOT / self.COMPOSITION_TOOL)
        self.assertIn("duplicateBytes", text,
                      "같은 dependency 가 여러 fat JAR 에 반복 포함되는 양을 재지 않는다")

    def test_tool_never_changes_packaging(self) -> None:
        """측정 도구가 포장 방식을 바꾸면 분석이 아니라 변경이다."""
        text = read(REPO_ROOT / self.COMPOSITION_TOOL)
        for forbidden in ("shutil.move", "os.remove", "unlink(", "write_bytes("):
            self.assertNotIn(forbidden, text,
                             f"측정 도구가 Artifact 를 변경한다: {forbidden}")

    def test_harness_requires_composition_before_a_size_decision(self) -> None:
        text = read(HARNESS)
        self.assertIn("payload composition", text,
                      "Release Size Finding 을 payload composition 으로 판정하라는 Rule 이 없다")
        for field in ("OSS separately vendored bytes", "duplicate embedded dependency estimated bytes",
                      "avoidable bytes"):
            self.assertIn(field, text, f"Harness 보고 항목이 빠졌다: {field}")

    def test_packaging_change_is_a_product_decision(self) -> None:
        text = read(HARNESS)
        self.assertIn("thin JAR / fat JAR Architecture", text,
                      "포장 방식 변경이 Product Contract 결정이라는 선언이 없다")


class HarnessAndRegistryRelation(unittest.TestCase):
    def test_harness_declares_the_release_asset_rule(self) -> None:
        text = read(HARNESS)
        self.assertIn("### 39.2 영구 Rule", text,
                      "Current Harness 에 Release Asset 계약이 없다")
        self.assertIn("Release Asset", text,
                      "Current Harness 에 Release Asset 계약이 없다")
        for name in (CANONICAL, TRACKED, UNTRACKED, TRANSIENT, LARGE_BINARY):
            self.assertIn(name, text, f"Harness 에 부류 선언이 없다: {name}")
        self.assertIn("projection", text.lower(),
                      "Open Git tree 가 투영 결과라는 계약이 Harness 에 없다")

    def test_registry_links_the_rule_to_this_validator(self) -> None:
        self.assertTrue(WORK_ITEM_REGISTRY.is_file(), "Work Item Registry 가 없다")
        self.assertIn("test_cpf_release_asset_freshness_contract", read(WORK_ITEM_REGISTRY),
                      "Registry 가 이 계약 Validator 를 참조하지 않는다")


if __name__ == "__main__":
    unittest.main(verbosity=2)
