#!/usr/bin/env python3
"""Release qualification target trust boundary.

Trust policy and verifier public key are supplied by CI from outside the tested
workload. The workload may provide an attestation, but never its own trust root.
"""
from __future__ import annotations
import base64, hashlib, json, os, subprocess, tempfile
from pathlib import Path
from urllib.parse import urlparse

class ReleaseTargetTrustError(RuntimeError): pass

def _load_json(path: str, label: str):
    p=Path(path)
    if not p.is_file(): raise ReleaseTargetTrustError(f'{label} missing: {p}')
    try: return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: raise ReleaseTargetTrustError(f'{label} invalid JSON') from e

def verify_release_target(url: str, expected_head: str) -> dict:
    """Verify endpoint identity and signed deployment attestation using CI-owned trust inputs."""
    u=urlparse(url); host=(u.hostname or '').lower(); expected_head=expected_head.lower().strip()
    if not host or u.scheme not in {'http','https'}: raise ReleaseTargetTrustError('qualification target must be http/https')
    policy_path=os.getenv('CPF_RELEASE_TRUST_POLICY_JSON','').strip()
    key_path=os.getenv('CPF_RELEASE_ATTESTATION_PUBLIC_KEY','').strip()
    att_path=os.getenv('CPF_RELEASE_DEPLOYMENT_ATTESTATION_JSON','').strip()
    sig_path=os.getenv('CPF_RELEASE_DEPLOYMENT_ATTESTATION_SIG','').strip()
    if not all((policy_path,key_path,att_path,sig_path)):
        raise ReleaseTargetTrustError('CI-owned trust policy, public key, attestation and signature are required')
    policy=_load_json(policy_path,'release trust policy'); att=_load_json(att_path,'deployment attestation')
    allowed=policy.get('allowedTargets') or []
    target_id=str(att.get('deploymentId','')).strip()
    matches=[x for x in allowed if isinstance(x,dict) and x.get('deploymentId')==target_id and str(x.get('host','')).lower()==host]
    if len(matches)!=1: raise ReleaseTargetTrustError('target is not uniquely allowlisted by deploymentId+host')
    rule=matches[0]
    if bool(rule.get('requireHttps',True)) and u.scheme!='https': raise ReleaseTargetTrustError('release target requires HTTPS')
    if str(att.get('sourceSha','')).lower()!=expected_head: raise ReleaseTargetTrustError('attested sourceSha differs from checkout HEAD')
    artifact=str(att.get('artifactDigest','')).lower()
    if not artifact.startswith('sha256:') or len(artifact)!=71: raise ReleaseTargetTrustError('attested artifactDigest must be sha256')
    pinned=str(rule.get('artifactDigest','')).lower()
    if pinned and pinned!=artifact: raise ReleaseTargetTrustError('artifact digest differs from CI policy')
    payload=json.dumps(att,sort_keys=True,separators=(',',':'),ensure_ascii=False).encode('utf-8')
    sig=Path(sig_path).read_bytes()
    # Accept base64 text signatures as well as raw DER/signature bytes.
    try:
        txt=sig.decode('ascii').strip()
        if txt and all(c.isalnum() or c in '+/=_-' for c in txt): sig=base64.b64decode(txt)
    except Exception: pass
    with tempfile.TemporaryDirectory(prefix='cpf-trust-') as td:
        pp=Path(td)/'payload.json'; sp=Path(td)/'signature.bin'; pp.write_bytes(payload); sp.write_bytes(sig)
        cp=subprocess.run(['openssl','dgst','-sha256','-verify',key_path,'-signature',str(sp),str(pp)],capture_output=True,text=True)
        if cp.returncode!=0: raise ReleaseTargetTrustError('deployment attestation signature verification failed')
    return {'deploymentId':target_id,'artifactDigest':artifact,'sourceSha':expected_head,'host':host}

def self_test() -> None:
    # Fundamental regression: an unconfigured localhost fake can never be trusted.
    saved={k:os.environ.pop(k,None) for k in ['CPF_RELEASE_TRUST_POLICY_JSON','CPF_RELEASE_ATTESTATION_PUBLIC_KEY','CPF_RELEASE_DEPLOYMENT_ATTESTATION_JSON','CPF_RELEASE_DEPLOYMENT_ATTESTATION_SIG']}
    try:
        try: verify_release_target('http://localhost:18080/fake','0'*40)
        except ReleaseTargetTrustError: return
        raise AssertionError('unconfigured fake localhost unexpectedly trusted')
    finally:
        for k,v in saved.items():
            if v is not None: os.environ[k]=v
