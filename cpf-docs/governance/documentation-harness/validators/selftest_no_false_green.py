#!/usr/bin/env python3
import json,subprocess,sys,tempfile
from pathlib import Path
H=Path(__file__).resolve().parents[1]
def run(*args): return subprocess.run([sys.executable,*map(str,args)],capture_output=True,text=True)
def good_readme():
    rich=("CPF는 업무 시스템의 개발·실행·연계·배치·운영 방식을 하나의 계약으로 묶어 프로젝트마다 반복되던 결정을 줄인다. "
          "각 기능은 실제 Consumer와 호출 경로를 기준으로 선택하며 정상 처리뿐 아니라 오류, UNKNOWN, 재시도, 복구와 운영 추적까지 함께 설명한다. "
          "이 구조 덕분에 개발자는 업무 코드에 집중하고 운영자는 동일한 식별자와 상태 모델로 문제를 추적할 수 있다. ")
    arch=("Channel과 Frontend 요청은 환경에 따라 L4 + Gateway, L4 only, Gateway only 중 하나를 사용한다. Gateway는 외부 경계이고 내부 Business Domain 간 호출을 우회시키지 않는다. "
          "Business Domain에는 cpf-member, cpf-external, 선택형 cpf-backoffice가 있으며 Public Starter와 Capability가 필요한 기술 기능을 조합한다. cpf-core는 topology-independent 핵심 계약, cpf-common은 업무 공통을 소유한다. "
          "cpf-batch는 scheduler, worker, center-cut, agent Runtime으로 실행되고 cpf-admin과 ADM은 플랫폼 운영 상태를 확인한다. Backoffice Frontend/BFF Channel과 cpf-backoffice Business Domain은 분리한다. "
          "Oracle, PostgreSQL, MariaDB DB3는 같은 Canonical Schema Lifecycle을 따르고 X-Transaction-Id, X-System-Code, System6, instanceId, Trace가 거래와 인스턴스를 추적한다. ")
    cli=("일반 개발자는 Public CPF Unified CLI를 사용한다. 환경은 `cpf bootstrap`으로 준비하고 `cpf build`로 컴파일하며 `cpf test`로 검증한다. "
         "새 업무 Domain은 `cpf domain-new`로 생성하고 기존 Domain은 `cpf domain-sync`로 현재 Framework 계약에 맞춘다. `cpf run`으로 실행한 뒤 `cpf status`로 상태를 확인하고 `cpf stop`으로 종료한다. "
         "환경을 초기화할 때는 `cpf reset`, 문제 진단은 `cpf doctor`, 설치 버전은 `cpf version`, 명령 도움말은 `cpf help`를 사용한다. dev, verify, publish, release는 Internal Framework 개발용 namespace이며 일반 개발자 Public 흐름과 구분한다. ")
    value=("Same JVM과 Remote Domain 호출은 같은 CpfDomainClient 계약으로 다루며 Timeout 이후 결과가 확정되지 않으면 UNKNOWN으로 처리하고 Retry 가능 여부를 구분해 Reconcile과 복구로 닫는다. "
           "Oracle PostgreSQL MariaDB 세 Vendor를 동일 Lifecycle로 검증해 DB3 이식성을 유지한다. Batch scheduler worker center-cut agent는 재시작과 takeover를 추적한다. "
           "권한, 승인, Audit 감사는 위험 운영 조치와 연결되고 X-Transaction-Id와 instanceId Trace로 실제 실행 인스턴스까지 추적한다. Generator와 Generated Domain, domain-sync는 Source와 Sample의 계약을 맞춘다. "
           "cpf-admin ADM 운영 화면은 상태, 오류, 모니터링을 제공하고 Starter Capability Profile 선택은 필요한 기능만 조합하도록 한다. ")
    nav=("프레임워크 개발자 가이드, 배치 개발자 가이드, 운영자 매뉴얼, Gateway 개발 가이드, Specification PDF를 상황에 맞게 선택한다. README는 전체 방향과 시작점을 제공하고 상세 옵션과 API Reference는 각 PDF에서 확인한다. ")
    sections=[
      ('CPF가 무엇인가',(rich+value)*3),('전체 아키텍처',arch*4),('개발자가 시작하는 흐름',cli*4),('업무 Domain과 호출',value*3),('데이터와 DB3',value*3),('Batch와 비동기 실행',value*3),('운영·보안·실패 복구',value*4),('문서에서 더 알아보기',nav*4)
    ]
    out=['# Core Platform Framework','',rich*4,'','![CPF 전체 Architecture](cpf-docs/assets/product-docs/architecture.png)','',arch]
    for h,b in sections: out += ['',f'## {h}','',b]
    out += ['', 'CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.']
    return '\n'.join(out)
def main():
    errs=[]
    for script in ['validate_quality_fixtures.py','validate_false_green_prevention.py']:
        r=run(H/'validators'/script)
        if r.returncode: errs.append(script+': '+r.stdout+r.stderr)
    with tempfile.TemporaryDirectory() as td:
        d=Path(td); asset=d/'a.png'; asset.write_bytes(b'hash-bound')
        cfg=json.loads((H/'visual-human-quality.json').read_text())
        review={'reviewer':'selftest','reviewedAt':'2026-08-29T21:00:00+09:00','visuals':[{'artifactPath':'a.png','artifactSha256':'0'*64,'scanPass':True,'detailPass':True,'originalSizeReviewed':True,'usage':'README','surfacesReviewed':['source-image','readme-900','readme-1200','readme-1440'],'geometryFingerprint':'x','metrics':{k:0 for k in cfg['hardZeroMetrics']}}]}
        rp=d/'review.json'; rp.write_text(json.dumps(review),encoding='utf-8')
        r=run(H/'validators/validate_visual_human_review.py','--review',rp,'--asset-root',d)
        if r.returncode==0: errs.append('stale SHA visual review incorrectly accepted')
        # Prove README completeness gate has a reachable PASS and rejects a thin variant.
        src=d/'source'; (src/'cpf-tools/runtime/cli/java').mkdir(parents=True)
        (src/'cpf-tools/runtime/cli/java/CpfCli.java').write_text('private static final Set<String> PUBLIC = Set.of("bootstrap","domain-new","domain-sync","build","test","run","stop","reset","status","doctor","version","help");\nprivate static final Set<String> INTERNAL_NAMESPACES = Set.of("dev","verify","publish","release");',encoding='utf-8')
        good=d/'README-good.md'; good.write_text(good_readme(),encoding='utf-8')
        r=run(H/'validators/validate_readme_product_completeness.py',good,'--source-root',src)
        if r.returncode: errs.append('good README rejected: '+r.stdout+r.stderr)
        thin=d/'README-thin.md'; thin.write_text('# CPF\n\n## 구조\n\narchitecture\n\n## 개발\n\ncpf build cpf test\n',encoding='utf-8')
        r=run(H/'validators/validate_readme_product_completeness.py',thin,'--source-root',src)
        if r.returncode==0: errs.append('thin README incorrectly accepted')
    if errs:
        print('HARNESS_HARDENING_SELFTEST=FAIL'); [print('-',e) for e in errs]; return 1
    print('HARNESS_HARDENING_SELFTEST=PASS POSITIVE_AND_NEGATIVE=VERIFIED'); return 0
if __name__=='__main__': raise SystemExit(main())
