"""CPF Python 테스트 전역 계약.

CPF 표준 인코딩은 UTF-8 이다. Windows 콘솔 기본 코드페이지가 cp949 라서 이 고정이 없으면
pytest 가 출력하는 한글 단정 메시지가 깨져 실패 원인을 읽을 수 없다. 실제로 게이트 실패 메시지가
`?α? ????? ...` 형태로 나와 진단이 불가능했다.

Test 진입점마다 같은 코드를 반복하지 않도록 이 conftest 가 정본 소유자다.
Tool 진입점(단독 실행되는 .py)은 각자 자기 출력 스트림을 고정한다 — 게이트는
`cpf-tools/verification/tests/test_cpf_python_console_utf8.py` 가 담당한다.
"""
from __future__ import annotations

import sys


def _force_utf8_console() -> None:
    # pytest 의 terminal writer 는 `sys.__stdout__` 를 붙잡고 있고, 출력 캡처가 켜지면
    # `sys.stdout` 은 캡처 스트림으로 바뀐다. 두 계열을 모두 고정해야 단정 메시지가 온전히 나온다.
    for stream in (sys.stdout, sys.stderr, sys.__stdout__, sys.__stderr__):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError):
            # 캡처된 스트림이나 재설정 불가 스트림은 그대로 둔다. 실패해도 테스트를 막지 않는다.
            pass


_force_utf8_console()
