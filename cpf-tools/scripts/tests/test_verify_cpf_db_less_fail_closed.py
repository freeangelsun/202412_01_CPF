#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "verify-cpf-db-less-fail-closed.py"
spec = importlib.util.spec_from_file_location("db_less_gate", MODULE_PATH)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


ADM = '''
private static final java.util.Set<String> MEMORY_ALLOWED_PROFILES = java.util.Set.of("edu", "test");
var mode = environment.getProperty("cpf.adm.persistence.mode", "DATABASE");
if (mode == Mode.MEMORY) { throw new CpfValidationException("denied"); }
'''
DATA_SOURCE = '''
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
@Bean(name = "cmnDataSource")
DataSource cmnDataSource(Environment environment) {
 return CpfDataSources.resolve(environment, "spring.datasource.cmn");
}
'''
MYBATIS = '''
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
CmnMyBatisConfig(@Qualifier("cmnDataSource") DataSource cmnDataSource) {}
'''
SAMPLE = '''
@Profile({"edu", "test"})
@ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true")
@Bean(name = "cmnSampleDataSource")
DataSource sample(Environment environment) {
 return CpfDataSources.resolve(environment, "spring.datasource.cmn-sample");
}
'''


class DbLessFailClosedTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.write(module.ADM_POLICY, ADM)
        self.write(module.CMN_DATASOURCE, DATA_SOURCE)
        self.write(module.CMN_MYBATIS, MYBATIS)
        self.write(module.CMN_SAMPLE_DATASOURCE, SAMPLE)

    def tearDown(self) -> None:
        self.temp.cleanup()

    def write(self, relative: str, text: str) -> None:
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def assert_gate_error(self, fragment: str) -> None:
        with self.assertRaises(module.GateError) as caught:
            module.validate(self.root)
        self.assertIn(fragment, str(caught.exception))

    def test_valid_starter_owned_persistence_passes(self) -> None:
        result = module.validate(self.root)
        self.assertEqual("PASS", result["status"])
        self.assertEqual(["edu", "test"], result["admMemoryProfiles"])
        self.assertEqual(module.JDBC_OWNER, result["jdbcOwner"])

    def test_missing_starter_owned_source_fails(self) -> None:
        (self.root / module.CMN_DATASOURCE).unlink()
        self.assert_gate_error("missing source")

    def test_stale_cpf_common_runtime_config_fails(self) -> None:
        self.write(
            "cpf-common/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java",
            DATA_SOURCE,
        )
        self.assert_gate_error("must be starter-owned")

    def test_product_mode_condition_removal_fails(self) -> None:
        self.write(module.CMN_MYBATIS, MYBATIS.replace(module.PRODUCT_CONDITION, "true"))
        self.assert_gate_error("MyBatis must share")

    def test_product_like_memory_profile_fails(self) -> None:
        self.write(module.ADM_POLICY, ADM.replace('"edu", "test"', '"edu", "test", "local"'))
        self.assert_gate_error("profiles must be exactly")

    def test_sample_shadowing_canonical_datasource_fails(self) -> None:
        self.write(module.CMN_SAMPLE_DATASOURCE, SAMPLE + '\n@Bean(name = "cmnDataSource") Object shadow() {}\n')
        self.assert_gate_error("must not shadow")

    def test_sample_requires_explicit_enablement(self) -> None:
        self.write(
            module.CMN_SAMPLE_DATASOURCE,
            SAMPLE.replace(
                '@ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true")',
                "",
            ),
        )
        self.assert_gate_error("explicit enabled=true")


if __name__ == "__main__":
    unittest.main()
