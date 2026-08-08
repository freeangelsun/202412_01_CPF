#!/usr/bin/env python3
from __future__ import annotations
import sys
from pathlib import Path as _TrustPath
sys.path.insert(0,str(_TrustPath(__file__).resolve().parents[1]))
from release_target_trust import verify_release_target, self_test as trust_self_test
import argparse,hashlib,json,os,re,subprocess,sys,urllib.error,urllib.request
from pathlib import Path
from urllib.parse import urlparse

REQUIRED={'xss','csrf','ssrf','idor','injection','path','archive','process','tenant','session','secret','replay','tamper','authorization'}
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

def attack_blob(case):
 vals=[str(case.get('attackPayload','')),str(case.get('url','')),str(case.get('body',''))]
 vals.extend(f'{k}:{v}' for k,v in (case.get('headers') or {}).items())
 return '\n'.join(vals).lower()

def require_semantics(case:dict)->None:
 cid=str(case.get('id','unknown'));cat=str(case.get('category','')).lower();attack=case.get('attack')
 if not isinstance(attack,dict) or len(str(attack.get('vector','')).strip())<3:raise SecurityError(f'{cid}: attack.vector is required')
 assertion=case.get('assertion')
 if not isinstance(assertion,dict) or not str(assertion.get('boundary','')).strip():raise SecurityError(f'{cid}: assertion.boundary is required')
 blob=attack_blob(case)
 def anytok(*xs):return any(x in blob for x in xs)
 if cat=='xss' and not anytok('<script','onerror','javascript:','<svg'):raise SecurityError(f'{cid}: XSS payload semantics missing')
 if cat=='csrf':
  if str(case.get('method','GET')).upper() in {'GET','HEAD','OPTIONS'} or not (case.get('headers') or {}).get('Origin'):raise SecurityError(f'{cid}: CSRF must forge a mutation origin')
  if not assertion.get('stateUnchanged'):raise SecurityError(f'{cid}: CSRF stateUnchanged assertion required')
 if cat=='ssrf' and not anytok('127.0.0.1','169.254.169.254','localhost','file://','gopher://'):raise SecurityError(f'{cid}: SSRF target semantics missing')
 if cat=='idor':
  if not attack.get('principalId') or not attack.get('foreignOwnerId') or attack.get('principalId')==attack.get('foreignOwnerId'):raise SecurityError(f'{cid}: IDOR principal/foreign owner mismatch required')
 if cat=='injection' and not anytok("' or ", 'union select',';drop ','${jndi:','sleep('):raise SecurityError(f'{cid}: injection payload semantics missing')
 if cat=='path' and not anytok('../','..%2f','%2e%2e','..\\'):raise SecurityError(f'{cid}: path traversal payload missing')
 if cat=='archive' and not (attack.get('entryPath') and any(x in str(attack.get('entryPath')).lower() for x in ('../','..\\','/etc/','c:\\'))):raise SecurityError(f'{cid}: archive traversal entryPath required')
 if cat=='process' and not anytok(';','&&','|','`','$(', '%comspec%'):raise SecurityError(f'{cid}: process injection payload missing')
 if cat=='tenant':
  if not attack.get('sourceTenant') or not attack.get('targetTenant') or attack.get('sourceTenant')==attack.get('targetTenant'):raise SecurityError(f'{cid}: cross-tenant identity required')
 if cat=='session' and not case.get('assert_session_rotated'):raise SecurityError(f'{cid}: session rotation assertion required')
 if cat=='secret' and not (case.get('forbid_response_regex') or assertion.get('secretAbsent')):raise SecurityError(f'{cid}: secret non-disclosure assertion required')
 if cat=='replay':
  if not attack.get('reusedRequestId') or not assertion.get('replayRejected'):raise SecurityError(f'{cid}: replay identity/rejection assertion required')
 if cat=='tamper':
  if not attack.get('tamperedField') or not attack.get('originalDigest') or not assertion.get('integrityRejected'):raise SecurityError(f'{cid}: tamper digest/integrity assertion required')
 if cat=='authorization':
  if not attack.get('requiredPermission') or not attack.get('principalWithoutPermission') or not assertion.get('authorizationRejected'):raise SecurityError(f'{cid}: authorization boundary semantics required')
 expected=case.get('expected_statuses')
 if str(case.get('kind','http')).lower()=='http' and (not isinstance(expected,list) or not expected):raise SecurityError(f'{cid}: explicit expected_statuses required')
 if cat in {'csrf','idor','tenant','replay','tamper','authorization'} and any(x in (expected or []) for x in (200,201,202,204)):
  raise SecurityError(f'{cid}: negative authorization/integrity case cannot accept success status')

