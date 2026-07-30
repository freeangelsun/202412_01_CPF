[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$Root = "."
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$rootPath = (Resolve-Path -LiteralPath $Root).Path
$utf8Bom = [System.Text.UTF8Encoding]::new($true)
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Read-Text([string]$RelativePath) {
    $path = Join-Path $rootPath $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required file is missing: $RelativePath"
    }
    return [System.IO.File]::ReadAllText($path)
}

function Write-Text([string]$RelativePath, [string]$Content, [bool]$Bom) {
    $path = Join-Path $rootPath $RelativePath
    $encoding = if ($Bom) { $utf8Bom } else { $utf8NoBom }
    [System.IO.File]::WriteAllText($path, $Content, $encoding)
}

function Replace-Exact([string]$RelativePath, [string]$Old, [string]$New, [bool]$Bom = $false) {
    $content = Read-Text $RelativePath
    if ($content.Contains($New)) {
        Write-Host "ALREADY_APPLIED $RelativePath"
        return
    }
    $count = ([regex]::Matches($content, [regex]::Escape($Old))).Count
    if ($count -ne 1) {
        throw "Expected exactly one baseline match in $RelativePath but found $count. Refuse non-deterministic patch."
    }
    Write-Text $RelativePath ($content.Replace($Old, $New)) $Bom
    Write-Host "PATCHED $RelativePath"
}

function Append-Section([string]$RelativePath, [string]$Marker, [string]$Section) {
    $content = Read-Text $RelativePath
    if ($content.Contains($Marker)) {
        Write-Host "ALREADY_APPLIED $RelativePath"
        return
    }
    $updated = $content.TrimEnd() + "`r`n`r`n" + $Section.Trim() + "`r`n"
    Write-Text $RelativePath $updated $false
    Write-Host "APPENDED $RelativePath"
}

$reqPath = "cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv"
$oldReq = 'OSS-BATCH-001,P1,Batch,Spring Batch 실행 Metadata 엔진 범위 도입,"cpf-batch control/scheduler/worker, job definition, execution context, restart",Job/Step/Execution/Restart 범용 기능을 CPF 자체 runtime이 중복 구현할 위험,Spring Batch는 Job/Step/JobRepository/ExecutionContext/Restart/Partition을 담당하고 CPF는 승인·Topology·Agent·Unknown·File/Shell·Fencing을 유지,동일 Job 시나리오 parity 후 metadata owner 이관; CPF definition/version→Spring Batch mapping; current custom duplicate metadata 제거; operator UI와 restart/stop 연결,Spring Batch dependency만 추가; 기존 execution metadata와 이중 정본; 승인·unknown을 Spring Batch 기본 상태로 축약,dual owner/table scanner; JobRepository schema lifecycle; job/step mapping contract; legacy metadata access 0,"restart after crash, skip/retry, partition, stop/abandon, response loss, remote worker failure, operator reconciliation","Job/Step DB rows, CPF approval/attempt ledger link, restart evidence, parity matrix",Batch,OSS-DB-001,OPEN_REVALIDATION,최신 exact SHA에서 구현·전체 소비자 이관·Legacy 제거·필수 실행 증적이 모두 확인되기 전 완료 금지'
$newReq = 'OSS-BATCH-001,P0,Batch,Spring Batch 전체 Primary Execution Engine 전환,"cpf-batch control/scheduler/center-cut/worker, 모든 job definition, file/db/api/shell execution, execution context, restart",센터컷·분산 Worker·File/Shell 실행과 Job/Step/Execution/Restart 범용 기능을 CPF 자체 runtime이 중복 구현할 위험,"Spring Batch가 모든 Batch 실행 생명주기·Metadata·Checkpoint·Restart·Flow·Parallel·Partition·Remote Execution을 담당하고 CPF는 승인·권한·Topology·Agent 보안·Fencing·Audit·UNKNOWN_RESULT 대사 Control Plane만 유지","모든 실제 Batch Consumer를 Spring Batch Job/Step으로 이관; Tasklet/Chunk/Reader/Processor/Writer 사용; JobRepository·ExecutionContext 정본화; JobOperator 기반 start/stop/restart/abandon; center-cut은 Parallel/Partition/Remote Step 계열로 전환; scheduler trigger→Spring Batch handoff; CPF ledger에 JobInstance/JobExecution/StepExecution ID 연결; 중복 자체 엔진과 metadata 제거","Spring Batch dependency·Wrapper·Sample만 추가; 일부 Job만 이관; 기존 center-cut/worker/execution engine과 이중 정본; 자체 restart/checkpoint/partition dispatcher 유지; 승인·unknown을 Spring Batch 기본 상태로 축약","duplicate engine/metadata/table scanner; JobRepository 3DB lifecycle; all consumer Job/Step mapping; custom center-cut/partition/restart access 0; scheduler-owned job state 0","Tasklet/Chunk; flow/conditional/parallel; local partition; remote partition/chunk/step; center-cut; worker crash; manager restart; checkpoint resume; duplicate launch; stop/restart/abandon; response loss and reconciliation; File/Shell crash; Oracle/PostgreSQL/MariaDB repository","Job/Step DB rows, CPF approval/attempt ledger linkage, all-consumer migration matrix, legacy deletion manifest, restart/remote execution/center-cut/3DB evidence",Batch,"OSS-DB-001,OSS-MSG-001",OPEN_REVALIDATION,최신 exact SHA에서 모든 Consumer 이관·자체 실행 엔진 제거·센터컷과 원격 실행·장애 복구·3DB Evidence가 모두 확인되기 전 완료 금지'
Replace-Exact $reqPath $oldReq $newReq $true

