"""한 구현 클래스가 여러 Bean 인터페이스를 구현할 때 생기는 주입 모호성을 차단한다.

`test_cpf_infrastructure_injection_resolvable.py` 는 `@Bean` **선언 타입**으로 공급자 수를 센다.
그래서 다음 형태를 놓친다.

```java
public final class DefaultCpfFixedLengthCodec implements CpfFixedLengthParser, CpfFixedLengthWriter {}

@Bean CpfFixedLengthParser cpfFixedLengthParser(...) { return new DefaultCpfFixedLengthCodec(...); }
@Bean CpfFixedLengthWriter cpfFixedLengthWriter(...) { return new DefaultCpfFixedLengthCodec(...); }
```

선언 타입으로는 `CpfFixedLengthParser` 공급자가 1개뿐이지만, **런타임 타입 기준으로는 두 Bean 모두**
`CpfFixedLengthParser` 다. 실제로 1-WAS 합성 기동이 이렇게 실패했다.

```
No qualifying bean of type 'CpfFixedLengthParser' available:
expected single matching bean but found 2: cpfFixedLengthParser,cpfFixedLengthWriter
```

`@ConditionalOnMissingBean(CpfFixedLengthParser.class)` 도 선언 타입만 보므로 막지 못한다.
따라서 그런 자리는 소비 지점에서 `@Qualifier` 로 해석 근거를 명시해야 한다(Harness §25.4).
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCAN_ROOTS = ("cpf-starters", "cpf-common", "cpf-admin", "cpf-gateway", "cpf-batch", "cpf-core")
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}

# `class X implements A, B { ... }` 에서 X 와 구현 인터페이스 목록을 얻는다.
IMPLEMENTS = re.compile(
    r"\bclass\s+(\w+)(?:\s*<[^>]*>)?(?:\s+extends\s+[\w.<>,\s]+?)?\s+implements\s+([\w.,<>\s]+?)\s*\{")
BEAN_METHOD = re.compile(
    r"@Bean\b[^\n]*\n(?:\s*@[\w.]+(?:\([^)]*\))?\s*\n)*"
    r"\s*(?:public\s+|protected\s+|private\s+)?(?:static\s+)?([\w.]+)\s+(\w+)\s*\(", re.M)
RETURNS_NEW = re.compile(r"return\s+new\s+(\w+)\s*\(")
QUALIFIER = re.compile(r"@Qualifier\b")


def _strip_comments(text: str) -> str:
    return re.sub(r"//[^\n]*", " ", re.sub(r"/\*.*?\*/", " ", text, flags=re.S))


@lru_cache(maxsize=1)
def _sources() -> tuple[Path, ...]:
    files: list[Path] = []
    for root in SCAN_ROOTS:
        directory = ROOT / root
        if not directory.is_dir():
            continue
        files += [
            path for path in directory.rglob("*.java")
            if "/src/main/java/" in path.as_posix()
            and not (set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS)
        ]
    return tuple(files)


@lru_cache(maxsize=1)
def _multi_interface_classes() -> dict[str, frozenset[str]]:
    """둘 이상의 인터페이스를 구현하는 클래스 -> 그 인터페이스 집합."""
    found: dict[str, frozenset[str]] = {}
    for path in _sources():
        text = _strip_comments(io.open(path, encoding="utf-8", errors="replace").read())
        for match in IMPLEMENTS.finditer(text):
            name = match.group(1)
            interfaces = {
                token.split("<")[0].split(".")[-1].strip()
                for token in match.group(2).split(",") if token.strip()
            }
            if len(interfaces) >= 2:
                found[name] = frozenset(interfaces)
    return found


def _violations() -> list[str]:
    multi = _multi_interface_classes()
    findings: list[str] = []
    for path in _sources():
        raw = io.open(path, encoding="utf-8", errors="replace").read()
        text = _strip_comments(raw)
        # 이 파일이 만드는 @Bean 중, 다중 인터페이스 구현체를 반환하는 것들을 모은다.
        produced: dict[str, set[str]] = {}
        for match in BEAN_METHOD.finditer(text):
            method_name = match.group(2)
            body = text[match.end():match.end() + 700]
            created = RETURNS_NEW.search(body)
            if not created or created.group(1) not in multi:
                continue
            for interface in multi[created.group(1)]:
                produced.setdefault(interface, set()).add(method_name)
        # 인터페이스 하나를 **서로 다른 @Bean 메서드 2개 이상**이 만들 때만 모호하다.
        # 한 클래스가 여러 인터페이스를 구현해도 그 클래스를 만드는 @Bean 이 하나뿐이면
        # 각 타입의 후보는 여전히 1개다(예: JdbcBatchExecutionControlPlaneAdapter).
        # 이 구분을 빼면 정상 구성까지 위반으로 잡혀 게이트가 의미를 잃는다.
        ambiguous = {
            interface for interface, methods in produced.items() if len(methods) >= 2
        }
        if not ambiguous:
            continue
        # 같은 파일 안에서 그 인터페이스들을 타입만으로 주입받는 @Bean 파라미터를 찾는다.
        for match in BEAN_METHOD.finditer(text):
            open_index = text.index("(", match.end() - 1)
            depth, end = 0, open_index
            for index in range(open_index, len(text)):
                if text[index] == "(":
                    depth += 1
                elif text[index] == ")":
                    depth -= 1
                    if depth == 0:
                        end = index
                        break
            params = text[open_index + 1:end]
            for chunk in params.split(","):
                token = chunk.strip()
                if not token or QUALIFIER.search(token):
                    continue
                parts = token.split()
                if len(parts) < 2:
                    continue
                declared = parts[-2].split("<")[0].split(".")[-1]
                if declared in ambiguous:
                    findings.append(
                        f"{path.relative_to(ROOT).as_posix()}:{match.group(2)} <- {declared}")
    return sorted(set(findings))


def test_multi_interface_bean_injection_is_qualified() -> None:
    violations = _violations()
    assert violations == [], (
        "한 구현 클래스가 여러 Bean 인터페이스를 구현하면 타입만으로는 후보가 여럿이 된다. "
        "@ConditionalOnMissingBean 은 선언 타입만 보므로 막지 못한다. "
        f"소비 지점에 @Qualifier 로 해석 근거를 명시해야 한다: {violations}")


def test_scan_actually_finds_multi_interface_implementations() -> None:
    multi = _multi_interface_classes()
    assert len(multi) >= 5, len(multi)
    assert "DefaultCpfFixedLengthCodec" in multi, sorted(multi)[:20]
    assert {"CpfFixedLengthParser", "CpfFixedLengthWriter"} <= multi["DefaultCpfFixedLengthCodec"]


def test_patterns_use_regex_boundaries_not_control_characters() -> None:
    for pattern in (IMPLEMENTS, BEAN_METHOD, RETURNS_NEW, QUALIFIER):
        assert chr(8) not in pattern.pattern
        assert chr(11) not in pattern.pattern
    sample = "class A implements B, C {"
    assert IMPLEMENTS.search(sample).group(1) == "A"
