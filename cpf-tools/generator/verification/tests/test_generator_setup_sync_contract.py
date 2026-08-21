#!/usr/bin/env python3
from __future__ import annotations
import hashlib, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'
ENGINE=ROOT/'cpf-tools/generator/engine'
sys.path.insert(0,str(ENGINE))
from cpf_domain_generator import load_yaml_subset  # type: ignore

class GeneratorSetupSyncContractTest(unittest.TestCase):
    def setUp(self):
        (ROOT/'build').mkdir(parents=True,exist_ok=True)
        self.td=tempfile.TemporaryDirectory(prefix='cpf-sync-',dir=ROOT/'build')
        self.stage=Path(self.td.name)
        self.definition=self.stage/'cpf-ledger/cpf-domain.yaml'
        self.profile=self.stage/'profile.json'
        self.output=self.stage/'cpf-ledger'
        self.base=[sys.executable,str(CLI),'--root',str(ROOT),'domain','setup','--name','ledger','--system-code','LDG',
                   '--definition-output',str(self.definition),'--db-profile-output',str(self.profile),'--output',str(self.output)]
        cp=self.exec_setup('--batch','--persistence','mybatis','--vendor','postgresql','--host','db.local','--port','5544',
                    '--database-name','ledgerdb','--schema-name','ledger','--domain-dependency','member:MBR:ping')
        self.assertEqual(0,cp.returncode,cp.stderr)
    def tearDown(self): self.td.cleanup()
    def exec_setup(self,*args):
        return subprocess.run(self.base+list(args),cwd=ROOT,capture_output=True,text=True)
    def digest(self): return hashlib.sha256(self.definition.read_bytes()).hexdigest()

    def test_sync_omission_preserves_existing_contract(self):
        before=load_yaml_subset(self.definition)
        cp=self.exec_setup('--sync','--cache','caffeine')
        self.assertEqual(0,cp.returncode,cp.stderr)
        after=load_yaml_subset(self.definition)
        self.assertTrue(after['modules']['batch'])
        self.assertEqual(before['domainDependencies'],after['domainDependencies'])
        self.assertEqual(before['runtime'] if 'runtime' in before else None,after.get('runtime'))
        self.assertEqual('caffeine',after['features']['cache'])

    def test_batch_disable_is_risky_and_requires_explicit_approval(self):
        cp=self.exec_setup('--sync','--no-batch','--preview')
        self.assertEqual(0,cp.returncode,cp.stderr)
        self.assertIn('modules.batch:disable',cp.stdout)
        before=self.digest()
        cp=self.exec_setup('--sync','--no-batch')
        self.assertNotEqual(0,cp.returncode)
        self.assertEqual(before,self.digest())
        cp=self.exec_setup('--sync','--no-batch','--approve-risky-change')
        self.assertEqual(0,cp.returncode,cp.stderr)
        self.assertFalse(load_yaml_subset(self.definition)['modules']['batch'])

    def test_dependency_clear_is_explicit_and_risky(self):
        before=self.digest()
        cp=self.exec_setup('--sync','--clear-domain-dependencies')
        self.assertNotEqual(0,cp.returncode)
        self.assertEqual(before,self.digest())
        cp=self.exec_setup('--sync','--clear-domain-dependencies','--approve-risky-change')
        self.assertEqual(0,cp.returncode,cp.stderr)
        self.assertEqual({},load_yaml_subset(self.definition)['domainDependencies'])

    def test_repeat_sync_is_definition_idempotent(self):
        cp=self.exec_setup('--sync','--cache','valkey')
        self.assertEqual(0,cp.returncode,cp.stderr)
        first=self.digest()
        cp=self.exec_setup('--sync','--cache','valkey')
        self.assertEqual(0,cp.returncode,cp.stderr)
        self.assertEqual(first,self.digest())

    def test_raw_secret_value_is_rejected_without_echoing_value(self):
        secret='plain-password-value'
        cp=self.exec_setup('--sync','--migration-secret-env',secret)
        self.assertNotEqual(0,cp.returncode)
        self.assertNotIn(secret,cp.stderr+cp.stdout)
        self.assertIn('ENV reference',cp.stderr)

if __name__=='__main__': unittest.main()