$scenarioPath = "cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv"
$oldS33 = 'QA32-S033,OSS-BATCH-001,P1,P1_OSS_FIRST_MIGRATION,POSITIVE_VERTICAL_SLICE,Spring Batch 실행 Metadata 엔진 범위 도입 정상 수직 Slice,"Repository HEAD와 시작 SHA를 기록하고 cpf-batch control/scheduler/worker, job definition, execution context, restart의 현재 구현 및 consumer inventory를 확보한다.",동일 Job 시나리오 parity 후 metadata owner 이관; CPF definition/version→Spring Batch mapping; current custom duplicate metadata 제거; operator UI와 restart/stop 연결을 구현한다. UI/API/Owner/DB 또는 State/Runtime Consumer/실패·복구/Audit가 해당되는 경우 모두 연결한 뒤 실제 실행한다.,Spring Batch는 Job/Step/JobRepository/ExecutionContext/Restart/Partition을 담당하고 CPF는 승인·Topology·Agent·Unknown·File/Shell·Fencing을 유지이 실제 Primary Path에서 동작하며 legacy fallback 없이 성공한다.,"Job/Step DB rows, CPF approval/attempt ledger link, restart evidence, parity matrix",Y,NOT_EXECUTED'
$newS33 = 'QA32-S033,OSS-BATCH-001,P0,P1_OSS_FIRST_MIGRATION,POSITIVE_VERTICAL_SLICE,Spring Batch 전체 Primary Execution Engine 정상 수직 Slice,"Repository HEAD와 시작 SHA를 기록하고 cpf-batch control/scheduler/center-cut/worker와 모든 File/DB/API/Shell Job Consumer 및 자체 실행 엔진 inventory를 확보한다.","모든 실제 Consumer를 Spring Batch Job/Step으로 이관하고 Tasklet/Chunk·JobRepository·ExecutionContext·JobOperator·Flow·Parallel·Local/Remote Partition 또는 Remote Step을 사용한다. Scheduler는 Trigger만 수행하고 CPF 원장과 Spring Batch 실행 ID를 연결한다. 센터컷과 Worker 실행을 실제 Runtime으로 수행한다.","Spring Batch가 CPF 배치 실행의 단일 Primary Engine으로 동작하고 CPF는 승인·권한·Topology·Agent 보안·Fencing·Audit·UNKNOWN_RESULT 대사 Control Plane만 유지한다. 자체 실행 Legacy나 fallback 없이 성공한다.","Job/Step DB rows, all-consumer migration matrix, CPF ledger linkage, center-cut/remote execution trace, legacy deletion manifest",Y,NOT_EXECUTED'
Replace-Exact $scenarioPath $oldS33 $newS33 $true

$oldS34 = 'QA32-S034,OSS-BATCH-001,P1,P1_OSS_FIRST_MIGRATION,NEGATIVE_FAILURE_INJECTION,Spring Batch 실행 Metadata 엔진 범위 도입 실패·우회 차단,"Repository HEAD와 시작 SHA를 기록하고 cpf-batch control/scheduler/worker, job definition, execution context, restart의 현재 구현 및 consumer inventory를 확보한다.","다음 실패·오용 조건을 fixture 또는 실제 환경으로 주입한다: restart after crash, skip/retry, partition, stop/abandon, response loss, remote worker failure, operator reconciliation. 또한 금지된 부분 구현 방식(Spring Batch dependency만 추가; 기존 execution metadata와 이중 정본; 승인·unknown을 Spring Batch 기본 상태로 축약)을 의도적으로 재현한다.","위험 입력·불완전 구현·정책 우회는 fail-closed로 차단되고, 상태·오류·원장·복구 가능성이 명확히 기록된다.","Negative fixture 결과, Exit Code, typed error/state, audit/ledger, Job/Step DB rows, CPF approval/attempt ledger link, restart evidence, parity matrix",Y,NOT_EXECUTED'
$newS34 = 'QA32-S034,OSS-BATCH-001,P0,P1_OSS_FIRST_MIGRATION,NEGATIVE_FAILURE_INJECTION,Spring Batch 전체 Primary Execution Engine 실패·우회 차단,"Repository HEAD와 시작 SHA를 기록하고 모든 Batch Consumer·센터컷·Worker·Scheduler handoff·JobRepository의 현재 구현과 Legacy inventory를 확보한다.","restart after crash; checkpoint resume; skip/retry; conditional/parallel flow; local/remote partition; remote worker crash; manager restart; stop/restart/abandon; duplicate launch; broker delay/duplicate; response loss; File/Shell crash; operator reconciliation; 3DB JobRepository lifecycle을 실제로 주입한다. 또한 dependency/Wrapper/Sample만 추가, 일부 Job만 이관, 기존 center-cut·restart·partition·metadata 이중 정본을 의도적으로 재현한다.","부분 구현과 이중 정본은 Gate에서 차단되고, Spring Batch 상태와 CPF UNKNOWN_RESULT·Fencing·Audit가 연결되어 실제 상태를 대사·복구한다.","Negative fixture 결과, Exit Code, Job/Step DB rows, CPF ledger linkage, restart/remote/center-cut/3DB evidence, duplicate side effect 0",Y,NOT_EXECUTED'
Replace-Exact $scenarioPath $oldS34 $newS34 $true

