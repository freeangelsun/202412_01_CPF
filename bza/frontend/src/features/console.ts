import { displayValue as value, escapeAttribute, escapeHtml } from "../shared/html";

const state: { accessToken: string | null; refreshToken: string | null; operator: any; current: string }={accessToken:sessionStorage.getItem('bza.accessToken'),refreshToken:sessionStorage.getItem('bza.refreshToken'),operator:null,current:'dashboard'};
const menus=[
  ['dashboard','대시보드','DASHBOARD'],['users','사용자','USER'],['sessions','내 세션','USER'],['organizations','조직','ORGANIZATION'],
  ['employees','직원','EMPLOYEE'],['roles','역할','ROLE'],['menus','메뉴','MENU'],
  ['permissions','권한','PERMISSION'],['approvals','결재','APPROVAL'],['audits','업무 감사','AUDIT'],
  ['notifications','알림','NOTIFICATION'],['attachments','첨부파일','ATTACHMENT'],
  ['savedSearches','저장 검색','SAVED_SEARCH'],['permissionTools','권한 분석','PERMISSION'],
  ['customers','고객','CUSTOMER'],['products','상품','PRODUCT'],['orders','주문','ORDER'],
  ['settings','업무 설정','SETTING'],['downloads','다운로드 감사','DOWNLOAD']
];
const editors={
  users:{menu:'USER',url:'/api/bza/admin-users',title:'사용자',key:'loginId',fields:[['loginId','로그인 ID','text'],['adminName','사용자명','text'],['roleCode','역할 코드','text'],['rawPassword','신규 비밀번호','password'],['useYn','사용 여부','yn'],['lockYn','잠금 여부','yn'],['passwordChangeRequiredYn','비밀번호 변경 필요','yn']]},
  roles:{menu:'ROLE',url:'/api/bza/roles',title:'역할',key:'roleCode',fields:[['roleCode','역할 코드','text'],['roleName','역할명','text'],['writeAllowedYn','쓰기 허용','yn'],['dataScope','데이터 범위','text'],['useYn','사용 여부','yn']]},
  menus:{menu:'MENU',url:'/api/bza/menus',title:'메뉴',key:'menuCode',fields:[['menuCode','메뉴 코드','text'],['menuName','메뉴명','text'],['parentMenuCode','상위 메뉴 코드','text'],['moduleCode','모듈 코드','text'],['routePath','화면 경로','text'],['iconCode','아이콘 코드','text'],['environmentCode','환경 코드','text'],['apiPath','API 경로','text'],['sortOrder','정렬 순서','number'],['useYn','사용 여부','yn']]},
  permissions:{menu:'PERMISSION',url:'/api/bza/permissions',title:'권한',key:'buttonCode',fields:[['roleCode','역할 코드','text'],['menuCode','메뉴 코드','text'],['buttonCode','버튼/행위 코드','text'],['permissionType','권한 유형','text'],['httpMethod','HTTP 메서드','text'],['apiPattern','API 경로 패턴','text'],['domainCode','업무 영역','text'],['environmentCode','환경 코드','text'],['dataScope','데이터 범위','text'],['allowYn','허용 여부','yn'],['useYn','사용 여부','yn']]},
  notifications:{menu:'NOTIFICATION',url:'/api/bza/notifications',title:'알림',key:'recipientLoginId',fields:[['recipientLoginId','수신 로그인 ID','text'],['notificationType','알림 유형','text'],['title','제목','text'],['messageBody','내용','textarea'],['referenceType','참조 유형','text'],['referenceId','참조 ID','text']]},
  savedSearches:{menu:'SAVED_SEARCH',url:'/api/bza/saved-searches',title:'저장 검색',key:'screenCode',fields:[['screenCode','화면 코드','text'],['searchName','검색명','text'],['criteriaJson','검색 조건 JSON','textarea'],['sharedYn','공유 여부','yn']]}
};
const $=(id: string): any=>{
  const element=document.getElementById(id);
  if(!element)throw new Error(`필수 화면 요소를 찾지 못했습니다: ${id}`);
  return element;
};

