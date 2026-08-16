import importlib.util
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/verification/tools/report-cpf-upgrade-impact.py'
spec=importlib.util.spec_from_file_location('upgrade_impact',SCRIPT); assert spec and spec.loader
module=importlib.util.module_from_spec(spec); spec.loader.exec_module(module)


def test_compare_marks_removed_public_surface_as_breaking():
    old={'publicStarters':['a'],'publicApi':['A'],'configPrefixes':['x'],'generatorContractSha256':{},'dbMigrations':{'oracle':['V1.sql']},'openApiOperations':{'ADM':['GET /x#x']}}
    new={'publicStarters':[],'publicApi':[],'configPrefixes':[],'generatorContractSha256':{},'dbMigrations':{'oracle':['V1.sql','V2.sql']},'openApiOperations':{'ADM':[]}}
    result=module.compare(old,new)
    assert result['status']=='BREAKING_REVIEW_REQUIRED'
    assert {'publicStarters','publicApi','configPrefixes','openApiOperations'} <= set(result['breakingCandidates'])
    assert 'dbMigrations' not in result['breakingCandidates']


def test_compare_allows_additive_surface_as_compatible_candidate():
    old={'publicStarters':['a'],'publicApi':['A'],'configPrefixes':['x'],'generatorContractSha256':{},'dbMigrations':{},'openApiOperations':{}}
    new={'publicStarters':['a','b'],'publicApi':['A','B'],'configPrefixes':['x','y'],'generatorContractSha256':{},'dbMigrations':{},'openApiOperations':{}}
    assert module.compare(old,new)['status']=='COMPATIBLE_CANDIDATE'
