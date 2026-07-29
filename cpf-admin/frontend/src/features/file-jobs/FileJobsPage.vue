<template>
  <section class="panel">
    <div class="panel-title">
      <div><p class="eyebrow">STREAMING FILE JOB</p><h2>대량파일 Job</h2></div>
      <div class="actions">
        <button type="button" @click="load">조회</button>
        <button v-if="canButton('FILE_JOB_UPLOAD','FILE_JOB')" type="button" @click="uploadOpen=true">Upload</button>
      </div>
    </div>
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
    <div class="table-wrap"><table><thead><tr><th>Job</th><th>Template</th><th>State</th><th>Rows</th><th>Checksum</th><th>요청자</th><th>관리</th></tr></thead>
      <tbody><tr v-for="job in jobs" :key="job.jobId"><td class="mono">{{job.jobId}}</td><td>{{job.templateCode}} v{{job.templateVersion}}</td><td><span class="cpf-status">{{job.state}}</span></td><td>{{job.successRows}} / {{job.totalRows}} · 실패 {{job.failedRows}}</td><td class="mono">{{job.sourceSha256?.slice(0,12)||'-'}}</td><td>{{job.requestedBy}}</td><td class="actions">
        <button type="button" @click="select(job)">상세</button>
        <button v-if="canButton('FILE_JOB_APPLY','FILE_JOB') && (job.state==='VALIDATED'||job.state==='READY_TO_APPLY')" type="button" @click="openControl(job,'apply')">적용</button>
        <button v-if="canButton('FILE_JOB_RETRY','FILE_JOB') && (job.state==='FAILED'||job.state==='PARTIAL_FAILED') && !job.errorCode?.startsWith('ROLLBACK_')" type="button" @click="openControl(job,'retry')">재시도</button>
        <button v-if="canButton('FILE_JOB_CANCEL','FILE_JOB') && ['RECEIVED','VALIDATED','READY_TO_APPLY'].includes(job.state)" type="button" @click="openControl(job,'cancel')">취소</button>
        <button v-if="canButton('FILE_JOB_ROLLBACK','FILE_JOB') && ['COMPLETED','PARTIAL_FAILED'].includes(job.state)&&job.rollbackSupported" type="button" @click="openControl(job,'rollback')">Rollback</button>
        <button v-if="canButton('FILE_JOB_RESOLVE','FILE_JOB') && job.state==='UNKNOWN_RESULT'" type="button" @click="openResolve(job)">결과 확정</button>
        <a v-if="canButton('FILE_JOB_DOWNLOAD','FILE_JOB') && job.state!=='EXPIRED'" :href="`/adm/api/file-jobs/${job.jobId}/artifact`">Artifact</a>
      </td></tr><tr v-if="!jobs.length"><td colspan="7">조회된 Job이 없습니다.</td></tr></tbody></table></div>
    <aside v-if="selected" class="detail-drawer"><h3>{{selected.jobId}}</h3><dl><dt>Operation</dt><dd>{{selected.operationId}}</dd><dt>Request Hash</dt><dd>{{selected.requestHash}}</dd><dt>Dry-run</dt><dd>{{selected.dryRun}}</dd><dt>Retention</dt><dd>{{selected.retentionUntil}}</dd><dt>통제 운영자</dt><dd>{{selected.controlBy||'-'}}</dd><dt>승인 ID</dt><dd>{{selected.approvalId||'-'}}</dd><dt>통제 사유</dt><dd>{{selected.controlReason||'-'}}</dd><dt>Error</dt><dd>{{selected.errorCode}} {{selected.errorMessage}}</dd></dl><div class="table-wrap"><table><thead><tr><th>Row</th><th>State</th><th>Business Key</th><th>Error</th></tr></thead><tbody><tr v-for="r in rows" :key="r.rowNumber"><td>{{r.rowNumber}}</td><td>{{r.state}}</td><td>{{r.businessKey}}</td><td>{{r.errorCode}} {{r.message}}</td></tr></tbody></table></div></aside>
    <dialog :open="uploadOpen" class="modal"><form class="modal-card" @submit.prevent="upload"><div class="card-head"><h2>Upload Job 접수</h2><button type="button" aria-label="닫기" @click="uploadOpen=false">×</button></div><label>Operation ID<input v-model="form.operationId" required></label><label>Template<input v-model="form.templateCode" required></label><label>Version<input v-model.number="form.templateVersion" type="number" min="1" required></label><label>Format<select v-model="form.format"><option>CSV</option><option>XLSX</option></select></label><label><input v-model="form.dryRun" type="checkbox"> Dry-run</label><label>파일<input type="file" accept=".csv,.xlsx" required @change="pick"></label><label>감사 사유<textarea v-model="form.reason" required></textarea></label><div class="dialog-actions"><button type="button" @click="uploadOpen=false">취소</button><button class="primary">접수</button></div></form></dialog>
    <dialog :open="resolveOpen" class="modal"><form class="modal-card" @submit.prevent="resolveUnknown"><div class="card-head"><h2>결과 불명 확정</h2><button type="button" aria-label="닫기" @click="closeResolve">×</button></div><p>대상 Job: <strong>{{resolveTarget?.jobId}}</strong></p><label>대상 행<select v-model.number="resolveForm.rowNumber" required><option v-for="r in unknownRows" :key="r.rowNumber" :value="r.rowNumber">{{r.rowNumber}} · {{r.state}}</option></select></label><label>확정 결과<select v-model="resolveForm.resolution"><option value="SIDE_EFFECT_NOT_APPLIED">Side Effect 미적용 확인</option><option value="SIDE_EFFECT_APPLIED">Side Effect 적용 확인</option><option value="SIDE_EFFECT_COMPENSATED">보상 완료 확인</option></select></label><label>Business Key<input v-model="resolveForm.businessKey"></label><label>Rollback Token<input v-model="resolveForm.rollbackToken" :required="resolveRollbackTokenRequired"></label><label>승인 ID<input v-model.trim="resolveForm.approvalId" required></label><label>감사 사유<textarea v-model="resolveForm.reason" required></textarea></label><label><input v-model="resolveConfirmed" type="checkbox" required> 외부 시스템·DB 결과와 감사 근거를 확인했습니다.</label><div class="dialog-actions"><button type="button" @click="closeResolve">취소</button><button class="primary" :disabled="!resolveConfirmed || !resolveForm.reason.trim() || !resolveForm.approvalId.trim()">확정</button></div></form></dialog>
    <dialog :open="controlOpen" class="modal"><form class="modal-card" @submit.prevent="control"><div class="card-head"><h2>{{ controlLabel }} 확인</h2><button type="button" aria-label="닫기" @click="closeControl">×</button></div><p>대상 Job: <strong>{{controlTarget?.jobId}}</strong></p><p>현재 상태: {{controlTarget?.state}}</p><label v-if="controlApprovalRequired">승인 ID<input v-model.trim="controlApprovalId" required></label><label>감사 사유<textarea v-model="controlReason" required></textarea></label><label><input v-model="impactConfirmed" type="checkbox" required> 대상과 영향 범위를 확인했습니다.</label><div class="dialog-actions"><button type="button" @click="closeControl">취소</button><button class="primary" :disabled="!impactConfirmed || !controlReason.trim() || (controlApprovalRequired && !controlApprovalId.trim())">실행</button></div></form></dialog>
  </section>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";