export async function initializeBzaConsole(){bind();if(state.accessToken){try{await loadOperator();showApp();await route(location.hash.slice(1)||'dashboard');}catch{clearSession();}}}

function bind(){
  $('loginForm').addEventListener('submit',login);
  $('logoutButton').addEventListener('click',logout);
  $('reloadButton').addEventListener('click',()=>route(state.current));
  $('passwordButton').addEventListener('click',()=>$('passwordDialog').showModal());
  $('changePasswordButton').addEventListener('click',changePassword);
  $('entityCancelButton').addEventListener('click',()=>$('entityDialog').close());
  $('entityForm').addEventListener('submit',saveEntity);
  $('navigation').addEventListener('click',event=>{const button=event.target.closest('[data-route]');if(button)location.hash=button.dataset.route;});
  window.addEventListener('hashchange',()=>route(location.hash.slice(1)||'dashboard'));
}

function renderNavigation(){
  $('navigation').innerHTML=visibleMenus().map(([id,label])=>`<button type="button" data-route="${id}"><span>${escapeHtml(label)}</span></button>`).join('');
}

function visibleMenus(){return menus.filter(([, ,menuCode])=>menuCode==='DASHBOARD'||hasMenu(menuCode));}
function hasMenu(menuCode){return (state.operator?.menus||[]).some(value=>String(value).toUpperCase()===menuCode);}
function hasPermission(menuCode,actionCode){const required=`${menuCode}:${actionCode}`;const all=`${menuCode}:ALL`;return (state.operator?.buttons||[]).some(value=>{const normalized=String(value).toUpperCase();return normalized===required||normalized===all;});}

async function login(event){
  event.preventDefault();$('loginMessage').textContent='';
  try{const result=await raw('/api/bza/auth/login',{method:'POST',body:JSON.stringify({loginId:$('loginId').value,password:$('password').value})});setTokens(result);state.operator=result.operator;showApp();location.hash='dashboard';await route('dashboard');if(result.operator?.passwordChangeRequiredYn==='Y')$('passwordDialog').showModal();}
  catch(error){$('loginMessage').textContent=error.message;}
}

async function logout(){try{await api('/api/bza/auth/logout',{method:'POST',body:JSON.stringify({refreshToken:state.refreshToken})});}finally{clearSession();}}
function clearSession(){sessionStorage.removeItem('bza.accessToken');sessionStorage.removeItem('bza.refreshToken');state.accessToken=null;state.refreshToken=null;state.operator=null;$('appView').hidden=true;$('loginView').hidden=false;}
function setTokens(result){state.accessToken=result.accessToken;if(result.refreshToken)state.refreshToken=result.refreshToken;sessionStorage.setItem('bza.accessToken',state.accessToken);if(state.refreshToken)sessionStorage.setItem('bza.refreshToken',state.refreshToken);}
async function loadOperator(){state.operator=await api('/api/bza/auth/me');}
function showApp(){$('loginView').hidden=true;$('appView').hidden=false;$('operatorName').textContent=state.operator?.operatorName||state.operator?.loginId||'';renderNavigation();}

async function api(url,options: any={},retry=true): Promise<any>{
  const headers={...(options.body instanceof FormData?{}:{'Content-Type':'application/json'}),...(options.headers||{})};if(state.accessToken)headers.Authorization=`Bearer ${state.accessToken}`;
  try{return await raw(url,{...options,headers});}catch(error){if(error.status===401&&retry&&state.refreshToken){const refreshed=await raw('/api/bza/auth/refresh',{method:'POST',body:JSON.stringify({refreshToken:state.refreshToken})});setTokens(refreshed);return api(url,options,false);}throw error;}
}
async function raw(url,options: any={}): Promise<any>{const response=await fetch(url,options);const text=await response.text();let data: any={};try{data=text?JSON.parse(text):{};}catch{data={message:text};}if(!response.ok){const error: any=new Error(data.message||`요청 실패 (${response.status})`);error.status=response.status;throw error;}return data;}

