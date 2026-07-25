<template>
  <template v-if="authenticated && !passwordChangeRequired">
      <section class="panel" v-show="activeMenu === 'permissions'">
        <div class="panel-title">
          <h2>권한 관리</h2>
          <div class="actions">
            <button type="button" @click="loadPermissions">조회</button>
            <button type="button" v-if="canWrite('PERMISSION')" @click="updateMenuPermission">메뉴 권한 저장</button>
            <button type="button" v-if="canWrite('PERMISSION')" @click="updateButtonPermission">버튼 권한 저장</button>
            <button type="button" v-if="canWrite('PERMISSION')" @click="updateApiPermissionRole">API 권한 저장</button>
          </div>
        </div>
        <div class="filters">
          <label>Role ID <input v-model="permissionForm.roleId" type="text"></label>
          <label>Menu ID <input v-model="permissionForm.menuId" type="text"></label>
          <label>Button ID <input v-model="permissionForm.buttonId" type="text"></label>
          <label>API Permission ID <input v-model="permissionForm.apiPermissionId" type="text"></label>
          <label>조회 <select v-model="permissionForm.readYn"><option>Y</option><option>N</option></select></label>
          <label>쓰기 <select v-model="permissionForm.writeYn"><option>Y</option><option>N</option></select></label>
          <label>삭제/허용 <select v-model="permissionForm.deleteYn"><option>Y</option><option>N</option></select></label>
          <label>사유 <input v-model="permissionForm.reason" type="text"></label>
        </div>
        <div class="filters">
          <label>역할명 <input v-model="roleForm.roleName" type="text"></label>
          <label>역할유형 <input v-model="roleForm.roleType" type="text"></label>
          <label>역할 설명 <input v-model="roleForm.description" type="text"></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createRole">역할 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateRole">역할 수정</button>
        </div>
        <div class="filters">
          <label>메뉴명 <input v-model="menuManageForm.menuName" type="text"></label>
          <label>상위 메뉴 <input v-model="menuManageForm.parentMenuId" type="text"></label>
          <label>메뉴 경로 <input v-model="menuManageForm.menuPath" type="text"></label>
          <label>정렬 <input v-model.number="menuManageForm.sortOrder" type="number"></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createManagedMenu">메뉴 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateManagedMenu">메뉴 수정</button>
        </div>
        <div class="filters">
          <label>행위 코드 <input v-model="buttonForm.actionCode" type="text"></label>
          <label>버튼명 <input v-model="buttonForm.buttonName" type="text"></label>
          <label>HTTP <input v-model="buttonForm.httpMethod" type="text"></label>
          <label>API Pattern <input v-model="buttonForm.apiPattern" type="text"></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createButton">버튼 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateButton">버튼 수정</button>
        </div>
        <div class="filters">
          <label>API 그룹 <input v-model="apiPermissionForm.apiGroupCode" type="text"></label>
          <label>API명 <input v-model="apiPermissionForm.apiName" type="text"></label>
          <label>권한 코드 <input v-model="apiPermissionForm.permissionCode" type="text"></label>
          <label>API Path <input v-model="apiPermissionForm.apiPath" type="text"></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createApiPermission">API 권한 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateApiPermission">API 권한 수정</button>
        </div>
        <pre class="detail">{{ pretty(permissionResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'password'">
        <div class="panel-title">
          <h2>비밀번호/세션 관리</h2>
          <div class="actions">
            <button type="button" @click="loadPasswordPolicy">정책 조회</button>
            <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="resetOperatorPassword">비밀번호 초기화</button>
            <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="unlockOperator">잠금 해제</button>
            <button type="button" @click="loadSessions">세션 조회</button>
          </div>
        </div>
        <div class="filters">
          <label>운영자 ID <input v-model="passwordForm.operatorId" type="text"></label>
          <label>새 비밀번호 <input v-model="passwordForm.newPassword" type="password"></label>
          <label>강제 변경 <select v-model="passwordForm.forceChange"><option :value="true">Y</option><option :value="false">N</option></select></label>
          <label>세션 ID <input v-model="passwordForm.sessionId" type="text"></label>
          <label>사유 <input v-model="passwordForm.reason" type="text"></label>
          <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="revokeSession">세션 강제 종료</button>
          <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="cleanupExpiredSessions">만료 세션 정리</button>
        </div>
        <pre class="detail">{{ pretty(passwordResult) }}</pre>
      </section>

      <section class="panel" v-show="activeMenu === 'security'">
        <div class="panel-title">
          <h2>보안 운영</h2>
          <div class="actions">
            <button type="button" @click="loadSecurity">조회</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="saveIpAllowlist">IP 저장</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="registerMfa">MFA 등록</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="verifyMfa">MFA 검증</button>
          </div>
        </div>
        <div class="filters">
          <label>IP/CIDR <input v-model="securityForm.ipPattern" type="text"></label>
          <label>설명 <input v-model="securityForm.description" type="text"></label>
          <label>운영자 ID <input v-model="securityForm.operatorId" type="text"></label>
          <label>Secret Ref <input v-model="securityForm.secretRef" type="text"></label>
          <label>OTP Code <input v-model="securityForm.otpCode" type="text"></label>
          <label>사유 <input v-model="securityForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(securityResult) }}</pre>
      </section>


  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AdmAccessPanels",
  mixins: [admConsoleMixin]
});
</script>
