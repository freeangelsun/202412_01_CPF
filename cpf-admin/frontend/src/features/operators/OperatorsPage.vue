<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h2>운영자 관리</h2>
        <p class="hint">운영자 등록과 상태 조회를 분리된 ADM 기능 패키지에서 관리합니다. 초기 비밀번호와 연락처 원문은 로그에 남기지 않습니다.</p>
      </div>
      <div class="actions">
        <button type="button" @click="loadOperators">새로고침</button>
      </div>
    </div>
    <div class="filters">
      <label>운영자 ID <input v-model="operatorForm.operatorId" type="text" autocomplete="off"></label>
      <label>운영자 이름 <input v-model="operatorForm.operatorName" type="text" autocomplete="off"></label>
      <label>연락처(휴대폰) <input v-model="operatorForm.mobileNo" type="tel" autocomplete="tel"></label>
      <label>내부 전화번호 <input v-model="operatorForm.officePhoneNo" type="tel" autocomplete="off"></label>
      <label>초기 비밀번호 <input v-model="operatorForm.password" type="password" autocomplete="new-password"></label>
      <label>등록 사유 <input v-model="operatorForm.reason" type="text"></label>
    </div>
    <div class="actions">
      <button type="button" class="primary" @click="createOperator">운영자 등록</button>
    </div>
    <div v-if="Array.isArray(operatorResult)" class="table-wrap">
      <table>
        <thead><tr><th>운영자 ID</th><th>이름</th><th>계정 상태</th><th>연락처(휴대폰)</th><th>내부 전화번호</th><th>역할</th><th>잠금</th><th>관리</th></tr></thead>
        <tbody>
          <tr v-for="operator in operatorResult" :key="operator.operatorId">
            <td>{{ operator.operatorId }}</td><td>{{ operator.operatorName }}</td>
            <td>{{ operator.accountStatus }}</td>
            <td>{{ operator.mobileNo || "-" }}</td><td>{{ operator.officePhoneNo || "-" }}</td>
            <td>{{ (operator.roleIds || []).join(", ") || "-" }}</td><td>{{ operator.locked ? "Y" : "N" }}</td>
            <td><button type="button" @click="viewOperatorRaw(operator)">원문 보기</button>
                <button v-if="operator.accountStatus !== 'ACTIVE' && (operator.roleIds || []).length" type="button" @click="activateOperator(operator)">활성화</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <pre v-else class="detail">{{ pretty(operatorResult) }}</pre>
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
