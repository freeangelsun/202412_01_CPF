<template>
  <section class="workbench" data-cpf-page="protocol-message-workbench">
    <header class="header">
      <div><h1>전문·Protocol Message Workbench</h1><p>메시지 Registry와 Transaction Trace를 함께 조회하며 민감 필드는 마스킹합니다.</p></div>
      <div class="actions"><button :disabled="loading" @click="resetForm">신규</button><button :disabled="loading" @click="loadMessages">새로고침</button></div>
    </header>

    <form class="filters" @submit.prevent="loadTrace">
      <label>Transaction ID<input v-model.trim="transactionId" autocomplete="off"></label>
      <label>최대 Event<select v-model.number="limit"><option :value="20">20</option><option :value="50">50</option><option :value="100">100</option></select></label>
      <button class="primary" type="submit" :disabled="!transactionId || loading">전문 흐름 추적</button>
    </form>
    <p v-if="error" class="state error" role="alert">{{ error }}</p>
    <p v-if="success" class="state success" role="status">{{ success }}</p>

    <div class="cards">
      <article class="card"><span>Message Registry</span><strong>{{ messages.length }}</strong><small>Locale·Code 정의</small></article>
      <article class="card"><span>Trace Event</span><strong>{{ traceEvents.length }}</strong><small>{{ transactionId || '-' }}</small></article>
    </div>

    <div class="tabs"><button :class="{primary:tab==='registry'}" @click="tab='registry'">Registry</button><button :class="{primary:tab==='trace'}" @click="tab='trace'">Protocol Trace</button></div>
    <div v-if="tab==='registry'" class="registry-layout">
      <div class="table-wrap"><table><thead><tr><th>ID</th><th>Code</th><th>Locale</th><th>External</th><th>Use</th></tr></thead><tbody>
        <tr v-for="(row,index) in messages" :key="index" :class="{selected:selected===row}" @click="selectMessage(row)"><td>{{ text(row,'messageId','message_id','id') }}</td><td>{{ text(row,'messageCode','message_code','code') }}</td><td>{{ text(row,'locale') }}</td><td>{{ text(row,'externalMessage','external_message','messageText','message_text','message') }}</td><td>{{ text(row,'useYn','use_yn','activeYn','active_yn','status') }}</td></tr>
        <tr v-if="!messages.length"><td colspan="5">등록된 메시지가 없습니다.</td></tr>
      </tbody></table></div>
      <form class="panel editor" @submit.prevent="saveMessage">
        <h2>{{ form.messageId ? '메시지 수정' : '메시지 등록' }}</h2>
        <div class="form-grid">
          <label>Message Code<input v-model.trim="form.messageCode" required maxlength="80"></label>
          <label>Locale<input v-model.trim="form.locale" required maxlength="20"></label>
          <label>Format<select v-model="form.messageFormatType"><option>FIXED</option><option>INDEXED</option><option>NAMED</option></select></label>
          <label>Use<select v-model="form.useYn"><option>Y</option><option>N</option></select></label>
          <label class="wide">External Message<textarea v-model.trim="form.externalMessage" required></textarea></label>
          <label class="wide">Internal Message<textarea v-model.trim="form.internalMessage" required></textarea></label>
          <label>Parameter Count<input v-model.number="form.parameterCount" type="number" min="0" max="99"></label>
          <label>Parameter Sample<input v-model.trim="form.parameterSample" placeholder='["fieldName"]'></label>
          <label class="wide">Description<input v-model.trim="form.description"></label>
          <label class="wide">변경 사유<textarea v-model.trim="form.reason" minlength="5" required></textarea></label>
        </div>
        <div class="actions"><button class="primary" type="submit" :disabled="loading">{{ form.messageId ? '수정 저장' : '등록' }}</button><button type="button" @click="resetForm">초기화</button><button v-if="form.messageId" class="danger" type="button" @click="pendingDelete=true">비활성</button></div>
        <div v-if="pendingDelete" class="state warning"><strong>메시지를 비활성화합니다.</strong><p>동일 Code·Locale를 사용하는 Consumer 영향을 확인했습니다.</p><label class="confirm"><input v-model="deleteConfirmed" type="checkbox"> 영향 확인 및 비활성 승인</label><div class="actions"><button class="danger" type="button" :disabled="!deleteConfirmed || loading" @click="disableMessage">비활성 실행</button><button type="button" @click="pendingDelete=false;deleteConfirmed=false">취소</button></div></div>
      </form>
    </div>
    <div v-else class="table-wrap"><table><thead><tr><th>시각</th><th>구간</th><th>Protocol/Channel</th><th>External</th><th>Status</th><th>Duration</th></tr></thead><tbody><tr v-for="(row,index) in traceEvents" :key="index" @click="selected=row"><td>{{ text(row,'timestamp','createdAt','created_at') }}</td><td>{{ text(row,'eventType','segmentId','segment_id') }}</td><td>{{ text(row,'protocol','channelCode','channel_code','apiPath','api_path') }}</td><td>{{ text(row,'externalInstitutionCode','external_institution_code','externalTransactionId','external_transaction_id') }}</td><td>{{ text(row,'status','state') }}</td><td>{{ text(row,'durationMs','duration_ms') }}</td></tr></tbody></table></div>
    <article v-if="selected && tab==='trace'" class="panel"><h2>마스킹 상세</h2><CpfStructuredData class="detail" :value="selected" /></article>
  </section>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createMessage, deleteMessage, findMessages, traceTransaction, updateMessage, type JsonMap, type MessageCommand } from './api'
