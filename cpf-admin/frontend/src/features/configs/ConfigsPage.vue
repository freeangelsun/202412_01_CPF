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
        <div class="filters"><label>Runtime Target <RuntimeInventorySelector v-model="runtimeInstanceId" capability="RUNTIME_CONFIG" /></label>
          <label>Config ID <input v-model.number="configForm.configId" type="number"></label>
          <label>Config Key <input v-model="configForm.configKey" type="text"></label>
          <label>Config Value <input v-model="configForm.configValue" type="text"></label>
          <label>유형 <input v-model="configForm.configType" type="text"></label>
          <label>암호화 <select v-model="configForm.encryptedYn"><option>Y</option><option>N</option></select></label>
          <label>사유 <input v-model="configForm.reason" type="text"></label>
        </div>
        <CpfStructuredData class="detail" :value="configResult" />
      </section>

  <section class="panel route-operation-panel"><h3>설정 상세·삭제</h3><div class="actions"><button type="button" @click="loadConfigDetail">상세 조회</button><button type="button" v-if="canWrite('CONFIG')" @click="deleteConfig">삭제</button></div></section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import RuntimeInventorySelector from "../../components/RuntimeInventorySelector.vue";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "ConfigsPage",
  components:{RuntimeInventorySelector},
  data(){return{runtimeInstanceId:""}},
  });
</script>
