"""CPF Gradle Task Explorer 사용자 가독성 계약.

핵심 원칙
- Task ID 는 CI/Script/CLI/Public Consumer 계약이므로 영어 내부 ID 를 유지한다.
- 사용자에게 보이는 group/description 만 한글 업무 의미로 관리한다.
- 사용자 진입점은 00~60에만 노출하고 내부 orchestration/gate/원시 명령은 90~99에서 용도별로 분리한다.

회귀 이력
- commit 5f2289d4(2026-08-29) 에서 번호 정렬 한글 group 이 영어 raw group 으로 퇴행했고,
  내부 publication/gate task 가 사용자 그룹에 대량 유입되었다. 그 재발을 fail-closed 로 막는다.
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
PUBLIC_TEMPLATE = ROOT / "cpf-tools/release/public/templates/build.gradle"
OPEN_GIT_TEMPLATE = ROOT / "cpf-tools/release/open-git/templates/build.gradle"
VSCODE_SETTINGS = ROOT / ".vscode/settings.json"

# 번호 정렬형 canonical 사용자 그룹. 자주 쓰는 작업이 앞, 운영/배포가 뒤에 온다.
CANONICAL_GROUPS = {
    "00. CPF 시작", "10. CPF 빌드", "15. CPF 테스트", "20. CPF 검증", "30. CPF 실행",
    "40. CPF 구성", "50. CPF 설정", "60. CPF 배포",
}
INTERNAL_GROUPS = {
    "90. CPF 내부 빌드", "91. CPF 내부 검증 Gate", "92. CPF 내부 테스트",
    "93. CPF 내부 실행", "94. CPF 내부 배포", "95. CPF 내부 IDE",
    "96. CPF 내부 진단",
    "98. CPF 구명령 호환 유지",
    "99. CPF 원시 명령 직접 사용 안 함",
}
# 사용자 그룹에 절대 노출하면 안 되는 내부 orchestration / verification task
INTERNAL_TASKS = {
    "aggregateQualityBuild", "compositeModuleIdentityGate", "corePomPurityGate",
    "cpfPrepareIdeClasspath", "cpfResourcePolicyGate", "cpfVerifyIdeClasspathModel",
    "cpfVerifyIdeClasspathReady", "gradleProjectDependencyClosureTest", "managedDependencyGate",
    "publicationGate", "qa34IntegrationTest", "qualityGate",
    "cpfPublishAllToArtifactStaging", "cpfPublishPublicToArtifactStaging",
    "cpfProjectPublicRuntimeAliases", "generateCpfRuntimeCapabilityMetadata",
    # publication orchestration 은 배포 진입점이 아니라 내부 구현이다.
    "cpfPublishAllToIsolatedLocal", "cpfPublishAllVerifiedLocalPlatformArtifacts",
}
# 배포 그룹에 고정 등록되는 전체 진입점. 개별 배포는 App/Domain 발견 결과로 투영된다.
DEPLOY_ENTRYPOINTS = {"cpfDeployAll"}
DIRECT_ROOT_ENTRYPOINT_GROUPS = {
    "cpfHelp": "00. CPF 시작",
    "cpfBuildAll": "10. CPF 빌드",
    "cpfTestAll": "15. CPF 테스트",
    "cpfVerifyFast": "20. CPF 검증",
    "cpfVerifyTargeted": "20. CPF 검증",
    "cpfVerifyAllLocal": "20. CPF 검증",
    "cpfDomainDiscovery": "40. CPF 구성",
    "cpfDatabaseDiscovery": "40. CPF 구성",
    "cpfModules": "40. CPF 구성",
    "cpfModulesAll": "40. CPF 구성",
    "cpfResourcePolicy": "50. CPF 설정",
    "cpfDeployAll": "60. CPF 배포",
    "cpfTargets": "40. CPF 구성",
}
# 전체 대상 명령은 이름에 All 을 두어 개별 대상과 한눈에 구분한다.
ALL_SCOPED_ENTRYPOINTS = {
    "cpfBuildAll", "cpfTestAll", "cpfVerifyAllLocal", "cpfModulesAll", "cpfDeployAll",
    "cpfRunAllBatch",
}
# Canonical Target Catalog 가 Build/Test/Run 에 함께 투영하는 논리 Target 진입점.
TARGET_SCOPED_ENTRYPOINTS = {
    "cpfBuildDev", "cpfBuildOnline", "cpfTestDev", "cpfTestOnline",
    "cpfRunDevLocal", "cpfRunOnlineLocal",
}
PUBLIC_TEMPLATE_ENTRYPOINT_GROUPS = {
    "cpfBuildAll": "10. CPF 빌드",
    "cpfTestAll": "15. CPF 테스트",
    "cpfVerifyAll": "20. CPF 검증",
    "cpfRuntimeDiscovery": "30. CPF 실행",
    "cpfDomainDiscovery": "40. CPF 구성",
    "cpfDatabaseDiscovery": "40. CPF 구성",
    "cpfWorkspace": "50. CPF 설정",
    "cpfPublicationDiscovery": "60. CPF 배포",
}
GROUP_PATTERN = re.compile(r"group\s*=\s*'([^']*)'")


def _task_group_map(text: str) -> dict[str, str]:
    """convention 에서 task -> group 매핑을 추출한다.

    group 선언은 등록 블록 안에서만 유효하다. 블록 밖까지 찾으면 group 을 선언하지 않는
    내부 task 가 뒤따르는 task 의 group 을 물려받아 잘못된 판정이 난다.
    """
    mapping: dict[str, str] = {}
    lines = text.split("\n")
    for index, line in enumerate(lines):
        m = re.search(r"tasks\.register\(\s*'([^']+)'", line)
        if not m:
            continue
        # 등록 블록은 보통 몇 줄 안에서 group 을 선언한다. 다음 등록 전까지만 본다.
        for candidate in lines[index:index + 8]:
            if candidate is not line and re.search(r"tasks\.register\(\s*'", candidate):
                break
            g = GROUP_PATTERN.search(candidate)
            if g:
                mapping[m.group(1)] = g.group(1)
                break
    return mapping


def _product_build_files() -> list[Path]:
    files = []
    for p in ROOT.rglob("build.gradle"):
        parts = p.relative_to(ROOT).parts
        # cpf-release 는 generated 릴리즈 산출물, build/ 는 Gradle output 이므로 제외한다.
        if "cpf-release" in parts or "build" in parts[:-1] or "node_modules" in parts:
            continue
        files.append(p)
    files.append(CONVENTION)
    return files


def test_no_english_or_legacy_group_regression():
    violations = []
    for p in _product_build_files():
        for value in GROUP_PATTERN.findall(p.read_text(encoding="utf-8")):
            if value.startswith("com.cpf") or value in {"build", "publishing"}:
                continue  # project coordinate / Gradle 표준 lifecycle 은 대상이 아니다
            if value not in CANONICAL_GROUPS | INTERNAL_GROUPS:
                violations.append(f"{p.relative_to(ROOT).as_posix()}: {value}")
    assert not violations, "canonical CPF 그룹 밖 group: " + "; ".join(sorted(set(violations)))


def test_internal_tasks_are_not_exposed_to_user_groups():
    mapping = _task_group_map(CONVENTION.read_text(encoding="utf-8"))
    leaked = sorted(t for t in INTERNAL_TASKS if mapping.get(t) in CANONICAL_GROUPS)
    assert not leaked, f"내부 task 가 사용자 그룹에 노출됨: {leaked}"


def test_deploy_group_exposes_only_whole_and_single_entrypoints():
    mapping = _task_group_map(CONVENTION.read_text(encoding="utf-8"))
    deploy = {t for t, g in mapping.items() if g == "60. CPF 배포"}
    assert deploy == DEPLOY_ENTRYPOINTS, f"배포 진입점이 전체/개별 2개가 아님: {sorted(deploy)}"


def test_user_tasks_have_korean_description():
    text = CONVENTION.read_text(encoding="utf-8")
    mapping = _task_group_map(text)
    missing = []
    for task, group in mapping.items():
        if group not in CANONICAL_GROUPS:
            continue
        block = text.split(f"tasks.register('{task}'", 1)
        if len(block) < 2:
            continue
        head = block[1][:600]
        desc = re.search(r"description\s*=\s*'([^']*)'", head)
        if not desc or not re.search(r"[가-힣]", desc.group(1)):
            missing.append(task)
    assert not missing, f"사용자 task 의 한글 description 누락: {sorted(missing)}"


def test_task_ids_remain_english_contract():
    ids = re.findall(r"tasks\.register\(\s*'([^']+)'", CONVENTION.read_text(encoding="utf-8"))
    assert ids, "convention 에 등록된 task 가 없다"
    korean = [t for t in ids if re.search(r"[가-힣]", t)]
    assert not korean, f"Task ID 가 한글화되었다(Consumer 계약 위반): {korean}"


def test_canonical_deploy_entrypoint_exists():
    # 배포 진입점이 사라지면 사용자가 배포를 시작할 방법이 없어진다.
    text = CONVENTION.read_text(encoding="utf-8")
    for task in DEPLOY_ENTRYPOINTS:
        assert f"tasks.register('{task}'" in text, f"배포 진입점 누락: {task}"


def test_root_user_entrypoint_groups_are_exact_and_stable():
    text = CONVENTION.read_text(encoding="utf-8")
    mapping = _task_group_map(text)
    assert {
        task: mapping.get(task) for task in DIRECT_ROOT_ENTRYPOINT_GROUPS
    } == DIRECT_ROOT_ENTRYPOINT_GROUPS
    assert "tasks.register(taskName) { group = '30. CPF 실행'" in text
    # Batch 통합 Runtime 만 별도 진입점이고, ALL/DEV/ONLINE 은 Target Catalog 가 투영한다.
    assert "registerCpfRunAlias('cpfRunAllBatch'," in text, "통합 Batch 실행 진입점 누락"
    for task in sorted(TARGET_SCOPED_ENTRYPOINTS):
        assert f"'{task}'" in text or task.replace("cpfBuild", "").replace("cpfTest", "") in text


def test_public_and_open_git_templates_keep_the_same_group_projection():
    for template in (PUBLIC_TEMPLATE, OPEN_GIT_TEMPLATE):
        mapping = _task_group_map(template.read_text(encoding="utf-8"))
        assert {
            task: mapping.get(task) for task in PUBLIC_TEMPLATE_ENTRYPOINT_GROUPS
        } == PUBLIC_TEMPLATE_ENTRYPOINT_GROUPS, template.relative_to(ROOT).as_posix()


def test_cpf_help_names_only_the_canonical_numbered_groups():
    text = CONVENTION.read_text(encoding="utf-8")
    expected = "00 시작 / 10 빌드 / 15 테스트 / 20 검증 / 30 실행 / 40 구성 / 50 설정 / 60 배포"
    assert expected in text
    assert "CPF Build/Test/Domain/Database/Runtime/Verification/Publication/Configuration-Discovery" not in text


def test_all_scoped_user_commands_say_all_and_legacy_names_are_internal_aliases():
    text = CONVENTION.read_text(encoding="utf-8")
    for task in sorted(ALL_SCOPED_ENTRYPOINTS):
        registered = (f"tasks.register('{task}'" in text
                      or f"registerCpfRunAlias('{task}'," in text
                      or f"task: '{task}'" in text)
        assert registered, f"전체 대상 진입점 누락: {task}"
    # 예전 이름은 한 곳(98)에 모으고, 새 이름을 가리키는 별칭 계약으로만 유지한다.
    assert "def cpfLegacyTaskAliases = [" in text
    assert "return cpfInternalTaskGroups.compat" in text
    for legacy, canonical in _legacy_aliases().items():
        # 호환 별칭은 전체 대상 진입점이나 개별 실행, 또는 내부로 옮긴 publication 을 가리킨다.
        assert (canonical in ALL_SCOPED_ENTRYPOINTS
                or canonical.startswith("cpfRun")
                or canonical.startswith("cpfPublishAll")), legacy
        assert f"tasks.register('{legacy}'" not in text, f"호환 별칭이 개별 등록으로 흩어짐: {legacy}"


def test_every_non_user_task_is_classified_by_purpose_after_all_projects_are_evaluated():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "gradle.projectsEvaluated {\n    allprojects { target ->" in text
    assert "def visibleRootTask = target == rootProject && cpfVisibleTaskNames.contains(task.name)" in text
    assert "task.group = cpfInternalTaskGroupFor(task.name)" in text
    for group in INTERNAL_GROUPS:
        assert group in text
    assert "[내부 빌드/호환] Gradle 표준 전체 Build" in text
    assert "[원시 명령] Gradle 또는 Plugin의 저수준 명령" in text
    assert "tasks.register('cpfTaskGroupContractGate')" in text
    assert "tasks.named('qualityGate') { dependsOn tasks.named('cpfTaskGroupContractGate') }" in text
    assert "CPF_TASK_GROUP_CONTRACT=PASS" in text


def test_vscode_gradle_view_stays_on_the_single_root_and_refreshable_without_reload():
    settings = json.loads(VSCODE_SETTINGS.read_text(encoding="utf-8"))
    assert settings["gradle.nestedProjects"] is False
    assert settings["gradle.reuseTerminals"] is True
    cache_arg = settings["java.import.gradle.arguments"]
    assert "C:\\" not in cache_arg
    assert "cpf-docs/governance/development-harness/evidence/platform/current/generated/gradle/project-cache" in cache_arg


# --- 생성형 Domain 자동 등록/삭제 계약 -------------------------------------------------

def _discovered_domains() -> list[dict]:
    """convention 과 동일한 규칙으로 Domain 을 발견한다(이름 하드코딩 없음)."""
    found = []
    for d in sorted(ROOT.iterdir()):
        if not d.is_dir():
            continue
        pf = d / "gradle.properties"
        if not pf.is_file():
            continue
        props = {}
        for line in pf.read_text(encoding="utf-8-sig").splitlines():
            if "=" in line and not line.strip().startswith("#"):
                k, v = line.split("=", 1)
                props[k.strip()] = v.strip()
        if props.get("cpf.domain.contractVersion") != "1":
            continue
        name = props.get("cpf.domain.name", "").strip()
        if not name:
            continue
        st = d / "settings.gradle"
        text = st.read_text(encoding="utf-8") if st.is_file() else ""
        modules = [m for m in ("online", "batch")
                   if f"include '{m}'" in text and (d / m).is_dir()]
        if modules:
            found.append({"name": name, "modules": modules})
    return found


def test_domain_tasks_are_projected_from_contract_not_hardcoded_names():
    text = CONVENTION.read_text(encoding="utf-8")
    # 계약 기반 발견이어야 생성기로 만들면 자동 등록되고 삭제하면 자동 사라진다.
    assert "cpf.domain.contractVersion" in text
    assert "cpfDiscoveredDomains" in text
    for domain in _discovered_domains():
        # Domain 이름이 convention 에 문자열로 박혀 있으면 자동 소멸이 보장되지 않는다.
        assert f"'{domain['name']}'" not in text, f"Domain 이름 하드코딩: {domain['name']}"


def test_every_domain_has_the_same_four_axes():
    text = CONVENTION.read_text(encoding="utf-8")
    for axis in ('"cpfBuild${d.cap}"', '"cpfVerify${d.cap}"', '"cpfDeploy${d.cap}"',
                 '"cpfRun${d.cap}${suffix}"'):
        assert axis in text, f"Domain 축 누락: {axis}"
    # 4축이 각각 사용자 그룹으로 투영되어야 한다.
    for group in ("'10. CPF 빌드'", "'20. CPF 검증'", "'30. CPF 실행'", "'60. CPF 배포'"):
        assert group in text


def test_standalone_domain_axes_use_process_isolation_with_current_source():
    """독립 Domain이 parent Gradle build를 재-include하지 않고 현재 checkout을 소비한다."""
    text = CONVENTION.read_text(encoding="utf-8")
    # GradleBuild는 parent build에서 Domain settings의 includeBuild(root)를 중첩해 실제 실행이 깨진다.
    assert "GradleBuild domainTask" not in text
    assert "configureCpfStandaloneDomainTask = { Exec domainTask" in text
    assert "cpfDomainGradleWrapper" in text
    assert "'--project-dir', domainDir.canonicalFile.absolutePath" in text
    assert "cpfProductCompositeRoot" in text
    # 독립 Domain은 Process boundary를 쓰고, root에 mount된 Domain은 같은 논리 project를 쓴다.
    assert "if (d.mounted)" in text
    assert 'dependsOn "${d.mountedPath}:build"' in text
    assert 'dependsOn "${d.mountedPath}:test"' in text
    for task in ("cpfInternalBuild${d.cap}${suffix}", "cpfInternalTest${d.cap}${suffix}",
                 "cpfBuild${d.cap}", "cpfVerify${d.cap}", "cpfRun${d.cap}${suffix}",
                 "cpfDeploy${d.cap}"):
        assert f'tasks.register("{task}"' in text


def test_all_build_and_test_use_the_same_resolved_domain_projection():
    """전체 build/test가 root subproject만 보고 독립 Generated Domain을 누락하면 안 된다."""
    text = CONVENTION.read_text(encoding="utf-8")
    assert "def cpfAllResolvedTarget = cpfResolveTarget('ALL')" in text
    assert "def cpfAllDomainBuildTasks" in text
    assert "def cpfAllDomainTestTasks" in text
    assert "dependsOn cpfAllDomainBuildTasks" in text
    assert "dependsOn cpfAllDomainTestTasks" in text


def test_batch_axis_only_when_contract_declares_batch():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "include 'batch'" in text and "include 'online'" in text
    domains = _discovered_domains()
    if domains:
        # 최소 한 Domain 은 online 을 가져야 현재 계약이 유효하다.
        assert any("online" in d["modules"] for d in domains)


def test_mounted_domain_does_not_duplicate_run_entrypoint():
    text = CONVENTION.read_text(encoding="utf-8")
    # root build 에 mount 된 online 은 독립 build 를 다시 띄우지 않고 suffix 없는 한 이름만 쓴다.
    quote = chr(39)
    marker = "def runName = (m == " + quote + "online" + quote + " && d.mounted)"
    assert marker in text
    assert 'dependsOn "${d.mountedPath}:bootRun"' in text


# --- App/Domain 4축 일관성 및 이름 규칙 계약 --------------------------------------

def _legacy_aliases() -> dict[str, str]:
    """convention 이 선언한 호환 별칭 계약을 읽는다."""
    text = CONVENTION.read_text(encoding="utf-8")
    block = text.split("def cpfLegacyTaskAliases = [", 1)[1].split("]", 1)[0]
    return dict(re.findall(r"'([^']+)'\s*:\s*'([^']+)'", block))


def _discovered_apps() -> list[str]:
    """convention 과 동일한 구조 규칙으로 개별 App 을 발견한다(이름 하드코딩 없음).

    최상위 cpf-* 디렉터리이면서 Spring Boot 실행 계약을 가진 project 만 개별 App 이다.
    cpf-tools/runtime/** 통합 Runtime 과 Domain 소유 module 은 상위 디렉터리가 달라 제외된다.
    """
    settings = (ROOT / "settings.gradle").read_text(encoding="utf-8")
    mounted = set(re.findall(r"projectDir\s*=\s*file\('([^']+)'\)", settings))
    apps = []
    for rel in sorted(mounted):
        d = ROOT / rel
        if d.parent != ROOT or not d.is_dir():
            continue
        bf = d / "build.gradle"
        if not bf.is_file() or "id 'org.springframework.boot'" not in bf.read_text(encoding="utf-8"):
            continue
        raw = d.name[4:] if d.name.startswith("cpf-") else d.name
        apps.append("".join(p[:1].upper() + p[1:] for p in raw.split("-") if p))
    return apps


def test_app_discovery_is_structural_and_not_name_hardcoded():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "cpfDiscoveredApps" in text
    assert "dir.parentFile != rootDir" in text
    assert "id 'org.springframework.boot'" in text
    for cap in _discovered_apps():
        assert f"'{cap}'" not in text, f"App 이름 하드코딩: {cap}"


def test_every_app_has_the_same_four_axes():
    text = CONVENTION.read_text(encoding="utf-8")
    for axis in ('"cpfBuild${a.cap}"', '"cpfVerify${a.cap}"',
                 '"cpfRun${a.cap}"', '"cpfDeploy${a.cap}"'):
        assert axis in text, f"App 축 누락: {axis}"


def test_build_verify_run_deploy_axes_cover_every_app_and_domain():
    """빌드/검증/실행/배포 네 축이 App 과 Domain 전부에 대칭으로 존재해야 한다."""
    text = CONVENTION.read_text(encoding="utf-8")
    for holder in ("a.cap", "d.cap"):
        for axis in ("cpfBuild", "cpfVerify", "cpfDeploy"):
            assert '"%s${%s}"' % (axis, holder) in text, f"{axis} 축이 {holder} 에 없음"
    assert '"cpfRun${a.cap}"' in text
    assert '"cpfRun${d.cap}"' in text or '"cpfRun${d.cap}${suffix}"' in text
    # 전체 배포는 발견된 App/Domain 을 모두 모아야 한다.
    assert 'dependsOn cpfDiscoveredApps.collect { "cpfDeploy${it.cap}".toString() }' in text
    assert 'dependsOn cpfDiscoveredDomains.collect { "cpfDeploy${it.cap}".toString() }' in text


def test_user_entrypoint_descriptions_declare_scope():
    """사용자 명령 설명은 전체/개별/조회 중 무엇인지 먼저 밝혀야 가독성이 유지된다."""
    text = CONVENTION.read_text(encoding="utf-8")
    mapping = _task_group_map(text)
    allowed = ("[전체", "[개발", "[온라인", "[배치", "[개별", "[조회", "[안내", "[선택")
    missing = []
    for task, group in mapping.items():
        if group not in CANONICAL_GROUPS:
            continue
        head = text.split(f"tasks.register('{task}'", 1)
        if len(head) < 2:
            continue
        desc = re.search(r"description\s*=\s*'([^']*)'", head[1][:600])
        if desc and not desc.group(1).startswith(allowed):
            missing.append(task)
    assert not missing, f"사용자 명령 설명에 범위 표기 누락: {sorted(missing)}"


def test_legacy_alias_targets_exist_as_user_entrypoints():
    """예전 이름이 사라진 명령을 가리키면 죽은 진입점이 된다."""
    text = CONVENTION.read_text(encoding="utf-8")
    dynamic = {"cpfRun" + cap for cap in _discovered_apps()}
    for legacy, canonical in _legacy_aliases().items():
        ok = (f"tasks.register('{canonical}'" in text
              or f"registerCpfRunAlias('{canonical}'," in text
              or f"task: '{canonical}'" in text
              or canonical in dynamic)
        assert ok, f"호환 별칭 대상이 존재하지 않음: {legacy} -> {canonical}"
    # 내부로 옮긴 publication 도 예전 이름으로는 계속 호출할 수 있어야 한다.
    assert "cpfInternalPublicationTasks" in text


def test_build_test_run_share_one_target_resolver():
    """ALL/DEV/ONLINE 정의가 축마다 갈라지면 같은 이름이 다른 대상을 뜻하게 된다."""
    text = CONVENTION.read_text(encoding="utf-8")
    assert "def cpfTargetCatalog = [" in text
    assert "def cpfResolveTarget = { String targetName ->" in text
    for target in ("ALL:", "DEV:", "ONLINE:"):
        assert target in text
    # Build/Test 는 Resolver 결과를, Run 은 같은 Catalog 의 runtimeMode 를 소비한다.
    assert text.count("cpfResolveTarget(") >= 2
    assert "cpfTargetCatalog[entry.target]" in text
    # Runtime 은 Local Runtime 이 이미 소유한 mode 계약을 재사용한다.
    for mode in ("runtimeMode: 'full'", "runtimeMode: 'standard'", "runtimeMode: 'minimal'"):
        assert mode in text


def test_target_membership_is_actually_different():
    """DEV 가 ALL 과 같은 대상이면 별도 진입점을 만들 이유가 없다."""
    text = CONVENTION.read_text(encoding="utf-8")
    assert "excludedRoles: [] as Set" in text
    assert "excludedRoles: ['gateway'] as Set" in text
    assert "excludedRoles: ['gateway', 'admin', 'backoffice-web'] as Set" in text
    # Batch capability 가 없는 Domain 에 Batch Task 를 만들지 않는다.
    assert "cpfBatchDomainModules.isEmpty()" in text


def test_resolved_target_is_shown_to_the_user():
    text = CONVENTION.read_text(encoding="utf-8")
    assert "tasks.register('cpfTargets')" in text
    assert "CPF TARGET =" in text


def test_backoffice_domain_and_web_are_independent_components():
    """Backoffice Domain 과 Web Frontend 는 서로 다른 Lifecycle 을 가진다.

    Domain 은 cpf.domain.contractVersion 계약으로, Web 은 최상위 Spring Boot App 구조 규칙으로
    각각 발견된다. 한쪽이 사라져도 다른 쪽 발견 경로는 영향을 받지 않아야 한다.
    """
    text = CONVENTION.read_text(encoding="utf-8")
    # 두 발견 경로가 분리되어 있어야 한 쪽 삭제가 다른 쪽을 끌고 가지 않는다.
    assert "cpfDiscoveredDomains" in text and "cpfDiscoveredApps" in text
    assert "cpf.domain.contractVersion" in text
    assert "dir.parentFile != rootDir" in text
    # Domain 소유 module(cpf-<domain>/online)은 App 발견에서 구조상 제외된다.
    assert "cpf-tools/runtime/** 통합 Runtime 과" in text or "상위 디렉터리가 rootDir 인 것만 App" in text
    # Optional Backoffice 가 없어도 진입점은 남아 정상 부재를 알린다.
    assert "cpfBackofficeMounted" in text
    assert "ABSENT (normal optional state)" in text


def test_optional_component_absence_keeps_other_components():
    """Optional Component 부재가 다른 Component 투영을 막으면 안 된다."""
    text = CONVENTION.read_text(encoding="utf-8")
    # App/Domain 투영은 각각 자기 발견 결과만 순회한다.
    assert "cpfDiscoveredApps.each { a ->" in text
    assert "cpfDiscoveredDomains.each { d ->" in text
    # 전체 배포는 발견된 것만 모은다(고정 목록이 아니다).
    assert 'dependsOn cpfDiscoveredApps.collect { "cpfDeploy${it.cap}".toString() }' in text
    assert 'dependsOn cpfDiscoveredDomains.collect { "cpfDeploy${it.cap}".toString() }' in text
