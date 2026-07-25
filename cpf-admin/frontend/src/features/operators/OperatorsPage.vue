<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h2>운영자 관리</h2>
        <p class="hint">운영자 등록과 상태 조회를 분리된 ADM 기능 패키지에서 관리합니다. 초기 비밀번호는 화면·로그에 재표시하지 않습니다.</p>
      </div>
      <div class="actions">
        <button type="button" @click="loadOperators">새로고침</button>
      </div>
    </div>
    <div class="filters">
      <label>운영자 ID <input v-model="operatorForm.operatorId" type="text" autocomplete="off"></label>
      <label>운영자 이름 <input v-model="operatorForm.operatorName" type="text" autocomplete="off"></label>
      <label>초기 비밀번호 <input v-model="operatorForm.password" type="password" autocomplete="new-password"></label>
      <label>등록 사유 <input v-model="operatorForm.reason" type="text"></label>
    </div>
    <div class="actions">
      <button type="button" class="primary" @click="createOperator">운영자 등록</button>
    </div>
    <pre class="detail">{{ pretty(operatorResult) }}</pre>
  </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "OperatorsPage",
  mixins: [admConsoleMixin],
  mounted() { this.loadOperators(); }
});
</script>
