<template>
  <template v-if="authenticated && !passwordChangeRequired">
      <section class="panel" v-show="activeMenu === 'logs'">
        <div class="panel-title">
          <h2>거래 로그</h2>
          <div class="actions">
            <button type="button" @click="searchLogs">조회</button>
            <button type="button" @click="downloadCsv('TRANSACTION_LOGS')">거래 CSV</button>
            <button type="button" @click="downloadCsv('ERROR_LOGS')">오류 CSV</button>
          </div>
        </div>
        <div class="filters">
          <label>거래 ID <input v-model="logSearch.transactionId" type="text"></label>
          <label>Trace ID <input v-model="logSearch.traceId" type="text"></label>
          <label>업무 거래 ID <input v-model="logSearch.businessTransactionId" type="text"></label>
          <label>URI <input v-model="logSearch.uri" type="text"></label>
          <label>응답코드 <input v-model="logSearch.responseCode" type="text"></label>
          <label>HTTP 상태 <input v-model="logSearch.httpStatus" type="number"></label>
          <label>회원번호 <input v-model="logSearch.memberNo" type="text"></label>
          <label>고객번호 <input v-model="logSearch.customerNo" type="text"></label>
          <label>다운로드 사유 <input v-model="downloadForm.reason" type="text"></label>
        </div>
        <div class="pager">
          <span>{{ sortedLogs.length }}건</span>
          <label>쪽 크기
            <select v-model.number="logPage.size">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </label>
          <button type="button" @click="moveLogPage(-1)">이전</button>
          <span>{{ logPage.page }} / {{ logTotalPages }}</span>
          <button type="button" @click="moveLogPage(1)">다음</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th @click="sortLogs('LOG_IDX')">LOG_IDX</th>
              <th @click="sortLogs('TRANSACTION_ID')">거래 ID</th>
              <th @click="sortLogs('TRACE_ID')">Trace</th>
              <th @click="sortLogs('MODULE_ID')">모듈</th>
              <th @click="sortLogs('WAS_ID')">WAS</th>
              <th @click="sortLogs('SERVER_INSTANCE_ID')">인스턴스</th>
              <th @click="sortLogs('BUSINESS_TRANSACTION_ID')">업무 ID</th>
              <th @click="sortLogs('URI')">URI</th>
              <th @click="sortLogs('HTTP_STATUS')">HTTP</th>
              <th @click="sortLogs('RESPONSE_CODE')">응답코드</th>
              <th @click="sortLogs('DURATION_MS')">소요</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in pagedLogs" :key="item.LOG_IDX" @click="loadLogDetail(item.LOG_IDX)">
              <td>{{ item.LOG_IDX }}</td>
              <td>{{ item.TRANSACTION_ID }}</td>
              <td>{{ item.TRACE_ID }}</td>
              <td>{{ item.MODULE_ID }}</td>
              <td>{{ item.WAS_ID }}</td>
              <td>{{ item.SERVER_INSTANCE_ID }}</td>
              <td>{{ item.BUSINESS_TRANSACTION_ID }}</td>
              <td>{{ item.URI }}</td>
              <td>{{ item.HTTP_STATUS }}</td>
              <td>{{ item.RESPONSE_CODE }}</td>
              <td>{{ item.DURATION_MS }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <div class="actions">
          <button type="button" v-for="tab in logDetailTabs" :key="tab" @click="logDetailTab = tab">{{ tab }}</button>
          <button type="button" @click="copyLogDetail">복사</button>
          <button type="button" @click="downloadLogDetail">상세 다운로드</button>
        </div>
        <pre class="detail">{{ activeLogDetailPayload }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'transactionGroups'">
        <div class="panel-title">
          <h2>거래 그룹</h2>
          <div class="actions">
            <button type="button" @click="loadTransactionGroups">조회</button>
            <button type="button" @click="resetTransactionGroupSearch">초기화</button>
          </div>
        </div>
        <p class="hint">transactionId 기준으로 모든 실행 모듈의 구간 타임라인과 표준/확장 헤더 스냅샷, 외부 호출 로그를 함께 조회합니다. Authorization 원문, X-Api-Key 원문, token/secret/password/credential/signature 계열 값은 저장하거나 표시하지 않습니다.</p>
        <div class="filters">
          <label>시작 From <input v-model="transactionGroupSearch.startedAtFrom" type="datetime-local"></label>
          <label>시작 To <input v-model="transactionGroupSearch.startedAtTo" type="datetime-local"></label>
          <label>transactionId <input v-model="transactionGroupSearch.transactionId" type="text"></label>
          <label>transactionSegmentId <input v-model="transactionGroupSearch.transactionSegmentId" type="text"></label>
          <label>상태 <select v-model="transactionGroupSearch.status"><option value="">전체</option><option>SUCCESS</option><option>FAILED</option><option>RUNNING</option></select></label>
          <label>실패 여부 <select v-model="transactionGroupSearch.failureYn"><option value="">전체</option><option>Y</option><option>N</option></select></label>
          <label>모듈 <input v-model="transactionGroupSearch.moduleCode" type="text" placeholder="MBR"></label>
          <label>출발 모듈 <input v-model="transactionGroupSearch.sourceModuleCode" type="text"></label>
          <label>대상 모듈 <input v-model="transactionGroupSearch.targetModuleCode" type="text"></label>
          <label>역할 <input v-model="transactionGroupSearch.transactionRole" type="text" placeholder="MAIN/SHARED/EXTERNAL"></label>
          <label>방향 <input v-model="transactionGroupSearch.direction" type="text" placeholder="INBOUND/OUTBOUND"></label>
          <label>고객번호 <input v-model="transactionGroupSearch.customerNo" type="text"></label>
          <label>회원번호 <input v-model="transactionGroupSearch.memberNo" type="text"></label>
          <label>사용자ID <input v-model="transactionGroupSearch.userId" type="text"></label>
          <label>운영자ID <input v-model="transactionGroupSearch.operatorId" type="text"></label>
          <label>채널 <input v-model="transactionGroupSearch.channelCode" type="text"></label>
          <label>최초채널 <input v-model="transactionGroupSearch.originalChannelCode" type="text"></label>
          <label>외부기관 <input v-model="transactionGroupSearch.externalInstitutionCode" type="text"></label>
          <label>외부거래ID <input v-model="transactionGroupSearch.externalTransactionId" type="text"></label>
          <label>API path <input v-model="transactionGroupSearch.apiPath" type="text"></label>
          <label>거래명 <input v-model="transactionGroupSearch.transactionName" type="text"></label>
          <label>오류코드 <input v-model="transactionGroupSearch.failureCode" type="text"></label>
          <label>소요 From(ms) <input v-model="transactionGroupSearch.durationMsFrom" type="number"></label>
          <label>소요 To(ms) <input v-model="transactionGroupSearch.durationMsTo" type="number"></label>
          <label>표준 헤더 검색 <input v-model="transactionGroupSearch.standardHeaderValue" type="text" placeholder="X-Channel-Code"></label>
          <label>확장 헤더 검색 <input v-model="transactionGroupSearch.extensionHeaderValue" type="text" placeholder="X-Cpf-Ext-*"></label>
        </div>
        <div class="pager">
          <span>{{ transactionGroups.length }}건</span>
          <label>정렬
            <select v-model="transactionGroupSort" @change="loadTransactionGroups">
              <option value="startedAtDesc">시작시간 최신순</option>
              <option value="durationDesc">소요시간 긴순</option>
              <option value="failedFirst">실패 우선</option>
              <option value="statusAsc">상태순</option>
              <option value="moduleAsc">모듈순</option>
            </select>
          </label>
          <label>쪽 크기
            <select v-model.number="transactionGroupPage.size" @change="loadTransactionGroups">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </label>
          <button type="button" @click="moveTransactionGroupPage(-1)">이전</button>
          <span>{{ transactionGroupPage.page }} / {{ transactionGroupTotalPages }}</span>
          <button type="button" @click="moveTransactionGroupPage(1)">다음</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th>거래ID</th><th>거래명/API</th><th>최초 모듈</th><th>호출 흐름</th><th>시작</th><th>종료</th><th>소요(ms)</th><th>상태</th><th>실패</th><th>실패 구간</th><th>고객/회원</th><th>채널</th><th>외부기관/거래ID</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in pagedTransactionGroups" :key="transactionIdOf(item)" @click="loadTransactionGroupDetail(transactionIdOf(item))">
              <td>{{ item.transaction_id || item.transactionId }}</td>
              <td>{{ item.transaction_name || item.transactionName }}<br>{{ item.api_path || item.apiPath }}</td>
              <td>{{ item.origin_module_code || item.originModuleCode || '-' }}</td>
              <td>{{ item.module_flow_text || item.moduleFlowText }}</td>
              <td>{{ item.started_at || item.startedAt }}</td>
              <td>{{ item.ended_at || item.endedAt }}</td>
              <td>{{ item.total_duration_ms || item.totalDurationMs }}</td>
              <td>{{ item.overall_status || item.overallStatus }}</td>
              <td>{{ item.failure_yn || item.failureYn }}</td>
              <td>{{ item.failed_module_code || item.failedModuleCode }} / {{ item.failed_segment_id || item.failedSegmentId }}</td>
              <td>{{ item.customer_no_masked || item.customerNoMasked }} / {{ item.member_no_masked || item.memberNoMasked }}</td>
              <td>{{ item.channel_code || item.channelCode }} / {{ item.original_channel_code || item.originalChannelCode }}</td>
              <td>{{ item.external_institution_code || item.externalInstitutionCode }} / {{ item.external_transaction_id || item.externalTransactionId }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <div class="actions">
          <button type="button" v-for="tab in transactionGroupDetailTabs" :key="tab" @click="transactionGroupDetailTab = tab">{{ tab }}</button>
        </div>
        <pre class="detail">{{ activeTransactionGroupPayload }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'transactions'">
        <div class="panel-title">
          <h2>거래 메타</h2>
          <div class="actions">
            <button type="button" @click="loadTransactions">조회</button>
            <button type="button" v-if="canWrite('TRANSACTION_META')" @click="scanTransactions">재스캔</button>
            <button type="button" v-if="canWrite('TRANSACTION_META')" @click="inactivateTransaction">비활성화</button>
          </div>
        </div>
        <div class="filters">
          <label>모듈 <input v-model="transactionSearch.moduleCode" type="text" placeholder="ADM"></label>
          <label>활성 <select v-model="transactionSearch.activeYn"><option>Y</option><option>N</option><option value="">전체</option></select></label>
          <label>거래 ID <input v-model="transactionSearch.transactionId" type="text"></label>
          <label>선택 거래 ID <input v-model="transactionSearch.selectedTransactionId" type="text"></label>
          <label>사유 <input v-model="transactionSearch.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(transactionResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'remoteLogs'">
        <div class="panel-title">
          <h2>원격 로그 아티팩트</h2>
          <div class="actions">
            <button type="button" @click="loadRemoteLogs">조회</button>
            <button type="button" v-if="canWrite('REMOTE_LOG')" :disabled="!selectedRemoteLog" @click="downloadRemoteLog">다운로드</button>
            <button type="button" v-if="canWrite('REMOTE_LOG')" :disabled="remoteLogSelectedIds.length === 0" @click="downloadRemoteLogBundle">선택 ZIP ({{ remoteLogSelectedIds.length }})</button>
            <button type="button" v-if="canWrite('REMOTE_LOG')" :disabled="remoteLogSelectedIds.length === 0" @click="createRemoteLogBundleJob">비동기 ZIP</button>
            <button type="button" :disabled="!remoteLogBundleJob.jobId" @click="loadRemoteLogBundleJob">작업 상태</button>
            <button type="button" v-if="canWrite('REMOTE_LOG')" :disabled="remoteLogBundleJob.status !== 'COMPLETED'" @click="downloadRemoteLogBundleJob">완료 ZIP 다운로드</button>
            <button type="button" @click="loadRemoteLogDiagnostics">진단</button>
          </div>
        </div>
        <div class="filters">
          <label>환경 <input v-model="remoteLogSearch.environment" type="text" placeholder="local"></label>
          <label>모듈 <input v-model="remoteLogSearch.module" type="text" placeholder="ADM"></label>
          <label>서비스 <input v-model="remoteLogSearch.service" type="text" placeholder="ADM"></label>
          <label>인스턴스 <input v-model="remoteLogSearch.instance" type="text"></label>
          <label>로그 유형 <input v-model="remoteLogSearch.logType" type="text" placeholder="transaction"></label>
          <label>파일명 <input v-model="remoteLogSearch.fileName" type="text"></label>
          <label>표준 온라인 ID <input v-model="remoteLogSearch.standardTransactionId" type="text"></label>
          <label>표준 배치 ID <input v-model="remoteLogSearch.standardBatchId" type="text"></label>
          <label>거래 ID <input v-model="remoteLogSearch.transactionId" type="text"></label>
          <label>구간 ID <input v-model="remoteLogSearch.segmentId" type="text"></label>
          <label>Job Instance ID <input v-model="remoteLogSearch.jobInstanceId" type="text"></label>
          <label>Job Execution ID <input v-model="remoteLogSearch.jobExecutionId" type="text"></label>
          <label>Step Execution ID <input v-model="remoteLogSearch.stepExecutionId" type="text"></label>
          <label>Scheduler ID <input v-model="remoteLogSearch.schedulerId" type="text"></label>
          <label>수정 시작 <input v-model="remoteLogSearch.modifiedFrom" type="text" placeholder="2026-07-15T00:00:00Z"></label>
          <label>수정 종료 <input v-model="remoteLogSearch.modifiedTo" type="text" placeholder="2026-07-15T23:59:59Z"></label>
          <label>최소 크기(byte) <input v-model.number="remoteLogSearch.minSize" type="number" min="0"></label>
          <label>최대 크기(byte) <input v-model.number="remoteLogSearch.maxSize" type="number" min="0"></label>
          <label>압축
            <select v-model="remoteLogSearch.compressed"><option value="">전체</option><option :value="true">압축</option><option :value="false">원본</option></select>
          </label>
          <label>활성
            <select v-model="remoteLogSearch.active"><option value="">전체</option><option :value="true">활성</option><option :value="false">보관</option></select>
          </label>
          <label>마지막 행 <input v-model.number="remoteLogSearch.lastLines" type="number" min="1" max="1000"></label>
          <label>본문 검색 <input v-model="remoteLogSearch.keyword" type="text"></label>
          <label>다운로드 사유 <input v-model="remoteLogSearch.reason" type="text"></label>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>선택</th><th>환경</th><th>모듈</th><th>서비스</th><th>인스턴스</th><th>유형</th><th>파일명</th><th>크기</th><th>수정 시각</th><th>보존 만료</th><th>상태</th></tr></thead>
            <tbody>
            <tr v-for="item in remoteLogResult" :key="item.artifactId" @click="previewRemoteLog(item)">
              <td><input v-model="remoteLogSelectedIds" :value="item.artifactId" type="checkbox" @click.stop></td>
              <td>{{ item.environment }}</td><td>{{ item.module }}</td><td>{{ item.service }}</td><td>{{ item.instance }}</td><td>{{ item.logType }}</td>
              <td>{{ item.fileName }}</td><td>{{ item.size }}</td><td>{{ item.modifiedAt }}</td><td>{{ item.retentionExpiresAt || '-' }}</td><td>{{ item.onlineStatus }} / {{ item.active ? '활성' : '보관' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <pre class="detail">{{ pretty(remoteLogPreview) }}</pre>
        <pre class="detail">{{ pretty(remoteLogBundleJob) }}</pre>
        <pre class="detail">{{ pretty(remoteLogDiagnostics) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'auditLogs'">
        <div class="panel-title">
          <h2>감사 로그</h2>
          <button type="button" @click="loadAuditLogs">조회</button>
        </div>
        <div class="filters">
          <label>운영자 ID <input v-model="auditSearch.operatorId" type="text"></label>
          <label>행위 <input v-model="auditSearch.actionType" type="text"></label>
          <label>대상 유형 <input v-model="auditSearch.targetType" type="text"></label>
          <label>대상 ID <input v-model="auditSearch.targetId" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(auditResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'logLevel'">
        <div class="panel-title">
          <h2>동적 로그 레벨</h2>
          <button type="button" @click="loadLogLevelRules">조회</button>
        </div>
        <div class="filters">
          <label>업무 거래 ID <input v-model="logLevelForm.businessTransactionId" type="text" placeholder="REF01EDU0001"></label>
          <label>거래 ID <input v-model="logLevelForm.transactionId" type="text"></label>
          <label>레벨 <select v-model="logLevelForm.logLevel"><option>DEBUG</option><option>INFO</option><option>TRACE</option></select></label>
          <label>TTL 초 <input v-model.number="logLevelForm.ttlSeconds" type="number"></label>
          <label>사유 <input v-model="logLevelForm.reason" type="text"></label>
          <button type="button" v-if="canWrite('DYNAMIC_LOG')" @click="registerLogLevelRule">등록</button>
        </div>
        <pre class="detail">{{ pretty(logLevelResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'logPolicies'">
        <div class="panel-title">
          <h2>로그 정책</h2>
          <div class="actions">
            <button type="button" @click="loadLogPolicies">조회</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="saveLogPolicy">저장</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="createLogPolicyOverride">Override 등록</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicyOverride">Override 중지</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="createTraceBoost">Trace Boost 등록</button>
            <button type="button" @click="loadTraceBoostRuntimeState">Trace Boost 상태</button>
            <button type="button" @click="loadTraceBoostHistory">Trace Boost 이력</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicy">정책 중지</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="refreshLogPolicyCache">Cache refresh</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="clearLogPolicyCache">Cache clear</button>
          </div>
        </div>
        <div class="filters">
          <label>Policy ID <input v-model.number="logPolicyForm.policyId" type="number"></label>
          <label>Policy Key <input v-model="logPolicyForm.policyKey" type="text"></label>
          <label>정책명 <input v-model="logPolicyForm.policyName" type="text"></label>
          <label>대상 유형 <input v-model="logPolicyForm.targetType" type="text"></label>
          <label>대상 ID <input v-model="logPolicyForm.targetId" type="text"></label>
          <label>레벨 <select v-model="logPolicyForm.logLevel"><option>TRACE</option><option>DEBUG</option><option>INFO</option><option>WARN</option><option>ERROR</option></select></label>
          <label>DB 로그 <select v-model="logPolicyForm.dbLogEnabledYn"><option>Y</option><option>N</option></select></label>
          <label>파일 로그 <select v-model="logPolicyForm.fileLogEnabledYn"><option>Y</option><option>N</option></select></label>
          <label>요청 본문 <select v-model="logPolicyForm.requestBodyLogYn"><option>Y</option><option>N</option></select></label>
          <label>응답 본문 <select v-model="logPolicyForm.responseBodyLogYn"><option>Y</option><option>N</option></select></label>
          <label>보존일 <input v-model.number="logPolicyForm.retentionDays" type="number"></label>
          <label>우선순위 <input v-model.number="logPolicyForm.priority" type="number"></label>
          <label>Trace 거래 ID <input v-model="logPolicyForm.traceBoostTransactionId" type="text"></label>
          <label>Trace 업무 거래 ID <input v-model="logPolicyForm.traceBoostBusinessTransactionId" type="text"></label>
          <label>Trace API 경로 <input v-model="logPolicyForm.traceBoostApiPath" type="text"></label>
          <label>Trace 상태 <input v-model="logPolicyForm.traceBoostStatus" type="text" placeholder="FAILED"></label>
          <label>Trace 실패코드 <input v-model="logPolicyForm.traceBoostFailureCode" type="text" placeholder="SERVICE_TIMEOUT"></label>
          <label>Trace 지연 기준(ms) <input v-model.number="logPolicyForm.traceBoostDurationMsGreaterThan" type="number"></label>
          <label>Trace TTL(초) <input v-model.number="logPolicyForm.traceBoostTtlSeconds" type="number"></label>
          <label>시작 <input v-model="logPolicyForm.effectiveStartAt" type="datetime-local"></label>
          <label>종료 <input v-model="logPolicyForm.effectiveEndAt" type="datetime-local"></label>
          <label>사유 <input v-model="logPolicyForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(logPolicyResult) }}</pre>
      </section>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AdmObservabilityPanels",
  mixins: [admConsoleMixin]
});
</script>
