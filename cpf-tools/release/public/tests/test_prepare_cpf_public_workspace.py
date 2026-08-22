from pathlib import Path
import importlib.util, tempfile, json
SCRIPT=Path(__file__).parents[1]/'prepare-cpf-public-workspace.py'
spec=importlib.util.spec_from_file_location('public_workspace',SCRIPT); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)

def fixture(tmp:Path):
    root=tmp/'root'; root.mkdir()
    for rel,text in [('cpf-member/build.gradle','x'),('gradlew','x'),('gradlew.bat','x'),('gradle/wrapper/gradle-wrapper.properties','x'),('tpl/README.md','r')]:
        p=root/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(text)
    policy={'defaultPolicy':'DENY','policyId':'P','sourceRules':[{'pattern':'cpf-member/**','target':'cpf-member','classification':'PUBLIC_GENERATED_SOURCE','required':True},{'pattern':'gradle/wrapper/**','target':'gradle/wrapper','classification':'PUBLIC_BOOTSTRAP','required':True},{'pattern':'gradlew','target':'gradlew','classification':'PUBLIC_BOOTSTRAP','required':True},{'pattern':'gradlew.bat','target':'gradlew.bat','classification':'PUBLIC_BOOTSTRAP','required':True}], 'templateRules':[{'source':'tpl/README.md','target':'README.md','classification':'PUBLIC_USER_DOC'}], 'allowedClassifications':['PUBLIC_GENERATED_SOURCE','PUBLIC_BOOTSTRAP','PUBLIC_USER_DOC','PUBLIC_RELEASE_METADATA'], 'forbiddenPathPrefixes':['cpf-core/'], 'forbiddenNamePatterns':['*.pem'], 'forbiddenContentPatterns':['PRIVATE KEY']}
    pp=root/'policy.json'; pp.write_text(json.dumps(policy)); return root,pp

def test_default_deny_stage_and_manifest():
    with tempfile.TemporaryDirectory() as d:
        root,pp=fixture(Path(d)); stage=Path(d)/'stage'
        result=m.prepare(root,stage,pp,'sha',False,False)
        assert result['status']=='PASS'; assert (stage/'cpf-member/build.gradle').is_file(); assert not (stage/'policy.json').exists(); assert (stage/'.cpf-public/READY.json').is_file()

def test_unclassified_file_fails():
    with tempfile.TemporaryDirectory() as d:
        root,pp=fixture(Path(d)); stage=Path(d)/'stage'; policy=m.load_json(pp); stage.mkdir(); (stage/'rogue.txt').write_text('x')
        try: m.verify_staging(stage,policy,{})
        except m.PublicSurfaceError as e: assert 'unclassified' in str(e)
        else: raise AssertionError('expected failure')


def test_single_file_target_without_suffix_is_not_nested():
    with tempfile.TemporaryDirectory() as d:
        root,pp=fixture(Path(d)); stage=Path(d)/'stage'
        result=m.prepare(root,stage,pp,'sha',False,False)
        assert result['status']=='PASS'
        assert (stage/'gradlew').is_file()
        assert not (stage/'gradlew/gradlew').exists()

def test_mandatory_domain_catalog_requires_physical_and_definition_identity():
    with tempfile.TemporaryDirectory() as d:
        root,pp=fixture(Path(d)); stage=Path(d)/'stage'
        # Add a canonical Domain definition to the fixture and require a public catalog alias.
        definition=root/'cpf-member/cpf-domain.yaml'; definition.write_text('domain:\n  name: member\n')
        policy=m.load_json(pp)
        policy['mandatoryDomainCatalogs']=[{'systemCode':'MBR','name':'member','source':'cpf-member/cpf-domain.yaml','target':'domains/member/cpf-domain.yaml','physicalProject':'cpf-member'}]
        pp.write_text(json.dumps(policy))
        result=m.prepare(root,stage,pp,'sha',False,False)
        assert result['status']=='PASS'
        assert (stage/'domains/member/cpf-domain.yaml').read_bytes()==(stage/'cpf-member/cpf-domain.yaml').read_bytes()
        # A post-stage drift must be rejected before READY can be trusted by downstream verification.
        (stage/'domains/member/cpf-domain.yaml').write_text('domain:\n  name: drift\n')
        try: m.verify_domain_catalogs(stage,policy)
        except m.PublicSurfaceError as e: assert 'drift' in str(e)
        else: raise AssertionError('expected mandatory domain catalog drift failure')
