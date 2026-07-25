<template>
      <section class="panel">
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "CachePage",
  mixins: [admConsoleMixin]
});
</script>
