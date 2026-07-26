<template>
      <section class="panel">
        <div class="panel-title">
          <h2>응답코드 관리</h2>
          <div class="actions">
            <button type="button" @click="loadResponseCodes">조회</button>
            <button type="button" v-if="canWrite('RESPONSE_CODE')" @click="createResponseCode">등록</button>
            <button type="button" v-if="canWrite('RESPONSE_CODE')" @click="updateResponseCode">수정</button>
            <button type="button" v-if="canDelete('RESPONSE_CODE')" @click="deleteResponseCode">삭제</button>
          </div>
        </div>
        <div class="filters">
          <label>Response Code <input v-model="responseCodeForm.responseCode" type="text"></label>
          <label>Message Code <input v-model="responseCodeForm.messageCode" type="text"></label>
          <label>결과 <select v-model="responseCodeForm.resultType"><option>S</option><option>E</option></select></label>
          <label>모듈 <input v-model="responseCodeForm.moduleId" type="text"></label>
          <label>그룹 <input v-model="responseCodeForm.responseGroup" type="text"></label>
          <label>일련번호 <input v-model="responseCodeForm.sequenceNo" type="text"></label>
          <label>HTTP <input v-model.number="responseCodeForm.httpStatus" type="number"></label>
          <label>사유 <input v-model="responseCodeReason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(responseCodeResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "ResponseCodesPage",
  mixins: [admConsoleMixin]
});
</script>
