import csv, json, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
SCRIPTS=ROOT/'cpf-tools/governance/tools'
GEN=SCRIPTS/'generate-cpf-project-inventory.py'; VERIFY=SCRIPTS/'verify-cpf-project-inventory.py'

class ProjectInventoryTest(unittest.TestCase):
    def make(self):
        t=tempfile.TemporaryDirectory(); root=Path(t.name)/'repo'; out=Path(t.name)/'out'; root.mkdir()
        (root/'cpf-core/src/main/java/com/cpf/core/api/demo').mkdir(parents=True)
        (root/'cpf-core/src/main/java/com/cpf/core/api/demo/DemoApi.java').write_text('package com.cpf.core.api.demo; public interface DemoApi {}',encoding='utf-8')
        (root/'cpf-admin/frontend/src/features/demo').mkdir(parents=True)
        (root/'cpf-admin/frontend/src/features/demo/Demo.vue').write_text('<template><div data-cpf-page="demo"/></template>',encoding='utf-8')
        (root/'cpf-docs/quality').mkdir(parents=True)
        with (root/'cpf-docs/quality/result.csv').open('w',newline='',encoding='utf-8') as f:
            w=csv.DictWriter(f,fieldnames=['requirement_id','source_paths','consumer_paths','test_paths','evidence_paths','development_status','verification_status']);w.writeheader();w.writerow({'requirement_id':'R1','source_paths':'cpf-core/src/main/java/com/cpf/core/api/demo/DemoApi.java','consumer_paths':'cpf-admin/frontend/src/features/demo/Demo.vue','test_paths':'','evidence_paths':'','development_status':'부분 구현','verification_status':'미검증'})
        policy={'officialDatabaseVendors':['oracle','postgresql','mariadb'],'unsupportedDatabaseTokens':['mysql','mssql','h2'],'moduleOwners':[{'prefix':'cpf-core/','owner':'cpf-core'},{'prefix':'cpf-admin/','owner':'cpf-admin'},{'prefix':'cpf-docs/','owner':'cpf-docs'}],'rootFiles':['policy.json','waivers.csv'],'publicPackageMarkers':['.api.','.spi.'],'internalPackageMarkers':['.internal.'],'controllerAnnotations':['@RestController','@Controller'],'configurationExtensions':['.properties','.yml','.yaml','.json'],'databaseExtensions':['.sql'],'frontendExtensions':['.vue','.ts'],'requiredTraceColumns':[]}
        (root/'policy.json').write_text(json.dumps(policy),encoding='utf-8'); (root/'waivers.csv').write_text('waiver_id,category,path_or_symbol,owner,reason,expires_on,approved_by\n',encoding='utf-8')
        return t,root,out
    def run_gen(self,root,out): return subprocess.run([sys.executable,str(GEN),'--root',str(root),'--policy','policy.json','--result-matrix','cpf-docs/quality/result.csv','--output-dir',str(out)],capture_output=True,text=True)
    def run_verify(self,root,out): return subprocess.run([sys.executable,str(VERIFY),'--inventory-dir',str(out),'--policy',str(root/'policy.json'),'--waivers',str(root/'waivers.csv')],capture_output=True,text=True)
    def test_valid_inventory(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup); self.assertEqual(self.run_gen(r,o).returncode,0); self.assertEqual(self.run_verify(r,o).returncode,0)
    def test_output_inside_repo_rejected(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup); cp=self.run_gen(r,r/'out'); self.assertNotEqual(cp.returncode,0)
    def test_unowned_file_rejected(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup); (r/'rogue.txt').write_text('x', encoding="utf-8"); self.assertEqual(self.run_gen(r,o).returncode,0); cp=self.run_verify(r,o); self.assertNotEqual(cp.returncode,0); self.assertIn('unowned',cp.stdout)
    def test_completed_missing_source_rejected(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup)
        p=r/'cpf-docs/quality/result.csv'; text=p.read_text(encoding="utf-8"); text=text.replace('cpf-core/src/main/java/com/cpf/core/api/demo/DemoApi.java','cpf-core/missing.java').replace('부분 구현','완료'); p.write_text(text, encoding="utf-8")
        self.assertEqual(self.run_gen(r,o).returncode,0); cp=self.run_verify(r,o); self.assertNotEqual(cp.returncode,0); self.assertIn('missing source',cp.stdout)
    def test_public_internal_import_rejected(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup); p=r/'cpf-core/src/main/java/com/cpf/core/api/demo/DemoApi.java'; p.write_text('package com.cpf.core.api.demo; import com.cpf.core.internal.Secret; public interface DemoApi { public Secret leak(); }', encoding="utf-8")
        self.assertEqual(self.run_gen(r,o).returncode,0); cp=self.run_verify(r,o); self.assertNotEqual(cp.returncode,0); self.assertIn('imports internal',cp.stdout)
    def test_public_implementation_may_use_internal_collaborator_without_contract_leak(self):
        t,r,o=self.make(); self.addCleanup(t.cleanup)
        p=r/'cpf-core/src/main/java/com/cpf/core/api/demo/DemoApi.java'
        p.write_text('package com.cpf.core.api.demo; import com.cpf.core.internal.Secret; public final class DemoApi { public String value(){ return Secret.class.getName(); } }', encoding='utf-8')
        self.assertEqual(self.run_gen(r,o).returncode,0); cp=self.run_verify(r,o); self.assertEqual(cp.returncode,0,cp.stdout+cp.stderr)
if __name__=='__main__': unittest.main()
