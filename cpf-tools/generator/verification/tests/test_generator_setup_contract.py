#!/usr/bin/env python3
from __future__ import annotations
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'
ENGINE=ROOT/'cpf-tools/generator/engine'
sys.path.insert(0,str(ENGINE))
from cpf_domain_generator import load_yaml_subset, validate_definition  # type: ignore

class GeneratorSetupContractTest(unittest.TestCase):
    def run_setup(self,*extra:str):
        td=tempfile.TemporaryDirectory(prefix='cpf-domain-setup-')
        stage=Path(td.name)
        definition=stage/'cpf-domain.yaml'; profile=stage/'cpf-db-profile.local.json'
        cmd=[sys.executable,str(CLI),'--root',str(ROOT),'domain','setup','--name','ledger','--system-code','LDG','--table-prefix','LDG','--vendor','postgresql','--database-name','businessdb','--definition-output',str(definition),'--db-profile-output',str(profile),*extra]
        cp=subprocess.run(cmd,cwd=ROOT,capture_output=True,text=True)
        return td,cp,definition,profile

    def test_default_package_is_domain_name_and_vendor_is_external_profile(self):
        td,cp,definition,profile=self.run_setup()
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            d=validate_definition(load_yaml_subset(definition))
            self.assertEqual('ledger',d.package_name)
            text=definition.read_text(encoding='utf-8')
            self.assertNotIn('packageName:',text)
            self.assertNotIn('postgresql',text)
            db=json.loads(profile.read_text(encoding='utf-8'))
            self.assertEqual('postgresql',db['database']['vendor'])
            self.assertEqual(5432,db['database']['port'])
            self.assertEqual('LDG_DB_MIGRATION_PASSWORD',db['database']['migration']['password']['env'])
            self.assertNotRegex(profile.read_text(encoding='utf-8'), r'password"\s*:\s*"')
        finally: td.cleanup()

    def test_explicit_package_override_remains_supported(self):
        td,cp,definition,profile=self.run_setup('--package-name','kr.example.ledger')
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            d=validate_definition(load_yaml_subset(definition))
            self.assertEqual('kr.example.ledger',d.package_name)
        finally: td.cleanup()

if __name__=='__main__': unittest.main()
