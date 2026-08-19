<script setup lang="ts">
import { onMounted } from 'vue'
import RuntimeInventorySelector from '../../../components/RuntimeInventorySelector.vue'
import ManagedServerForm from '../components/ManagedServerForm.vue'
import ManagedServerTable from '../components/ManagedServerTable.vue'
import RuntimeInventoryTable from '../components/RuntimeInventoryTable.vue'
import { useServerManagement } from '../composables/useServerManagement'

const state = useServerManagement()
onMounted(state.load)
</script>
<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">CENTRAL SERVER REGISTRY</p><h2>서버 관리 · Runtime Inventory</h2><p>Server는 한 번 등록하고 Gateway·Batch·Logging·Health·Configuration에서 동일 Identity를 참조합니다.</p></div>
      <button type="button" class="ghost" :disabled="state.loading.value" @click="state.load">새로고침</button>
    </div>
    <p v-if="state.error.value" class="cpf-alert danger" role="alert">{{ state.error.value }}</p>
    <p v-if="state.notice.value" class="cpf-alert success" role="status">{{ state.notice.value }}</p>
    <section class="cpf-card">
      <div class="cpf-form-grid">
        <label>Environment<input v-model.trim="state.filters.environment"></label>
        <label>Status<select v-model="state.filters.status"><option value="">ALL</option><option>REGISTERED</option><option>ACTIVE</option><option>DISABLED</option><option>DECOMMISSIONED</option><option>UNKNOWN</option></select></label>
        <label>Capability<input v-model.trim="state.filters.capability" placeholder="FILE_LOGGING / BATCH_AGENT"></label>
        <label>Keyword<input v-model.trim="state.filters.keyword" @keyup.enter="state.search"></label>
        <button class="primary" @click="state.search">조회</button>
        <button @click="state.clearForm">신규 등록</button>
      </div>
    </section>
    <section class="server-grid">
      <div class="cpf-card table-card">
        <ManagedServerTable
          :rows="state.servers.value" :total="state.serverTotal.value" :page="state.serverPage.value" :size="state.serverSize.value" :loading="state.loading.value"
          @row="state.selectServer" @page="state.changeServerPage" @size="state.changeServerSize"
        />
      </div>
      <ManagedServerForm :form="state.form" :selected="state.selected.value" :loading="state.loading.value" @save="state.saveServer" @disable="state.disableServer" />
    </section>
    <section class="cpf-card runtime-section">
      <div class="panel-title"><div><h3>Runtime Inventory</h3><p>Stable Managed Server와 ephemeral Runtime/Capability를 분리해 조회합니다.</p></div></div>
      <RuntimeInventorySelector v-model="state.runtimeId.value" :environment="state.filters.environment" :capability="state.filters.capability" />
      <RuntimeInventoryTable
        :rows="state.runtimes.value" :total="state.runtimeTotal.value" :page="state.runtimePage.value" :size="state.runtimeSize.value" :loading="state.loading.value"
        @page="state.changeRuntimePage" @size="state.changeRuntimeSize"
      />
    </section>
  </div>
</template>
<style scoped>.server-grid{display:grid;grid-template-columns:minmax(0,1.6fr) minmax(20rem,.8fr);gap:1rem}.table-card{min-height:30rem}.runtime-section{min-height:30rem}.success{color:var(--el-color-success,#67c23a)}@media(max-width:1000px){.server-grid{grid-template-columns:1fr}}</style>
