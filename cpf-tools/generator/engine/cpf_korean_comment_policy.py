#!/usr/bin/env python3
"""CPF 신규/변경 Java Source의 한글 주석 정책을 결정적으로 보강한다.

Generator와 Repository currentizer가 같은 구현을 사용해 생성 후 수동 패치로 인한 diff를 만들지 않는다.
"""
from __future__ import annotations
import re

HANGUL=re.compile(r'[가-힣]')
TYPE_DECL=re.compile(r'\b(class|interface|enum|@interface|record)\s+([A-Za-z_][A-Za-z0-9_]*)')
METHOD_DECL=re.compile(r'^\s*(public|protected)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?[\w<>?,.\[\] ]+\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(')
CTOR_DECL=re.compile(r'^\s*(public|protected)\s+([A-Z][A-Za-z0-9_]*)\s*\(')
FIELD_DECL=re.compile(r'^\s*(?:private|protected|public)\s+(?:final\s+)?[\w<>?,.\[\] ]+\s+[A-Za-z_][A-Za-z0-9_]*\s*(?:=|;)')
COMPLEX_DESIGN=re.compile(r'(?i)(?:synchronized|@Transactional|catch\s*\(|(?:if|switch|when)\b[^\n]*(?:vendor|dialect)|(?:while|for)\b[^\n]*retry|rollback|reconcile)')
EXCLUDE_METHODS={'toString','hashCode','equals','getClass','clone','finalize'}


def _korean_comment_before(lines:list[str], idx:int, window:int)->bool:
    block='\n'.join(lines[max(0,idx-window):idx])
    return bool(HANGUL.search(block) and re.search(r'(?s)(//|/\*|\*|#|--).*?[가-힣]',block))


def ensure_java_korean_contract_comments(text:str)->str:
    """기존 코드를 바꾸지 않고 설명 주석만 추가한다. 여러 번 실행해도 결과가 동일하다."""
    src=text.replace('\r\n','\n')
    original_trailing=src.endswith('\n')
    lines=src.splitlines()
    out:list[str]=[]
    for raw in lines:
        line=raw
        stripped=line.strip()
        indent=line[:len(line)-len(line.lstrip())]
        # 현재 출력 누적분을 기준으로 인접 한국어 설명이 이미 있으면 추가하지 않는다.
        type_match=TYPE_DECL.search(line) if not stripped.startswith('//') else None
        if type_match and not _korean_comment_before(out,len(out),9):
            name=type_match.group(2)
            out.append(f'{indent}/** {name} 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */')
        if not re.search(r'\b(class|interface|enum|record|@interface)\b',line):
            m=METHOD_DECL.match(line) or CTOR_DECL.match(line)
            if m:
                name=m.group(2)
                if name not in EXCLUDE_METHODS and not name.startswith(('get','set','is')):
                    prior='\n'.join(out[max(0,len(out)-2):])
                    if not ('@Override' in prior and len(stripped)<120) and not _korean_comment_before(out,len(out),7):
                        out.append(f'{indent}/** {name} 작업을 CPF 표준 계약에 따라 수행한다. */')
        if FIELD_DECL.match(line) and '@ConfigurationProperties' in '\n'.join(lines) and 'static final' not in line:
            if not _korean_comment_before(out,len(out),7):
                out.append(f'{indent}/** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */')
        if COMPLEX_DESIGN.search(line) and not re.match(r'^\s*(?://|\*|/\*)',line):
            if not _korean_comment_before(out,len(out),10) and not HANGUL.search(line):
                out.append(f'{indent}// 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.')
        out.append(line)
    result='\n'.join(out)
    if original_trailing or result: result+='\n'
    return result
