<template>
      <section class="panel">
        <div class="panel-title">
          <h2>메시지 관리</h2>
          <div class="actions">
            <button type="button" @click="loadMessages">조회</button>
            <button type="button" v-if="canWrite('MESSAGE')" @click="createMessage">등록</button>
            <button type="button" v-if="canWrite('MESSAGE')" @click="updateMessage">수정</button>
          </div>
        </div>
        <div class="filters">
          <label>Message ID <input v-model.number="messageForm.messageId" type="number"></label>
          <label>Message Code <input v-model="messageForm.messageCode" type="text"></label>
          <label>Locale <input v-model="messageForm.locale" type="text"></label>
          <label>외부 메시지 <input v-model="messageForm.externalMessage" type="text"></label>
          <label>내부 메시지 <input v-model="messageForm.internalMessage" type="text"></label>
          <label>사유 <input v-model="messageForm.reason" type="text"></label>
        </div>
        <CpfStructuredData class="detail" :value="messageResult" />
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({
  name: "MessagesPage",
  setup() { return useAdmConsolePage(); }
});
</script>
