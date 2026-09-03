#!/usr/bin/env python3
"""Compatibility entry: canonical NXT3 layout Gate로 위임한다."""

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
# 구 호환 진입점은 canonical NXT3 Layout Gate 하나로만 위임하여 판정 중복을 방지한다.
from cpf_nxt3_layout_gate import main
if __name__=='__main__': raise SystemExit(main())
