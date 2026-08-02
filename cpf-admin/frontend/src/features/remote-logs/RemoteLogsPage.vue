<template>
      <section class="panel">
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
        <CpfStructuredData class="detail" :value="remoteLogPreview" />
        <CpfStructuredData class="detail" :value="remoteLogBundleJob" />
        <CpfStructuredData class="detail" :value="remoteLogDiagnostics" />
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "RemoteLogsPage",
  });
</script>
