<template>
  <template v-if="authenticated && !passwordChangeRequired">
      <section class="panel" v-show="activeMenu === 'batch'">
        <div class="panel-title">
          <h2>배치 관제</h2>
          <div class="actions">
            <button type="button" @click="loadBatch">조회</button>
            <button type="button" v-if="canWrite('BATCH')" @click="registerBatchJob">배치 등록</button>
            <button type="button" v-if="canWrite('BATCH')" @click="runBatchJob">수동 실행</button>
            <button type="button" v-if="canWrite('BATCH')" @click="retryBatchExecution">실패 재수행</button>
            <button type="button" v-if="canWrite('BATCH')" @click="stopBatchExecution">중지</button>
            <button type="button" v-if="canWrite('BATCH')" @click="runBatchSchedulerOnce">스케줄러 1회 실행</button>
            <button type="button" v-if="canWrite('BATCH')" @click="saveBusinessDay">영업일 저장</button>
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
          <label>사유 <input v-model="batchForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(batchResult) }}</pre>
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
          <pre class="detail">{{ pretty(centerCutResult) }}</pre>
        </div>
      </section>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AdmBatchPanels",
  mixins: [admConsoleMixin]
});
</script>
