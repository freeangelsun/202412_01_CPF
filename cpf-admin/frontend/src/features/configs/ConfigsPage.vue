<template>
      <section class="panel">
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "ConfigsPage",
  mixins: [admConsoleMixin]
});
</script>