import { errorMessage, flattenTimeline, read, text } from './support'
const route=useRoute()
const messages=ref<JsonMap[]>([]),trace=ref<JsonMap|null>(null),transactionId=ref(String(route.query.transactionId??'')),limit=ref(100),tab=ref<'registry'|'trace'>('registry'),selected=ref<JsonMap|null>(null),loading=ref(false),error=ref(''),success=ref(''),pendingDelete=ref(false),deleteConfirmed=ref(false)
const form=reactive<MessageCommand>({messageCode:'',locale:'ko-KR',messageFormatType:'FIXED',externalMessage:'',internalMessage:'',parameterCount:0,parameterSample:'',description:'',useYn:'Y',reason:''})
const traceEvents=computed(()=>flattenTimeline(trace.value))
function rowNumber(row:JsonMap,...keys:string[]):number|undefined{const raw=read(row,...keys);const value=Number(raw);return Number.isSafeInteger(value)&&value>0?value:undefined}
function resetForm(){Object.assign(form,{messageId:undefined,messageCode:'',locale:'ko-KR',messageFormatType:'FIXED',externalMessage:'',internalMessage:'',parameterCount:0,parameterSample:'',description:'',useYn:'Y',reason:''});selected.value=null;pendingDelete.value=false;deleteConfirmed.value=false;error.value='';success.value=''}
function selectMessage(row:JsonMap){selected.value=row;Object.assign(form,{messageId:rowNumber(row,'messageId','message_id','id'),messageCode:text(row,'messageCode','message_code','code').replace(/^-$|^null$/,''),locale:text(row,'locale').replace(/^-$|^null$/,'ko-KR'),messageFormatType:(text(row,'messageFormatType','message_format_type')||'FIXED') as MessageCommand['messageFormatType'],externalMessage:text(row,'externalMessage','external_message','messageText','message_text','message').replace(/^-$|^null$/,''),internalMessage:text(row,'internalMessage','internal_message','externalMessage','external_message','messageText','message_text').replace(/^-$|^null$/,''),parameterCount:Number(read(row,'parameterCount','parameter_count')??0),parameterSample:text(row,'parameterSample','parameter_sample').replace(/^-$|^null$/,''),description:text(row,'description').replace(/^-$|^null$/,''),useYn:(text(row,'useYn','use_yn','activeYn','active_yn')==='N'?'N':'Y'),reason:'메시지 운영 변경'});pendingDelete.value=false;deleteConfirmed.value=false}
function validateForm(){if(!form.messageCode.trim())throw new Error('Message Code가 필요합니다.');if(!form.locale.trim())throw new Error('Locale이 필요합니다.');if(!form.externalMessage.trim())throw new Error('External Message가 필요합니다.');if(!form.internalMessage.trim())throw new Error('Internal Message가 필요합니다.');if(form.reason.trim().length<5)throw new Error('감사 가능한 변경 사유를 5자 이상 입력하세요.')}
async function loadMessages(){loading.value=true;error.value='';try{messages.value=await findMessages()}catch(e){error.value=errorMessage(e)}finally{loading.value=false}}
async function saveMessage(){loading.value=true;error.value='';success.value='';try{validateForm();if(form.messageId)await updateMessage(form.messageId,{...form});else await createMessage({...form});success.value=form.messageId?'메시지를 수정했습니다.':'메시지를 등록했습니다.';await loadMessages();if(!form.messageId)resetForm()}catch(e){error.value=errorMessage(e)}finally{loading.value=false}}
async function disableMessage(){if(!form.messageId||!deleteConfirmed.value)return;loading.value=true;error.value='';success.value='';try{validateForm();messages.value=await deleteMessage(form.messageId,form.reason);success.value='메시지를 비활성화했습니다.';resetForm()}catch(e){error.value=errorMessage(e)}finally{loading.value=false}}
async function loadTrace(){if(!transactionId.value)return;loading.value=true;error.value='';try{trace.value=await traceTransaction(transactionId.value,limit.value);tab.value='trace'}catch(e){error.value=errorMessage(e)}finally{loading.value=false}}
onMounted(async()=>{await loadMessages();if(transactionId.value)await loadTrace()})
</script>
<style scoped>
.workbench{display:grid;gap:1rem}.header,.actions,.tabs{display:flex;gap:.65rem;align-items:center;justify-content:space-between;flex-wrap:wrap}.filters,.form-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:.7rem;align-items:end}.filters label,.form-grid label{display:grid;gap:.3rem}.wide{grid-column:1/-1}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:.75rem}.card,.panel{border:1px solid #d7dde5;border-radius:.6rem;padding:1rem;background:#fff}.card strong{font-size:1.35rem;display:block}.registry-layout{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(22rem,.75fr);gap:1rem}.table-wrap{overflow:auto;border:1px solid #d7dde5;border-radius:.5rem}table{border-collapse:collapse;width:100%;min-width:700px}th,td{padding:.65rem;border-bottom:1px solid #e4e8ed;text-align:left;vertical-align:top}tbody tr{cursor:pointer}tbody tr:hover,tbody tr.selected{background:#f3f6f9}.state{padding:.75rem;border-radius:.4rem;background:#eef3f7}.state.error{background:#fff0f0;color:#a51d1d}.state.warning{background:#fff8dc;color:#715600}.state.success{background:#edf9f0;color:#126b35}.danger{background:#9d1c1c;color:white}.primary{background:#1f5f99;color:white}.detail{white-space:pre-wrap;overflow:auto;max-height:480px;background:#111827;color:#e5e7eb;padding:1rem;border-radius:.5rem}.editor textarea{min-height:75px}.confirm{display:flex!important;gap:.5rem;align-items:center!important}@media(max-width:1000px){.registry-layout{grid-template-columns:1fr}}
</style>
