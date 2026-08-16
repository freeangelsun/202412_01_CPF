<template>
      <section class="panel">
        <div class="panel-title">
          <h2>배치 · Center-Cut 관제</h2><p class="cpf-muted">Schedule의 calendarId는 CMN 영업일 관리 API를 공통 참조하며, Job/Execution/Worker/transactionId를 같은 추적 축으로 사용합니다.</p>
          <div class="actions">
            <button type="button" @click="loadBatch">조회</button>
            <button type="button" v-if="canWrite('BATCH')" @click="registerBatchJob">배치 등록</button>
            <button type="button" v-if="canWrite('BATCH')" @click="runBatchJob">수동 실행</button>
            <button type="button" v-if="canWrite('BATCH')" @click="retryBatchExecution">실패 재수행</button>
            <button type="button" v-if="canWrite('BATCH')" @click="stopBatchExecution">중지</button>
            <button type="button" v-if="canWrite('BATCH')" @click="runBatchSchedulerOnce">스케줄러 1회 실행</button>
            <button type="button" @click="loadBatchJobDetail">Job 상세</button>
            <button type="button" @click="simulateBatchSchedule">수행 시뮬레이션</button>
            <button type="button" @click="loadBatchRelations">관계 조회</button>
            <button type="button" @click="loadBatchTargets">수행 대상 조회</button>
            <button type="button" @click="loadBatchSteps">Step 이력</button>
            <button type="button" @click="loadBatchWorkers">Worker 상태</button>
            <button type="button" @click="loadBatchLocks">Lock 조회</button>
            <button type="button" v-if="canWrite('BATCH')" @click="releaseBatchLock">Lock 해제</button>
            <button type="button" @click="loadBatchGhostCandidates">Ghost 후보</button>
            <button type="button" v-if="canWrite('BATCH')" @click="actBatchGhost">Ghost 조치</button>
            <button type="button" @click="loadBatchOperations">운영 로그</button>
            <button type="button" @click="downloadCsv('BATCH_EXECUTIONS')">배치 CSV</button>
          </div>
        </div>
        <div class="filters">
          <label>Job ID <input v-model="batchForm.jobId" type="text"></label>
          <label>Job명 <input v-model="batchForm.jobName" type="text"></label>
          <label>유형 <select v-model="batchForm.jobType"><option>TASKLET</option><option>CHUNK</option><option>RETRY</option></select></label>
          <label>Execution ID <input v-model.number="batchForm.executionId" type="number"></label>
          <label>Schedule ID <input v-model="batchForm.scheduleId" type="text"></label>
          <label>파라미터 <input v-model="batchForm.jobParameters" type="text"></label>
          <label>캘린더 <input v-model="batchForm.calendarId" type="text"></label>
          <label>영업일 <input v-model="batchForm.businessDate" type="date"></label>
          <label>시뮬레이션 일수 <input v-model.number="batchForm.simulationDays" type="number" min="1" max="62"></label>
          <label>대상 상태 <input v-model="batchForm.dispatchStatus" type="text" placeholder="WAITING"></label>
          <label>Heartbeat 제한초 <input v-model.number="batchForm.heartbeatTimeoutSeconds" type="number" min="30" max="86400"></label>
          <label>Lock Key <input v-model="batchForm.lockKey" type="text"></label>
          <label>Ghost 조치 <select v-model="batchForm.ghostActionType"><option>FAIL</option><option>ABANDON</option><option>RELEASE_LOCK</option></select></label>
          <label>승인 요청 ID <input v-model.trim="batchForm.approvalRequestId" type="text" placeholder="독립 승인 완료 ID"></label>
          <label>멱등 키 <input v-model.trim="batchForm.idempotencyKey" type="text" placeholder="비우면 최초 실행 시 자동 생성"></label>
          <label>Expected Version <input v-model.number="batchForm.expectedVersion" type="number" min="0" placeholder="Retry/Stop/Lock/Ghost 필수"></label>
          <label>사유 <input v-model="batchForm.reason" type="text"></label>
          <p class="cpf-muted">위험조치는 ‘위험조치 승인’ 화면에서 독립 승인한 요청 ID를 사용합니다. 결과가 불명확하면 같은 멱등 키로 상태를 먼저 확인한 뒤 재시도하세요.</p>
        </div>
        <div class="subsection">
          <div class="panel-title">
            <div>
              <h3>실행 추적 조회</h3>
              <p class="cpf-muted">Job · Transaction · Spring Job Instance · Worker · Server Instance를 한 화면에서 교차 검색합니다.</p>
            </div>
            <div class="actions"><button type="button" @click="loadExecutionTrace">실행 추적 조회</button></div>
          </div>
          <div class="filters">
            <label>Job ID <input v-model="executionTraceForm.jobId" type="text"></label>
            <label>Transaction ID <input v-model="executionTraceForm.transactionId" type="text"></label>
            <label>Spring Job Instance <input v-model.number="executionTraceForm.springBatchJobInstanceId" type="number"></label>
            <label>Worker ID <input v-model="executionTraceForm.workerId" type="text"></label>
            <label>Server Instance <input v-model="executionTraceForm.serverInstanceId" type="text"></label>
            <label>조회 건수 <input v-model.number="executionTraceForm.limit" type="number" min="1" max="500"></label>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Execution</th><th>Job</th><th>Spring Job Instance</th><th>Worker</th><th>Server</th><th>Transaction ID</th><th>상태</th><th>시작</th><th>Job Log</th></tr></thead>
              <tbody>
                <tr v-for="(row,index) in executionTraceRows" :key="traceValue(row,'execution_id','EXECUTION_ID') || index">
                  <td>{{ traceValue(row,'execution_id','EXECUTION_ID') }}</td>
                  <td>{{ traceValue(row,'job_id','JOB_ID') }}</td>
                  <td>{{ traceValue(row,'spring_batch_job_instance_id','SPRING_BATCH_JOB_INSTANCE_ID') }}</td>
                  <td>{{ traceValue(row,'worker_id','WORKER_ID') }}</td>
                  <td>{{ traceValue(row,'server_instance_id','SERVER_INSTANCE_ID') }}</td>
                  <td>{{ traceValue(row,'transaction_id','TRANSACTION_ID') }}</td>
                  <td>{{ traceValue(row,'status_code','STATUS_CODE','status','STATUS') }}</td>
                  <td>{{ traceValue(row,'start_time','START_TIME') }}</td>
                  <td>{{ traceValue(row,'job_log_relative_path','JOB_LOG_RELATIVE_PATH') }}</td>
                </tr>
                <tr v-if="!executionTraceRows.length"><td colspan="9" class="cpf-muted">조회 결과가 없습니다.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
        <StructuredDetails
          :value="batchResult"
          title="배치 작업 응답 상세"
          empty-text="배치 조회 또는 작업 결과가 없습니다."
        />
        <div class="subsection">
          <div class="panel-title">
            <h3>Center-Cut 관제</h3>
            <div class="actions">
              <button type="button" @click="loadCenterCut">Center-Cut 갱신</button>
              <button type="button" @click="loadCenterCutJobDetail">Job 상세</button>
              <button type="button" @click="loadCenterCutTargets">Target 조회</button>
              <button type="button" @click="loadCenterCutResults">Result 조회</button>
            </div>
          </div>
          <div class="filters">
            <label>Center-Cut Job ID <input v-model="centerCutForm.centerCutJobId" type="text"></label>
            <label>Target 상태 <input v-model="centerCutForm.statusCode" type="text" placeholder="FAILED"></label>
            <label>Result 상태 <input v-model="centerCutForm.resultStatus" type="text" placeholder="FAILED"></label>
            <label>조회 건수 <input v-model.number="centerCutForm.limit" type="number" min="1" max="500"></label>
          </div>
          <div class="summary-grid">
            <div class="metric"><span>전체</span><strong>{{ centerCutResult.summary?.totalCount ?? '-' }}</strong></div>
            <div class="metric"><span>대기</span><strong>{{ centerCutResult.summary?.readyCount ?? '-' }}</strong></div>
            <div class="metric"><span>처리중</span><strong>{{ centerCutResult.summary?.runningCount ?? '-' }}</strong></div>
            <div class="metric"><span>성공</span><strong>{{ centerCutResult.summary?.successCount ?? '-' }}</strong></div>
            <div class="metric"><span>실패</span><strong>{{ centerCutResult.summary?.failedCount ?? '-' }}</strong></div>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
              <tr><th>Job ID</th><th>배치 Job</th><th>Provider</th><th>Handler</th><th>Chunk</th><th>Retry</th><th>사용</th></tr>
              </thead>
              <tbody>
              <tr v-for="job in centerCutResult.jobs || []" :key="job.centerCutJobId" @click="centerCutForm.centerCutJobId = job.centerCutJobId; loadCenterCutJobDetail();">
                <td>{{ job.centerCutJobId }}</td>
                <td>{{ job.batchJobId }}</td>
                <td>{{ job.providerKey }}</td>
                <td>{{ job.handlerKey }}</td>
                <td>{{ job.chunkSize }}</td>
                <td>{{ job.retryLimit }}</td>
                <td>{{ job.useYn }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
              <tr><th>Target</th><th>업무키</th><th>업무일자</th><th>상태</th><th>재시도</th><th>Transaction ID</th><th>Parent Segment</th><th>Segment ID</th><th>실패 사유</th></tr>
              </thead>
              <tbody>
              <tr v-for="target in centerCutResult.targets || []" :key="target.targetId">
                <td>{{ target.targetId }}</td>
                <td>{{ target.businessKey }}</td>
                <td>{{ target.businessDate }}</td>
                <td>{{ target.statusCode }}</td>
                <td>{{ target.retryCount }}</td>
                <td>{{ target.transactionId }}</td>
                <td>{{ target.parentSegmentId }}</td>
                <td>{{ target.transactionSegmentId }}</td>
                <td>{{ target.lastErrorMessage }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
              <tr><th>Result</th><th>Target</th><th>업무키</th><th>상태</th><th>메시지</th><th>Transaction ID</th><th>Parent Segment</th><th>Segment ID</th><th>Payload</th></tr>
              </thead>
              <tbody>
              <tr v-for="result in centerCutResult.results || []" :key="result.resultId" @click="loadCenterCutResultDetail(result.resultId)">
                <td>{{ result.resultId }}</td>
                <td>{{ result.targetId }}</td>
                <td>{{ result.businessKey }}</td>
                <td>{{ result.resultStatus }}</td>
                <td>{{ result.resultMessage }}</td>
                <td>{{ result.transactionId }}</td>
                <td>{{ result.parentSegmentId }}</td>
                <td>{{ result.transactionSegmentId }}</td>
                <td>{{ result.resultPayloadMasked }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          <StructuredDetails
            :value="centerCutResult"
            title="Center-Cut 응답 상세"
            empty-text="Center-Cut 조회 결과가 없습니다."
          />
        </div>
      </section>

  <section class="panel route-operation-panel"><h3>Execution Paging</h3><button type="button" @click="loadBatchExecutionPage">Execution Page 조회</button></section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import StructuredDetails from "../../components/StructuredDetails.vue";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "BatchPage",
  components: { StructuredDetails },

  data() {
    return {
      executionTraceForm: {
        jobId: "",
        transactionId: "",
        springBatchJobInstanceId: null as number | null,
        workerId: "",
        serverInstanceId: "",
        limit: 100
      },
      executionTraceRows: [] as Array<Record<string, any>>
    };
  },
  methods: {
    async loadExecutionTrace() {
      const query = this.buildParams(this.executionTraceForm).toString();
      const endpoint = query ? `/adm/api/batch/executions?${query}` : "/adm/api/batch/executions";
      this.executionTraceRows = await this.getJson(endpoint) || [];
    },
    traceValue(row: Record<string, any>, ...keys: string[]) {
      for (const key of keys) {
        if (row && row[key] !== undefined && row[key] !== null) return row[key];
      }
      return "";
    }
  }
});
</script>
