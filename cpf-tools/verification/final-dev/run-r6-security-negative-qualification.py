#!/usr/bin/env python3
from __future__ import annotations
import argparse,hashlib,json,os,re,subprocess,sys,urllib.error,urllib.request
from pathlib import Path
from urllib.parse import urlparse

REQUIRED={'xss','csrf','ssrf','idor','injection','path','archive','process','tenant','session','secret'}
ENV_TOKEN=re.compile(r'\$\{([A-Z][A-Z0-9_]*)\}')
SECRET_PATTERN=re.compile(r'(?i)(password\s*[=:]\s*\S+|authorization\s*[:=]\s*bearer\s+\S+|api[_-]?key\s*[=:]\s*\S+|secret\s*[=:]\s*\S+)')
class SecurityError(RuntimeError):pass

def expand(v):
 if isinstance(v,str):
  def sub(m):
   raw=os.getenv(m.group(1))
   if raw is None or not raw.strip():raise SecurityError(f'missing environment variable: {m.group(1)}')
   return raw.strip()
  return ENV_TOKEN.sub(sub,v)
 if isinstance(v,list):return [expand(x) for x in v]
 if isinstance(v,dict):return {k:expand(x) for k,x in v.items()}
 return v

def safe_url(url):
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:raise SecurityError('HTTP case URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:raise SecurityError('non-local HTTP negative case must use https')

def http_case(case):
 url=case['url'];safe_url(url);body=case.get('body');data=body.encode() if isinstance(body,str) else None
 headers=case.get('headers') or {}
 req=urllib.request.Request(url,data=data,headers=headers,method=str(case.get('method','GET')).upper())
 status=0;resp_headers={};raw=b''
 try:
  with urllib.request.urlopen(req,timeout=float(case.get('timeout_seconds',20))) as r: status=r.status;resp_headers=dict(r.headers);raw=r.read(int(case.get('max_response_bytes',1048576)))
 except urllib.error.HTTPError as e: status=e.code;resp_headers=dict(e.headers);raw=e.read(int(case.get('max_response_bytes',1048576)))
 expected=case.get('expected_statuses')
 if not isinstance(expected,list) or not expected or status not in expected:raise SecurityError(f"{case['id']}: unexpected HTTP status {status}")
 text=raw.decode('utf-8','replace')
 forbidden=case.get('forbid_response_regex')
 if forbidden and re.search(forbidden,text,re.I|re.S):raise SecurityError(f"{case['id']}: forbidden response pattern matched")
 required=case.get('require_response_regex')
 if required and not re.search(required,text,re.I|re.S):raise SecurityError(f"{case['id']}: required response pattern missing")
 if case.get('assert_session_rotated'):
  supplied=str(headers.get('Cookie',''))
  set_cookie='\n'.join(v for k,v in resp_headers.items() if k.lower()=='set-cookie')
  if not set_cookie or (supplied and supplied in set_cookie):raise SecurityError(f"{case['id']}: session rotation evidence missing")
 return {'statusCode':status,'responseSha256':hashlib.sha256(raw).hexdigest(),'responseBytes':len(raw)}

def command_case(root,case):
 cmd=case.get('command')
 if not isinstance(cmd,list) or len(cmd)<2 or any(not isinstance(x,str) or not x for x in cmd):raise SecurityError(f"{case['id']}: command must contain executable and repository script")
 if cmd[0].lower() not in {'python','python3','pwsh','node'}:raise SecurityError(f"{case['id']}: command executable not allowed")
 script=(root/cmd[1]).resolve()
 if root not in script.parents or not script.is_file():raise SecurityError(f"{case['id']}: command script must exist under repository root")
 cp=subprocess.run([cmd[0],str(script),*cmd[2:]],cwd=root,text=True,capture_output=True,timeout=float(case.get('timeout_seconds',120)),check=False)
 expected=case.get('expected_exit_codes',[0])
 if cp.returncode not in expected:raise SecurityError(f"{case['id']}: unexpected exit {cp.returncode}")
 blob=(cp.stdout+'\n'+cp.stderr).encode()
 return {'exitCode':cp.returncode,'outputSha256':hashlib.sha256(blob).hexdigest(),'outputBytes':len(blob)}

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--corpus',default=os.getenv('CPF_R6_SECURITY_CORPUS',''));ap.add_argument('--output-json',required=True,type=Path);a=ap.parse_args()
 root=Path(a.root).resolve()
 if not a.corpus:raise SecurityError('CPF_R6_SECURITY_CORPUS / --corpus is required')
 corpus_path=Path(a.corpus);corpus_path=corpus_path if corpus_path.is_absolute() else root/corpus_path
 raw=json.loads(corpus_path.read_text(encoding='utf-8-sig'))
 if raw.get('schemaVersion')!=1 or not isinstance(raw.get('cases'),list):raise SecurityError('security corpus schemaVersion=1 and cases[] are required')
 rows=[];covered=set();failures=[]
 ids=set()
 for original in raw['cases']:
  try:
   case=expand(original);cid=str(case.get('id','')).strip();cat=str(case.get('category','')).strip().lower();kind=str(case.get('kind','http')).lower()
   if not cid or cid in ids:raise SecurityError('case id missing/duplicate')
   ids.add(cid)
   if cat not in REQUIRED:raise SecurityError(f'{cid}: unsupported category {cat}')
   covered.add(cat)
   detail=http_case(case) if kind=='http' else command_case(root,case) if kind=='command' else (_ for _ in ()).throw(SecurityError(f'{cid}: unsupported kind {kind}'))
   rows.append({'id':cid,'category':cat,'kind':kind,'status':'PASS',**detail})
  except Exception as e:
   cid=str(original.get('id','unknown'));cat=str(original.get('category','unknown')).lower();rows.append({'id':cid,'category':cat,'kind':str(original.get('kind','http')),'status':'FAIL','errorType':type(e).__name__});failures.append(f'{cid}:{e}')
 missing=sorted(REQUIRED-covered)
 if missing:failures.append('missing categories: '+','.join(missing))
 summary={'schemaVersion':1,'protocol':'CPF-R6-SECURITY-NEGATIVE','status':'FAIL' if failures else 'PASS','requiredCategories':sorted(REQUIRED),'coveredCategories':sorted(covered),'cases':rows,'failureCount':len(failures)}
 text=json.dumps(summary,ensure_ascii=False,indent=2)+'\n'
 # Evidence itself must not contain obvious credentials or any configured high-risk secret value.
 if SECRET_PATTERN.search(text):failures.append('secret pattern found in sanitized evidence')
 sensitive_values=[]
 for name,value in os.environ.items():
  upper=name.upper()
  if value and len(value)>=8 and any(x in upper for x in ('TOKEN','PASSWORD','SECRET','PRIVATE_KEY','API_KEY')):sensitive_values.append(value)
 if any(v in text for v in sensitive_values):failures.append('configured secret value leaked into evidence')
 summary['failureCount']=len(failures);summary['status']='FAIL' if failures else 'PASS';text=json.dumps(summary,ensure_ascii=False,indent=2)+'\n'
 a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(text,encoding='utf-8')
 if failures:raise SecurityError('; '.join(failures[:12]))
 print(f'[CPF][R6I][SECURITY-NEGATIVE][PASS] cases={len(rows)} categories={len(covered)}');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except (SecurityError,OSError,json.JSONDecodeError,subprocess.TimeoutExpired) as e:print(f'[CPF][R6I][SECURITY-NEGATIVE][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
