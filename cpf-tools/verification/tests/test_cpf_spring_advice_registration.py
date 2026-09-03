"""Spring MVC Advice 가 "등록만 되고 실행되지 않는" 상태를 정적으로 차단한다.

증상 근거: `CpfBffCredentialResponseAdvice` 는 `ResponseBodyAdvice<Object>` 를 구현하고
`@Bean` 으로 등록됐지만 `@ControllerAdvice` 가 없었다. Spring MVC 는
`ControllerAdviceBean.findAnnotatedBeans` 로만 Advice 를 수집하므로 이 Bean 은 한 번도
실행되지 않았고, 그 결과 (1) accessToken/refreshToken 이 Browser 응답 Body 에 그대로 노출되고
(2) Session 에 BFF credential handle 이 저장되지 않아 로그인 이후 모든 ADM API 가 401 이었다.
Browser 는 Authorization Header 사용이 금지되어 있어 우회 경로도 없었다.

되돌리면 재발할 증상: 1-WAS 로그인 직후 `POST /adm/api/log-policies/cache/refresh` 가 401 이 되고
ADM 운영 콘솔 전체가 로그인 이후 아무 것도 호출하지 못한다.
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

# 이 인터페이스들은 @ControllerAdvice Bean 에서만 수집된다.
ADVICE_INTERFACES = ("ResponseBodyAdvice", "RequestBodyAdvice")

EXCLUDED_PARTS = ("/build/", "/bin/", "/node_modules/", "/src/test/", "/generated/")

DECLARATION = re.compile(r"\b(?:class|record|enum)\s+(\w+)[^{]*\bimplements\b([^{]*)\{", re.DOTALL)

# 주석 안의 문구를 annotation 으로 오인하지 않도록 먼저 주석을 제거한다.
# 실제로 Javadoc 의 {@code @ControllerAdvice} 설명 때문에 negative mutation 을 한 번 놓쳤다.
COMMENTS = re.compile(r"/\*.*?\*/|//[^\n]*", re.DOTALL)

# @ControllerAdvice 는 FQN 으로도 쓸 수 있다(@org.springframework...ControllerAdvice).
ADVICE_ANNOTATION = re.compile(r"@(?:[\w.]*\.)?(?:Rest)?ControllerAdvice\b")


def _strip_comments(text: str) -> str:
    """줄 번호가 어긋나지 않도록 주석을 같은 개수의 개행으로 치환한다."""
    return COMMENTS.sub(lambda match: "\n" * match.group(0).count("\n"), text)


def _owned_java_files() -> list[Path]:
    files: list[Path] = []
    for path in REPO_ROOT.rglob("*.java"):
        normalized = "/" + path.relative_to(REPO_ROOT).as_posix()
        if any(part in normalized for part in EXCLUDED_PARTS):
            continue
        files.append(path)
    return files


class SpringAdviceRegistrationTest(unittest.TestCase):
    def test_body_advice_implementations_declare_controller_advice(self) -> None:
        violations: list[str] = []
        for path in _owned_java_files():
            text = _strip_comments(io.open(path, encoding="utf-8").read())
            if not any(name in text for name in ADVICE_INTERFACES):
                continue
            for match in DECLARATION.finditer(text):
                type_name, implemented = match.group(1), match.group(2)
                if not any(name in implemented for name in ADVICE_INTERFACES):
                    continue
                # 선언 바로 앞의 annotation 블록만 본다. import 나 다른 타입 선언은 보지 않는다.
                head = text[: match.start()]
                cut = max(head.rfind(";"), head.rfind("}"))
                head = head[cut + 1 :] if cut >= 0 else head
                if ADVICE_ANNOTATION.search(head):
                    continue
                relative = path.relative_to(REPO_ROOT).as_posix()
                violations.append(f"{relative}::{type_name}")
        self.assertEqual(
            [],
            sorted(violations),
            "ResponseBodyAdvice/RequestBodyAdvice 구현에는 @ControllerAdvice 가 필요하다. "
            "없으면 Bean 으로 등록돼도 Spring MVC 가 수집하지 않아 조용히 실행되지 않는다: "
            f"{sorted(violations)}",
        )


if __name__ == "__main__":
    unittest.main()
