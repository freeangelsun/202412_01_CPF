<template>
  <div class="cpf-page-stack">
    <section class="cpf-card">
      <div class="cpf-card-head"><h1>Feature Flag 운영</h1><span class="cpf-status warning">위험 조치 승인 필요</span></div>
      <p>Typed 평가, 검색·상세, 만료 Override, Kill Switch를 운영합니다. Flag 값은 목록·감사 화면에 원문으로 노출하지 않습니다.</p>
      <p v-if="error" class="cpf-error" role="alert">{{ error }}</p>
      <div class="cpf-toolbar"><input v-model.trim="query" aria-label="Flag 검색" placeholder="Flag Key"><button @click="search">검색</button></div>
      <table class="cpf-table"><thead><tr><th>Flag</th><th>Source</th><th>Revision</th><th>Reason</th></tr></thead><tbody><tr v-for="row in rows" :key="row.flagKey" @click="selected=row"><td>{{ row.flagKey }}</td><td>{{ row.source }}</td><td>{{ row.revision }}</td><td>{{ row.reasonCode }}</td></tr><tr v-if="!rows.length"><td colspan="4">조회 결과가 없습니다.</td></tr></tbody></table>
    </section>
    <section class="cpf-card">
      <h2>Typed 평가</h2>
      <div class="cpf-form-grid"><label>Flag Key<input v-model.trim="evaluation.flagKey"></label><label>Type<select v-model="evaluation.valueType"><option>BOOLEAN</option><option>STRING</option><option>INTEGER</option><option>DECIMAL</option></select></label><label>Fallback<input v-model="evaluation.value"></label><label>Targeting Key<input v-model.trim="evaluation.targetingKey"></label></div>
      <button class="primary" :disabled="busy" @click="evaluate">평가</button><pre v-if="evaluated">{{ safeResult(evaluated) }}</pre>
    </section>
    <section class="cpf-card">
      <h2>Override 승인 요청</h2>
      <div class="cpf-form-grid"><label>Flag Key<input v-model.trim="override.flagKey"></label><label>Type<select v-model="override.valueType"><option>BOOLEAN</option><option>STRING</option><option>INTEGER</option><option>DECIMAL</option></select></label><label>Value<input v-model="override.value" type="password" autocomplete="off"></label><label>Expires At<input v-model="override.expiresAt" type="datetime-local"></label><label class="span-2">사유<input v-model.trim="override.reason"></label></div>
      <button class="primary" :disabled="busy" @click="requestOverride">요청</button>
      <div class="cpf-form-grid"><label>Request ID<input v-model.trim="decision.requestId"></label><label>승인/회수 사유<input v-model.trim="decision.reason"></label></div>
      <div class="cpf-action-row"><button :disabled="busy" @click="approve">승인</button><button class="danger" :disabled="busy" @click="revoke">회수</button></div>
    </section>
    <section class="cpf-card"><h2>Kill Switch</h2><div class="cpf-form-grid"><label>Flag Key<input v-model.trim="kill.flagKey"></label><label>상태<select v-model="kill.enabled"><option :value="true">활성</option><option :value="false">해제</option></select></label><label class="span-2">사유<input v-model.trim="kill.reason"></label></div><button class="danger" :disabled="busy" @click="setKill">반영</button></section>
  </div>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import { admFeatureFlagApproveOverride, admFeatureFlagEvaluate, admFeatureFlagRequestOverride, admFeatureFlagRevokeOverride, admFeatureFlagSearch, admFeatureFlagSetKillSwitch } from "../../generated/cpf-api";
type Row={flagKey:string;source:string;revision:number;reasonCode:string;value?:unknown};
export default defineComponent({name:"FeatureFlagsPage",data(){return{busy:false,error:"",query:"",rows:[] as Row[],selected:null as Row|null,evaluated:null as Row|null,evaluation:{flagKey:"",valueType:"BOOLEAN",value:"false",targetingKey:"operator-preview"},override:{flagKey:"",valueType:"BOOLEAN",value:"false",expiresAt:"",reason:""},decision:{requestId:"",reason:""},kill:{flagKey:"",enabled:true,reason:""}}},mounted(){void this.search();},methods:{async run<T>(action:()=>Promise<T>){this.busy=true;this.error="";try{return await action();}catch(e){this.error=e instanceof Error?e.message:String(e);return undefined;}finally{this.busy=false;}},async search(){const result=await this.run(()=>admFeatureFlagSearch<Row[]>({query:{query:this.query,page:0,size:100}}));this.rows=result??[];},async evaluate(){this.evaluated=(await this.run(()=>admFeatureFlagEvaluate<Row>({data:{...this.evaluation,attributes:{channel:"ADM"}}})))??null;},async requestOverride(){const result=await this.run(()=>admFeatureFlagRequestOverride<{requestId:string}>({data:{...this.override,expiresAt:new Date(this.override.expiresAt).toISOString()}}));if(result)this.decision.requestId=result.requestId;this.override.value="";},async approve(){await this.run(()=>admFeatureFlagApproveOverride({path:{requestId:this.decision.requestId},data:{reason:this.decision.reason},headers:{"X-CPF-Risk-Confirmed":"confirmed"}}));await this.search();},async revoke(){await this.run(()=>admFeatureFlagRevokeOverride({path:{requestId:this.decision.requestId},data:{reason:this.decision.reason},headers:{"X-CPF-Risk-Confirmed":"confirmed"}}));await this.search();},async setKill(){await this.run(()=>admFeatureFlagSetKillSwitch({path:{flagKey:this.kill.flagKey},data:{enabled:this.kill.enabled,reason:this.kill.reason},headers:{"X-CPF-Risk-Confirmed":"confirmed"}}));await this.search();},safeResult(row:Row){return JSON.stringify({flagKey:row.flagKey,source:row.source,revision:row.revision,reasonCode:row.reasonCode},null,2);}}});
</script>