async function route(name){const allowed=visibleMenus();state.current=allowed.some(([id])=>id===name)?name:'dashboard';document.querySelectorAll('[data-route]').forEach(button=>button.classList.toggle('active',(button as HTMLElement).dataset.route===state.current));const label=allowed.find(([id])=>id===state.current)?.[1]||'대시보드';$('pageTitle').textContent=label;$('pageDescription').textContent='BZA 업무 백오피스 운영';$('content').innerHTML='<div class="empty">조회 중...</div>';try{await loaders[state.current]();}catch(error){$('content').innerHTML=`<div class="error">${escapeHtml(error.message)}</div>`;}}

const loaders={
  dashboard:async()=>{const [summary,approvals]=await Promise.all([api('/api/bza/dashboard'),readList('APPROVAL','/api/bza/backoffice/approvals?limit=20')]);$('content').innerHTML=`<div class="metrics">${metric('활성 사용자',summary.activeUserCount)}${metric('활성 직원',summary.activeEmployeeCount)}${metric('진행 결재',summary.pendingApprovalCount)}${metric('읽지 않은 알림',summary.unreadNotificationCount)}${metric('오늘 감사',summary.todayAuditCount)}</div>${tablePanel('최근 결재',approvals,['approvalNo','title','requesterEmployeeNo','approvalStatus','updatedAt'],true)}`;bindApprovalRows();},
  users:()=>loadManagedTable('users','사용자 목록',['adminLoginId','adminName','roleCode','useYn','lockYn','lastLoginAt']),
  sessions:()=>loadSessions(),
  organizations:()=>loadTable('/api/bza/backoffice/organizations','조직 목록',['organizationCode','parentOrganizationCode','organizationName','organizationType','useYn']),
  employees:()=>loadTable('/api/bza/backoffice/employees','직원 목록',['employeeNo','employeeName','organizationCode','positionCode','jobTitleCode','employmentStatus']),
  roles:()=>loadManagedTable('roles','역할 목록',['roleCode','roleName','writeAllowedYn','dataScope','useYn']),
  menus:()=>loadManagedTable('menus','메뉴 목록',['menuCode','menuName','moduleCode','routePath','apiPath','useYn']),
  permissions:()=>loadManagedTable('permissions','권한 매트릭스',['roleCode','menuCode','buttonCode','permissionType','httpMethod','allowYn']),
  approvals:async()=>{const items=await api('/api/bza/backoffice/approvals?limit=100');$('content').innerHTML=tablePanel('결재 문서',items,['approvalNo','approvalType','title','requesterEmployeeNo','approvalStatus','currentStepNo','versionNo'],true);bindApprovalRows();},
  audits:()=>loadTable('/api/bza/backoffice/audits?limit=100','업무 감사',['createdAt','actorId','actionType','targetType','targetId','reason']),
  notifications:()=>loadNotifications(),
  attachments:()=>loadAttachments(),
  savedSearches:()=>loadManagedTable('savedSearches','저장 검색',['screenCode','searchName','criteriaJson','sharedYn','createdBy','updatedAt']),
  permissionTools:()=>loadPermissionTools(),
  customers:()=>loadTable('/api/bza/customers','고객 목록',['customerNo','customerName','email','mobileNo','customerStatus']),
  products:()=>loadTable('/api/bza/products','상품 목록',['productCode','productName','productType','price','useYn']),
  orders:()=>loadTable('/api/bza/orders','주문 목록',['orderNo','customerNo','productCode','orderQuantity','orderAmount','orderStatus']),
  settings:()=>loadTable('/api/bza/settings','업무 설정',['settingKey','settingValue','valueType','encryptedYn','useYn']),
  downloads:async()=>{const [policies,audits]=await Promise.all([api('/api/bza/downloads'),api('/api/bza/download-audits?limit=100')]);$('content').innerHTML=tablePanel('다운로드 정책',policies,['policyKey','policyValue','description','useYn'],false)+tablePanel('다운로드 감사',audits,['createdAt','actorId','downloadCode','fileName','resultStatus','reason'],false);}
};

