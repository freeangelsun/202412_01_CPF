<template>
      <section class="panel">
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "TransactionGroupsPage",
  mixins: [admConsoleMixin]
});
</script>
