#!/usr/bin/env python3
"""신규/수정 Source 한글 주석 검증의 호환 진입점입니다.

실제 판정은 전체 Public API/Config 범위를 검사하는 canonical verifier 하나에 위임해
wrapper와 full gate의 Coverage가 달라지는 false PASS를 막습니다.
"""

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import sys

# import 하는 순간 Source Tree 에 __pycache__/*.pyc 가 생긴다. 그 산출물은 clean-source
# 게이트가 garbage 로 판정하므로, 이 게이트가 다른 게이트를 오염시키게 된다. 호출자가
# -B 를 주지 않아도 안전하도록 여기서 바이트코드 기록을 끈다.
sys.dont_write_bytecode = True

from verify_nxt3_korean_comment import main  # noqa: E402

if __name__ == "__main__":
    raise SystemExit(main())