$oldS35 = 'QA32-S035,OSS-BATCH-001,P1,P1_OSS_FIRST_MIGRATION,COMPLETION_GATE,Spring Batch 실행 Metadata 엔진 범위 도입 완료 판정 Gate,모든 변경이 Working Tree에 존재하고 Result Matrix 초안이 작성되어 있다.,"정적·Architecture Gate(dual owner/table scanner; JobRepository schema lifecycle; job/step mapping contract; legacy metadata access 0)를 실행하고 동일 패턴을 repository 전체, generator template, generated source, published artifact에서 재검색한다.","미구현·부분 구현·미검증 행이 남으면 완료가 거부된다. 모든 소비자 이관, legacy 제거, 실행 Evidence가 있을 때만 완료된다.","Gate command/환경/시작·종료시각/Exit Code/report hash; Job/Step DB rows, CPF approval/attempt ledger link, restart evidence, parity matrix",Y,NOT_EXECUTED'
$newS35 = 'QA32-S035,OSS-BATCH-001,P0,P1_OSS_FIRST_MIGRATION,COMPLETION_GATE,Spring Batch 전체 Primary Execution Engine 완료 판정 Gate,모든 변경이 Working Tree에 존재하고 Result Matrix 초안이 작성되어 있다.,"duplicate engine/metadata/table scanner; all-consumer Job/Step mapping; custom center-cut/restart/checkpoint/partition dispatcher/worker aggregation access 0; scheduler-owned Job state 0; JobRepository 3DB lifecycle; actual center-cut and remote execution Gate를 실행하고 repository 전체·generator·generated source·published artifact를 재검색한다.","일부 Consumer·Wrapper·Sample·Dual Path·미검증 Runtime이 남으면 완료가 거부된다. Spring Batch가 단일 실행 Primary이며 Legacy 제거와 장애·복구·센터컷·원격·3DB Evidence가 모두 있을 때만 완료된다.","Gate command/환경/시작·종료시각/Exit Code/report hash; all-consumer matrix; legacy deletion manifest; Job/Step DB rows; center-cut/remote/restart/3DB evidence",Y,NOT_EXECUTED'
Replace-Exact $scenarioPath $oldS35 $newS35 $true

$section = @'
## QA32 OSS Primary Engine Amendment — 2026-07-31

Authoritative steering: `cpf-docs/work/current/CPF_20260731_QA32_OSS_PRIMARY_ENGINE_STEERING.md`

- Every `ADOPT_NOW` OSS must become the real Product Primary Path for the generic responsibility assigned to it.
- Dependency, wrapper, skeleton, sample, or partial-consumer adoption is `PARTIAL`.
- Consumer migration, legacy removal, runtime/fault/recovery, supply-chain reconciliation, and exact-SHA evidence are mandatory.
- Spring Batch is revised from scoped metadata use to the Primary Execution Engine for all CPF batch execution, including center-cut and distributed worker execution.
- CPF retains only product Control Plane responsibilities: approval, authorization, version, topology, agent security, fencing, audit, UNKNOWN_RESULT reconciliation, and operations UX.
'@

Append-Section "cpf-docs/work/current/CPF_20260730_QA32_DEVELOPMENT_REMEDIATION_REQUEST.md" "## QA32 OSS Primary Engine Amendment — 2026-07-31" $section
Append-Section "cpf-docs/work/current/CPF_20260730_QA32_GPT_DEVELOPMENT_INSTRUCTION.md" "## QA32 OSS Primary Engine Amendment — 2026-07-31" $section
Append-Section "cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER.md" "## QA32 OSS Primary Engine Amendment — 2026-07-31" $section

Write-Host "DONE: QA32 OSS Primary Engine amendment applied fail-closed."