async function readList(menuCode,url){return hasMenu(menuCode)?api(url):[];}
async function loadTable(url,title,columns){const items=await api(url);$('content').innerHTML=tablePanel(title,items,columns,false);}
async function loadManagedTable(name,title,columns){
  const config=editors[name];const items=await api(config.url);const writable=hasPermission(config.menu,'WRITE');
  const action=writable?`<button type="button" class="primary" data-entity-add="${name}">등록</button>`:'';
  const head=columns.map(column=>`<th>${escapeHtml(column)}</th>`).join('');
  const rows=items.map((item,index)=>`<tr>${columns.map(column=>`<td>${escapeHtml(value(item[column]))}</td>`).join('')}${writable?`<td><button type="button" data-entity-edit="${name}" data-index="${index}">수정</button></td>`:''}</tr>`).join('');
  $('content').innerHTML=`<section class="panel"><div class="panel-head"><h2>${escapeHtml(title)}</h2><div class="inline-actions"><span>${items.length}건</span>${action}</div></div><div class="table-wrap"><table><thead><tr>${head}${writable?'<th>관리</th>':''}</tr></thead><tbody>${rows||`<tr><td colspan="${columns.length+(writable?1:0)}" class="empty">조회 결과가 없습니다.</td></tr>`}</tbody></table></div></section>`;
  $('content').querySelector('[data-entity-add]')?.addEventListener('click',()=>openEntityEditor(name,{}));
  $('content').querySelectorAll('[data-entity-edit]').forEach(button=>button.addEventListener('click',()=>openEntityEditor(name,items[Number(button.dataset.index)])));
}
function openEntityEditor(name,item){
  const config=editors[name];$('entityForm').dataset.editor=name;$('entityDialogTitle').textContent=`${config.title} 등록·수정`;$('entityMessage').textContent='';
  $('entityFields').innerHTML=config.fields.map(([field,label,type])=>{
    const sourceField=field==='loginId'?'adminLoginId':field;const current=item[sourceField]??'';
    if(type==='yn')return `<label>${escapeHtml(label)}<select name="${field}"><option value="Y" ${current==='Y'?'selected':''}>Y</option><option value="N" ${current==='N'?'selected':''}>N</option></select></label>`;
    if(type==='textarea')return `<label class="wide">${escapeHtml(label)}<textarea name="${field}" rows="4" ${field===config.key?'required':''}>${escapeHtml(current)}</textarea></label>`;
    return `<label>${escapeHtml(label)}<input name="${field}" type="${type}" value="${type==='password'?'':escapeAttribute(current)}" ${field===config.key?'required':''}></label>`;
  }).join('')+`<label class="wide">감사 사유<textarea name="reason" rows="3" required></textarea></label>`;
  $('entityDialog').showModal();
}
async function saveEntity(event){
  event.preventDefault();const name=event.currentTarget.dataset.editor;const config=editors[name];if(!config||!hasPermission(config.menu,'WRITE'))return;
  const payload: any=Object.fromEntries(new FormData(event.currentTarget).entries());
  if(payload.rawPassword==='')delete payload.rawPassword;if(payload.sortOrder!==undefined&&payload.sortOrder!=='')payload.sortOrder=Number(payload.sortOrder);
  try{await api(config.url,{method:'POST',body:JSON.stringify(payload)});$('entityDialog').close();await route(name);}catch(error){$('entityMessage').textContent=error.message;}
}
function metric(label,value){return `<div class="metric"><span>${escapeHtml(label)}</span><strong>${value}</strong></div>`;}
function tablePanel(title,items,columns,approval){const writable=approval&&hasPermission('APPROVAL','WRITE');const head=columns.map(column=>`<th>${escapeHtml(column)}</th>`).join('');const rows=items.map(item=>`<tr ${approval?`data-approval-id="${item.approvalId}"`:''}>${columns.map(column=>`<td>${column==='approvalStatus'?`<span class="status ${escapeHtml(String(item[column]||''))}">${escapeHtml(value(item[column]))}</span>`:escapeHtml(value(item[column]))}</td>`).join('')}${writable?'<td><div class="inline-actions"><button data-action="APPROVE">승인</button><button data-action="REJECT">반려</button></div></td>':''}</tr>`).join('');return `<section class="panel"><div class="panel-head"><h2>${escapeHtml(title)}</h2><span>${items.length}건</span></div><div class="table-wrap"><table><thead><tr>${head}${writable?'<th>처리</th>':''}</tr></thead><tbody>${rows||`<tr><td colspan="${columns.length+(writable?1:0)}" class="empty">조회 결과가 없습니다.</td></tr>`}</tbody></table></div></section>`;}
function bindApprovalRows(){if(!hasPermission('APPROVAL','WRITE'))return;$('content').addEventListener('click',async event=>{const button=event.target.closest('[data-action]');if(!button)return;const row=button.closest('[data-approval-id]');const reason=prompt('처리 사유를 입력하세요.');if(!reason)return;try{await api(`/api/bza/backoffice/approvals/${row.dataset.approvalId}/actions`,{method:'POST',body:JSON.stringify({action:button.dataset.action,idempotencyKey:crypto.randomUUID(),reason,comment:reason})});await route('approvals');}catch(error){alert(error.message);}});}

