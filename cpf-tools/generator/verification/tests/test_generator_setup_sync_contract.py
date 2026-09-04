#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, os, shutil, subprocess, sys, tempfile, unittest
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
        return subprocess.run(self.base+list(args),cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace')
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
                cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env,
            )
            self.assertEqual(0,created.returncode,created.stderr)
            output=workspace/'cpf-cleanup'
            legacy=[output/'cpf-domain.yaml',output/'cpf-generator.lock.json']
            legacy[0].write_text('transient legacy input\n',encoding='utf-8',newline='\n')
            legacy[1].write_text('{}\n',encoding='utf-8',newline='\n')

            default=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertNotEqual(0,default.returncode)
            default_result=json.loads(default.stdout)
            self.assertEqual('VERIFICATION_PENDING_DELETE',default_result['status'])
            self.assertEqual(['cpf-domain.yaml','cpf-generator.lock.json'],default_result['results'][0]['deleteCandidates'])
            self.assertTrue(all(path.is_file() for path in legacy))

            approved=subprocess.run(base+['sync','--approve-generated-delete'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertEqual(0,approved.returncode,approved.stderr)
            approved_result=json.loads(approved.stdout)
            self.assertEqual('PASS',approved_result['status'])
            self.assertTrue(approved_result['approvedGeneratedDelete'])
            self.assertTrue(all(not path.exists() for path in legacy))

            repeated=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertEqual(0,repeated.returncode,repeated.stderr)
            self.assertEqual('PASS',json.loads(repeated.stdout)['status'])

    def test_template_adoption_requires_explicit_approval_and_git_clean_generated_output(self):
        git=shutil.which('git')
        if not git:
            self.skipTest('Git is required for template-adoption provenance validation')
        with tempfile.TemporaryDirectory(prefix='cpf-template-adoption-') as temporary:
            workspace=Path(temporary)/'workspace'; workspace.mkdir()
            state=Path(temporary)/'state'
            process_env={**os.environ,'CPF_GENERATOR_RESOURCE_ROOT':str(ROOT),'CPF_GENERATOR_WORK_ROOT':str(state)}
            base=[sys.executable,str(CLI),'--root',str(workspace),'domain']
            created=subprocess.run(
                base+['create','--name','adopt','--system-code','ADP','--business-feature','work'],
                cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertEqual(0,created.returncode,created.stderr)
            build=workspace/'cpf-adopt/build.gradle'
            current=build.read_text(encoding='utf-8')
            build.write_text(current+'// historical generated template\n',encoding='utf-8',newline='\n')
            for command in ([git,'init'],[git,'config','user.email','cpf-test@example.invalid'],
                            [git,'config','user.name','CPF Test'],[git,'add','cpf-adopt'],
                            [git,'commit','-m','historical generated template']):
                completed=subprocess.run(command,cwd=workspace,capture_output=True,text=True,encoding='utf-8',errors='replace')
                self.assertEqual(0,completed.returncode,completed.stdout+completed.stderr)
            (state/'verification/cpf-adopt/generation-state.json').unlink()

            pending=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertNotEqual(0,pending.returncode)
            self.assertEqual('VERIFICATION_PENDING_TEMPLATE_ADOPTION',json.loads(pending.stdout)['status'])
            adopted=subprocess.run(base+['sync','--approve-template-adoption'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
            self.assertEqual(0,adopted.returncode,adopted.stderr)
            result=json.loads(adopted.stdout)
            self.assertEqual('PASS',result['status'])
            self.assertTrue(result['approvedTemplateAdoption'])
            self.assertEqual(current,build.read_text(encoding='utf-8'))
            self.assertTrue((state/'verification/cpf-adopt/generation-state.json').is_file())

    def test_sync_reconciles_only_generated_source_that_already_equals_current_template(self):
        """Old transient hash must not reject an already-current generated file.

        This is deliberately not an overwrite path: the state is made stale, while the
        generated build remains byte-identical to the current engine template. A different
        user edit below must still fail closed.
        """
        state_root=self.stage/'state'
        process_env={**os.environ,'CPF_GENERATOR_RESOURCE_ROOT':str(ROOT),'CPF_GENERATOR_WORK_ROOT':str(state_root)}
        workspace=self.stage/'template-equivalent-workspace'; workspace.mkdir()
        base=[sys.executable,str(CLI),'--root',str(workspace),'domain']
        created=subprocess.run(
            base+['create','--name','equivalent','--system-code','EQV','--business-feature','work'],
            cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
        self.assertEqual(0,created.returncode,created.stderr)
        build=workspace/'cpf-equivalent/build.gradle'
        current=build.read_bytes()
        state_file=state_root/'verification/cpf-equivalent/generation-state.json'
        state=json.loads(state_file.read_text(encoding='utf-8'))
        for row in state['expectedFiles']:
            if row['path']=='build.gradle': row['sha256']='0'*64
        state_file.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')

        reconciled=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
        self.assertEqual(0,reconciled.returncode,reconciled.stderr)
        result=json.loads(reconciled.stdout)['results'][0]
        self.assertEqual(['build.gradle'],result['templateEquivalentStateReconciled'])
        self.assertEqual(current,build.read_bytes())

        build.write_bytes(current+b'// actual user edit\n')
        rejected=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
        self.assertNotEqual(0,rejected.returncode)
        self.assertIn('사용자 수정 Generated 파일',rejected.stderr+rejected.stdout)

    def test_sync_treats_windows_crlf_checkout_as_the_same_generated_text(self):
        """Git autocrlf must not turn a canonical Generated file into a false user edit."""
        state_root=self.stage/'crlf-state'
        process_env={**os.environ,'CPF_GENERATOR_RESOURCE_ROOT':str(ROOT),'CPF_GENERATOR_WORK_ROOT':str(state_root)}
        workspace=self.stage/'crlf-workspace'; workspace.mkdir()
        base=[sys.executable,str(CLI),'--root',str(workspace),'domain']
        created=subprocess.run(
            base+['create','--name','crlf','--system-code','CRL','--business-feature','work'],
            cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
        self.assertEqual(0,created.returncode,created.stderr)
        build=workspace/'cpf-crlf/build.gradle'
        build.write_text(build.read_text(encoding='utf-8'),encoding='utf-8',newline='\r\n')
        synced=subprocess.run(base+['sync'],cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',env=process_env)
        self.assertEqual(0,synced.returncode,synced.stderr)
        self.assertEqual('PASS',json.loads(synced.stdout)['status'])

if __name__=='__main__': unittest.main()
