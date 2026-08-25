<script setup lang="ts">
import { computed } from 'vue'
import { asArray, asRecord, text, type ApprovalDetailPayload } from '../model/approvalModel'

const props = defineProps<{ detail?: ApprovalDetailPayload }>()
const document = computed(() => asRecord(props.detail?.approvalDocument))
const summary = computed(() => asRecord(document.value?.summary) ?? {})
const sections = computed(() => asArray(document.value?.sections).map(asRecord).filter(Boolean) as Record<string, unknown>[])
const changes = computed(() => asArray(document.value?.changes).map(asRecord).filter(Boolean) as Record<string, unknown>[])
const history = computed(() => asArray(props.detail?.history).map(asRecord).filter(Boolean) as Record<string, unknown>[])
const execution = computed(() => asRecord(props.detail?.execution))
const judgementReady = computed(() => document.value?.judgementReady === true)
</script>

<template>
  <section v-if="props.detail" aria-labelledby="approval-document-title">
    <header class="page-header">
      <div>
        <h3 id="approval-document-title">결재 판단문서</h3>
        <p>승인 전에 요청 Snapshot, 변경 전후, 처리이력과 실제 실행결과를 확인합니다.</p>
      </div>
      <strong>{{ judgementReady ? '판단정보 준비완료' : '판단정보 미완료' }}</strong>
    </header>

    <h4>공통정보</h4>
    <dl class="form-grid">
      <template v-for="(value, key) in summary" :key="String(key)">
        <div><dt>{{ key }}</dt><dd>{{ text(value) }}</dd></div>
      </template>
    </dl>

    <template v-for="section in sections" :key="text(section.sectionCode)">
      <h4>{{ text(section.title) }}</h4>
      <dl class="form-grid">
        <template v-for="(value, key) in (asRecord(section.fields) ?? {})" :key="String(key)">
          <div><dt>{{ key }}</dt><dd>{{ text(value) }}</dd></div>
        </template>
      </dl>
    </template>

    <h4>변경 전 / 변경 후</h4>
    <div class="table-wrap">
      <table>
        <thead><tr><th>필드</th><th>Before</th><th>After</th><th>변경</th></tr></thead>
        <tbody>
          <tr v-if="changes.length === 0"><td colspan="4">비교 가능한 변경 항목이 없습니다.</td></tr>
          <tr v-for="row in changes" :key="text(row.field)">
            <td>{{ text(row.field) }}</td><td>{{ text(row.before) }}</td><td>{{ text(row.after) }}</td><td>{{ row.changed === true ? '변경' : '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <h4>실제 실행결과</h4>
    <dl v-if="execution" class="form-grid">
      <template v-for="(value, key) in execution" :key="String(key)">
        <div><dt>{{ key }}</dt><dd>{{ text(value) }}</dd></div>
      </template>
    </dl>
    <p v-else>아직 실행결과가 없습니다.</p>

    <h4>처리이력</h4>
    <div class="table-wrap">
      <table>
        <thead><tr><th>시각</th><th>Action</th><th>Actor</th><th>상태</th><th>사유/의견</th></tr></thead>
        <tbody>
          <tr v-if="history.length === 0"><td colspan="5">처리이력이 없습니다.</td></tr>
          <tr v-for="(row, index) in history" :key="`${text(row.historyId)}-${index}`">
            <td>{{ text(row.createdAt ?? row.processedAt) }}</td>
            <td>{{ text(row.actionType ?? row.action) }}</td>
            <td>{{ text(row.actorEmployeeNo ?? row.actorId) }}</td>
            <td>{{ text(row.afterStatus ?? row.status) }}</td>
            <td>{{ text(row.reason ?? row.comment) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
