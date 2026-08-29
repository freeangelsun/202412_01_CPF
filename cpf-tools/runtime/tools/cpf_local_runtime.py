#!/usr/bin/env python3
"""Canonical cross-platform CPF local runtime engine.

Official users invoke this through the Java `cpf` CLI. Legacy OS scripts are thin
compatibility wrappers only; runtime semantics live here exactly once.
"""
from __future__ import annotations
import argparse, json, os, re, shutil, signal, socket, subprocess, sys, time
from pathlib import Path

ALLOWED_PROFILES={'local','dev','test','stg','prod'}
ALLOWED_MODES={'integrated','minimal','standard','full','integration'}

def fail(msg:str, code:int=2)->int:
    print(f'CPF_LOCAL_RUNTIME=FAIL message={msg}',file=sys.stderr); return code

def root_of(value:str|None)->Path:
    root=Path(value).resolve() if value else Path(__file__).resolve().parents[3]
    if not (root/'settings.gradle').is_file(): raise ValueError(f'CPF root invalid: {root}')
    return root

def read_props(path:Path)->dict[str,str]:
    out={}
    if not path.is_file(): return out
    for raw in path.read_text(encoding='utf-8-sig').splitlines():
        s=raw.strip()
        if not s or s.startswith('#') or '=' not in s: continue
        k,v=s.split('=',1); out[k.strip()]=v.strip()
    return out

def resolve_policy(root:Path,profile:str,mode:str)->dict[str,str]:
    common=read_props(root/'gradle/cpf-runtime/common.properties')
    env=read_props(root/f'gradle/cpf-runtime/{profile}.properties')
    module=read_props(root/'cpf-tools/runtime/cpf-local-runtime/cpf-resource.properties')
    merged={**common,**env,**module}
    if os.environ.get('CPF_WEB_XMS'): merged['runtime.web.xms']=os.environ['CPF_WEB_XMS']
    if os.environ.get('CPF_WEB_XMX'): merged['runtime.web.xmx']=os.environ['CPF_WEB_XMX']
    mode_key=f'runtime.web.mode.{mode}.xmx'
    if not module.get('runtime.web.xmx') and not module.get('runtime.xmx') and not os.environ.get('CPF_WEB_XMX') and merged.get(mode_key):
        merged['runtime.web.xmx']=merged[mode_key]
    return merged

def mb(v:str)->int:
    m=re.fullmatch(r'(\d+)([mMgG])',v or '')
    if not m: raise ValueError(f'invalid CPF memory value: {v}')
    n=int(m.group(1)); return n*1024 if m.group(2).lower()=='g' else n

def java25()->str:
    home=os.environ.get('CPF_JAVA25_HOME') or os.environ.get('JAVA_HOME')
    candidates=[]
    if home: candidates.append(Path(home)/'bin'/('java.exe' if os.name=='nt' else 'java'))
    found=shutil.which('java')
    if found: candidates.append(Path(found))
    for exe in candidates:
        if not exe.is_file(): continue
        cp=subprocess.run([str(exe),'-version'],text=True,encoding='utf-8',errors='replace',stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
        if re.search(r'version\s+"25(?:\.|\")',cp.stdout): return str(exe)
    raise RuntimeError('Java 25 required. Set CPF_JAVA25_HOME or JAVA_HOME.')

def pid_file(root:Path)->Path: return root/'build/cpf-local-runtime/local-web.pid'
def state_file(root:Path)->Path: return root/'build/cpf-local-runtime/runtime-state.json'

def pid_alive(pid:int)->bool:
    try: os.kill(pid,0); return True
    except OSError: return False

def port_free(host:str,port:int)->bool:
    s=socket.socket();
    try: s.bind((host,port)); return True
    except OSError: return False
    finally: s.close()

def start(root:Path,profile:str,mode:str,skip_build:bool)->int:
    if profile not in ALLOWED_PROFILES: return fail(f'unsupported profile={profile}')
    if mode not in ALLOWED_MODES: return fail(f'unsupported mode={mode}')
    java=java25(); port=int(os.environ.get('CPF_LOCAL_RUNTIME_PORT','8080')); host=os.environ.get('CPF_LOCAL_BIND_ADDRESS','127.0.0.1')
    if not 1 <= port <= 65535: return fail(f'invalid port={port}')
    pf=pid_file(root)
    if pf.is_file():
        try: pid=int(pf.read_text().strip())
        except ValueError: pid=-1
        if pid>0 and pid_alive(pid): return fail(f'already running pid={pid}',4)
        pf.unlink(missing_ok=True)
    if not port_free(host,port): return fail(f'port already in use: {host}:{port}',4)
    policy=resolve_policy(root,profile,mode)
    xms=policy.get('runtime.web.xms','250m'); xmx=policy.get('runtime.web.xmx','500m'); step=int(policy.get('heap.step.mb','250')); ceiling=int(policy.get('runtime.memory.ceiling.mb','1000'))
    if policy.get('runtime.memory.enforceCeiling','true').lower()=='true':
        for name,value in [('Xms',xms),('Xmx',xmx)]:
            n=mb(value)
            if n<step or n>ceiling or n%step: return fail(f'{name}={value} must use {step}MB increments and stay <={ceiling}MB')
        if mb(xms)>mb(xmx): return fail('Xms must be <= Xmx')
    gradle=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not gradle.is_file(): return fail(f'gradle wrapper missing: {gradle}',69)
    env={**os.environ,'JAVA_HOME':str(Path(java).parent.parent),'JAVA_TOOL_OPTIONS':(os.environ.get('JAVA_TOOL_OPTIONS','')+' -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8').strip()}
    if not skip_build:
        cmd=[str(gradle),'-PcpfIncludeGeneratedDomains=true',f'-PcpfResourceProfile={profile}',':runtime:local:bootJar','--no-daemon','--no-parallel']
        cp=subprocess.run(cmd,cwd=root,env=env)
        if cp.returncode: return fail(f'bootJar failed exit={cp.returncode}',cp.returncode)
    jars=sorted((root/'cpf-tools/runtime/cpf-local-runtime/build/libs').glob('*-local-web.jar'))
    if not jars: return fail('cpf-local-runtime bootJar not found',3)
    logs=root/'build/cpf-local-runtime/logs'; logs.mkdir(parents=True,exist_ok=True); pf.parent.mkdir(parents=True,exist_ok=True)
    out=(logs/'LOCAL_WEB.out.log').open('ab',buffering=0); err=(logs/'LOCAL_WEB.err.log').open('ab',buffering=0)
    cmd=[java,f'-Xms{xms}',f'-Xmx{xmx}',f'-XX:MaxMetaspaceSize={policy.get("runtime.jvm.maxMetaspace","256m")}',f'-XX:MaxDirectMemorySize={policy.get("runtime.jvm.maxDirectMemory","128m")}',f'-XX:ReservedCodeCacheSize={policy.get("runtime.jvm.reservedCodeCache","128m")}',f'-Xss{policy.get("runtime.jvm.threadStack","1m")}','-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8','-jar',str(jars[-1]),f'--spring.profiles.active=local,local-{mode}',f'--server.address={host}',f'--server.port={port}','--cpf.environment=local','--cpf.local.runtime.enabled=true','--cpf.local.modules.domains.enabled=true','--cpf.local.modules.domains.auto-discover=true']
    creation=0
    if os.name=='nt': creation=getattr(subprocess,'CREATE_NEW_PROCESS_GROUP',0)|getattr(subprocess,'DETACHED_PROCESS',0)
    p=subprocess.Popen(cmd,cwd=root,env=env,stdin=subprocess.DEVNULL,stdout=out,stderr=err,start_new_session=(os.name!='nt'),creationflags=creation)
    pf.write_text(str(p.pid)+'\n',encoding='ascii')
    state={'pid':p.pid,'host':host,'port':port,'mode':mode,'resourceProfile':profile,'xms':xms,'xmx':xmx,'startedAt':time.time(),'jar':str(jars[-1].relative_to(root))}
    state_file(root).write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f'CPF_LOCAL_RUNTIME=STARTED pid={p.pid} address={host} port={port} mode={mode} resourceProfile={profile} Xms={xms} Xmx={xmx}')
    return 0

