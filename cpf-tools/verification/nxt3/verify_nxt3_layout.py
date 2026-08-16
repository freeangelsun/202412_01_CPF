#!/usr/bin/env python3
"""Compatibility entry: canonical NXT3 layout Gate로 위임한다."""
# 구 호환 진입점은 canonical NXT3 Layout Gate 하나로만 위임하여 판정 중복을 방지한다.
from cpf_nxt3_layout_gate import main
if __name__=='__main__': raise SystemExit(main())
