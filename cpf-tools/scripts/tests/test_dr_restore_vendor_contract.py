from __future__ import annotations
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=(ROOT/'cpf-tools/scripts/verify-dr-restore.ps1').read_text(encoding='utf-8-sig')

class DrRestoreVendorContractTest(unittest.TestCase):
    def test_three_official_vendor_clients_and_queries(self):
        for token in ["'mariadb'","'postgresql'","'oracle'",'mariadb client','psql','sqlplus','information_schema.tables','user_objects']:
            self.assertIn(token,SCRIPT)
    def test_operator_reason_approval_and_sanitized_hash_only_evidence(self):
        for token in ['Operator','Reason','ApprovalReference','resultSha256','resultContentStored=$false','sanitized=$true']:
            self.assertIn(token,SCRIPT)
        self.assertNotIn('result=($result',SCRIPT)
        self.assertNotIn('result=$stdout',SCRIPT)
    def test_oracle_external_store_and_no_echo(self):
        for token in ['/nolog','CONNECT /@$OracleConnectIdentifier','SET ECHO OFF','SET VERIFY OFF','SET DEFINE OFF']:
            self.assertIn(token,SCRIPT)
        self.assertNotRegex(SCRIPT,r'CONNECT\s+\$User/')
    def test_empty_schema_and_client_failure_fail_closed(self):
        self.assertIn('EMPTY_RESTORED_SCHEMA',SCRIPT)
        self.assertIn('CLIENT_EXIT_NONZERO',SCRIPT)
        self.assertIn("if($status -ne 'PASS')",SCRIPT)

if __name__=='__main__': unittest.main()
