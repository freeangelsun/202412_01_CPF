#!/usr/bin/env python3
"""신규/수정 Source 한글 주석 검증의 호환 진입점입니다.

실제 판정은 전체 Public API/Config 범위를 검사하는 canonical verifier 하나에 위임해
wrapper와 full gate의 Coverage가 달라지는 false PASS를 막습니다.
"""
from verify_nxt3_korean_comment import main

if __name__ == "__main__":
    raise SystemExit(main())
