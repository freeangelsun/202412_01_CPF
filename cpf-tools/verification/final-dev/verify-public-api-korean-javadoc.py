#!/usr/bin/env python3
from __future__ import annotations
import argparse
import re
import sys
import tempfile
from pathlib import Path

KOREAN = re.compile(r'[가-힣]')
PUBLIC_TYPE = re.compile(
    r'^\s*public\s+(?:sealed\s+|non-sealed\s+|final\s+|abstract\s+)?'
    r'(class|interface|record|enum)\s+(\w+)', re.M)
EXPLICIT_PUBLIC_METHOD = re.compile(
    r'^[ \t]*public[ \t]+(?:static[ \t]+)?(?:default[ \t]+)?(?:synchronized[ \t]+)?'
    r'(?:<[^;{()]+>[ \t]+)?[\w<>, ?\[\].@]+[ \t]+(\w+)[ \t]*\(([^)]*)\)[ \t]*'
    r'(?:throws[ \t]+([^;{]+))?[ \t]*[;{]', re.M)
IMPLICIT_INTERFACE_METHOD = re.compile(
    r'^(?![ \t]*(?:private|public)\b)(?![ \t]*(?:if|for|while|switch|catch|return|throw|new)\b)'
    r'(?:[ \t]{0,4})(?![ \t])(?:(?:default|static|abstract)[ \t]+)*(?:<[^;{()]+>[ \t]+)?'
    r'[\w<>,?\[\].@]+(?:[ \t]+[\w<>,?\[\].@]+)*[ \t]+(\w+)[ \t]*\(([^)]*)\)[ \t]*'
    r'(?:throws[ \t]+([^;{]+))?[ \t]*[;{]', re.M)


def split_params(raw: str) -> list[str]:
    out, cur = [], []
    angle = paren = bracket = 0
    for ch in raw:
        if ch == '<': angle += 1
        elif ch == '>' and angle: angle -= 1
        elif ch == '(': paren += 1
        elif ch == ')' and paren: paren -= 1
        elif ch == '[': bracket += 1
        elif ch == ']' and bracket: bracket -= 1
        if ch == ',' and angle == 0 and paren == 0 and bracket == 0:
            part = ''.join(cur).strip(); cur = []
            if part: out.append(part)
        else:
            cur.append(ch)
    part = ''.join(cur).strip()
    if part: out.append(part)
    return out


def param_name(raw: str) -> str | None:
    raw = re.sub(r'@\w+(?:\([^)]*\))?\s*', '', raw)
    raw = re.sub(r'\bfinal\s+', '', raw)
    parts = raw.split()
    return None if not parts else parts[-1].replace('...', '').replace('[]', '')


def preceding_doc(text: str, start: int) -> str | None:
    before = text[:start]
    a, b = before.rfind('/**'), before.rfind('*/')
    if a < 0 or b < a:
        return None
    between = before[b + 2:].strip()
    if between and not all(line.lstrip().startswith('@') for line in between.splitlines() if line.strip()):
        return None
    return before[a:b + 2]


def return_type(signature: str, name: str) -> str:
    prefix = signature.split(name + '(', 1)[0]
    prefix = re.sub(r'^\s*public\s+', '', prefix)
    for token in ('static ', 'default ', 'abstract ', 'synchronized '):
        if prefix.startswith(token): prefix = prefix[len(token):]
    prefix = re.sub(r'^<[^>]+>\s*', '', prefix)
    return prefix.strip().split()[-1] if prefix.strip() else ''


def inspect_method(root: Path, p: Path, text: str, m, errs: list[str]) -> None:
    name, raw, throws = m.group(1), m.group(2), m.group(3)
    sig = m.group(0)
    if re.search(r'\b(?:record|class|interface|enum)\s+' + re.escape(name) + r'\s*\(', sig):
        return
    doc = preceding_doc(text, m.start())
    line = text.count('\n', 0, m.start()) + 1
    if doc is None:
        errs.append(f'{p.relative_to(root)}:{line}:{name}:missing-javadoc')
        return
    if not KOREAN.search(doc):
        errs.append(f'{p.relative_to(root)}:{line}:{name}:non-korean-contract')
    for part in split_params(raw):
        pn = param_name(part)
        if pn and '@param ' + pn not in doc:
            errs.append(f'{p.relative_to(root)}:{line}:{name}:{pn}:missing-param')
    ret = return_type(sig, name)
    if ret and ret != 'void' and '@return' not in doc:
        errs.append(f'{p.relative_to(root)}:{line}:{name}:missing-return')
    if throws:
        for exc in [x.strip().split()[-1] for x in throws.split(',') if x.strip()]:
            simple = exc.split('.')[-1]
            if not re.search(r'@throws\s+(?:[\w.]*\.)?' + re.escape(simple) + r'\b', doc):
                errs.append(f'{p.relative_to(root)}:{line}:{name}:{simple}:missing-throws')


