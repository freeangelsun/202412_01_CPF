<template>
      <section class="panel">
        <div class="panel-title">
          <h2>알림 관리</h2>
          <div class="actions">
            <button type="button" @click="loadNotifications">조회</button>
            <button type="button" v-if="canWrite('NOTIFICATION')" @click="saveNotificationRule">저장</button>
            <button type="button" v-if="canWrite('NOTIFICATION')" @click="disableNotificationRule">비활성화</button>
            <button type="button" v-if="canWrite('NOTIFICATION')" @click="sendNotificationTest">테스트 발송</button>
            <button type="button" @click="downloadCsv('NOTIFICATION_DELIVERY_LOGS')">발송 이력 CSV</button>
          </div>
        </div>
        <div class="filters">
          <label>Rule ID <input v-model.number="notificationForm.ruleId" type="number"></label>
          <label>Event Type <input v-model="notificationForm.eventType" type="text"></label>
          <label>Event Sub Type <input v-model="notificationForm.eventSubType" type="text"></label>
          <label>Channel <input v-model="notificationForm.channelCode" type="text"></label>
          <label>Severity <select v-model="notificationForm.severity"><option>INFO</option><option>WARN</option><option>ERROR</option></select></label>
          <label>Receiver Group <input v-model="notificationForm.receiverGroup" type="text"></label>
          <label>사용 <select v-model="notificationForm.useYn"><option>Y</option><option>N</option></select></label>
          <label>수신자 <input v-model="notificationForm.receiver" type="text"></label>
          <label>테스트 메시지 <input v-model="notificationForm.message" type="text"></label>
          <label>사유 <input v-model="notificationForm.reason" type="text"></label>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Rule ID</th><th>Event</th><th>채널</th><th>심각도</th><th>수신 그룹</th><th>사용</th></tr></thead>
            <tbody>
            <tr v-for="rule in notificationResult.rules" :key="rule.ruleId || rule.rule_id" @click="selectNotificationRule(rule)">
              <td>{{ rule.ruleId || rule.rule_id }}</td>
              <td>{{ rule.eventType || rule.event_type }} / {{ rule.eventSubType || rule.event_sub_type }}</td>
              <td>{{ rule.channelCode || rule.channel_code }}</td>
              <td>{{ rule.severity }}</td>
              <td>{{ rule.receiverGroup || rule.receiver_group }}</td>
              <td>{{ rule.useYn || rule.use_yn }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <pre class="detail">{{ pretty(notificationResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "NotificationsPage",
  mixins: [admConsoleMixin]
});
</script>