async function loadNotifications(){const items=await api('/api/bza/notifications?limit=100');const writable=hasPermission('NOTIFICATION','WRITE');$('content').innerHTML=`<section class="panel"><div class="panel-head"><h2>내 알림</h2>${writable?'<button type="button" class="primary" data-notification-add>등록</button>':''}</div><div class="table-wrap"><table><thead><tr><th>등록일시</th><th>유형</th><th>제목</th><th>내용</th><th>상태</th><th>관리</th></tr></thead><tbody>${items.map(item=>`<tr><td>${escapeHtml(value(item.createdAt))}</td><td>${escapeHtml(value(item.notificationType))}</td><td>${escapeHtml(value(item.title))}</td><td>${escapeHtml(value(item.messageBody))}</td><td>${item.readYn==='Y'?'읽음':'미확인'}</td><td>${item.readYn==='Y'?'':`<button type="button" data-notification-read="${item.notificationId}">읽음</button>`}</td></tr>`).join('')||'<tr><td colspan="6" class="empty">조회 결과가 없습니다.</td></tr>'}</tbody></table></div></section>`;$('content').querySelector('[data-notification-add]')?.addEventListener('click',()=>openEntityEditor('notifications',{}));$('content').querySelectorAll('[data-notification-read]').forEach(button=>button.addEventListener('click',async()=>{const reason=prompt('읽음 처리 사유를 입력하세요.');if(reason){await api(`/api/bza/notifications/${button.dataset.notificationRead}/read?reason=${encodeURIComponent(reason)}`,{method:'POST'});await loadNotifications();}}));}

async function loadSessions(){const items=await api('/api/bza/auth/sessions?limit=50');$('content').innerHTML=`<section class="panel"><div class="panel-head"><h2>내 refresh 세션</h2></div><div class="table-wrap"><table><thead><tr><th>세션 ID</th><th>발급 거래</th><th>발급일시</th><th>만료일시</th><th>폐기 여부</th><th>관리</th></tr></thead><tbody>${items.map(item=>`<tr><td>${escapeHtml(value(item.sessionId))}</td><td>${escapeHtml(value(item.transactionGlobalId))}</td><td>${escapeHtml(value(item.createdAt))}</td><td>${escapeHtml(value(item.expiresAt))}</td><td>${escapeHtml(value(item.revokedYn))}</td><td>${item.revokedYn==='Y'?'':`<button type="button" data-session-revoke="${item.sessionId}">폐기</button>`}</td></tr>`).join('')||'<tr><td colspan="6" class="empty">조회 결과가 없습니다.</td></tr>'}</tbody></table></div></section>`;$('content').querySelectorAll('[data-session-revoke]').forEach(button=>button.addEventListener('click',async()=>{const reason=prompt('세션 폐기 사유를 입력하세요.');if(reason){await api(`/api/bza/auth/sessions/${button.dataset.sessionRevoke}/revoke?reason=${encodeURIComponent(reason)}`,{method:'POST'});await loadSessions();}}));}

