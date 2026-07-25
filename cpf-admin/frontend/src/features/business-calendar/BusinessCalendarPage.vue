<template>
  <section class="cpf-page">
    <header class="cpf-page-header">
      <div>
        <p class="cpf-eyebrow">CMN-CALENDAR · shared policy</p>
        <h2>영업일 · 휴일 관리</h2>
        <p class="cpf-muted">ADM에서 관리한 Calendar를 Batch/Scheduler와 업무 Domain이 같은 CMN API로 사용합니다.</p>
      </div>
      <div class="cpf-actions">
        <button class="cpf-btn" @click="load">조회</button>
        <button class="cpf-btn cpf-btn-primary" @click="save">저장</button>
      </div>
    </header>

    <div class="cpf-card-grid">
      <article class="cpf-card">
        <span>Calendar</span><strong>{{ calendarId }}</strong>
      </article>
      <article class="cpf-card">
        <span>선택일</span><strong>{{ businessDate }}</strong>
      </article>
      <article class="cpf-card">
        <span>저장 Provider</span><strong>{{ writable ? 'WRITE' : 'READ ONLY' }}</strong>
      </article>
    </div>

    <section class="cpf-panel">
      <div class="cpf-form-grid">
        <label>Calendar ID<input v-model.trim="calendarId" /></label>
        <label>기준일<input v-model="businessDate" type="date" /></label>
        <label>구분
          <select v-model="businessDay">
            <option :value="true">영업일</option>
            <option :value="false">휴일</option>
          </select>
        </label>
        <label>Day Type<input v-model.trim="dayType" placeholder="HOLIDAY" /></label>
        <label>기관코드<input v-model.trim="institutionCode" placeholder="BANK" /></label>
        <label>사유<input v-model.trim="reason" placeholder="휴일 사유" /></label>
      </div>
    </section>

    <section class="cpf-panel">
      <div class="cpf-section-title"><h3>Calendar Override</h3><span>{{ items.length }}건</span></div>
      <div class="cpf-table-wrap">
        <table class="cpf-table">
          <thead><tr><th>일자</th><th>구분</th><th>Type</th><th>기관</th><th>사유</th><th>Version</th></tr></thead>
          <tbody>
            <tr v-for="row in items" :key="row.businessDate" @click="select(row)">
              <td>{{ row.businessDate }}</td>
              <td><span :class="['cpf-badge', row.businessDay ? 'is-success' : 'is-danger']">{{ row.businessDay ? '영업일' : '휴일' }}</span></td>
              <td>{{ row.dayType }}</td><td>{{ row.institutionCode }}</td><td>{{ row.reason }}</td><td>{{ row.version }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-if="message" class="cpf-notice">{{ message }}</p>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { cpfApi } from "../../shared/cpfApi";

type CalendarDay = {businessDate:string;businessDay:boolean;dayType:string;institutionCode:string;reason:string;version:number};

const calendarId = ref("DEFAULT");
const businessDate = ref(new Date().toISOString().slice(0,10));
const businessDay = ref(false);
const dayType = ref("HOLIDAY");
const institutionCode = ref("");
const reason = ref("");
const version = ref(0);
const writable = ref(true);
const items = ref<CalendarDay[]>([]);
const message = ref("");

async function load(){
  message.value="";
  const result = await cpfApi(`/adm/api/business-calendars/${encodeURIComponent(calendarId.value)}/days?limit=366`);
  writable.value=Boolean(result.writable); items.value=result.items||[];
}
function select(row:CalendarDay){
  businessDate.value=row.businessDate;businessDay.value=row.businessDay;dayType.value=row.dayType;
  institutionCode.value=row.institutionCode||"";reason.value=row.reason||"";version.value=row.version||0;
}
async function save(){
  const result=await cpfApi(`/adm/api/business-calendars/${encodeURIComponent(calendarId.value)}/days/${businessDate.value}?expectedVersion=${version.value}`,{
    method:"PUT",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({businessDay:businessDay.value,dayType:dayType.value,institutionCode:institutionCode.value,reason:reason.value})
  });
  version.value=result.version||0;message.value="저장했습니다.";await load();
}
onMounted(load);
</script>
