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
    def run_setup(self,*extra:str,persistence:str='mybatis'):
        (ROOT/'build').mkdir(parents=True,exist_ok=True)
        td=tempfile.TemporaryDirectory(prefix='cpf-domain-setup-',dir=ROOT/'build')
        stage=Path(td.name)
        definition=stage/'domains/ledger/cpf-domain.yaml'; profile=stage/'local/ledger/cpf-db-profile.local.json'; output=stage/'cpf-ledger'
        cmd=[sys.executable,str(CLI),'--root',str(ROOT),'domain','setup','--name','ledger','--system-code','LDG','--table-prefix','LDG','--persistence',persistence,'--definition-output',str(definition),'--db-profile-output',str(profile),'--output',str(output)]
        if persistence!='none':
            cmd += ['--vendor','postgresql','--database-name','businessdb','--schema-name','ldg','--migration-user','cpf_ldg_migration','--runtime-user','cpf_ldg_runtime']
        cmd += list(extra)
        cp=subprocess.run(cmd,cwd=ROOT,capture_output=True,text=True)
        return td,cp,definition,profile,output

    def test_setup_generates_definition_profile_and_project_without_domain_db_vendor_tree(self):
        td,cp,definition,profile,output=self.run_setup()
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            d=validate_definition(load_yaml_subset(definition))
            self.assertEqual('ledger',d.package_name)
            self.assertTrue((output/'online').is_dir())
            self.assertFalse((output/'db').exists())
            text=definition.read_text(encoding='utf-8')
            self.assertNotIn('postgresql',text)
            db=json.loads(profile.read_text(encoding='utf-8'))
            self.assertEqual(2,db['profileVersion'])
            self.assertEqual('postgresql',db['database']['vendor'])
            self.assertEqual('ldgDB',db['database']['logicalDatabase'])
            self.assertEqual('cpf_ldg_migration',db['database']['migration']['username'])
            self.assertEqual('cpf_ldg_runtime',db['database']['runtime']['username'])
            self.assertEqual('LDG_DB_MIGRATION_PASSWORD',db['database']['migration']['password']['env'])
            self.assertEqual('LDG_DB_RUNTIME_PASSWORD',db['database']['runtime']['password']['env'])
            self.assertNotRegex(profile.read_text(encoding='utf-8'), r'password"\s*:\s*"(?!\{)')
        finally: td.cleanup()

    def test_persistence_none_creates_no_db_profile_and_rejects_db_binding(self):
        td,cp,definition,profile,output=self.run_setup('--preset','custom','--no-sample-transaction',persistence='none')
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            self.assertFalse(profile.exists())
            self.assertFalse((output/'db').exists())
        finally: td.cleanup()
        td,cp,definition,profile,output=self.run_setup('--preset','custom','--no-sample-transaction','--vendor','mariadb','--database-name','x','--schema-name','x',persistence='none')
        try:
            self.assertNotEqual(0,cp.returncode)
            self.assertIn('persistence=none',cp.stderr)
        finally: td.cleanup()

    def test_operation_level_dependency_and_external_client_are_canonical_definition(self):
        td,cp,definition,profile,output=self.run_setup('--domain-dependency','member:MBR:ping','--external-client','bank:bank-interface:fixed-length')
        try:
            # ROOT has canonical member definition so dependency target validation is executable.
            self.assertEqual(0,cp.returncode,cp.stderr)
            raw=load_yaml_subset(definition)
            self.assertEqual(['ping'],raw['domainDependencies']['member']['operations'])
            self.assertEqual('fixed-length',raw['externalClients']['bank']['capability'])
        finally: td.cleanup()

    def test_migration_and_runtime_accounts_must_be_separate(self):
        td,cp,definition,profile,output=self.run_setup('--runtime-user','cpf_ldg_migration')
        try:
            self.assertNotEqual(0,cp.returncode)
            self.assertIn('분리',cp.stderr)
        finally: td.cleanup()

if __name__=='__main__': unittest.main()
