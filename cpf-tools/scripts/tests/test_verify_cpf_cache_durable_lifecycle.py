from __future__ import annotations
import importlib.util
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-cache-durable-lifecycle.py'
def load():
    spec=importlib.util.spec_from_file_location('cache_durable_gate',SCRIPT); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m

def test_cache_durable_gate_passes_product_tree(tmp_path):
    module=load(); result=module.verify(Path(__file__).resolve().parents[3]); assert result['status']=='PASS'; assert set(result['vendors'])=={'oracle','postgresql','mariadb'}; assert all(v['nullableNamespaceCacheKey'] for v in result['vendors'].values()); assert result['vendors']['oracle']['oracleEmptyStringSafe']

def test_cache_durable_gate_requires_all_lifecycle_files():
    module=load(); assert len(module.LIFECYCLE)==6; assert 'rollback/R101__cache_invalidation_ledger.sql' in module.LIFECYCLE

def test_namespace_invalidation_uses_sql_null_and_no_empty_string_default():
    repo=Path(__file__).resolve().parents[3]
    store=(repo/'cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/JdbcCpfCacheInvalidationStore.java').read_text()
    assert 'statement.setNull(4, java.sql.Types.VARCHAR)' in store
    for vendor in ('oracle','postgresql','mariadb'):
        ddl=(repo/f'cpf-tools/db/vendor/{vendor}/source/16_cache_invalidation_ledger.sql').read_text().upper()
        line=next(x for x in ddl.splitlines() if 'CACHE_KEY_VALUE' in x)
        assert 'NOT NULL' not in line
        assert "DEFAULT ''" not in line
