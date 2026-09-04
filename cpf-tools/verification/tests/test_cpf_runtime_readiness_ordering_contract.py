"""기동 시점 계약: Listen 이전에 끝나야 하는 초기화를 Listen 이후로 미루지 못하게 한다.

증상 근거: `CpfOperationCatalogBootstrap` 이 `ApplicationListener<ApplicationReadyEvent>` 였다.
Spring Boot 는 Tomcat 이 포트를 Listen 한 뒤(`finishRefresh`)에야 ApplicationReadyEvent 를
발행하므로, Operation Catalog 가 아직 비어 있는 상태에서 포트가 열려 있는 창이 생긴다.
1-WAS 실행에서 `Started ...` 0.9초 뒤 도착한 첫 업무 거래(MBW_AUTH_LOGIN)가
`OPERATION_NOT_REGISTERED` 로 거절되어 LOCAL_FILE_LOG_STANDARD 가 실패했다.
운영 롤링 배포에서도 LB 가 포트를 보고 트래픽을 넣으면 같은 창에서 재현된다.

되돌리면 재발할 증상: 기동 직후 도착한 업무 거래가 무작위로 409 OPERATION_NOT_REGISTERED 로
거절되고, 재현이 타이밍에 좌우되어 False Green 으로 넘어간다.

BFF SecurityContext 계약도 함께 지킨다. `CpfBffSessionBridgeFilter` 가 SecurityContext 를
`SecurityContextHolder` 에만 올리면 `SessionManagementFilter` 가 매 요청을 신규 인증으로 보고
sessionFixation(changeSessionId) 와 `CsrfAuthenticationStrategy` 를 재적용한다. 그러면 CSRF
토큰과 Session ID 가 매 요청 회전해 직전 응답으로 받은 토큰이 항상 무효가 되고, ADM 콘솔의
모든 상태 변경 요청이 403 CSRF_TOKEN_MISSING 으로 막힌다.
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

COMMENTS = re.compile(r"/\*.*?\*/|//[^\n]*", re.DOTALL)

CATALOG_BOOTSTRAP = REPO_ROOT / (
    "cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOperationCatalogBootstrap.java"
)
BFF_BRIDGE = REPO_ROOT / (
    "cpf-starters/security/session/jdbc/src/main/java/com/cpf/security/session/jdbc"
    "/CpfBffSessionBridgeFilter.java"
)


def source(path: Path) -> str:
    text = io.open(path, encoding="utf-8").read()
    return COMMENTS.sub(" ", text)


class RuntimeReadinessOrderingContractTest(unittest.TestCase):
    def test_operation_catalog_syncs_before_the_web_server_listens(self) -> None:
        self.assertTrue(CATALOG_BOOTSTRAP.is_file(), f"missing: {CATALOG_BOOTSTRAP}")
        body = source(CATALOG_BOOTSTRAP)
        self.assertIn(
            "implements SmartInitializingSingleton",
            body,
            "Operation Catalog 동기화는 singleton 초기화 완료 시점(Listen 이전)에 끝나야 한다.",
        )
        self.assertNotIn(
            "ApplicationReadyEvent",
            body,
            "ApplicationReadyEvent 는 포트가 열린 뒤에 발행된다. Catalog 동기화를 그 시점으로 미루면 "
            "기동 직후 첫 업무 거래가 OPERATION_NOT_REGISTERED 로 거절된다.",
        )

    def test_bff_bridge_persists_security_context_to_the_session(self) -> None:
        self.assertTrue(BFF_BRIDGE.is_file(), f"missing: {BFF_BRIDGE}")
        body = source(BFF_BRIDGE)
        self.assertIn(
            "SecurityContextRepository",
            body,
            "BFF bridge 는 SecurityContext 를 Session 에 고정해야 한다.",
        )
        self.assertRegex(
            body,
            r"securityContextRepository\s*\.\s*saveContext\s*\(",
            "SecurityContext 를 저장하지 않으면 SessionManagementFilter 가 매 요청 신규 인증으로 보고 "
            "CSRF 토큰과 Session ID 를 회전시켜 모든 상태 변경 요청이 403 이 된다.",
        )


if __name__ == "__main__":
    unittest.main()
