<template>
  <div v-if="open" class="danger-dialog-backdrop" role="presentation" @click.self="cancel">
    <section class="danger-dialog" role="dialog" aria-modal="true" :aria-labelledby="`${dialogId}-title`">
      <header><span class="risk">{{ riskLabel }}</span><h2 :id="`${dialogId}-title`">{{ title }}</h2><p>{{ description }}</p></header>
      <dl class="target-summary"><template v-for="(value,key) in target" :key="key"><dt>{{ key }}</dt><dd>{{ value }}</dd></template></dl>
      <label>변경 사유 <textarea v-model.trim="form.reason" minlength="5" required placeholder="감사 가능한 구체적 사유를 입력하세요."></textarea></label>
      <label v-if="approvalRequired">승인 ID <input v-model.trim="form.approvalId" required :readonly="Boolean(initialApprovalId)" placeholder="승인된 요청 ID"></label>
      <label v-if="approvalRequired">멱등 키 <input v-model.trim="form.idempotencyKey" required :readonly="Boolean(initialIdempotencyKey)" placeholder="승인 Request Key와 동일한 값"></label>
      <label v-if="expectedVersion !== undefined">Expected Version <input :value="expectedVersion" readonly></label>
      <label class="confirm"><input v-model="form.confirmed" type="checkbox"> 결과불명 가능성과 복구 절차를 확인했습니다.</label>
      <p v-if="validationMessage" class="error-text">{{ validationMessage }}</p>
      <footer><button class="ghost" type="button" :disabled="submitting" @click="cancel">취소</button><button class="danger" type="button" :disabled="submitting" @click="submit">{{ submitting ? '처리 중...' : confirmLabel }}</button></footer>
    </section>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, watch } from "vue";
const props = withDefaults(defineProps<{ open:boolean; title:string; description:string; target?:Record<string,string|number>; risk?:"HIGH"|"CRITICAL"; approvalRequired?:boolean; expectedVersion?:number; expectedVersionRequired?:boolean; submitting?:boolean; confirmLabel?:string; initialApprovalId?:string; initialIdempotencyKey?:string; initialReason?:string }>(), { target:()=>({}), risk:"HIGH", approvalRequired:false, expectedVersionRequired:false, submitting:false, confirmLabel:"실행", initialApprovalId:"", initialIdempotencyKey:"", initialReason:"" });
const emit=defineEmits<{ cancel:[]; confirm:[payload:{reason:string;approvalId:string;expectedVersion?:number;idempotencyKey:string}] }>();
const dialogId=`cpf-danger-${Math.random().toString(36).slice(2)}`;
const form=reactive({reason:"",approvalId:"",idempotencyKey:"",confirmed:false});
const validationMessage=computed(()=>form.reason.length<5?"사유를 5자 이상 입력하세요.":props.approvalRequired&&!form.approvalId?"승인 ID가 필요합니다.":props.approvalRequired&&!form.idempotencyKey?"승인 Request Key와 동일한 멱등 키가 필요합니다.":props.expectedVersionRequired&&(props.expectedVersion===undefined||!Number.isSafeInteger(props.expectedVersion)||props.expectedVersion<0)?"최신 Expected Version이 없습니다. 다시 조회하세요.":!form.confirmed?"확인 항목에 동의해야 합니다.":"");
const riskLabel=computed(()=>props.risk==="CRITICAL"?"중대 위험 조치":"위험 조치");
watch(
  () => [props.open, props.initialReason, props.initialApprovalId, props.initialIdempotencyKey] as const,
  ([open]) => {
    if (!open) return
    form.reason = props.initialReason
    form.approvalId = props.initialApprovalId
    form.idempotencyKey = props.initialIdempotencyKey || crypto.randomUUID()
    form.confirmed = false
  },
)

function cancel(){if(!props.submitting)emit("cancel");}
function submit(){if(validationMessage.value)return;emit("confirm",{reason:form.reason,approvalId:form.approvalId,expectedVersion:props.expectedVersion,idempotencyKey:form.idempotencyKey});}
</script>
<style scoped>
.danger-dialog-backdrop{position:fixed;inset:0;background:rgba(15,23,42,.58);display:grid;place-items:center;padding:1rem;z-index:1000}.danger-dialog{width:min(38rem,100%);max-height:90vh;overflow:auto;background:#fff;border-radius:1rem;padding:1.25rem;box-shadow:0 24px 80px rgba(0,0,0,.3)}.danger-dialog header h2{margin:.35rem 0}.risk{font-weight:700;color:#b42318}.target-summary{display:grid;grid-template-columns:auto 1fr;gap:.3rem 1rem;background:#f7f8fa;padding:.75rem;border-radius:.5rem}.target-summary dt{font-weight:700}.danger-dialog label{display:grid;gap:.35rem;margin-top:.8rem}.danger-dialog textarea{min-height:6rem}.confirm{grid-template-columns:auto 1fr!important;align-items:center}.error-text{color:#b42318}.danger-dialog footer{display:flex;justify-content:flex-end;gap:.5rem;margin-top:1rem}
</style>
