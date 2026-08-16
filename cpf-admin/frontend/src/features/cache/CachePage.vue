<template>
<section class="panel">
  <div class="panel-title"><h2>캐시 관리</h2><span class="cpf-status warning">위험 조치 승인 필요</span><div class="actions"><button type="button" @click="loadCacheSummary">조회</button><template v-if="canButton('CACHE_REFRESH','CACHE')"><button v-for="target in cacheTargets" :key="target" type="button" @click="refreshCache(target)">{{target}} 갱신 승인 요청</button></template></div></div>
  <div class="filters">
    <label>Tenant <input v-model="cacheControl.tenantId"></label><label>Namespace <input v-model="cacheControl.namespace"></label><label>Key <input v-model="cacheControl.key"></label><label>Version <input v-model.number="cacheControl.version" type="number" min="1"></label><label>감사 사유 <input v-model="cacheReason" required></label>
  </div>
  <div class="actions">
    <button v-if="canButton('CACHE_EVICT_KEY','CACHE')" type="button" @click="evictCacheKey">단일 Key 제거 승인 요청</button>
    <button v-if="canButton('CACHE_EVICT_NAMESPACE','CACHE')" type="button" @click="evictCacheNamespace">Namespace 제거 승인 요청</button>
    <button v-if="canButton('CACHE_RECONCILE','CACHE')" type="button" @click="reconcileCache">Durable 재조정 승인 요청</button>
  </div>
  <CpfStructuredData class="detail" :value="cacheResult" />
</section>
</template>
<script lang="ts">
import {defineComponent} from "vue";import {useAdmConsolePage} from "../../app/useAdmConsolePage";
export default defineComponent({setup(){return useAdmConsolePage()},name:"CachePage",});
</script>
