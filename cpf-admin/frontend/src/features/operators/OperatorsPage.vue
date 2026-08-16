<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h2>운영자 관리</h2>
        <p class="hint">운영자 등록과 상태 조회를 관리합니다. 초기 비밀번호와 연락처 원문은 일반 목록 상태나 로그에 남기지 않습니다.</p>
      </div>
      <div class="actions"><button type="button" @click="loadOperators">새로고침</button></div>
    </div>
    <div class="filters">
      <label>운영자 ID <input v-model="operatorForm.operatorId" type="text" autocomplete="off"></label>
      <label>운영자 이름 <input v-model="operatorForm.operatorName" type="text" autocomplete="off"></label>
      <label>연락처(휴대폰) <input v-model="operatorForm.mobileNo" type="tel" autocomplete="tel"></label>
      <label>내부 전화번호 <input v-model="operatorForm.officePhoneNo" type="tel" autocomplete="off"></label>
      <label>초기 비밀번호 <input v-model="operatorForm.password" type="password" autocomplete="new-password"></label>
      <label>운영 사유 <input v-model="operatorForm.reason" type="text" maxlength="500"></label>
    </div>
    <div class="actions">
      <button v-if="canCreateOperator" type="button" class="primary" @click="createOperator">운영자 등록</button>
    </div>
    <div v-if="Array.isArray(operatorResult)" class="table-wrap">
      <table>
        <thead><tr><th>운영자 ID</th><th>이름</th><th>계정 상태</th><th>연락처</th><th>내부 전화번호</th><th>역할</th><th>잠금</th><th>관리</th></tr></thead>
        <tbody>
          <tr v-for="operator in operatorResult" :key="operator.operatorId">
            <td>{{ operator.operatorId }}</td><td>{{ operator.operatorName }}</td><td>{{ operator.accountStatus }}</td>
            <td>{{ operator.mobileNo || "-" }}</td><td>{{ operator.officePhoneNo || "-" }}</td>
            <td>{{ (operator.roleIds || []).join(", ") || "-" }}</td><td>{{ operator.locked ? "Y" : "N" }}</td>
            <td>
              <button v-if="canViewRaw" type="button" @click="openOperatorRaw(operator)">원문 보기</button>
              <button v-if="operator.accountStatus !== 'ACTIVE' && (operator.roleIds || []).length && canUpdateStatus" type="button" @click="activateOperator(operator)">활성화</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <CpfStructuredData v-else class="detail" :value="operatorResult" />

    <dialog :open="operatorRawOpen" class="modal">
      <form class="modal-card" @submit.prevent="viewOperatorRaw">
        <div class="card-head">
          <div><p class="eyebrow">PII RAW</p><h2>운영자 연락처 원문 조회</h2></div>
          <button type="button" class="icon-button" aria-label="닫기" @click="closeOperatorRaw">×</button>
        </div>
        <p class="hint">원문 조회는 별도 권한과 구체적인 사유가 필요하며, 조회 시도와 결과는 감사 로그에 기록됩니다.</p>
        <p v-if="operatorRawTarget" class="hint">대상: {{ operatorRawTarget.operatorId }} / {{ operatorRawTarget.operatorName }}</p>
        <label>조회 사유
          <textarea v-model="operatorRawReason" rows="3" minlength="5" maxlength="500" required autocomplete="off"></textarea>
        </label>
        <p v-if="operatorRawError" class="error-banner" role="alert">{{ operatorRawError }}</p>
        <div v-if="operatorRawLoading" class="empty-state" aria-live="polite">조회 중...</div>
        <dl v-else-if="Object.keys(operatorRawResult || {}).length" class="detail-list">
          <dt>휴대폰</dt><dd>{{ operatorRawResult.mobileNo || "-" }}</dd>
          <dt>내부 전화번호</dt><dd>{{ operatorRawResult.officePhoneNo || "-" }}</dd>
        </dl>
        <div class="dialog-actions">
          <button type="button" class="ghost" @click="closeOperatorRaw">닫기</button>
          <button type="submit" class="primary" :disabled="operatorRawLoading">원문 조회</button>
        </div>
      </form>
    </dialog>
  </section>

  <section class="panel route-operation-panel"><h3>역할·세션·연락처 운영</h3><div class="filters"><label>대상 운영자 ID <input v-model="operationForm.operatorId"></label><label>Role IDs <input v-model="operationForm.roleIds" placeholder="ADM_VIEWER,ADM_OPERATOR"></label><label>사유 <input v-model="operationForm.reason"></label></div><div class="actions"><button type="button" @click="loadOperatorRoles">역할 조회</button><button type="button" @click="loadOperatorSessions">세션 조회</button><button v-if="canUnlock" type="button" @click="unlockManagedOperator">잠금 해제</button><button v-if="canUpdateContact" type="button" @click="updateOperatorContact">연락처 수정</button><button v-if="canUpdateRoles" type="button" @click="updateOperatorRoles">역할 수정</button></div><CpfStructuredData class="detail" :value="operationResult" /></section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "OperatorsPage",

  computed: {
    canCreateOperator(): boolean { return this.canButton("OPERATOR_CREATE", "OPERATOR"); },
    canUpdateStatus(): boolean { return this.canButton("OPERATOR_STATUS_UPDATE", "OPERATOR"); },
    canUpdateContact(): boolean { return this.canButton("OPERATOR_CONTACT_UPDATE", "OPERATOR"); },
    canUpdateRoles(): boolean { return this.canButton("OPERATOR_ROLE_UPDATE", "OPERATOR"); },
    canViewRaw(): boolean { return this.canButton("OPERATOR_PII_RAW", "OPERATOR"); },
    canUnlock(): boolean { return this.canButton("PASSWORD_UNLOCK", "PASSWORD"); }
  },
  mounted() { this.loadOperators(); },
  beforeUnmount() { this.closeOperatorRaw(); }
});
</script>
