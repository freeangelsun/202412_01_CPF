<template>
  <template v-if="authenticated && !passwordChangeRequired">
      <section class="panel" v-show="activeMenu === 'members'">
        <div class="panel-title">
          <h2>회원 관리</h2>
          <button type="button" @click="searchMembers">조회</button>
        </div>
        <div class="filters">
          <label>회원번호 <input v-model="memberSearch.memberNo" type="text"></label>
          <label>고객번호 <input v-model="memberSearch.customerNo" type="text"></label>
          <label>로그인 ID <input v-model="memberSearch.loginId" type="text"></label>
          <label>이름 <input v-model="memberSearch.name" type="text"></label>
          <label>이메일 <input v-model="memberSearch.email" type="text"></label>
          <label>휴대폰 <input v-model="memberSearch.mobileNo" type="text"></label>
          <label>상태 <input v-model="memberSearch.memberStatus" type="text"></label>
          <label>권한 <input v-model="memberSearch.roleCode" type="text"></label>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th>ID</th><th>회원번호</th><th>고객번호</th><th>로그인 ID</th><th>이름</th><th>상태</th><th>잠금</th><th>탈퇴</th><th>채널</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in memberResult.items" :key="item.id" @click="loadMemberDetail(item.id)">
              <td>{{ item.id }}</td><td>{{ item.member_no }}</td><td>{{ item.customer_no }}</td><td>{{ item.login_id }}</td>
              <td>{{ item.name }}</td><td>{{ item.member_status }}</td><td>{{ item.lock_yn }}</td><td>{{ item.withdraw_yn }}</td><td>{{ item.channel_code }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <div class="filters">
          <label>회원 ID <input v-model.number="memberForm.memberId" type="number"></label>
          <label>회원번호 <input v-model="memberForm.memberNo" type="text"></label>
          <label>고객번호 <input v-model="memberForm.customerNo" type="text"></label>
          <label>로그인 ID <input v-model="memberForm.loginId" type="text"></label>
          <label>이름 <input v-model="memberForm.name" type="text"></label>
          <label>이메일 <input v-model="memberForm.email" type="text"></label>
          <label>휴대폰 <input v-model="memberForm.mobileNo" type="text"></label>
          <label>상태 <input v-model="memberStatusForm.memberStatus" type="text"></label>
          <label>잠금 <select v-model="memberStatusForm.lockYn"><option>Y</option><option>N</option></select></label>
          <label>탈퇴 <select v-model="memberStatusForm.withdrawYn"><option>Y</option><option>N</option></select></label>
          <label>역할 <input v-model="memberRoleForm.roleCode" type="text"></label>
          <label>사유 <input v-model="memberForm.reason" type="text"></label>
        </div>
        <div class="actions">
          <button type="button" v-if="canWrite('MEMBER')" @click="createMember">등록</button>
          <button type="button" v-if="canWrite('MEMBER')" @click="updateMember">수정</button>
          <button type="button" v-if="canWrite('MEMBER')" @click="updateMemberStatus">상태 변경</button>
          <button type="button" v-if="canWrite('MEMBER')" @click="grantMemberRole">권한 부여</button>
          <button type="button" v-if="canDelete('MEMBER')" @click="revokeMemberRole">권한 회수</button>
        </div>
        <pre class="detail">{{ pretty(memberDetail) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'notifications'">
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

      <section class="panel" v-show="activeMenu === 'downloads'">
        <div class="panel-title">
          <h2>다운로드 감사</h2>
          <div class="actions">
            <button type="button" @click="loadDownloadPolicies">조회</button>
            <button type="button" @click="downloadCsv(downloadForm.downloadType)">CSV 다운로드</button>
          </div>
        </div>
        <div class="filters">
          <label>다운로드 유형
            <select v-model="downloadForm.downloadType">
              <option>TRANSACTION_LOGS</option>
              <option>ERROR_LOGS</option>
              <option>BATCH_EXECUTIONS</option>
              <option>NOTIFICATION_DELIVERY_LOGS</option>
            </select>
          </label>
          <label>대상 화면 <input v-model="downloadForm.targetType" type="text"></label>
          <label>시작 <input v-model="downloadForm.fromDate" type="text" placeholder="2026-06-19 00:00:00"></label>
          <label>종료 <input v-model="downloadForm.toDate" type="text" placeholder="2026-06-19 23:59:59"></label>
          <label>거래 ID <input v-model="downloadForm.transactionId" type="text"></label>
          <label>Trace ID <input v-model="downloadForm.traceId" type="text"></label>
          <label>Job ID <input v-model="downloadForm.jobId" type="text"></label>
          <label>건수 <input v-model.number="downloadForm.limit" type="number"></label>
          <label>사유 <input v-model="downloadForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(downloadResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'messages'">
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
        <pre class="detail">{{ pretty(messageResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'codes'">
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AdmBusinessPanels",
  mixins: [admConsoleMixin]
});
</script>