export default defineComponent({
  name:"FileJobsPage",mixins:[admConsoleMixin],
  data(){return{jobs:[] as any[],rows:[] as any[],selected:null as any,error:"",uploadOpen:false,file:null as File|null,controlOpen:false,controlTarget:null as any,controlAction:"",controlReason:"",controlApprovalId:"",impactConfirmed:false,resolveOpen:false,resolveTarget:null as any,unknownRows:[] as any[],resolveConfirmed:false,resolveForm:{rowNumber:0,resolution:"SIDE_EFFECT_NOT_APPLIED",businessKey:"",rollbackToken:"",reason:"",approvalId:""},form:{operationId:crypto.randomUUID(),templateCode:"ADM_NOTIFICATION_RULE_IMPORT",templateVersion:1,format:"CSV",dryRun:true,reason:"대량파일 Job 접수"}}},
  computed:{
    controlLabel(){return({apply:"적용",retry:"재시도",cancel:"취소",rollback:"Rollback"} as Record<string,string>)[this.controlAction]||"운영 조치"},
    controlApprovalRequired(){return ['apply','rollback'].includes(this.controlAction)},
    resolveRollbackTokenRequired(){
      const row=(this.unknownRows as any[]).find((item:any)=>item.rowNumber===this.resolveForm.rowNumber);
      return this.resolveForm.resolution==='SIDE_EFFECT_APPLIED' && !!row && !String(row.state).startsWith('ROLLBACK_');
    }
  },
  mounted(){this.load()},
  methods:{
    async request(url:string,options:any={}){options.headers=this.apiHeaders(options.headers||{});const response=await fetch(url,options);return this.parseResponse(response)},
    async load(){this.error="";try{this.jobs=await this.request("/adm/api/file-jobs?limit=200")}catch(e:any){this.error=e.message}},
    pick(e:Event){this.file=(e.target as HTMLInputElement).files?.[0]||null},
    async upload(){if(!this.file||!this.requireReason(this.form.reason))return;const data=new FormData();Object.entries(this.form).forEach(([k,v])=>data.append(k,String(v)));data.append("file",this.file);try{await this.request("/adm/api/file-jobs/uploads",{method:"POST",body:data});this.uploadOpen=false;this.form.operationId=crypto.randomUUID();await this.load()}catch(e:any){this.error=e.message}},
    async select(job:any){this.selected=job;try{this.rows=await this.request(`/adm/api/file-jobs/${job.jobId}/rows`)}catch(e:any){this.error=e.message}},
    openControl(job:any,action:string){this.controlTarget=job;this.controlAction=action;this.controlReason=`File Job ${this.controlLabel}`;this.controlApprovalId="";this.impactConfirmed=false;this.controlOpen=true},
    closeControl(){this.controlOpen=false;this.controlTarget=null;this.controlAction="";this.controlReason="";this.controlApprovalId="";this.impactConfirmed=false},
    async openResolve(job:any){this.resolveTarget=job;try{const result=await this.request(`/adm/api/file-jobs/${job.jobId}/rows`);this.unknownRows=result.filter((r:any)=>['DISPATCHING','UNKNOWN_RESULT','ROLLBACK_DISPATCHING','ROLLBACK_UNKNOWN_RESULT'].includes(r.state));this.resolveForm={rowNumber:this.unknownRows[0]?.rowNumber||0,resolution:"SIDE_EFFECT_NOT_APPLIED",businessKey:"",rollbackToken:"",reason:"결과 불명 운영 확인",approvalId:""};this.resolveConfirmed=false;this.resolveOpen=true}catch(e:any){this.error=e.message}},
    closeResolve(){this.resolveOpen=false;this.resolveTarget=null;this.resolveConfirmed=false},
    async resolveUnknown(){if(!this.resolveTarget||!this.resolveConfirmed||!this.requireReason(this.resolveForm.reason))return;try{await this.request(`/adm/api/file-jobs/${this.resolveTarget.jobId}/resolve-unknown`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(this.resolveForm)});this.closeResolve();await this.load()}catch(e:any){this.error=e.message}},
    async control(){if(!this.controlTarget||!this.impactConfirmed||!this.requireReason(this.controlReason))return;try{await this.request(`/adm/api/file-jobs/${this.controlTarget.jobId}/${this.controlAction}`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({reason:this.controlReason,approvalId:this.controlApprovalId||null})});this.closeControl();await this.load()}catch(e:any){this.error=e.message}}
  }
});
</script>
