<template>
  <section class="panel">
    <div class="panel-title">
      <h2>알림 관리</h2>
      <div class="actions">
        <button type="button" @click="loadNotifications">조회</button>
        <button type="button" v-if="canWrite('NOTIFICATION')" @click="saveNotificationRule">규칙 저장</button>
        <button type="button" v-if="canWrite('NOTIFICATION')" @click="disableNotificationRule">규칙 비활성화</button>
        <button type="button" v-if="canWrite('NOTIFICATION')" @click="sendNotificationTest">테스트 발송</button>
        <button type="button" @click="downloadCsv('NOTIFICATION_DELIVERY_LOGS')">발송 이력 CSV</button>
      </div>
    </div>

    <h3>알림 규칙</h3>
    <div class="filters">
      <label>Rule ID <input v-model.number="notificationForm.ruleId" type="number"></label>
      <label>Event Type <input v-model="notificationForm.eventType" type="text"></label>
      <label>Event Sub Type <input v-model="notificationForm.eventSubType" type="text"></label>
      <label>Channel <input v-model="notificationForm.channelCode" type="text"></label>
      <label>Severity
        <select v-model="notificationForm.severity">
          <option>INFO</option><option>WARN</option><option>ERROR</option>
        </select>
      </label>
      <label>Receiver Group <input v-model="notificationForm.receiverGroup" type="text"></label>
      <label>사용 <select v-model="notificationForm.useYn"><option>Y</option><option>N</option></select></label>
      <label>수신자 <input v-model="notificationForm.receiver" type="text"></label>
      <label>테스트 메시지 <input v-model="notificationForm.message" type="text"></label>
      <label>규칙 변경 사유 <input v-model="notificationForm.reason" type="text"></label>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr><th>Rule ID</th><th>Event</th><th>채널</th><th>심각도</th><th>수신 그룹</th><th>사용</th></tr>
        </thead>
        <tbody>
          <tr v-for="rule in notificationResult.rules" :key="rule.ruleId" @click="selectNotificationRule(rule)">
            <td>{{ rule.ruleId }}</td>
            <td>{{ rule.eventType }} / {{ rule.eventSubType || "-" }}</td>
            <td>{{ rule.channelCode }}</td>
            <td>{{ rule.severity }}</td>
            <td>{{ rule.receiverGroup || "-" }}</td>
            <td>{{ rule.useYn }}</td>
          </tr>
          <tr v-if="!notificationResult.rules?.length">
            <td colspan="6">조회된 알림 규칙이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="panel-title">
      <h3>Durable Outbox 발송 이력</h3>
      <div class="actions">
        <button
          type="button"
          v-if="canWrite('NOTIFICATION')"
          :disabled="!notificationDeliveryActionAllowed('retry')"
          @click="retryNotificationDelivery">
          재시도
        </button>
        <button
          type="button"
          v-if="canWrite('NOTIFICATION')"
          :disabled="!notificationDeliveryActionAllowed('cancel')"
          @click="cancelNotificationDelivery">
          취소
        </button>
      </div>
    </div>
    <div class="filters">
      <label>선택 Delivery ID
        <input v-model.number="notificationDeliveryForm.deliveryId" type="number" readonly>
      </label>
      <label>Expected Version
        <input v-model.number="notificationDeliveryForm.expectedVersion" type="number" readonly>
      </label>
      <label>Operation ID
        <input v-model="notificationDeliveryForm.operationId" type="text" readonly>
      </label>
      <label>운영 조치 사유
        <input v-model="notificationDeliveryForm.reason" type="text">
      </label>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Delivery</th><th>Operation</th><th>상태</th><th>대상</th><th>수신자</th>
            <th>시도</th><th>다음 시도</th><th>오류</th><th>Lease</th><th>Version</th><th>요청/수정</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="delivery in notificationResult.deliveryLogs"
            :key="delivery.deliveryId"
            :aria-selected="selectedNotificationDelivery?.deliveryId === delivery.deliveryId"
            @click="selectNotificationDelivery(delivery)">
            <td>{{ delivery.deliveryId }}</td>
            <td>
              <strong>{{ delivery.operationId }}</strong><br>
              <small>{{ delivery.requestHash }}</small>
            </td>
            <td>{{ delivery.deliveryStatus }}</td>
            <td>{{ delivery.targetType }} / {{ delivery.targetId }}</td>
            <td>{{ delivery.receiver }}</td>
            <td>{{ delivery.attemptCount }} / {{ delivery.maxAttempts }}</td>
            <td>{{ delivery.nextAttemptAt || "-" }}</td>
            <td>
              {{ delivery.lastErrorCode || "-" }}<br>
              <small>{{ delivery.deliveryMessage || "-" }}</small>
            </td>
            <td>
              {{ delivery.leaseOwner || "-" }}<br>
              <small>{{ delivery.leaseUntil || "-" }}</small>
            </td>
            <td>{{ delivery.version }}</td>
            <td>
              {{ delivery.requestedAt || "-" }}<br>
              <small>{{ delivery.updatedBy || "-" }} / {{ delivery.updatedAt || "-" }}</small>
            </td>
          </tr>
          <tr v-if="!notificationResult.deliveryLogs?.length">
            <td colspan="11">조회된 발송 이력이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <h3>Provider Attempt 이력</h3>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Attempt</th><th>Worker</th><th>상태</th><th>Provider 상태</th>
            <th>실패·응답 내용</th><th>시작</th><th>완료</th><th>Lease Version</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="attempt in notificationResult.attempts"
            :key="`${attempt.deliveryId}-${attempt.attemptNo}`">
            <td>{{ attempt.attemptNo }}</td>
            <td>{{ attempt.workerId }}</td>
            <td>{{ attempt.attemptStatus }}</td>
            <td>{{ attempt.providerStatus || "-" }}</td>
            <td>{{ attempt.providerMessage || "-" }}</td>
            <td>{{ attempt.startedAt || "-" }}</td>
            <td>{{ attempt.completedAt || "-" }}</td>
            <td>{{ attempt.leaseVersion }}</td>
          </tr>
          <tr v-if="!notificationResult.attempts?.length">
            <td colspan="8">선택한 발송 건의 Provider Attempt 이력이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="notificationResult.action?.deliveryId" role="status">
      최근 조치: Delivery {{ notificationResult.action.deliveryId }},
      상태 {{ notificationResult.action.deliveryStatus }},
      Version {{ notificationResult.action.version }}
    </p>
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
