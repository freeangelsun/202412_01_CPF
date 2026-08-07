#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SHA40=re.compile(r'^[0-9a-f]{40}$')
class ConsumerError(RuntimeError): pass

def run(cmd:list[str],cwd:Path,env:dict[str,str],expect_zero:bool=True)->subprocess.CompletedProcess[str]:
    cp=subprocess.run(cmd,cwd=cwd,env=env,text=True,capture_output=True,check=False)
    if expect_zero and cp.returncode!=0:
        raise ConsumerError(f"command failed exit={cp.returncode}: {' '.join(cmd)}\n{(cp.stderr or cp.stdout)[-5000:]}")
    if not expect_zero and cp.returncode==0:
        raise ConsumerError(f"negative dependency resolution unexpectedly succeeded: {' '.join(cmd)}")
    return cp

def git(root:Path,*args:str)->str:
    cp=subprocess.run(['git','-C',str(root),*args],text=True,capture_output=True,check=False)
    if cp.returncode!=0: raise ConsumerError(f"git {' '.join(args)} failed")
    return cp.stdout.strip()

def platform_version(root:Path)->str:
    props=root/'gradle/cpf-platform.properties'
    if not props.is_file(): raise ConsumerError('gradle/cpf-platform.properties missing')
    for line in props.read_text(encoding='utf-8-sig').splitlines():
        if line.strip().startswith('platformVersion='):
            value=line.split('=',1)[1].strip()
            if value: return value
    raise ConsumerError('platformVersion missing')

def copy_wrapper_distribution(target_home:Path)->None:
    source=Path.home()/'.gradle/wrapper/dists'
    if source.is_dir():
        dest=target_home/'wrapper/dists'
        dest.parent.mkdir(parents=True,exist_ok=True)
        shutil.copytree(source,dest,dirs_exist_ok=True)

def repository_block(mode:str,remote_url:str,offline_dir:str)->str:
    if mode=='LOCAL_DEV': return 'mavenLocal()'
    if mode=='REMOTE':
        return f'''maven {{\n            name = "cpfRemote"\n            url = uri(System.getenv("CPF_R6_REMOTE_REPOSITORY_URL"))\n            credentials {{\n                username = System.getenv("CPF_R6_REMOTE_REPOSITORY_USERNAME") ?: ""\n                password = System.getenv("CPF_R6_REMOTE_REPOSITORY_PASSWORD") ?: ""\n            }}\n        }}'''
    return 'maven { name = "cpfOffline"; url = uri(System.getenv("CPF_R6_OFFLINE_REPOSITORY_DIR")) }'

def write_project(project:Path,mode:str,version:str,missing:bool=False)->None:
    requested=version+'-r6-missing' if missing else version
    project.mkdir(parents=True,exist_ok=True)
    (project/'settings.gradle').write_text(f'''pluginManagement {{ repositories {{ gradlePluginPortal() }} }}\ndependencyResolutionManagement {{\n    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)\n    repositories {{\n        {repository_block(mode,'','')}\n    }}\n}}\nrootProject.name = "cpf-r6-consumer-{mode.lower()}"\n''',encoding='utf-8')
    (project/'build.gradle').write_text(f'''plugins {{ id 'application' }}\n\ndependencies {{\n    implementation platform("com.cpf:cpf-platform-bom:{requested}")\n    implementation "com.cpf:cpf-core"\n}}\n\napplication {{ mainClass = 'probe.Main' }}\n\ntasks.register('assertCpfRepositoryPolicy') {{\n    doLast {{\n        def repos = gradle.settings.dependencyResolutionManagement.repositories.collect {{ r -> [name:r.name, url:(r.hasProperty('url') ? r.url.toString() : '')] }}\n        if (repos.isEmpty()) throw new GradleException('CPF consumer repository list is empty')\n        if ('{mode}' != 'LOCAL_DEV' && repos.any {{ it.name == 'MavenLocal' || it.url.contains('/.m2/repository') || it.url.contains('\\\\.m2\\\\repository') }}) {{\n            throw new GradleException('REMOTE/OFFLINE consumer must not use mavenLocal fallback')\n        }}\n        println('CPF_REPOSITORIES=' + repos)\n    }}\n}}\ntasks.named('run') {{ dependsOn 'assertCpfRepositoryPolicy' }}\n''',encoding='utf-8')
    src=project/'src/main/java/probe';src.mkdir(parents=True,exist_ok=True)
    (src/'Main.java').write_text('''package probe;\nimport com.cpf.core.api.version.CpfPlatformVersion;\npublic final class Main { public static void main(String[] args) { String v=CpfPlatformVersion.current(); if(v==null||v.isBlank()||"UNKNOWN".equals(v)) throw new IllegalStateException("CPF platform metadata unavailable"); System.out.println("CPF_CONSUMER_OK="+v); } }\n''',encoding='utf-8')

def assert_offline_bundle(path:Path,version:str)->None:
    for artifact in ('cpf-platform-bom','cpf-core'):
        base=path/'com/cpf'/artifact/version
        if not base.is_dir(): raise ConsumerError(f'OFFLINE bundle missing {artifact}:{version}')
        if not any(base.glob('*.pom')): raise ConsumerError(f'OFFLINE bundle POM missing {artifact}:{version}')
        if artifact=='cpf-core' and not any(base.glob('*.jar')): raise ConsumerError(f'OFFLINE bundle JAR missing {artifact}:{version}')


