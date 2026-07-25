<template>
  <template v-if="authenticated && !passwordChangeRequired">
      <section class="panel" v-show="activeMenu === 'standardExecutions'">
        <div class="panel-title">
          <h2>표준 실행 카탈로그</h2>
          <button type="button" @click="loadStandardExecutions">조회</button>
        </div>
        <div class="filters">
          <label>유형
            <select v-model="standardExecutionSearch.type">
              <option value="">전체</option>
              <option value="ONLINE">온라인</option>
              <option value="BATCH">배치</option>
            </select>
          </label>
          <label>소유 업무 <input v-model="standardExecutionSearch.ownerDomain" type="text" placeholder="CPF"></label>
          <label>검색어 <input v-model="standardExecutionSearch.keyword" type="text" placeholder="ID, 실행명, source, endpoint"></label>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>표준 실행 ID</th><th>유형</th><th>실행명</th><th>소유 업무</th><th>Source 모듈</th><th>Endpoint</th></tr></thead>
            <tbody>
            <tr v-for="item in standardExecutionResult.items || []" :key="item.standardExecutionId" @click="loadStandardExecutionDetail(item.standardExecutionId)">
              <td>{{ item.standardExecutionId }}</td>
              <td>{{ item.executionType }}</td>
              <td>{{ item.executionName }}</td>
              <td>{{ item.ownerDomain }}</td>
              <td>{{ item.sourceModule }}</td>
              <td>{{ item.endpoint || '-' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <pre class="detail">{{ pretty(standardExecutionDetail) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'channelPolicy'">
        <div class="panel-title">
          <h2>통합 채널 정책</h2>
          <div class="actions">
            <button type="button" @click="loadChannelPolicy">조회</button>
            <button type="button" v-if="canWrite('CHANNEL_POLICY')" @click="refreshChannelPolicy">스냅샷 갱신</button>
            <button type="button" @click="exportChannelPolicyPackage">패키지 반출</button>
            <button type="button" v-if="canWrite('CHANNEL_POLICY')" @click="importChannelPolicyPackage">패키지 반입</button>
          </div>
        </div>
        <p class="hint">현재 Gateway와 온라인 거래가 사용하는 불변 스냅샷 버전: {{ channelSnapshot.version }}</p>
        <div class="filters">
          <label>채널 코드 <input v-model.trim="channelForm.channelCode" type="text" maxlength="30"></label>
          <label>채널명 <input v-model.trim="channelForm.channelName" type="text" maxlength="100"></label>
          <label>채널 유형
            <select v-model="channelForm.channelType"><option>CLIENT</option><option>OPERATOR</option><option>SYSTEM</option></select>
          </label>
          <label>신뢰 수준
            <select v-model="channelForm.trustLevel"><option>EXTERNAL</option><option>INTERNAL</option></select>
          </label>
          <label><input v-model="channelForm.clientChannel" type="checkbox"> 최초 유입 채널</label>
          <label><input v-model="channelForm.internalChannel" type="checkbox"> 내부 호출 채널</label>
          <label><input v-model="channelForm.authenticationRequired" type="checkbox"> 인증 필수</label>
          <label><input v-model="channelForm.signatureRequired" type="checkbox"> 요청 서명 필수</label>
          <label><input v-model="channelForm.active" type="checkbox"> 사용</label>
          <label>설명 <input v-model.trim="channelForm.description" type="text" maxlength="500"></label>
          <label>감사 사유 <input v-model.trim="channelForm.reason" type="text" maxlength="500"></label>
          <button type="button" v-if="canWrite('CHANNEL_POLICY')" @click="saveChannel">채널 저장</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>채널</th><th>명칭</th><th>유형</th><th>신뢰</th><th>인증</th><th>서명</th><th>사용</th><th>버전</th></tr></thead>
            <tbody>
            <tr v-for="item in channelItems" :key="item.channelCode" @click="selectChannel(item)">
              <td>{{ item.channelCode }}</td><td>{{ item.channelName }}</td><td>{{ item.channelType }}</td><td>{{ item.trustLevel }}</td>
              <td>{{ item.authenticationRequired ? '필수' : '선택' }}</td><td>{{ item.signatureRequired ? '필수' : '선택' }}</td>
              <td>{{ item.active ? '사용' : '중지' }}</td><td>{{ item.version }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <div class="filters policy-editor">
          <label>정책 키 <input v-model.trim="channelPolicyForm.policyKey" type="text" maxlength="100"></label>
          <label>표준 실행 ID <input v-model.trim="channelPolicyForm.standardExecutionId" type="text" maxlength="10"></label>
          <label>최초 채널 <input v-model.trim="channelPolicyForm.originalChannelCode" type="text" maxlength="30"></label>
          <label>호출 채널 <input v-model.trim="channelPolicyForm.callerChannelCode" type="text" maxlength="30"></label>
          <label>요청 유형 <input v-model.trim="channelPolicyForm.requestType" type="text" maxlength="30"></label>
          <label>최대 TPS <input v-model.number="channelPolicyForm.maxTps" type="number" min="0"></label>
          <label><input v-model="channelPolicyForm.allowed" type="checkbox"> 실행 허용</label>
          <label><input v-model="channelPolicyForm.authenticationRequired" type="checkbox"> 인증 필수</label>
          <label><input v-model="channelPolicyForm.signatureRequired" type="checkbox"> 서명 필수</label>
          <label><input v-model="channelPolicyForm.active" type="checkbox"> 정책 사용</label>
          <label>감사 사유 <input v-model.trim="channelPolicyForm.reason" type="text" maxlength="500"></label>
          <button type="button" v-if="canWrite('CHANNEL_POLICY')" @click="saveChannelExecutionPolicy">거래 정책 저장</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>정책 키</th><th>표준 실행</th><th>최초 채널</th><th>호출 채널</th><th>요청 유형</th><th>허용</th><th>최대 TPS</th><th>버전</th></tr></thead>
            <tbody>
            <tr v-for="item in channelSnapshot.policies || []" :key="item.policyKey" @click="selectChannelExecutionPolicy(item)">
              <td>{{ item.policyKey }}</td><td>{{ item.standardExecutionId }}</td><td>{{ item.originalChannelCode }}</td>
              <td>{{ item.callerChannelCode }}</td><td>{{ item.requestType }}</td><td>{{ item.allowed ? '허용' : '거부' }}</td>
              <td>{{ item.maxTps || '제한 없음' }}</td><td>{{ item.version }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <label class="package-editor">정책 패키지 JSON
          <textarea v-model="channelPackageText" rows="12" spellcheck="false"></textarea>
        </label>
        <label class="inline-check"><input v-model="channelImportDryRun" type="checkbox"> 반입 전 사전 검증만 수행</label>
      </section>

      <section class="panel" v-show="activeMenu === 'reliability'">
        <div class="panel-title">
          <h2>신뢰성 처리 관제</h2>
          <div class="actions">
            <button type="button" @click="loadReliability">통합 조회</button>
            <button type="button" @click="loadBatchJobLogDetail">BAT 로그 상세</button>
            <button type="button" v-if="canWrite('RELIABILITY')" @click="replayDlq">DLQ 재처리</button>
            <button type="button" v-if="canWrite('RELIABILITY')" @click="resolveUnknownResult">결과 수동 확정</button>
          </div>
        </div>
        <div class="filters">
          <label>Scope <input v-model="reliabilitySearch.scope" type="text"></label>
          <label>상태 <input v-model="reliabilitySearch.status" type="text"></label>
          <label>멱등 Key <input v-model="reliabilitySearch.key" type="text"></label>
          <label>transactionId <input v-model="reliabilitySearch.transactionId" type="text"></label>
          <label>Topic <input v-model="reliabilitySearch.topic" type="text"></label>
          <label>Endpoint <input v-model="reliabilitySearch.endpointCode" type="text"></label>
          <label>Unknown 유형 <input v-model="reliabilitySearch.type" type="text"></label>
          <label>업무일자 <input v-model="reliabilitySearch.businessDate" type="text" placeholder="yyyyMMdd"></label>
          <label>Job 이름 <input v-model="reliabilitySearch.jobName" type="text"></label>
          <label>JobInstance ID <input v-model.number="reliabilitySearch.jobInstanceId" type="number" min="1"></label>
          <label>조회 건수 <input v-model.number="reliabilitySearch.limit" type="number" min="1" max="500"></label>
          <label>Replay Message ID <input v-model="reliabilityAction.messageId" type="text"></label>
          <label>Unknown ID <input v-model="reliabilityAction.unknownId" type="text"></label>
          <label>확정 상태
            <select v-model="reliabilityAction.targetStatus">
              <option>CONFIRMED_SUCCESS</option>
              <option>CONFIRMED_FAILURE</option>
              <option>RETRY_PENDING</option>
              <option>MANUAL_REVIEW</option>
              <option>RESOLVED</option>
            </select>
          </label>
          <label>감사 사유 <input v-model="reliabilityAction.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(reliabilityResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'serviceRegistry'">
        <div class="panel-title">
          <h2>Service Call Registry</h2>
          <button type="button" @click="loadServiceRegistry">Refresh</button>
        </div>
        <div class="filters">
          <label>Service ID <input v-model="serviceRegistrySearch.serviceId" type="text" placeholder="MBR"></label>
          <label>Endpoint Code <input v-model="serviceRegistrySearch.endpointCode" type="text" placeholder="MBR_API"></label>
          <label>Instance Status <input v-model="serviceRegistrySearch.instanceStatus" type="text" placeholder="UP"></label>
          <label>Transaction ID <input v-model="serviceRegistrySearch.transactionId" type="text"></label>
          <label>Limit <input v-model.number="serviceRegistrySearch.limit" type="number"></label>
        </div>
        <div class="grid two">
          <section>
            <h3>Services / Endpoints</h3>
            <pre class="detail">{{ pretty({ services: serviceRegistryResult.services, endpoints: serviceRegistryResult.endpoints }) }}</pre>
          </section>
          <section>
            <h3>Instances / Health</h3>
            <pre class="detail">{{ pretty({ instances: serviceRegistryResult.instances, health: serviceRegistryResult.health }) }}</pre>
          </section>
          <section>
            <h3>Routing / Circuit</h3>
            <pre class="detail">{{ pretty({ routingPolicies: serviceRegistryResult.routingPolicies, circuits: serviceRegistryResult.circuits }) }}</pre>
          </section>
          <section>
            <h3>Call History</h3>
            <pre class="detail">{{ pretty(serviceRegistryResult.callHistory) }}</pre>
          </section>
        </div>
      </section>

      <section class="panel" v-show="activeMenu === 'cache'">
        <div class="panel-title">
          <h2>캐시 관리</h2>
          <div class="actions">
            <button type="button" @click="loadCacheSummary">조회</button>
            <template v-if="canWrite('CACHE')">
              <button type="button" v-for="target in cacheTargets" :key="target" @click="refreshCache(target)">{{ target }}</button>
            </template>
          </div>
        </div>
        <div class="filters" v-if="canWrite('CACHE')">
          <label>사유 <input v-model="cacheReason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(cacheResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'configs'">
        <div class="panel-title">
          <h2>설정 관리</h2>
          <div class="actions">
            <button type="button" @click="loadConfigs">조회</button>
            <button type="button" v-if="canWrite('CONFIG')" @click="createConfig">등록</button>
            <button type="button" v-if="canWrite('CONFIG')" @click="updateConfig">수정</button>
          </div>
        </div>
        <div class="filters">
          <label>Config ID <input v-model.number="configForm.configId" type="number"></label>
          <label>Config Key <input v-model="configForm.configKey" type="text"></label>
          <label>Config Value <input v-model="configForm.configValue" type="text"></label>
          <label>유형 <input v-model="configForm.configType" type="text"></label>
          <label>암호화 <select v-model="configForm.encryptedYn"><option>Y</option><option>N</option></select></label>
          <label>사유 <input v-model="configForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(configResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'responseCodes'">
        <div class="panel-title">
          <h2>응답코드 관리</h2>
          <div class="actions">
            <button type="button" @click="loadResponseCodes">조회</button>
            <button type="button" v-if="canWrite('RESPONSE_CODE')" @click="createResponseCode">등록</button>
            <button type="button" v-if="canWrite('RESPONSE_CODE')" @click="updateResponseCode">수정</button>
            <button type="button" v-if="canDelete('RESPONSE_CODE')" @click="deleteResponseCode">삭제</button>
          </div>
        </div>
        <div class="filters">
          <label>Response Code <input v-model="responseCodeForm.responseCode" type="text"></label>
          <label>Message Code <input v-model="responseCodeForm.messageCode" type="text"></label>
          <label>결과 <select v-model="responseCodeForm.resultType"><option>S</option><option>E</option></select></label>
          <label>모듈 <input v-model="responseCodeForm.moduleId" type="text"></label>
          <label>그룹 <input v-model="responseCodeForm.responseGroup" type="text"></label>
          <label>일련번호 <input v-model="responseCodeForm.sequenceNo" type="text"></label>
          <label>HTTP <input v-model.number="responseCodeForm.httpStatus" type="number"></label>
          <label>사유 <input v-model="responseCodeReason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(responseCodeResult) }}</pre>
      </section>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AdmPlatformPanels",
  mixins: [admConsoleMixin]
});
</script>
