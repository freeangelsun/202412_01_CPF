import importlib.util
import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

MODULE = Path(__file__).resolve().parents[3] / 'verification' / 'release_target_trust.py'
spec = importlib.util.spec_from_file_location('release_target_trust_under_test', MODULE)
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)


class ReleaseTargetTrustTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix='cpf-release-trust-test-')
        self.root = Path(self.tmp.name)
        self.private = self.root / 'private.pem'
        self.public = self.root / 'public.pem'
        subprocess.run(['openssl','genpkey','-algorithm','RSA','-pkeyopt','rsa_keygen_bits:2048','-out',str(self.private)], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(['openssl','pkey','-in',str(self.private),'-pubout','-out',str(self.public)], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        self.policy = self.root / 'policy.json'
        self.att = self.root / 'att.json'
        self.sig = self.root / 'att.sig'
        self.policy.write_text(json.dumps({'allowedTargets':[{'deploymentId':'d1','host':'localhost','requireHttps':False,'artifactDigest':'sha256:'+'a'*64}]}), encoding='utf-8')
        self.saved = {k: os.environ.get(k) for k in [
            'CPF_RELEASE_TRUST_POLICY_JSON','CPF_RELEASE_ATTESTATION_PUBLIC_KEY',
            'CPF_RELEASE_DEPLOYMENT_ATTESTATION_JSON','CPF_RELEASE_DEPLOYMENT_ATTESTATION_SIG']}
        os.environ['CPF_RELEASE_TRUST_POLICY_JSON']=str(self.policy)
        os.environ['CPF_RELEASE_ATTESTATION_PUBLIC_KEY']=str(self.public)
        os.environ['CPF_RELEASE_DEPLOYMENT_ATTESTATION_JSON']=str(self.att)
        os.environ['CPF_RELEASE_DEPLOYMENT_ATTESTATION_SIG']=str(self.sig)

    def tearDown(self):
        for k,v in self.saved.items():
            if v is None: os.environ.pop(k,None)
            else: os.environ[k]=v
        self.tmp.cleanup()

    def sign(self, payload):
        self.att.write_text(json.dumps(payload, sort_keys=True, separators=(',', ':'), ensure_ascii=False), encoding='utf-8')
        subprocess.run(['openssl','dgst','-sha256','-sign',str(self.private),'-out',str(self.sig),str(self.att)], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    def base(self):
        return {'deploymentId':'d1','artifactDigest':'sha256:'+'a'*64}

    def test_working_tree_sha256_is_canonical(self):
        identity='b'*64
        payload=self.base() | {'sourceSha':'UNAVAILABLE','sourceIdentitySha256':identity}
        self.sign(payload)
        result=mod.verify_release_target('http://localhost/probe', identity)
        self.assertEqual(identity,result['sourceIdentitySha256'])
        self.assertEqual('UNAVAILABLE',result['sourceSha'])

    def test_working_tree_sha256_mismatch_fails_closed(self):
        payload=self.base() | {'sourceSha':'UNAVAILABLE','sourceIdentitySha256':'b'*64}
        self.sign(payload)
        with self.assertRaisesRegex(mod.ReleaseTargetTrustError,'sourceIdentitySha256'):
            mod.verify_release_target('http://localhost/probe','c'*64)

    def test_working_tree_sha256_missing_fails_closed(self):
        payload=self.base() | {'sourceSha':'1'*40}
        self.sign(payload)
        with self.assertRaisesRegex(mod.ReleaseTargetTrustError,'sourceIdentitySha256'):
            mod.verify_release_target('http://localhost/probe','b'*64)

    def test_explicit_legacy_git_sha_is_compatible(self):
        sha='1'*40
        payload=self.base() | {'sourceSha':sha}
        self.sign(payload)
        result=mod.verify_release_target('http://localhost/probe',sha)
        self.assertEqual(sha,result['sourceSha'])
        self.assertNotIn('sourceIdentitySha256',result)

    def test_invalid_identity_length_rejected_before_trust(self):
        with self.assertRaisesRegex(mod.ReleaseTargetTrustError,'expected source identity'):
            mod.verify_release_target('http://localhost/probe','a'*63)

    def test_tampered_attestation_signature_fails(self):
        identity='b'*64
        payload=self.base() | {'sourceSha':'UNAVAILABLE','sourceIdentitySha256':identity}
        self.sign(payload)
        self.att.write_text(json.dumps(payload | {'deploymentId':'d2'}, sort_keys=True, separators=(',', ':')), encoding='utf-8')
        with self.assertRaises(mod.ReleaseTargetTrustError):
            mod.verify_release_target('http://localhost/probe',identity)

    def test_artifact_digest_policy_mismatch_fails(self):
        identity='b'*64
        payload=self.base() | {'artifactDigest':'sha256:'+'c'*64,'sourceSha':'UNAVAILABLE','sourceIdentitySha256':identity}
        self.sign(payload)
        with self.assertRaisesRegex(mod.ReleaseTargetTrustError,'artifact digest'):
            mod.verify_release_target('http://localhost/probe',identity)


if __name__ == '__main__':
    unittest.main()
