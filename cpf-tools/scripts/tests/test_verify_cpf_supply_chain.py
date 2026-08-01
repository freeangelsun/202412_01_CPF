from __future__ import annotations
import csv, json, subprocess, sys, tempfile, unittest
from pathlib import Path

SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-supply-chain.py'

class SupplyChainVerifierTest(unittest.TestCase):
    def fixture(self)->Path:
        root=Path(tempfile.mkdtemp())
        (root/'cpf-tools/release').mkdir(parents=True)
        (root/'cpf-tools/supply-chain').mkdir(parents=True)
        (root/'cpf-tools/governance').mkdir(parents=True)
        (root/'cpf-docs/legal').mkdir(parents=True)
        (root/'settings.gradle').write_text("include 'cpf-core'\ninclude ':cpf-batch:contract'\nproject(':cpf-core').projectDir = file('cpf-core')\n",encoding='utf-8')
        (root/'cpf-docs/legal/THIRD_PARTY_NOTICES_QA32.md').write_text('# notices\n',encoding='utf-8')
        with (root/'cpf-tools/supply-chain/approved-primary-oss.csv').open('w',encoding='utf-8',newline='') as f:
            w=csv.DictWriter(f,fieldnames=['component','version','license','source_url']);w.writeheader();w.writerow({'component':'spring','version':'1','license':'Apache-2.0','source_url':'https://example.test/spring'})
        policy={'schemaVersion':1,'policyId':'p','allowedLicenses':['Apache-2.0'],'conditionalLicenses':['MPL-2.0'],'deniedLicenses':['UNKNOWN','NOASSERTION'],'releaseRequiredEvidence':['artifact-manifest.json','artifact-sha256.txt','cyclonedx-bom.json','license-report.json','vulnerability-report.json','signature-verification.json'],'requiredTools':[{'name':x,'purpose':'test'} for x in ['cyclonedx-gradle','ort','syft','grype','cpf-release-signer']],'failClosed':True}
        (root/'cpf-tools/supply-chain/cpf-supply-chain-policy.json').write_text(json.dumps(policy),encoding='utf-8')
        catalog={'schemaVersion':1,'catalogId':'c','sourceShaPolicy':'runtime','officialDatabaseVendors':['oracle','postgresql','mariadb'],'artifacts':[
          {'artifactId':'cpf-core','ownerPath':'cpf-core','kind':'library','producer':'gradle','outputPattern':'cpf-core/build/libs/*.jar','requiredAttestations':['sha256'],'consumer':'test'},
          {'artifactId':'cpf-batch-contract','ownerPath':'cpf-batch/contract','kind':'library','producer':'gradle','outputPattern':'cpf-batch/contract/build/libs/*.jar','requiredAttestations':['sha256'],'consumer':'test'},
          *[{'artifactId':'db-'+v,'ownerPath':'cpf-tools/db/vendor/'+v,'kind':'database-pack','producer':'db','outputPattern':'cpf-tools/db/vendor/'+v+'/**','requiredAttestations':['sha256'],'consumer':'db'} for v in ['oracle','postgresql','mariadb']]
        ]}
        (root/'cpf-tools/release/cpf-final-artifact-catalog.json').write_text(json.dumps(catalog),encoding='utf-8')
        env={'schemaVersion':1,'manifestId':'x','sourceSha':'0'*40,'sanitized':True,'operatingSystem':{'name':'Windows','version':'1','architecture':'x86_64'},'tools':[{'name':n,'version':v,'command':n} for n,v in [('java','25'),('gradle-wrapper','8'),('node','22'),('npm','10'),('python','3'),('powershell','7')]],'databases':[{'vendor':v,'version':'1','profile':v,'available':False} for v in ['oracle','postgresql','mariadb']],'services':[],'browsers':[{'name':n,'version':'1','available':False} for n in ['chromium','firefox','webkit']]}
        (root/'cpf-tools/governance/cpf-runtime-environment-manifest.template.json').write_text(json.dumps(env),encoding='utf-8')
        (root/'cpf-tools/governance/cpf-runtime-environment-manifest.schema.json').write_text(json.dumps({'type':'object'}),encoding='utf-8')
        return root
    def execute(self,root:Path,*args:str):
        return subprocess.run([sys.executable,str(SCRIPT),'--root',str(root),*args],capture_output=True,text=True)
    def test_static_catalog_passes_and_evidence_dir_argument_is_accepted(self):
        root=self.fixture(); evidence=root.parent/f'outside-evidence-{root.name}'; evidence.mkdir(exist_ok=True)
        cp=self.execute(root,'--evidence-dir',str(evidence))
        self.assertEqual(0,cp.returncode,cp.stdout+cp.stderr)
    def test_missing_included_project_rejected(self):
        root=self.fixture(); data=json.loads((root/'cpf-tools/release/cpf-final-artifact-catalog.json').read_text(encoding="utf-8"));data['artifacts']=[x for x in data['artifacts'] if x['ownerPath']!='cpf-core'];(root/'cpf-tools/release/cpf-final-artifact-catalog.json').write_text(json.dumps(data), encoding="utf-8")
        cp=self.execute(root);self.assertNotEqual(0,cp.returncode);self.assertIn('missing from artifact catalog',cp.stdout)
    def test_unsupported_database_pack_rejected(self):
        root=self.fixture(); data=json.loads((root/'cpf-tools/release/cpf-final-artifact-catalog.json').read_text(encoding="utf-8"));data['officialDatabaseVendors'].append('mysql');(root/'cpf-tools/release/cpf-final-artifact-catalog.json').write_text(json.dumps(data), encoding="utf-8")
        cp=self.execute(root);self.assertNotEqual(0,cp.returncode);self.assertIn('official DB vendors',cp.stdout)
    def test_release_without_evidence_fails_closed(self):
        root=self.fixture();cp=self.execute(root,'--release');self.assertNotEqual(0,cp.returncode);self.assertIn('release evidence directory missing',cp.stdout)

if __name__=='__main__':unittest.main()