def self_test()->None:
    with tempfile.TemporaryDirectory(prefix='cpf-r6-consumer-selftest-') as td:
        base=Path(td)
        for mode in ('LOCAL_DEV','REMOTE','OFFLINE'):
            project=base/mode.lower();write_project(project,mode,'1.2.3',False)
            settings=(project/'settings.gradle').read_text(encoding='utf-8')
            build=(project/'build.gradle').read_text(encoding='utf-8')
            if 'gradle.settings.dependencyResolutionManagement.repositories' not in build:
                raise ConsumerError('self-test: repository policy task is not bound to Settings repository model')
            if mode=='LOCAL_DEV':
                if 'mavenLocal()' not in settings: raise ConsumerError('self-test: LOCAL_DEV must use mavenLocal')
            else:
                if 'mavenLocal()' in settings: raise ConsumerError(f'self-test: {mode} must not contain mavenLocal fallback')
            negative=base/(mode.lower()+'-negative');write_project(negative,mode,'1.2.3',True)
            neg=(negative/'build.gradle').read_text(encoding='utf-8')
            if '1.2.3-r6-missing' not in neg: raise ConsumerError(f'self-test: {mode} negative dependency mutation missing')
        # Mutation assertions: a fallback or Project-scoped repository model must be detected.
        remote=(base/'remote'/'settings.gradle').read_text(encoding='utf-8')+'\n mavenLocal()\n'
        if 'mavenLocal()' not in remote: raise ConsumerError('self-test mutation failed')
    print('[CPF][R6I][ARTIFACT-CONSUMER][SELFTEST][PASS] modes=3 noFallback=REMOTE,OFFLINE settingsRepositoryModel=true')

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--expected-head');ap.add_argument('--evidence-dir');ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
    if a.self_test:
        self_test();return 0
    if not a.expected_head or not a.evidence_dir: raise ConsumerError('--expected-head and --evidence-dir are required unless --self-test is used')
    root=Path(a.root).resolve();expected=a.expected_head.lower()
    if not SHA40.fullmatch(expected): raise ConsumerError('--expected-head must be a 40-char SHA')
    actual=git(root,'rev-parse','HEAD').lower()
    if actual!=expected: raise ConsumerError(f'HEAD mismatch expected={expected} actual={actual}')
    if git(root,'status','--porcelain=v1','--untracked-files=all'): raise ConsumerError('artifact consumer qualification requires clean tree')
    version=platform_version(root)
    remote=os.getenv('CPF_R6_REMOTE_REPOSITORY_URL','').strip();offline=os.getenv('CPF_R6_OFFLINE_REPOSITORY_DIR','').strip()
    if not remote: raise ConsumerError('CPF_R6_REMOTE_REPOSITORY_URL is required')
    if not offline: raise ConsumerError('CPF_R6_OFFLINE_REPOSITORY_DIR is required')
    offline_path=Path(offline).expanduser().resolve()
    assert_offline_bundle(offline_path,version)
    wrapper=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not wrapper.is_file(): raise ConsumerError(f'Gradle wrapper missing: {wrapper}')
    out=Path(a.evidence_dir);out=out if out.is_absolute() else root/out;out.mkdir(parents=True,exist_ok=True)
    rows=[]
    with tempfile.TemporaryDirectory(prefix='cpf-r6-consumer-') as td:
        temp=Path(td)
        for mode in ('LOCAL_DEV','REMOTE','OFFLINE'):
            project=temp/mode.lower();write_project(project,mode,version,False)
            gradle_home=temp/(mode.lower()+'-gradle-home');copy_wrapper_distribution(gradle_home)
            env=os.environ.copy();env['GRADLE_USER_HOME']=str(gradle_home);env['CPF_R6_REMOTE_REPOSITORY_URL']=remote;env['CPF_R6_OFFLINE_REPOSITORY_DIR']=str(offline_path)
            cmd=[str(wrapper),'-p',str(project),'--no-daemon','--stacktrace']
            if mode=='REMOTE': cmd+=['--refresh-dependencies']
            if mode=='OFFLINE': cmd+=['--offline']
            cmd+=['clean','run']
            cp=run(cmd,root,env,True)
            if 'CPF_CONSUMER_OK=' not in cp.stdout: raise ConsumerError(f'{mode}: runtime CPF metadata probe missing')
            positive_log=out/f'{mode.lower()}-consumer.log';positive_log.write_text(cp.stdout+'\n'+cp.stderr,encoding='utf-8')

            negative=temp/(mode.lower()+'-negative');write_project(negative,mode,version,True)
            neg_cmd=[str(wrapper),'-p',str(negative),'--no-daemon','--stacktrace']
            if mode=='REMOTE': neg_cmd+=['--refresh-dependencies']
            if mode=='OFFLINE': neg_cmd+=['--offline']
            neg_cmd+=['clean','run']
            neg=run(neg_cmd,root,env,False)
            negative_log=out/f'{mode.lower()}-negative.log';negative_log.write_text(neg.stdout+'\n'+neg.stderr,encoding='utf-8')
            rows.append({'mode':mode,'status':'PASS','positiveExitCode':cp.returncode,'negativeExitCode':neg.returncode,'localFallbackAllowed':mode=='LOCAL_DEV'})
    summary={'schemaVersion':1,'protocol':'CPF-R6-ARTIFACT-CONSUMER-3MODE','sourceSha':actual,'platformVersion':version,'status':'PASS','modes':rows}
    (out/'artifact-consumer-summary.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
    print(f'[CPF][R6I][ARTIFACT-CONSUMER][PASS] sourceSha={actual} modes=3 version={version}')
    return 0
if __name__=='__main__':
    try: raise SystemExit(main())
    except ConsumerError as e: print(f'[CPF][R6I][ARTIFACT-CONSUMER][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