def contract_errors(root: Path):
    errs: list[str] = []
    checked_methods = checked_ctors = checked_types = files = 0
    base = root / 'cpf-core/src/main/java'
    if not base.exists():
        return ['cpf-core source root missing'], 0, 0, 0, 0
    for p in base.rglob('*.java'):
        posix = p.as_posix()
        if not any(x in posix for x in (
            '/api/', '/spi/', '/common/transaction/', '/common/logging/lineage/', '/common/logging/spi/'
        )):
            continue
        text = p.read_text(encoding='utf-8', errors='replace')
        public_types = list(PUBLIC_TYPE.finditer(text))
        if not public_types:
            continue
        files += 1
        for tm in public_types:
            checked_types += 1
            doc = preceding_doc(text, tm.start())
            line = text.count('\n', 0, tm.start()) + 1
            name = tm.group(2)
            if doc is None:
                errs.append(f'{p.relative_to(root)}:{line}:{name}:type-missing-javadoc')
            elif not KOREAN.search(doc):
                errs.append(f'{p.relative_to(root)}:{line}:{name}:type-non-korean-contract')

        seen: set[tuple[int, str]] = set()
        matches = []
        for m in EXPLICIT_PUBLIC_METHOD.finditer(text):
            key = (m.start(), m.group(1)); seen.add(key); matches.append(m)
        if any(tm.group(1) == 'interface' for tm in public_types):
            for m in IMPLICIT_INTERFACE_METHOD.finditer(text):
                key = (m.start(), m.group(1))
                if key in seen or m.group(1) == p.stem:
                    continue
                # Exclude type declarations accidentally shaped like methods.
                if re.search(r'\b(?:record|class|interface|enum)\s+', m.group(0)):
                    continue
                seen.add(key); matches.append(m)
        for m in sorted(matches, key=lambda x: x.start()):
            checked_methods += 1
            inspect_method(root, p, text, m, errs)

        ctor_re = re.compile(
            r'^\s*public\s+' + re.escape(p.stem) +
            r'\s*\(([^)]*)\)\s*(?:throws\s+([^;{]+))?[{]', re.M)
        for m in ctor_re.finditer(text):
            checked_ctors += 1
            raw, throws = m.group(1), m.group(2)
            doc = preceding_doc(text, m.start())
            line = text.count('\n', 0, m.start()) + 1
            if doc is None:
                errs.append(f'{p.relative_to(root)}:{line}:{p.stem}:constructor-missing-javadoc')
                continue
            if not KOREAN.search(doc):
                errs.append(f'{p.relative_to(root)}:{line}:{p.stem}:constructor-non-korean-contract')
            for part in split_params(raw):
                pn = param_name(part)
                if pn and '@param ' + pn not in doc:
                    errs.append(f'{p.relative_to(root)}:{line}:{p.stem}:{pn}:constructor-missing-param')
            if throws:
                for exc in [x.strip().split()[-1] for x in throws.split(',') if x.strip()]:
                    simple = exc.split('.')[-1]
                    if not re.search(r'@throws\s+(?:[\w.]*\.)?' + re.escape(simple) + r'\b', doc):
                        errs.append(f'{p.relative_to(root)}:{line}:{p.stem}:{simple}:constructor-missing-throws')
    if checked_types == 0:
        errs.append('no Public API/SPI types were checked (false-green prohibited)')
    if checked_methods + checked_ctors == 0:
        errs.append('no Public API/SPI methods/constructors were checked (false-green prohibited)')
    return errs, files, checked_types, checked_methods, checked_ctors


def self_test() -> None:
    assert split_params('Map<String,Object> values, Function<T,K> keyExtractor') == [
        'Map<String,Object> values', 'Function<T,K> keyExtractor']
    assert param_name('@Nullable final Map<String,Object> values') == 'values'
    assert not KOREAN.search('/** English only */')
    with tempfile.TemporaryDirectory(prefix='cpf-javadoc-selftest-') as td:
        r = Path(td)
        p = r / 'cpf-core/src/main/java/com/cpf/core/api/sample/SamplePort.java'
        p.parent.mkdir(parents=True)
        good = (
            'package com.cpf.core.api.sample;\n'
            '/** 공개 샘플 포트입니다. */\n'
            'public interface SamplePort {\n'
            '    /** 식별자로 값을 조회합니다.\n'
            '     * @param id 식별자\n'
            '     * @return 조회 값\n'
            '     */\n'
            '    String find(String id);\n'
            '}\n')
        p.write_text(good, encoding='utf-8')
        errs, *_ = contract_errors(r)
        assert not errs, errs
        p.write_text(good.replace('     * @param id 식별자\n', ''), encoding='utf-8')
        errs, *_ = contract_errors(r)
        assert any('missing-param' in e for e in errs), errs
        delegated = (
            'package com.cpf.core.api.sample;\n'
            '/** 생성자 위임 검증 샘플입니다. */\n'
            'public final class SamplePort {\n'
            '    /** 기본 생성자입니다. @param id 식별자 */\n'
            '    public SamplePort(String id) { this(id, 1); }\n'
            '    /** 확장 생성자입니다. @param id 식별자 @param version 버전 */\n'
            '    public SamplePort(String id, int version) { }\n'
            '}\n')
        p.write_text(delegated, encoding='utf-8')
        errs, *_ = contract_errors(r)
        assert not any(':this:' in e for e in errs), errs


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', type=Path, default=Path('.'))
    ap.add_argument('--self-test', action='store_true')
    args = ap.parse_args()
    if args.self_test:
        self_test()
    errs, files, types, methods, ctors = contract_errors(args.root.resolve())
    if errs:
        print('FAIL public API/SPI Korean Javadoc')
        print('\n'.join(errs[:3000]))
        return 1
    print(f'PASS public API/SPI Korean Javadoc checkedFiles={files} checkedTypes={types} checkedMethods={methods} checkedConstructors={ctors}')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
