<template>
      <section class="panel">
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
          <label>Operation ID <input v-model.trim="channelPolicyForm.operationId" type="text" maxlength="160" placeholder="MBR_MEMBER_JOIN 또는 *"></label>
          <label>Caller Channel <input v-model.trim="channelPolicyForm.callerChannel" type="text" maxlength="30" placeholder="WEB2, MOBILE, MBR 또는 ANY"></label>
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
            <thead><tr><th>정책 키</th><th>Operation ID</th><th>Caller Channel</th><th>허용</th><th>최대 TPS</th><th>버전</th></tr></thead>
            <tbody>
            <tr v-for="item in channelSnapshot.policies || []" :key="item.policyKey" @click="selectChannelExecutionPolicy(item)">
              <td>{{ item.policyKey }}</td><td>{{ item.operationId }}</td><td>{{ item.callerChannel }}</td><td>{{ item.allowed ? '허용' : '거부' }}</td>
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "ChannelPolicyPage",
  });
</script>
