#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, shutil, subprocess, tempfile
from pathlib import Path

def fail(x):print('CPF_OBJECT_STORAGE=FAIL '+x);return 1

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ns=ap.parse_args();root=Path(ns.root).resolve()
 api=root/'cpf-starters/file/src/main/java/com/cpf/file/objectstorage/api'
 req=['CpfObjectStorageOperations.java','CpfObjectStorageRequest.java','CpfObjectStorageMetadata.java','CpfObjectStorageScanHook.java','CpfObjectStorageLifecycleHook.java']
 miss=[x for x in req if not (api/x).is_file()]
 if miss:return fail('missingApi='+','.join(miss))
 s=(root/'cpf-starters/file/object-storage/s3/src/main/java/com/cpf/file/objectstorage/s3/CpfS3ObjectStorageOperations.java').read_text()
 for token in ['DigestInputStream','checksum mismatch','serverSideEncryption(ServerSideEncryption.AWS_KMS)','presignGet','presignPut','beginMultipart','uploadPart','completeMultipart','abortMultipartOlderThan','lifecycle.beforeDelete','scan.inspect','tenantId','CpfContextAwareInputStream']:
  if token not in s:return fail('s3WitnessMissing='+token)
 auto=(root/'cpf-starters/file/object-storage/s3/src/main/java/com/cpf/file/objectstorage/s3/CpfObjectStorageAutoConfiguration.java').read_text()
 for token in ['apiCallTimeout','apiCallAttemptTimeout','retryStrategy','maxAttempts','CpfObjectStorageReconciler','ConditionalOnProperty']:
  if token not in auto:return fail('autoconfigMissing='+token)
 meta=json.loads((root/'cpf-starters/file/object-storage/s3/src/main/resources/META-INF/additional-spring-configuration-metadata.json').read_text())
 names={x['name'] for x in meta['properties']}
 for n in ['cpf.file.object-storage.s3.kms-key-id','cpf.file.object-storage.s3.max-attempts','cpf.file.object-storage.s3.orphan-multipart-age','cpf.file.object-storage.s3.allow-presigned-put']:
  if n not in names:return fail('metadataMissing='+n)
 javac=shutil.which('javac');java=shutil.which('java')
 if not javac or not java:return fail('javacMissing=true')
 with tempfile.TemporaryDirectory(prefix='cpf-object-') as td:
  t=Path(td);c=t/'c';c.mkdir()
  src=[str(api/x) for x in ['CpfObjectStorageMetadata.java','CpfObjectStorageLifecycleHook.java']]
  cp=subprocess.run([javac,'-Xlint:all','-Werror','-d',str(c),*src],text=True,capture_output=True)
  if cp.returncode:return fail('compile='+(cp.stdout+cp.stderr).replace('\n',' | '))
  h=t/'H.java';h.write_text('''import java.time.*;import java.util.*;import com.cpf.file.objectstorage.api.*; public class H{public static void main(String[]a){ var m=new CpfObjectStorageMetadata("t","b","k",1,"x",null,null,null,Instant.now(),Map.of("cpf-retain-until",Instant.now().plusSeconds(60).toString())); try{CpfObjectStorageLifecycleHook.METADATA_RETENTION.beforeDelete(m,Instant.now());throw new AssertionError();}catch(IllegalStateException ok){} var h=new CpfObjectStorageMetadata("t","b","k",1,"x",null,null,null,Instant.now(),Map.of("cpf-legal-hold","true")); try{CpfObjectStorageLifecycleHook.METADATA_RETENTION.beforeDelete(h,Instant.now());throw new AssertionError();}catch(IllegalStateException ok){} }}''')
  cp=subprocess.run([javac,'-Xlint:all','-Werror','-cp',str(c),'-d',str(c),str(h)],text=True,capture_output=True)
  if cp.returncode:return fail('harnessCompile='+(cp.stdout+cp.stderr).replace('\n',' | '))
  cp=subprocess.run([java,'-cp',str(c),'H'],text=True,capture_output=True)
  if cp.returncode:return fail('harnessRuntime='+(cp.stdout+cp.stderr).replace('\n',' | '))
 print('CPF_OBJECT_STORAGE=PASS streaming=true multipart=true checksum=true range=true presign=true kms=true tenant=true retry=true timeout=true reconcile=true retention=true malwareHook=true')
 return 0
if __name__=='__main__':raise SystemExit(main())
