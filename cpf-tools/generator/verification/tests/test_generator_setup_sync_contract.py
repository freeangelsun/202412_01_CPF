#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, os, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'
ENGINE=ROOT/'cpf-tools/generator/engine'
sys.path.insert(0,str(ENGINE))
from cpf_domain_generator import load_domain_gradle_contract  # type: ignore

class GeneratorSetupSyncContractTest(unittest.TestCase):
    def setUp(self):
        test_root=ROOT/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/tests'
        test_root.mkdir(parents=True,exist_ok=True)
        self.td=tempfile.TemporaryDirectory(prefix='cpf-sync-',dir=test_root)
        self.stage=Path(self.td.name)
        self.contract=self.stage/'cpf-ledger/gradle.properties'
        self.profile=self.stage/'profile.json'
        self.output=self.stage/'cpf-ledger'
        self.base=[sys.executable,str(CLI),'--root',str(ROOT),'domain','setup','--name','ledger','--system-code','LDG',
                   '--db-profile-output',str(self.profile),'--output',str(self.output)]
        cp=self.exec_setup('--batch','--persistence','mybatis','--vendor','postgresql','--host','db.local','--port','5544',
                    '--database-name','ledgerdb','--schema-name','ledger','--domain-dependency','member:MBR:ping')
        self.assertEqual(0,cp.returncode,cp.stderr)
    def tearDown(self): self.td.cleanup()
    def exec_setup(self,*args):
        return subprocess.run(self.base+list(args),cwd=ROOT,capture_output=True,text=True)
    def digest(self): return hashlib.sha256(self.contract.read_bytes()).hexdigest()

    def test_sync_omission_preserves_existing_contract(self):
        before=load_domain_gradle_contract(self.contract)
        cp=self.exec_setup('--sync','--cache','caffeine')
        self.assertEqual(0,cp.returncode,cp.stderr)
        after=load_domain_gradle_contract(self.contract)
        self.assertTrue(after.batch)
        self.assertEqual(before.domain_dependencies,after.domain_dependencies)
        self.assertEqual(before.local_online_port,after.local_online_port)
        self.assertEqual('caffeine',after.cache)

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
        self.assertFalse(load_domain_gradle_contract(self.contract).batch)
        self.assertFalse((self.output/'batch').exists())

    def test_dependency_clear_is_explicit_and_risky(self):
        before=self.digest()
        cp=self.exec_setup('--sync','--clear-domain-dependencies')
        self.assertNotEqual(0,cp.returncode)
        self.assertEqual(before,self.digest())
        cp=self.exec_setup('--sync','--clear-domain-dependencies','--approve-risky-change')
        self.assertEqual(0,cp.returncode,cp.stderr)
        self.assertEqual((),load_domain_gradle_contract(self.contract).domain_dependencies)

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

    def test_workspace_sync_requires_then_applies_explicit_legacy_metadata_delete(self):
        with tempfile.TemporaryDirectory(prefix='cpf-sync-approved-delete-') as temporary:
            workspace=Path(temporary)
            process_env={**os.environ,'CPF_GENERATOR_RESOURCE_ROOT':str(ROOT)}
            base=[sys.executable,str(CLI),'--root',str(workspace),'domain']
            created=subprocess.run(
                base+['create','--name','cleanup','--system-code','CLN','--business-feature','work'],
                cwd=ROOT,capture_output=True,text=True,env=process_env,
            )
            self.assertEqual(0,created.returncode,created.stderr)
            output=workspace/'cpf-cleanup'
            legacy=[output/'cpf-domain.yaml',output/'cpf-generator.lock.json']
            legacy[0].write_text('transient legacy input\n',encoding='utf-8',newline='\n')
            legacy[1].write_text('{}\n',encoding='utf-8',newline='\n')

            default=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,env=process_env)
            self.assertEqual(0,default.returncode,default.stderr)
            default_result=json.loads(default.stdout)
            self.assertEqual('VERIFICATION_PENDING_DELETE',default_result['status'])
            self.assertEqual(['cpf-domain.yaml','cpf-generator.lock.json'],default_result['results'][0]['deleteCandidates'])
            self.assertTrue(all(path.is_file() for path in legacy))

            approved=subprocess.run(base+['sync','--approve-generated-delete'],cwd=ROOT,capture_output=True,text=True,env=process_env)
            self.assertEqual(0,approved.returncode,approved.stderr)
            approved_result=json.loads(approved.stdout)
            self.assertEqual('PASS',approved_result['status'])
            self.assertTrue(approved_result['approvedGeneratedDelete'])
            self.assertTrue(all(not path.exists() for path in legacy))

            repeated=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,env=process_env)
            self.assertEqual(0,repeated.returncode,repeated.stderr)
            self.assertEqual('PASS',json.loads(repeated.stdout)['status'])

if __name__=='__main__': unittest.main()
