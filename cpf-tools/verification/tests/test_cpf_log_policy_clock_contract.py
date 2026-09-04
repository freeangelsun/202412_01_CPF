"""로그 정책 유효구간과 거래 로그 시각이 같은 시간 기준을 쓰도록 고정한다.

증상 근거: `LogPolicyCache` 는 `LocalDateTime.ofInstant(clock.instant(), clock.getZone())` 로
override 유효구간을 비교하고, `LoggingAspect` 는 `LocalDateTime.now(clock)` 으로 거래 로그의
START_TIME 을 남긴다. 두 clock 기본값이 달라지면 같은 DB 안에서 서로 다른 wall-clock 이 섞이고,
운영자가 등록한 유효구간이 timezone offset 만큼 어긋나 정책이 조용히 적용되지 않는다.
실제로 1-WAS 검증에서 KST(+9) 로 등록한 override 가 UTC 기준 평가와 어긋나
`resolvedSource=DB_POLICY / overrideId=(없음)` 로 무시됐다.

되돌리면 재발할 증상: 로그 정책 override 를 등록해도 아무 일도 일어나지 않고, 실패가 아니라
"정책이 없는 것처럼" 동작해 원인 추적이 매우 어렵다.
"""

from __future__ import annotations

import io
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]
OBSERVABILITY = REPO_ROOT / "cpf-starters" / "platform-operations" / "observability" / "src" / "main" / "java"

ASPECT = OBSERVABILITY / "com/cpf/platform/operations/observability/internal/logging/LoggingAspect.java"
CACHE = OBSERVABILITY / "com/cpf/platform/operations/observability/internal/logging/policy/LogPolicyCache.java"
WIRING = OBSERVABILITY / (
    "com/cpf/starter/platform/operations/observability"
    "/CpfPersistedTransactionObservabilityAutoConfiguration.java"
)
SMOKE = REPO_ROOT / "cpf-tools" / "runtime" / "tools" / "smoke-log-policy-runtime.ps1"
ADM_SERVICE = REPO_ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogPolicyService.java"

COMMENTS = re.compile(r"/\*.*?\*/|//[^\n]*", re.DOTALL)
# Clock 기본값은 소스에서 읽는다. 기대값을 이 파일에 복제하면 정본이 바뀌어도 게이트가 통과한다.
CLOCK_DEFAULT = re.compile(r"getIfUnique\(\s*Clock::(\w+)\s*\)")


def clock_defaults(path: Path) -> list[str]:
    body = COMMENTS.sub(" ", io.open(path, encoding="utf-8").read())
    return CLOCK_DEFAULT.findall(body)


class LogPolicyClockContractTest(unittest.TestCase):
    def test_transaction_log_and_policy_cache_share_one_clock_default(self) -> None:
        aspect = clock_defaults(ASPECT)
        wiring = clock_defaults(WIRING)
        self.assertTrue(aspect, f"LoggingAspect 의 Clock 기본값을 찾지 못했다: {ASPECT}")
        self.assertTrue(wiring, f"LogPolicyCache 조립의 Clock 기본값을 찾지 못했다: {WIRING}")
        self.assertEqual(
            set(aspect), set(wiring),
            "거래 로그 시각과 로그 정책 유효구간이 서로 다른 clock 기준을 쓰면 override 가 "
            f"timezone offset 만큼 어긋나 조용히 무시된다. aspect={sorted(set(aspect))} wiring={sorted(set(wiring))}",
        )

    def test_policy_cache_is_actually_wired(self) -> None:
        # 구현 클래스가 있어도 Bean 으로 만들지 않으면 정책은 항상 기본값으로 동작한다.
        body = COMMENTS.sub(" ", io.open(WIRING, encoding="utf-8").read())
        for required in ("LogPolicyRepository", "LogPolicyCache", "LogPolicyResolver"):
            self.assertIn(
                f"{required} cpfLogPolicy", body,
                f"{required} 를 Bean 으로 등록하지 않으면 DB Log Policy 가 통째로 무시된다.",
            )

    def test_verifier_expresses_effective_window_in_the_runtime_clock(self) -> None:
        smoke = io.open(SMOKE, encoding="utf-8").read()
        aspect = clock_defaults(ASPECT)
        expected_utc = "systemUTC" in aspect
        uses_utc = "[DateTime]::UtcNow" in smoke
        self.assertEqual(
            expected_utc, uses_utc,
            "검증기의 유효구간 시각 기준이 Runtime clock 과 다르다. Runtime 이 UTC 면 검증기도 UTC 로 "
            "보내야 하고, 반대도 같다.",
        )


    def test_adm_validates_the_effective_window_with_the_runtime_clock(self) -> None:
        # ADM 이 bare LocalDateTime.now() 로 검증하면 JVM 로컬 zone 을 쓰게 된다.
        # Runtime 은 주입된 clock(기본 UTC)으로 평가하므로 UTC 가 아닌 배포에서 서로 어긋난다.
        body = COMMENTS.sub(" ", io.open(ADM_SERVICE, encoding="utf-8").read())
        bare = re.findall(r"LocalDateTime\.now\(\s*\)", body)
        self.assertEqual(
            [], bare,
            "AdmLogPolicyService 는 주입된 clock 으로만 현재시각을 읽어야 한다. "
            "bare LocalDateTime.now() 는 Runtime 평가 기준과 어긋난다.",
        )
        self.assertIn("getIfUnique(Clock::", body, "ADM 은 Runtime 과 같은 Clock 기본값을 써야 한다.")
        self.assertEqual(
            set(clock_defaults(ASPECT)), set(clock_defaults(ADM_SERVICE)),
            "ADM 검증 clock 과 Runtime 거래 로그 clock 의 기본값이 다르다.",
        )


if __name__ == "__main__":
    unittest.main()
