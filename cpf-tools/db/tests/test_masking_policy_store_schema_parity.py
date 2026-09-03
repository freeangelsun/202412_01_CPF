"""마스킹 정책 저장소의 SQL이 정본 스키마와 일치하는지 검증한다.

`JdbcCpfMaskingPolicyStore` 는 스스로 "canonical three-vendor DDL is owned by the DB workstream"
이라고 선언하는데, 실제로 그 DDL 이 **저장소의 어떤 SQL 에도 없었다**. 그 결과
`cpf.security.masking-policy.mode=jdbc` 는 어디에도 설정되지 않았고, 운영자가 마스킹 정책을
바꿀 수단이 존재한 적이 없다(Harness §28.1/§28.4).

이제 정본 테이블 4종을 `platform-schema.json` 에 넣었지만, 단위 테스트 harness 는 `FakeAccess`
(메모리 구현)를 쓰므로 **SQL 문자열과 실제 컬럼의 어긋남을 잡지 못한다.** 이 게이트가 그 간극을
정적으로 닫는다.
"""
from __future__ import annotations

import io
import json
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCHEMA = ROOT / "cpf-tools/db/canonical/platform-schema.json"
STORE = ROOT / (
    "cpf-starters/security/secret/src/main/java/com/cpf/security/secret/internal/"
    "JdbcCpfMaskingPolicyStore.java")

TABLES = (
    "CPF_MASKING_POLICY_SHARD",
    "CPF_MASKING_POLICY_HEAD",
    "CPF_MASKING_POLICY_VERSION",
    "CPF_MASKING_POLICY_COMMAND",
)
IDENTIFIER = re.compile(r"[a-z_][a-z0-9_]*")


@lru_cache(maxsize=1)
def _canonical_columns() -> dict[str, set[str]]:
    document = json.load(io.open(SCHEMA, encoding="utf-8"))
    found: dict[str, set[str]] = {}
    for table in document["tables"]:
        if table["name"] in TABLES:
            found[table["name"]] = {column["name"].lower() for column in table["columns"]}
    return found


@lru_cache(maxsize=1)
def _store_source() -> str:
    return io.open(STORE, encoding="utf-8").read()


def _referenced_columns() -> set[str]:
    """저장소 SQL이 실제로 쓰는 컬럼 이름을 모은다.

    Java 문자열 연결로 쪼개진 SQL 을 이어 붙인 뒤 소문자 식별자만 추출한다. 테이블/별칭/예약어는
    아래 canonical 컬럼 집합과 교집합을 취해 걸러낸다(과다검출로 게이트가 무의미해지지 않도록).
    """
    text = _store_source()
    joined = re.sub(r'"\s*\+\s*"', "", text)
    return {match.group(0) for match in IDENTIFIER.finditer(joined)}


def test_canonical_masking_policy_tables_exist() -> None:
    columns = _canonical_columns()
    missing = [name for name in TABLES if name not in columns]
    assert missing == [], (
        "마스킹 정책 제어 테이블이 정본 스키마에 없다. 저장소는 있는데 DDL 이 없으면 "
        f"운영자가 정책을 바꿀 수단이 실제로 존재하지 않는다: {missing}")


def test_store_sql_only_uses_canonical_columns() -> None:
    canonical = set()
    for names in _canonical_columns().values():
        canonical |= names
    referenced = _referenced_columns()
    # 저장소가 반드시 다루어야 하는 핵심 컬럼이 실제로 SQL 에 등장하는지 본다.
    required = {
        "shard_id", "singleton_id", "active_version",
        "policy_version", "sensitive_keys_csv", "max_length", "mask_bearer_flag",
        "value_rules_csv", "updated_at", "updated_by", "update_reason",
        "command_id_hash", "command_hash", "result_version", "result_sensitive_keys_csv",
        "result_max_length", "result_mask_bearer_flag", "result_value_rules_csv",
        "result_updated_at", "result_updated_by", "result_reason", "recorded_at",
    }
    assert required <= canonical, sorted(required - canonical)
    missing = sorted(required - referenced)
    assert missing == [], (
        f"저장소 SQL 이 정본 컬럼을 사용하지 않는다: {missing}")


def test_store_uses_canonical_uppercase_table_names() -> None:
    text = _store_source()
    lowercase = [name for name in TABLES if name.lower() in text]
    assert lowercase == [], (
        "정본 스키마는 대문자 테이블명을 쓴다. 소문자로 조회하면 identifier case 를 보존하는 "
        f"Linux MariaDB 에서 테이블을 찾지 못한다: {lowercase}")
    for name in TABLES:
        assert name in text, name


def test_operator_selected_value_rules_are_persisted() -> None:
    """운영자 선택이 재기동 후에도 살아 있어야 한다."""
    text = _store_source()
    assert "value_rules_csv" in text
    assert "result_value_rules_csv" in text
    assert "CpfMaskingValueRule.toCsv" in text
    assert "CpfMaskingValueRule.parseCsv" in text


def test_yes_no_flag_is_not_read_through_get_boolean() -> None:
    """CHAR(1) 'Y'/'N' 을 getBoolean 으로 읽으면 벤더에 따라 조용히 false 가 된다."""
    text = _store_source()
    assert 'getBoolean("mask_bearer_flag")' not in text
    assert 'getBoolean("result_mask_bearer_flag")' not in text
    assert "yesNo(" in text