def http_case(case):
 url=case['url'];safe_url(url);body=case.get('body');data=body.encode() if isinstance(body,str) else None;headers=case.get('headers') or {};req=urllib.request.Request(url,data=data,headers=headers,method=str(case.get('method','GET')).upper());status=0;resp_headers={};raw=b''
 try:
  with urllib.request.urlopen(req,timeout=float(case.get('timeout_seconds',20))) as r:status=r.status;resp_headers=dict(r.headers);raw=r.read(int(case.get('max_response_bytes',1048576)))
 except urllib.error.HTTPError as e:status=e.code;resp_headers=dict(e.headers);raw=e.read(int(case.get('max_response_bytes',1048576)))
 expected=case.get('expected_statuses')
 if status not in expected:raise SecurityError(f"{case['id']}: unexpected HTTP status {status}")
 text=raw.decode('utf-8','replace');forbidden=case.get('forbid_response_regex');required=case.get('require_response_regex')
 if forbidden and re.search(forbidden,text,re.I|re.S):raise SecurityError(f"{case['id']}: forbidden response pattern matched")
 if required and not re.search(required,text,re.I|re.S):raise SecurityError(f"{case['id']}: required response pattern missing")
 if case.get('assert_session_rotated'):
  supplied=str(headers.get('Cookie',''));set_cookie='\n'.join(v for k,v in resp_headers.items() if k.lower()=='set-cookie')
  if not set_cookie or (supplied and supplied in set_cookie):raise SecurityError(f"{case['id']}: session rotation evidence missing")
 return {'statusCode':status,'responseSha256':hashlib.sha256(raw).hexdigest(),'responseBytes':len(raw),'boundary':case['assertion']['boundary']}

def command_case(root,case):
 cmd=case.get('command')
 if not isinstance(cmd,list) or len(cmd)<2 or any(not isinstance(x,str) or not x for x in cmd):raise SecurityError(f"{case['id']}: command must contain executable and repository script")
 if cmd[0].lower() not in {'python','python3','pwsh','node'}:raise SecurityError(f"{case['id']}: command executable not allowed")
 script=(root/cmd[1]).resolve()
 if root not in script.parents or not script.is_file():raise SecurityError(f"{case['id']}: command script must exist under repository root")
 cp=subprocess.run([cmd[0],str(script),*cmd[2:]],cwd=root,text=True,capture_output=True,timeout=float(case.get('timeout_seconds',120)),check=False);expected=case.get('expected_exit_codes',[0])
 if cp.returncode not in expected:raise SecurityError(f"{case['id']}: unexpected exit {cp.returncode}")
 blob=(cp.stdout+'\n'+cp.stderr).encode();return {'exitCode':cp.returncode,'outputSha256':hashlib.sha256(blob).hexdigest(),'outputBytes':len(blob),'boundary':case['assertion']['boundary']}

def validate_corpus(raw:dict)->None:
 if raw.get('schemaVersion')!=2 or not isinstance(raw.get('cases'),list):raise SecurityError('security corpus schemaVersion=2 and cases[] are required')
 covered=set();ids=set()
 for original in raw['cases']:
  case=expand(original);cid=str(case.get('id','')).strip();cat=str(case.get('category','')).strip().lower()
  if not cid or cid in ids:raise SecurityError('case id missing/duplicate')
  ids.add(cid)
  if cat not in REQUIRED:raise SecurityError(f'{cid}: unsupported category {cat}')
  require_semantics(case);covered.add(cat)
 missing=sorted(REQUIRED-covered)
 if missing:raise SecurityError('missing categories: '+','.join(missing))

