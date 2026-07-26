<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h2>거래 로그</h2>
        <p class="hint">
          CPF DB 거래 로그를 transactionId와 실행 위치 기준으로 조회합니다.
          민감정보는 서버의 마스킹 결과만 표시합니다.
        </p>
      </div>
      <div class="actions">
        <button type="button" @click="searchLogs">조회</button>
        <button type="button" :disabled="!logDetail?.item" @click="copyLogDetail">상세 복사</button>
        <button type="button" :disabled="!logDetail?.item" @click="downloadLogDetail">상세 저장</button>
      </div>
    </div>

    <div class="filters">
      <label>transactionId <input v-model="logSearch.transactionId" type="text"></label>
      <label>traceId <input v-model="logSearch.traceId" type="text"></label>
      <label>업무 거래 ID <input v-model="logSearch.businessTransactionId" type="text"></label>
      <label>URI <input v-model="logSearch.uri" type="text"></label>
      <label>응답 코드 <input v-model="logSearch.responseCode" type="text"></label>
      <label>HTTP 상태 <input v-model="logSearch.httpStatus" type="number"></label>
      <label>회원번호 <input v-model="logSearch.memberNo" type="text"></label>
      <label>고객번호 <input v-model="logSearch.customerNo" type="text"></label>
      <label>채널 <input v-model="logSearch.channelCode" type="text"></label>
      <label>로그 유형 <input v-model="logSearch.logType" type="text"></label>
    </div>

    <div class="pager">
      <span>{{ sortedLogs.length }}건</span>
      <label>
        쪽 크기
        <select v-model.number="logPage.size">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
      </label>
      <button type="button" :disabled="logPage.page <= 1" @click="moveLogPage(-1)">이전</button>
      <span>{{ logPage.page }} / {{ logTotalPages }}</span>
      <button type="button" :disabled="logPage.page >= logTotalPages" @click="moveLogPage(1)">다음</button>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
        <tr>
          <th><button type="button" @click="sortLogs('LOG_IDX')">IDX</button></th>
          <th><button type="button" @click="sortLogs('TRANSACTION_ID')">transactionId</button></th>
          <th>거래명 / URI</th>
          <th>Module / WAS</th>
          <th>Instance / Host</th>
          <th>채널</th>
          <th>HTTP / 응답</th>
          <th><button type="button" @click="sortLogs('START_TIME')">시작</button></th>
          <th><button type="button" @click="sortLogs('DURATION_MS')">소요(ms)</button></th>
        </tr>
        </thead>
        <tbody>
        <tr
          v-for="item in pagedLogs"
          :key="item.LOG_IDX || item.logIdx"
          @click="loadLogDetail(item.LOG_IDX || item.logIdx)"
        >
          <td>{{ item.LOG_IDX || item.logIdx }}</td>
          <td>{{ item.TRANSACTION_ID || item.transactionId }}</td>
          <td>
            {{ item.BUSINESS_TRANSACTION_NAME || item.businessTransactionName || "-" }}
            <br>
            {{ item.URI || item.uri || "-" }}
          </td>
          <td>{{ item.MODULE_ID || item.moduleId || "-" }} / {{ item.WAS_ID || item.wasId || "-" }}</td>
          <td>
            {{ item.SERVER_INSTANCE_ID || item.serverInstanceId || "-" }}
            <br>
            {{ item.HOST_NAME || item.hostName || "-" }}
          </td>
          <td>{{ item.ORIGINAL_CHANNEL_CODE || item.originalChannelCode || "-" }} / {{ item.CHANNEL_CODE || item.channelCode || "-" }}</td>
          <td>{{ item.HTTP_STATUS || item.httpStatus || "-" }} / {{ item.RESPONSE_CODE || item.responseCode || "-" }}</td>
          <td>{{ item.START_TIME || item.startTime || "-" }}</td>
          <td>{{ item.DURATION_MS ?? item.durationMs ?? "-" }}</td>
        </tr>
        <tr v-if="!pagedLogs.length">
          <td colspan="9">조회된 거래 로그가 없습니다.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="actions">
      <button
        v-for="tab in logDetailTabs"
        :key="tab"
        type="button"
        :class="{ active: logDetailTab === tab }"
        @click="logDetailTab = tab"
      >
        {{ tab }}
      </button>
    </div>
    <pre class="detail">{{ activeLogDetailPayload }}</pre>
  </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "LogsPage",
  mixins: [admConsoleMixin]
});
</script>
