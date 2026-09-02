"""합성 Runtime에서 해소 불가능한 인프라 Bean 주입을 차단한다.

DataSource/PlatformTransactionManager/JdbcTemplate 처럼 한 Runtime 에 후보가 여럿인 타입을
`@Qualifier` 없이 받으면 Spring 은 파라미터 이름으로만 해소한다. 이름과 같은 Bean 이 없고
`@Primary` 도 없으면 기동이 실패한다. 1-WAS(ADM+Backoffice+Gateway+Common) 합성에서 이 결함이
`AdmNotificationOutboxService` -> `AdmFileJobService` 순으로 연달아 드러났고, Spring 이 한 번에
하나씩만 보고하기 때문에 매번 전체 실행을 한 사이클씩 소모했다.

컴파일도 단위테스트도 잡지 못한다. 모듈 단독 실행에서는 후보가 하나라 통과하고 합성에서만
깨지므로, 합성 모듈 집합을 명시해 정적으로 강제한다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
# 1-WAS(cpf-local-runtime)가 실제로 합성하는 모듈 집합.
COMPOSED_MODULES = (
    "cpf-admin",
    "cpf-backoffice/online",
    "cpf-backoffice-web",
    "cpf-gateway",
    "cpf-common",
    "cpf-starters",
    "cpf-framework",
    "cpf-internal",
    "cpf-education",
    "cpf-tools/runtime/cpf-local-runtime",
)
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}
# 검사 대상 타입을 목록으로 고정하지 않는다. 고정했더니 Clock 이 목록에 없어
# AdmOperationsGovernanceService 기동 실패를 한 사이클 더 태우고서야 발견했다.
# 후보 타입은 이 합성이 실제로 선언하는 @Bean 반환 타입에서 도출한다.
NEVER_AMBIGUOUS = frozenset({"void", "String", "int", "long", "boolean", "double", "Object"})
# 1-WAS 는 CpfLocalRuntimePlatformDataSourcePrimary(BeanFactoryPostProcessor)로
# cpfPlatformDataSource 에 primary 를 지정한다. 정적으로는 보이지 않는 primary 이므로
# DataSource 무자격 주입은 합성 Runtime 에서 실제로 해소된다.
RUNTIME_PRIMARY_TYPES = frozenset({"DataSource"})
# verify_admin_dependency_boundaries.py 가 admin app 에 금지하는 optional provider leaf 들이다.
# 1-WAS 합성에 들어가지 않으므로 이 게이트의 대상이 아니다.
OPTIONAL_PROVIDER_LEAVES = (
    "cpf-starters/messaging/jms", "cpf-starters/messaging/ibm-mq",
    "cpf-starters/messaging/rabbitmq", "cpf-starters/messaging/kafka",
    "cpf-starters/data/transaction/jta", "cpf-starters/file/sftp",
    "cpf-starters/data/cache/redis", "cpf-starters/data/cache/valkey",
    "cpf-starters/data/lock/valkey", "cpf-starters/file/object-storage/s3",
    "cpf-starters/security/oidc", "cpf-starters/security/session/valkey",
)
# opt-in AutoConfiguration 의 @Bean 은 그 capability 를 켜지 않은 Runtime 에서 생성되지 않는다.
# 켜는 순간 문제가 되지만, 그 전환은 default-on 게이트가 따로 막는다.
OPT_IN_CONFIG = re.compile(r"@ConditionalOnProperty\b(?![^)]*matchIfMissing)[^)]*\)", re.S)
STEREOTYPE = re.compile(
    r"(?<![\w.])@(Component|Service|Repository|Controller|RestController"
    r"|CpfService|CpfRepository|CpfController|CpfRestController|Configuration)\b")
BEAN = re.compile(r"@Bean\b([^\n]*)")
STRING_CONSTANT = re.compile(r'static\s+final\s+String\s+\w+\s*=\s*"([^"]+)"')
PARAMETER = re.compile(r"(?:^|[\s(<,])([A-Z][\w]*)\s+(\w+)\s*$")
CONDITIONAL_ON_MISSING = re.compile(r"@ConditionalOnMissingBean\s*(?:\(([^;]*?)\))?", re.S)


@lru_cache(maxsize=1)
def _sources() -> tuple[Path, ...]:
    files: list[Path] = []
    for module in COMPOSED_MODULES:
        directory = ROOT / module
        if not directory.is_dir():
            continue
        files += [
            path for path in directory.rglob("*.java")
            if "/src/main/java/" in path.as_posix()
            and not (set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS)
        ]
    return tuple(files)


def _balanced(text: str, open_index: int) -> tuple[str, int] | tuple[None, None]:
    """`(` 위치에서 짝이 맞는 `)` 까지를 돌려준다.

    `@Value("${a:b}")` 처럼 파라미터 안에 괄호가 들어가므로 단순 `[^)]*` 로 자르면 뒤쪽
    파라미터가 통째로 사라진다. 실제로 그 때문에 `AdmFileJobService` 를 놓쳤다.
    """
    depth = 0
    for index in range(open_index, len(text)):
        char = text[index]
        if char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return text[open_index + 1:index], index
    return None, None


def _strip_comments(text: str) -> str:
    """파라미터 안의 줄주석/블록주석을 지운다."""
    without_block = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
    return re.sub(r"//[^\n]*", " ", without_block)


def _split_parameters(params: str) -> list[str]:
    """최상위 콤마로만 자른다. 제네릭과 annotation 인자 안의 콤마는 구분자가 아니다."""
    chunks: list[str] = []
    depth = 0
    current = ""
    for char in params:
        if char in "(<[":
            depth += 1
        elif char in ")>]":
            depth -= 1
        if char == "," and depth == 0:
            chunks.append(current)
            current = ""
        else:
            current += char
    if current.strip():
        chunks.append(current)
    return chunks


@lru_cache(maxsize=1)
def _bean_facts() -> tuple[frozenset[str], frozenset[str], dict[str, int]]:
    """선언된 Bean 이름, @Primary 를 가진 타입, 타입별 무조건 공급자 수를 모은다."""
    names: set[str] = set()
    primary: set[str] = set()
    counts: dict[str, int] = {}
    for path in _sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        names.update(STRING_CONSTANT.findall(text))
        for match in BEAN.finditer(text):
            head = match.group(1)
            names.update(re.findall(r'"([^"]+)"', head))
            tail = text[match.end():match.end() + 900]
            declaration = re.search(
                r"(?:^|\n)[ \t]*(?:public\s+|protected\s+|private\s+)?(?:static\s+)?"
                r"([\w.]+(?:<[^>]*>)?)\s+(\w+)\s*\(", tail)
            if not declaration:
                continue
            names.add(declaration.group(2))
            bean_type = declaration.group(1).split(".")[-1]
            annotations = tail[:declaration.start()] + head
            condition = CONDITIONAL_ON_MISSING.search(annotations)
            # 타입 기준 @ConditionalOnMissingBean 은 같은 타입이 이미 있으면 물러나므로
            # 런타임 후보를 늘리지 않는다. 그러나 name= 형태는 '그 이름'만 보기 때문에
            # 같은 타입의 다른 Bean 을 전혀 막지 못한다. Clock 3종이 정확히 그 경우다.
            backs_off = bool(condition) and "name" not in (condition.group(1) or "")
            if not backs_off:
                counts[bean_type] = counts.get(bean_type, 0) + 1
            if "@Primary" in annotations:
                primary.add(bean_type)
    return frozenset(names), frozenset(primary), counts


def _injected_parameter_lists(text: str, class_name: str) -> list[str]:
    """Spring 이 실제로 호출하는 생성자의 파라미터만 돌려준다."""
    signatures: list[tuple[str, str]] = []
    for match in re.finditer(
            r"(?:public|protected|private)?\s*" + re.escape(class_name) + r"\s*\(", text):
        params, end = _balanced(text, match.end() - 1)
        if params is None:
            continue
        if "{" not in text[end + 1:end + 80].split(";")[0]:
            continue
        signatures.append((text[max(0, match.start() - 260):match.start()], params))
    if not signatures:
        return []
    annotated = [params for head, params in signatures if "Autowired" in head]
    if annotated:
        return annotated
    return [signatures[0][1]] if len(signatures) == 1 else []


def _always_active(header: str) -> bool:
    """이 구성이 모듈만 있으면 무조건 활성인지.

    @ConditionalOnClass/@ConditionalOnBean 은 라이브러리/Bean 존재를 보는 게이트라 정적으로
    활성 여부를 알 수 없고, opt-in @ConditionalOnProperty 는 켜지 않으면 Bean 이 생기지 않는다.
    이 둘은 여기서 판정하지 않는다(켜는 전환 자체는 default-on 범위 게이트가 막는다).
    조건이 없거나 matchIfMissing=true 인 구성만 '항상 활성'으로 본다. 실제로 resilience 를
    matchIfMissing=true 로 바꾼 순간 이 부류가 되어 batch scheduler 와 Gateway 를 깨뜨렸다.
    """
    # @ConditionalOnSingleCandidate 는 후보가 여럿이면 스스로 물러난다. 바로 이 게이트가 막으려는
    # 모호성 상황에서 Bean 이 만들어지지 않으므로 위반이 아니다.
    if ("@ConditionalOnClass" in header or "@ConditionalOnBean" in header
            or "@ConditionalOnSingleCandidate" in header):
        return False
    condition = OPT_IN_CONFIG.search(header)
    return condition is None


def _bean_method_parameter_lists(text: str, header: str = "") -> list[str]:
    """@Bean 메서드의 파라미터 목록.

    stereotype 생성자만 보던 초판은 `CpfResilienceAutoConfiguration.cpfResilienceExecutor` 의
    무자격 Clock 을 놓쳤고, 그 capability 가 기본 제공으로 바뀐 순간 batch scheduler 와 Gateway 가
    함께 기동 실패했다. AutoConfiguration 이 만드는 Bean 도 같은 주입 규칙을 받는다.
    """
    if not _always_active(header):
        return []
    lists: list[str] = []
    for match in re.finditer(r"@Bean\b", text):
        tail = text[match.end():match.end() + 1500]
        declaration = re.search(
            r"(?:^|\n)[ \t]*(?:public\s+|protected\s+|private\s+)?(?:static\s+)?"
            r"[\w.<>,\[\] ]+?\s+\w+\s*\(", tail)
        if not declaration:
            continue
        # 조건은 파일 header 뿐 아니라 @Bean 바로 위(그리고 중첩 Configuration)에도 붙는다.
        # `CpfJdbcStarterAutoConfiguration.cpfJdbcOperations` 는 @ConditionalOnSingleCandidate 로
        # 후보가 여럿이면 스스로 물러나므로 위반이 아니다.
        local = text[max(0, match.start() - 400):match.start()] + tail[:declaration.start()]
        if not _always_active(local):
            continue
        params, _ = _balanced(tail, declaration.end() - 1)
        if params is not None:
            lists.append(params)
    return lists


def _violations() -> list[str]:
    names, primary, counts = _bean_facts()
    found: list[str] = []
    for path in _sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        if not (STEREOTYPE.search(text) or "@Bean" in text):
            continue
        header = text[:text.find("public class")] if "public class" in text else text[:600]
        relative = path.relative_to(ROOT).as_posix()
        if relative.startswith(OPTIONAL_PROVIDER_LEAVES):
            bean_lists = []
        else:
            bean_lists = _bean_method_parameter_lists(text, header)
        for params in (_injected_parameter_lists(text, path.stem) + bean_lists):
            for chunk in _split_parameters(params):
                # 파라미터 사이의 주석에 "Qualifier" 같은 단어가 들어가면 아래 skip 조건이
                # 오작동한다. 실제로 이 게이트가 그 때문에 위반을 놓쳤다. 주석을 먼저 지운다.
                chunk = _strip_comments(chunk)
                if ("Qualifier" in chunk or "ObjectProvider" in chunk
                        or "Value" in chunk):
                    continue
                match = PARAMETER.search(chunk.strip())
                if not match:
                    continue
                bean_type, parameter = match.group(1), match.group(2)
                if bean_type in NEVER_AMBIGUOUS or bean_type in RUNTIME_PRIMARY_TYPES:
                    continue
                if (counts.get(bean_type, 0) > 1
                        and bean_type not in primary
                        and parameter not in names):
                    found.append(
                        f"{path.relative_to(ROOT).as_posix()}: {bean_type} {parameter}")
    return sorted(set(found))


def test_ambiguous_infrastructure_beans_are_qualified() -> None:
    violations = _violations()
    assert violations == [], (
        "합성 Runtime 에 후보가 여럿인 인프라 Bean 은 @Qualifier 로 Role 을 명시해야 한다"
        f" (미명시 시 기동 실패 또는 다른 DataSource 커밋): {violations}"
    )


def test_scan_actually_sees_multiple_candidates() -> None:
    # 후보가 하나뿐이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    _, primary, counts = _bean_facts()
    ambiguous = {t for t, n in counts.items()
                 if n > 1 and t not in primary and t not in NEVER_AMBIGUOUS}
    # 후보 타입이 도출되지 않으면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(ambiguous) >= 5, f"ambiguous types not derived: {sorted(ambiguous)}"
    assert "Clock" in ambiguous, f"Clock must be derived as ambiguous: {sorted(ambiguous)}"
    assert len(_sources()) > 500, f"composed sources not scanned: {len(_sources())}"


def test_opt_in_annotation_pattern_is_a_regex_boundary_not_a_control_character() -> None:
    assert OPT_IN_CONFIG.search('@ConditionalOnProperty(prefix = "cpf", name = "enabled")')
    assert not OPT_IN_CONFIG.search(
        '@ConditionalOnProperty(prefix = "cpf", name = "enabled", matchIfMissing = true)')
    assert "\x08" not in OPT_IN_CONFIG.pattern