def status(root:Path)->int:
    pf=pid_file(root)
    if not pf.is_file(): print('CPF_STATUS=STOPPED'); return 0
    try: pid=int(pf.read_text().strip())
    except ValueError: print('CPF_STATUS=STALE reason=invalid-pid'); return 2
    if pid_alive(pid): print(f'CPF_STATUS=RUNNING pid={pid}'); return 0
    print(f'CPF_STATUS=STALE pid={pid}'); return 2

def stop(root:Path)->int:
    pf=pid_file(root)
    if not pf.is_file(): print('CPF_LOCAL_RUNTIME=STOPPED already=true'); return 0
    try: pid=int(pf.read_text().strip())
    except ValueError: pid=-1
    if pid>0 and pid_alive(pid):
        try: os.kill(pid,signal.SIGTERM)
        except OSError: pass
        for _ in range(50):
            if not pid_alive(pid): break
            time.sleep(.1)
        if pid_alive(pid):
            try: os.kill(pid,signal.SIGKILL if hasattr(signal,'SIGKILL') else signal.SIGTERM)
            except OSError: pass
    pf.unlink(missing_ok=True); state_file(root).unlink(missing_ok=True)
    print(f'CPF_LOCAL_RUNTIME=STOPPED pid={pid}')
    return 0

def reset(root:Path,confirm:bool)->int:
    if not confirm: return fail('reset requires --confirm',69)
    stop(root)
    for rel in ('build/cpf-local-runtime','build/cpf-bootstrap','build/cpf-local'):
        target=(root/rel).resolve()
        if root not in target.parents or 'build' not in target.parts: return fail(f'unsafe reset target={target}')
        if target.exists(): shutil.rmtree(target)
    print('CPF_LOCAL_RUNTIME=RESET_COMPLETED'); return 0

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('command',choices=['start','status','stop','reset']); ap.add_argument('--root'); ap.add_argument('--profile',default=os.environ.get('CPF_RESOURCE_PROFILE','local')); ap.add_argument('--mode',default=os.environ.get('CPF_LOCAL_MODE','integrated')); ap.add_argument('--skip-build',action='store_true'); ap.add_argument('--confirm',action='store_true'); ns=ap.parse_args()
    try: root=root_of(ns.root)
    except Exception as e: return fail(str(e))
    try:
        if ns.command=='start': return start(root,ns.profile,ns.mode,ns.skip_build)
        if ns.command=='status': return status(root)
        if ns.command=='stop': return stop(root)
        return reset(root,ns.confirm)
    except Exception as e: return fail(str(e),69 if 'required' in str(e).lower() else 1)
if __name__=='__main__': raise SystemExit(main())