async function loadAttachments(){const groupId='GENERAL';const items=await api(`/api/bza/attachments?groupId=${groupId}`);const writable=hasPermission('ATTACHMENT','WRITE');$('content').innerHTML=`${writable?'<section class="panel"><div class="panel-head"><h2>첨부파일 업로드</h2></div><form id="attachmentForm" class="dialog-grid"><label>그룹 ID<input name="groupId" value="GENERAL" required></label><label>감사 사유<input name="reason" required></label><label class="wide">파일<input name="file" type="file" required></label><button class="primary" type="submit">업로드</button></form></section>':''}${tablePanel('첨부파일',items,['attachmentId','attachmentGroupId','originalFileName','contentType','fileSize','checksumSha256','scanStatus','createdAt'],false)}`;$('attachmentForm')?.addEventListener('submit',async event=>{event.preventDefault();const form=new FormData(event.currentTarget);await api('/api/bza/attachments',{method:'POST',body:form});await loadAttachments();});}

async function loadPermissionTools(){$('content').innerHTML='<section class="panel"><div class="panel-head"><h2>역할 권한 비교</h2></div><form id="roleCompareForm" class="dialog-grid"><label>기준 역할<input name="leftRoleCode" required></label><label>비교 역할<input name="rightRoleCode" required></label><button class="primary" type="submit">비교</button></form><div id="permissionResult"></div></section><section class="panel"><div class="panel-head"><h2>권한 시뮬레이션</h2></div><form id="permissionSimulationForm" class="dialog-grid"><label>역할<input name="roleCode" required></label><label>메뉴<input name="menuCode" required></label><label>행위<input name="actionCode" required></label><label>HTTP 메서드<input name="httpMethod" value="GET" required></label><label class="wide">API 경로<input name="apiPath" value="/api/bza/" required></label><label>환경<input name="environmentCode" value="ALL" required></label><label>업무 영역<input name="domainCode" value="BZA" required></label><label class="wide">감사 사유<textarea name="reason" required></textarea></label><button class="primary" type="submit">시뮬레이션</button></form><pre id="simulationResult"></pre></section>';$('roleCompareForm').addEventListener('submit',async event=>{event.preventDefault();const form=new FormData(event.currentTarget);const rows=await api(`/api/bza/permissions/compare?leftRoleCode=${encodeURIComponent(String(form.get('leftRoleCode')))}&rightRoleCode=${encodeURIComponent(String(form.get('rightRoleCode')))}`);$('permissionResult').innerHTML=tablePanel('권한 차이',rows,['permissionKey','leftRoleCode','rightRoleCode','different'],false);});$('permissionSimulationForm').addEventListener('submit',async event=>{event.preventDefault();const payload=Object.fromEntries(new FormData(event.currentTarget).entries());const result=await api('/api/bza/permissions/simulate',{method:'POST',body:JSON.stringify(payload)});$('simulationResult').textContent=JSON.stringify(result,null,2);});}

async function changePassword(){const message=$('passwordMessage');message.textContent='';try{await api('/api/bza/auth/password/change',{method:'POST',body:JSON.stringify({currentPassword:$('currentPassword').value,newPassword:$('newPassword').value,newPasswordConfirm:$('newPasswordConfirm').value})});message.style.color='#087f5b';message.textContent='변경되었습니다. 다시 로그인하세요.';setTimeout(()=>{ $('passwordDialog').close();clearSession();},700);}catch(error){message.style.color='';message.textContent=error.message;}}
