<template>
      <section class="panel">
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
          <label>Role
            <select v-model="permissionForm.roleId" aria-label="권한을 변경할 역할 선택">
              <option value="">역할 선택</option>
              <option v-for="role in (permissionResult.roles || [])" :key="role.roleId" :value="role.roleId">{{ role.roleName || role.roleId }} · {{ role.roleId }}</option>
            </select>
          </label>
          <label>Menu
            <select v-model="permissionForm.menuId" aria-label="권한을 변경할 메뉴 선택">
              <option value="">메뉴 선택</option>
              <option v-for="menu in (permissionResult.menus || [])" :key="menu.menuId" :value="menu.menuId">{{ menu.menuName || menu.menuId }} · {{ menu.menuId }}</option>
            </select>
          </label>
          <label>Button
            <select v-model="permissionForm.buttonId" aria-label="권한을 변경할 버튼 선택">
              <option value="">버튼 선택</option>
              <option v-for="button in (permissionResult.buttons || [])" :key="button.buttonId" :value="button.buttonId">{{ button.buttonName || button.buttonId }} · {{ button.buttonId }}</option>
            </select>
          </label>
          <label>API Permission
            <select v-model="permissionForm.apiPermissionId" aria-label="권한을 변경할 API 권한 선택">
              <option value="">API 권한 선택</option>
              <option v-for="api in (permissionResult.apiPermissions || [])" :key="api.apiPermissionId" :value="api.apiPermissionId">{{ api.apiName || api.apiPermissionId }} · {{ api.apiPermissionId }}</option>
            </select>
          </label>
          <label>조회 <select v-model="permissionForm.readYn"><option>Y</option><option>N</option></select></label>
          <label>쓰기 <select v-model="permissionForm.writeYn"><option>Y</option><option>N</option></select></label>
          <label>삭제/허용 <select v-model="permissionForm.deleteYn"><option>Y</option><option>N</option></select></label>
          <label>사유 <input v-model="permissionForm.reason" type="text"></label>
        </div>
        <div class="filters">
          <label>역할명 <input v-model="roleForm.roleName" type="text"></label>
          <label>역할유형 <input v-model="roleForm.roleType" type="text"></label>
          <label>역할 설명 <input v-model="roleForm.description" type="text"></label><label>사용 상태 <select v-model="roleForm.useYn"><option>Y</option><option>N</option></select></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createRole">역할 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateRole">역할 수정</button><button type="button" v-if="canWrite('PERMISSION')" @click="updateRoleStatus">역할 상태 적용</button>
        </div>
        <div class="filters">
          <label>메뉴명 <input v-model="menuManageForm.menuName" type="text"></label>
          <label>상위 메뉴 <input v-model="menuManageForm.parentMenuId" type="text"></label>
          <label>메뉴 경로 <input v-model="menuManageForm.menuPath" type="text"></label>
          <label>정렬 <input v-model.number="menuManageForm.sortOrder" type="number"></label><label>사용 상태 <select v-model="menuManageForm.useYn"><option>Y</option><option>N</option></select></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createManagedMenu">메뉴 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateManagedMenu">메뉴 수정</button><button type="button" v-if="canWrite('PERMISSION')" @click="updateManagedMenuStatus">메뉴 상태 적용</button>
        </div>
        <div class="filters">
          <label>행위 코드 <input v-model="buttonForm.actionCode" type="text"></label>
          <label>버튼명 <input v-model="buttonForm.buttonName" type="text"></label>
          <label>HTTP <input v-model="buttonForm.httpMethod" type="text"></label>
          <label>API Pattern <input v-model="buttonForm.apiPattern" type="text"></label><label>사용 상태 <select v-model="buttonForm.useYn"><option>Y</option><option>N</option></select></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createButton">버튼 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateButton">버튼 수정</button><button type="button" v-if="canWrite('PERMISSION')" @click="updateButtonStatus">버튼 상태 적용</button>
        </div>
        <div class="filters">
          <label>API 그룹 <input v-model="apiPermissionForm.apiGroupCode" type="text"></label>
          <label>API명 <input v-model="apiPermissionForm.apiName" type="text"></label>
          <label>권한 코드 <input v-model="apiPermissionForm.permissionCode" type="text"></label>
          <label>API Path <input v-model="apiPermissionForm.apiPath" type="text"></label><label>사용 상태 <select v-model="apiPermissionForm.useYn"><option>Y</option><option>N</option></select></label>
          <button type="button" v-if="canWrite('PERMISSION')" @click="createApiPermission">API 권한 등록</button>
          <button type="button" v-if="canWrite('PERMISSION')" @click="updateApiPermission">API 권한 수정</button><button type="button" v-if="canWrite('PERMISSION')" @click="updateApiPermissionStatus">API 상태 적용</button>
        </div>
        <CpfStructuredData class="detail" :value="permissionResult" />
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "PermissionsPage",
  });
</script>
