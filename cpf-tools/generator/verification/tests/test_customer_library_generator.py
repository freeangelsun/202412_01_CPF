from __future__ import annotations
import importlib.util
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
ENGINE=ROOT/'cpf-tools/generator/engine/cpf_customer_library_generator.py'
spec=importlib.util.spec_from_file_location('customer_lib',ENGINE); mod=importlib.util.module_from_spec(spec); sys.modules['customer_lib']=mod; spec.loader.exec_module(mod)


def domain(root:Path,name='member') -> Path:
    target=root/f'cpf-{name}'; target.mkdir()
    (target/'gradle.properties').write_text('cpf.domain.contractVersion=1\n',encoding='utf-8')
    return target


def test_create_library_has_korean_guidance_and_no_internal_dependency(tmp_path:Path):
    result=mod.create_library(tmp_path,'company-common','com.acme.shared','com.acme.shared.common')
    assert result['status']=='PASS'
    project=tmp_path/'customer-libraries/company-common'
    assert '고객사 공통' in (project/'build.gradle').read_text(encoding='utf-8')
    assert ':internal:' not in (project/'build.gradle').read_text(encoding='utf-8')
    assert '고객사 여러 업무 Domain' in next((project/'src/main/java').rglob('package-info.java')).read_text(encoding='utf-8')


def test_attach_is_explicit_per_domain_and_sync_is_idempotent(tmp_path:Path):
    mod.create_library(tmp_path,'company-common','com.acme.shared','com.acme.shared.common')
    member=domain(tmp_path,'member'); external=domain(tmp_path,'external')
    mod.attach_library(tmp_path,'company-common',['member'])
    assert 'company-common' in (member/'customer-libraries.properties').read_text(encoding='utf-8')
    assert not (external/'customer-libraries.properties').exists()
    dep=(member/'customer-library-dependencies.gradle').read_text(encoding='utf-8')
    assert 'com.acme.shared:company-common:1.0.0-SNAPSHOT' in dep
    first=dep
    mod.sync_libraries(tmp_path)
    assert (member/'customer-library-dependencies.gradle').read_text(encoding='utf-8')==first


def test_create_requires_explicit_customer_group(tmp_path:Path):
    try: mod.create_library(tmp_path,'company-common')
    except mod.CustomerLibraryError as exc: assert '--group' in str(exc)
    else: raise AssertionError('customer-owned group must be explicit')


def test_verify_rejects_cpf_internal_dependency(tmp_path:Path):
    mod.create_library(tmp_path,'company-common','com.acme.shared')
    build=tmp_path/'customer-libraries/company-common/build.gradle'
    build.write_text(build.read_text(encoding='utf-8')+'\nimplementation project(\':internal:secret\')\n',encoding='utf-8')
    try: mod.verify_library(tmp_path,'company-common')
    except mod.CustomerLibraryError as exc: assert 'Internal dependency' in str(exc)
    else: raise AssertionError('must fail closed')
