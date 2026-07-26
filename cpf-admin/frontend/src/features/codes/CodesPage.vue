<template>
      <section class="panel">
        <div class="panel-title">
          <h2>코드 관리</h2>
          <div class="actions">
            <button type="button" @click="loadCodes">조회</button>
            <button type="button" v-if="canWrite('CODE')" @click="createCode">등록</button>
            <button type="button" v-if="canWrite('CODE')" @click="updateCode">수정</button>
          </div>
        </div>
        <div class="filters">
          <label>Code ID <input v-model.number="codeForm.codeId" type="number"></label>
          <label>Parent ID <input v-model.number="codeForm.parentId" type="number"></label>
          <label>Code Key <input v-model="codeForm.codeKey" type="text"></label>
          <label>Code Value <input v-model="codeForm.codeValue" type="text"></label>
          <label>설명 <input v-model="codeForm.description" type="text"></label>
          <label>사유 <input v-model="codeForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(codeResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "CodesPage",
  mixins: [admConsoleMixin]
});
</script>
