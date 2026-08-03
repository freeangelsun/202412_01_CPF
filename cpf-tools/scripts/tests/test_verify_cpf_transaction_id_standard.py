#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "verify-cpf-transaction-id-standard.py"
spec = importlib.util.spec_from_file_location("tx_gate", MODULE_PATH)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class TransactionIdentityGateTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.write(
            "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java",
            "MODULE_ID_LENGTH = 3; WAS_ID_LENGTH = 7; DEFAULT_SEQUENCE_DIGITS = 7;\n",
        )
        self.write(
            "cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java",
            "inboundHeaderValidator.missingRequiredHeaders(request);\n",
        )
        self.write(
            "cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java",
            "TransactionIdGenerator.isValid(value);\n",
        )
        self.write(
            module.OPENAPI_CONFIG,
            '"X-Transaction-Id" true yyyyMMddHHmmssSSS\n',
        )
        self.write(
            "cpf-core/src/main/resources/application-cpf.yml",
            "module-id: ${CPF_MODULE_CODE:${CPF_APP_MODULE_ID:${CPF_MODULE_ID:CPF}}}\nwas-id: ${CPF_APP_WAS_ID:${WAS_ID:${CPF_INSTANCE_ID:appAP01}}}\n",
        )
        self.write(
            "cpf-core/src/main/java/com/cpf/core/common/system/CpfSystemCodes.java",
            'public static final String CORE = "CPF";\n',
        )
        self.write("cpf-admin/frontend/src/shared/transaction.ts", "export const createTransactionId = () => '';\n")
        self.write(
            "cpf-member/src/main/java/com/cpf/member/MemberApi.java",
            '@CpfOnlineTransaction(id = "OMBRAB0001") class MemberApi {}\n',
        )
        for vendor, schema, test_data in (
            ("mariadb", "SERVER_INSTANCE_ID VARCHAR(160)", "DATE(@sample_start_time)"),
            ("postgresql", "SERVER_INSTANCE_ID VARCHAR(160)", "DATE(:sample_start_time)"),
            ("oracle", "SERVER_INSTANCE_ID VARCHAR2(160 CHAR)", "TRUNC(&&sample_start_time)"),
        ):
            self.write(f"cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql", schema + "\n")
            self.write(f"cpf-tools/db/vendor/{vendor}/source/70_test_data.sql", test_data + "\n")

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

    def test_valid_contract_passes(self) -> None:
        result = module.validate(self.root)
        self.assertEqual("PASS", result["status"])
        self.assertEqual(1, result["executionAnnotationCount"])
        self.assertEqual(0, result["duplicateExecutionIdCount"])
        self.assertEqual(0, result["legacyExecutionIdCount"])

    def test_stale_core_openapi_owner_fails(self) -> None:
        self.write(module.STALE_OPENAPI_CONFIG, '"X-Transaction-Id" true yyyyMMddHHmmssSSS\n')
        self.assert_gate_error("must be starter-owned")

    def test_oracle_schema_drift_fails(self) -> None:
        self.write("cpf-tools/db/vendor/oracle/source/10_cpf_schema.sql", "SERVER_INSTANCE_ID VARCHAR2(80 CHAR)\n")
        self.assert_gate_error("oracle/source/10_cpf_schema.sql")

    def test_missing_official_vendor_test_data_fails(self) -> None:
        (self.root / "cpf-tools/db/vendor/postgresql/source/70_test_data.sql").unlink()
        self.assert_gate_error("missing file")

    def test_invalid_execution_id_fails(self) -> None:
        self.write(
            "cpf-member/src/main/java/com/cpf/member/MemberApi.java",
            '@CpfOnlineTransaction(id = "OMBR-API-AA-0001") class MemberApi {}\n',
        )
        self.assert_gate_error("invalid standard execution ID")

    def test_annotation_prefix_mismatch_fails(self) -> None:
        self.write(
            "cpf-member/src/main/java/com/cpf/member/MemberApi.java",
            '@CpfBatchJob(id = "OMBRAB0001") class MemberApi {}\n',
        )
        self.assert_gate_error("type/prefix mismatch")

    def test_nested_module_duplicate_is_not_skipped(self) -> None:
        self.write(
            "cpf-batch/worker/src/main/java/com/cpf/batch/Job.java",
            '@CpfBatchJob(id = "BMBRAB0001") class Job {}\n',
        )
        self.write(
            "cpf-batch/scheduler/src/main/java/com/cpf/batch/OtherJob.java",
            '@CpfBatchJob(id = "BMBRAB0001") class OtherJob {}\n',
        )
        self.assert_gate_error("duplicate standard execution ID")

    def test_legacy_production_id_fails(self) -> None:
        self.write("cpf-gateway/src/main/resources/routes.yml", "id: OMBR-API-AA-0001\n")
        self.assert_gate_error("legacy standard execution ID use")

    def test_legacy_id_in_test_source_is_ignored(self) -> None:
        self.write("cpf-member/src/test/java/com/cpf/member/Fixture.java", 'String old = "OMBR-API-AA-0001";\n')
        result = module.validate(self.root)
        self.assertEqual("PASS", result["status"])

    def test_alias_seed_is_exempt(self) -> None:
        self.write(
            "cpf-tools/db/vendor/mariadb/source/52_standard_execution_alias_seed.sql",
            "INSERT INTO x VALUES ('OMBR-API-AA-0001');\n",
        )
        result = module.validate(self.root)
        self.assertEqual("PASS", result["status"])


if __name__ == "__main__":
    unittest.main()
