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
from cpf_domain_generator import load_domain_gradle_contract  # type: ignore

class GeneratorSetupContractTest(unittest.TestCase):
    def run_setup(self,*extra:str,persistence:str='mybatis'):
        test_root=ROOT/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/tests'
        test_root.mkdir(parents=True,exist_ok=True)
        td=tempfile.TemporaryDirectory(prefix='cpf-domain-setup-',dir=test_root)
        stage=Path(td.name)
        profile=stage/'local/ledger/cpf-db-profile.local.json'; output=stage/'cpf-ledger'; contract=output/'gradle.properties'
        cmd=[sys.executable,str(CLI),'--root',str(ROOT),'domain','setup','--name','ledger','--system-code','LDG','--table-prefix','LDG','--persistence',persistence,'--db-profile-output',str(profile),'--output',str(output)]
        if persistence!='none':
            cmd += ['--vendor','postgresql','--database-name','businessdb','--schema-name','ldg','--migration-user','cpf_ldg_migration','--runtime-user','cpf_ldg_runtime']
        cmd += list(extra)
        cp=subprocess.run(cmd,cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace')
        return td,cp,contract,profile,output

    def test_setup_generates_definition_profile_and_project_without_domain_db_vendor_tree(self):
        td,cp,contract,profile,output=self.run_setup()
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            d=load_domain_gradle_contract(contract)
            self.assertEqual('ledger',d.package_name)
            self.assertFalse((output/'cpf-domain.yaml').exists())
            self.assertFalse((output/'cpf-generator.lock.json').exists())
            self.assertFalse(any(output.rglob('generated-domain.properties')))
            self.assertTrue((output/'online').is_dir())
            self.assertFalse((output/'db').exists())
            application=next((output/'online/src/main/java').rglob('*OnlineApplication.java'))
            application_text=application.read_text(encoding='utf-8')
            self.assertIn('org.mybatis.spring.annotation.MapperScan',application_text)
            self.assertIn('sqlSessionFactoryRef="cpfDomainSqlSessionFactory"',application_text)
            application_yml=(output/'online/src/main/resources/application.yml').read_text(encoding='utf-8')
            for token in (
                '  domain:\n    persistence:',
                '      enabled: true',
                '      required: true',
                '      provider: mybatis',
                '      data-source-prefix: spring.datasource',
            ):
                self.assertIn(token,application_yml)
            root_settings=(output/'settings.gradle').read_text(encoding='utf-8')
            root_build=(output/'build.gradle').read_text(encoding='utf-8')
            online_build=(output/'online/build.gradle').read_text(encoding='utf-8')
            self.assertIn("providers.gradleProperty('cpfManagedGradleRoot')",root_settings)
            self.assertIn("gradle.gradleUserHomeDir",root_settings)
            self.assertNotIn("new File(rootDir, '.cpf",root_settings)
            self.assertIn("gradle.startParameter.projectCacheDir",root_settings)
            self.assertIn("generateCpfDomainRuntimeDescriptor",root_build)
            controller=next((output/'online/src/main/java').rglob('*TransactionController.java')).read_text(encoding='utf-8')
            self.assertIn('@GetMapping("/{id}")',controller)
            self.assertIn('def previousTransactionEnd = classBodyStart + 1',root_build)
            self.assertIn('def prefix = text.substring(previousTransactionEnd, txMatcher.start())',root_build)
            self.assertNotIn("text.lastIndexOf('}', txMatcher.start())",root_build)
            runtime_contract=json.loads((ROOT/'cpf-tools/generator/contracts/central-domain-template-contract.json').read_text(encoding='utf-8'))['buildRuntimeContract']
            for vendor in runtime_contract['vendors'].values():
                self.assertIn(vendor['jdbcDriver'],online_build)
            self.assertIn('runtimeOnly cpfSelectedJdbcDriver',online_build)
            self.assertIn("tasks.register('validateCpfJdbcDriverSelection')",online_build)
            self.assertIn("tasks.named('processResources') { dependsOn tasks.named('validateCpfJdbcDriverSelection') }",online_build)
            self.assertNotIn("runtimeOnly 'org.postgresql:postgresql'",online_build)
            text=contract.read_text(encoding='utf-8')
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

    def test_setup_generates_consumed_logging_contract_for_online_and_batch(self):
        td,cp,contract,profile,output=self.run_setup('--batch')
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            for module in ('online','batch'):
                resources=output/module/'src/main/resources'
                application=(resources/'application.yml').read_text(encoding='utf-8')
                self.assertEqual(1,application.count('\ncpf:\n'))
                for token in (
                    '  logging:',
                    'root: ${CPF_LOG_ROOT:logs}',
                    'instance-id: ${CPF_RUNTIME_INSTANCE_ID:${HOSTNAME:local}}',
                    'maintenance-interval: ${CPF_LOG_MAINTENANCE_INTERVAL:1h}',
                    'file-name: runtime.log',
                    'file-name: error.log',
                    'level: ERROR',
                    'rolling: DAILY',
                    'compress-after-days: 5',
                    'delete-after-days: 365',
                    'Transaction Evidence와 혼용하지 않습니다',
                    'Console 출력도 함께 유지됩니다',
                ):
                    self.assertIn(token,application)
                for name,level in {
                    'local':'TRACE',
                    'dev':'DEBUG',
                    'test':'DEBUG',
                    'stg':'INFO',
                    'prod':'INFO',
                }.items():
                    profile_yml=(resources/f'application-{name}.yml').read_text(encoding='utf-8')
                    self.assertIn(f'root: ${{CPF_LOG_LEVEL:{level}}}',profile_yml)
                    self.assertIn('환경별 기본 레벨',profile_yml)
            self.assertIn('web-application-type: none',(output/'batch/src/main/resources/application.yml').read_text(encoding='utf-8'))
        finally: td.cleanup()

    def test_persistence_none_creates_no_db_profile_and_rejects_db_binding(self):
        td,cp,contract,profile,output=self.run_setup('--preset','custom','--no-sample-transaction',persistence='none')
        try:
            self.assertEqual(0,cp.returncode,cp.stderr)
            self.assertFalse(profile.exists())
            self.assertFalse((output/'db').exists())
            application=next((output/'online/src/main/java').rglob('*OnlineApplication.java'))
            application_text=application.read_text(encoding='utf-8')
            self.assertNotIn('org.mybatis.spring.annotation.MapperScan',application_text)
            self.assertNotIn('@MapperScan',application_text)
            application_yml=(output/'online/src/main/resources/application.yml').read_text(encoding='utf-8')
            self.assertNotIn('  domain:\n    persistence:',application_yml)
            online_build=(output/'online/build.gradle').read_text(encoding='utf-8')
            self.assertNotIn('cpfJdbcDriverByVendor',online_build)
            self.assertNotIn('validateCpfJdbcDriverSelection',online_build)
        finally: td.cleanup()
        td,cp,contract,profile,output=self.run_setup('--preset','custom','--no-sample-transaction','--vendor','mariadb','--database-name','x','--schema-name','x',persistence='none')
        try:
            self.assertNotEqual(0,cp.returncode)
            self.assertIn('persistence=none',cp.stderr)
        finally: td.cleanup()

    def test_operation_level_dependency_and_external_client_are_canonical_definition(self):
        td,cp,contract,profile,output=self.run_setup('--domain-dependency','member:MBR:ping','--external-client','bank:bank-interface:fixed-length')
        try:
            # ROOT has canonical member definition so dependency target validation is executable.
            self.assertEqual(0,cp.returncode,cp.stderr)
            domain=load_domain_gradle_contract(contract)
            self.assertEqual(('ping',),domain.domain_dependencies[0].operations)
            self.assertEqual('fixed-length',domain.external_clients[0].capability)
            online_build=(output/'online/build.gradle').read_text(encoding='utf-8')
            self.assertIn('implementation "member:online:1.0.0-SNAPSHOT"',online_build)
        finally: td.cleanup()

    def test_migration_and_runtime_accounts_must_be_separate(self):
        td,cp,contract,profile,output=self.run_setup('--runtime-user','cpf_ldg_migration')
        try:
            self.assertNotEqual(0,cp.returncode)
            self.assertIn('분리',cp.stderr)
        finally: td.cleanup()

if __name__=='__main__': unittest.main()