def self_test()->int:
 label_only={'schemaVersion':2,'cases':[{'id':c,'category':c,'kind':'http','url':'http://localhost/test','expected_statuses':[403],'attack':{'vector':'label-only'},'assertion':{'boundary':'test'}} for c in sorted(REQUIRED)]}
 try:validate_corpus(label_only)
 except SecurityError:pass
 else:raise SecurityError('label-only corpus mutation survived semantic validation')
 legacy={'schemaVersion':1,'cases':[]}
 try:validate_corpus(legacy)
 except SecurityError:pass
 else:raise SecurityError('legacy schema mutation survived')
 print(f'[CPF][SECNEG][PASS] selfTest=true semanticCategories={len(REQUIRED)}');return 0

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--corpus',default=os.getenv('CPF_R6_SECURITY_CORPUS',''));ap.add_argument('--output-json',type=Path);ap.add_argument('--self-test',action='store_true');ap.add_argument('--expected-head',default=os.getenv('CPF_EXPECTED_HEAD',''));a=ap.parse_args()
 if a.self_test:
  trust_self_test();return self_test()
 if not a.output_json:raise SecurityError('--output-json is required')
 root=Path(a.root).resolve()
 if len(a.expected_head.strip())!=40:raise SecurityError('expected checkout HEAD is required')
 if not a.corpus:raise SecurityError('CPF_R6_SECURITY_CORPUS / --corpus is required')
 corpus_path=Path(a.corpus);corpus_path=corpus_path if corpus_path.is_absolute() else root/corpus_path;raw=json.loads(corpus_path.read_text(encoding='utf-8-sig'));validate_corpus(raw)
 rows=[];covered=set();failures=[]
 for original in raw['cases']:
  try:
   case=expand(original);cid=str(case['id']);cat=str(case['category']).lower();kind=str(case.get('kind','http')).lower();covered.add(cat)
   if kind=='http': verify_release_target(str(case.get('url','')),a.expected_head)
   detail=http_case(case) if kind=='http' else command_case(root,case) if kind=='command' else (_ for _ in ()).throw(SecurityError(f'{cid}: unsupported kind {kind}'));rows.append({'id':cid,'category':cat,'kind':kind,'status':'PASS',**detail})
  except Exception as e:rows.append({'id':str(original.get('id','unknown')),'category':str(original.get('category','unknown')).lower(),'kind':str(original.get('kind','http')),'status':'FAIL','errorType':type(e).__name__});failures.append(str(e))
 summary={'schemaVersion':2,'protocol':'CPF-R6-SECURITY-NEGATIVE-SEMANTIC','status':'FAIL' if failures else 'PASS','requiredCategories':sorted(REQUIRED),'coveredCategories':sorted(covered),'cases':rows,'failureCount':len(failures)};text=json.dumps(summary,ensure_ascii=False,indent=2)+'\n'
 if SECRET_PATTERN.search(text):failures.append('secret pattern found in sanitized evidence')
 sensitive=[v for n,v in os.environ.items() if v and len(v)>=8 and any(x in n.upper() for x in ('TOKEN','PASSWORD','SECRET','PRIVATE_KEY','API_KEY'))]
 if any(v in text for v in sensitive):failures.append('configured secret value leaked into evidence')
 summary['failureCount']=len(failures);summary['status']='FAIL' if failures else 'PASS';a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 if failures:raise SecurityError('; '.join(failures[:12]))
 print(f'[CPF][R6][SECURITY-NEGATIVE][PASS] cases={len(rows)} categories={len(covered)}');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except (SecurityError,OSError,json.JSONDecodeError,subprocess.TimeoutExpired) as e:print(f'[CPF][R6][SECURITY-NEGATIVE][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
