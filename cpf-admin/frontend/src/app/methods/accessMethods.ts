import { admAuthLogin, admAuthLogout, admOperatorChangePassword, admOperatorFindOperators, admOperatorCreateOperator, admOperatorFindCreateResult, admOperatorRawContact, admOperatorUpdateStatus, admOperatorPasswordPolicy, admOperatorResetPassword, admOperatorUnlockOperator, admOperatorFindSessions, admOperatorRevokeSession, admOperatorCleanupExpiredSessions, admSecurityFindIpAllowlist, admSecurityFindMfaStates, admSecurityRegisterMfa, admSecurityVerifyMfa, admPermissionCreateApiPermission, admPermissionUpdateApiPermission, admPermissionUpdateApiPermissionStatus, admPermissionUpdateButtonPermission, admPermissionUpdateButtonStatus, admPermissionUpdateMenuPermission, admPermissionUpdateMenuStatus, admPermissionUpdateRoleApiPermission, admPermissionUpdateRoleStatus } from "../../generated/cpf-api";

export const accessMethods = {
  permission(menuId) {
        const found = this.authorizedMenus.find(menu => (menu.menuId || menu.id) === menuId);
        return found || { readAllowed: false, writeAllowed: false, deleteAllowed: false };
      },
  async login() {
        if (!this.loginForm.operatorId || !this.loginForm.password) {
          this.authMessage = "운영자 ID와 비밀번호를 입력하세요.";
          return;
        }
        let data;
        try {
          data = await admAuthLogin<any>({ data: this.loginForm });
        } catch (error: any) {
          this.authMessage = error?.message || "로그인에 실패했습니다.";
          return;
        }
        if (!data?.operator?.operatorId) {
          this.authMessage = JSON.stringify(data || { message: "서버 세션이 생성되지 않았습니다." }, null, 2);
          return;
        }
        this.sessionLoaded = true;
        this.currentOperator = data.operator || {};
        this.authorizedMenus = data.menus || [];
        this.authorizedButtons = Array.isArray(data.buttonIds) ? data.buttonIds : [];
        this.buttonsLoaded = Array.isArray(data.buttonIds);
        this.permissionsLoaded = true;
        this.authMessage = "";
        if (this.passwordChangeRequired) {
          this.setMessage("비밀번호 변경이 필요합니다.");
          return;
        }
        this.setMessage("로그인되었습니다.");
        await this.loadInitialData();
      },
  async changeOwnPassword() {
        const form = this.forcedPasswordForm;
        if (!form.currentPassword || !form.newPassword || !form.newPasswordConfirm) {
          this.authMessage = "현재 비밀번호와 새 비밀번호, 확인값을 모두 입력하세요.";
          return;
        }
        if (form.newPassword !== form.newPasswordConfirm) {
          this.authMessage = "새 비밀번호와 확인값이 일치하지 않습니다.";
          return;
        }
        if (!this.requireReason(form.reason)) {
          this.authMessage = this.uiMessage;
          return;
        }
        const operatorId = this.currentOperator.operatorId;
        const result = await admOperatorChangePassword({ path: { operatorId }, data: {
          currentPassword: form.currentPassword, newPassword: form.newPassword,
          newPasswordConfirm: form.newPasswordConfirm, reason: form.reason
        } });
        if (!result?.operatorId) {
          this.authMessage = "비밀번호 변경 응답에 운영자 ID가 없습니다.";
          return;
        }
        this.forcedPasswordForm.currentPassword = "";
        this.forcedPasswordForm.newPassword = "";
        this.forcedPasswordForm.newPasswordConfirm = "";
        this.clearSession("비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인하세요.");
      },
  async logout() {
        try {
          await admAuthLogout({});
        } catch (error) {
          // 서버 세션 폐기가 실패해도 Browser 자격증명/민감 상태는 반드시 제거한다.
        } finally {
          this.clearSession("로그아웃되었습니다.");
        }
      },
  async loadPermissions() {
        const roles = await this.getJson("/adm/api/permissions/roles");
        const menus = await this.getJson("/adm/api/permissions/menus");
        const buttons = await this.getJson("/adm/api/permissions/buttons");
        const menuMatrix = await this.getJson("/adm/api/permissions/menu-matrix");
        const buttonMatrix = await this.getJson("/adm/api/permissions/button-matrix");
        const apiPermissions = await this.getJson("/adm/api/permissions/api-permissions");
        const apiMatrix = await this.getJson("/adm/api/permissions/api-matrix");
        this.permissionResult = { roles, menus, buttons, menuMatrix, buttonMatrix, apiPermissions, apiMatrix };
      },
  async updateMenuPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.menuId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await admPermissionUpdateMenuPermission({
          path: { roleId: this.permissionForm.roleId, menuId: this.permissionForm.menuId },
          data: {
            readYn: this.permissionForm.readYn, writeYn: this.permissionForm.writeYn,
            deleteYn: this.permissionForm.deleteYn, reason: this.permissionForm.reason
          }
        });
        this.setMessage("메뉴 권한을 저장했습니다.");
      },
  async updateButtonPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.buttonId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await admPermissionUpdateButtonPermission({
          path: { roleId: this.permissionForm.roleId, buttonId: this.permissionForm.buttonId },
          data: { allowYn: this.permissionForm.buttonAllowYn, reason: this.permissionForm.reason }
        });
        this.setMessage("버튼 권한을 저장했습니다.");
      },
  async updateApiPermissionRole() {
        if (!this.permissionForm.roleId || !this.permissionForm.apiPermissionId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await admPermissionUpdateRoleApiPermission({
          path: { roleId: this.permissionForm.roleId, apiPermissionId: this.permissionForm.apiPermissionId },
          data: { allowYn: this.permissionForm.apiAllowYn, reason: this.permissionForm.reason }
        });
        this.setMessage("API 권한을 저장했습니다.");
      },
  async updateRoleStatus() {
        if (!this.roleForm.roleId || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await admPermissionUpdateRoleStatus({ path: { roleId: this.roleForm.roleId }, data: { useYn: this.roleForm.useYn, reason: this.roleForm.reason } });
        this.setMessage("역할 사용 상태를 변경했습니다.");
      },
  async updateManagedMenuStatus() {
        if (!this.menuManageForm.menuId || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await admPermissionUpdateMenuStatus({ path: { menuId: this.menuManageForm.menuId }, data: { useYn: this.menuManageForm.useYn, reason: this.menuManageForm.reason } });
        this.setMessage("메뉴 사용 상태를 변경했습니다.");
      },
  async updateButtonStatus() {
        if (!this.buttonForm.buttonId || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await admPermissionUpdateButtonStatus({ path: { buttonId: this.buttonForm.buttonId }, data: { useYn: this.buttonForm.useYn, reason: this.buttonForm.reason } });
        this.setMessage("버튼 사용 상태를 변경했습니다.");
      },
  async updateApiPermissionStatus() {
        if (!this.apiPermissionForm.apiPermissionId || !this.requireReason(this.apiPermissionForm.reason)) return;
        this.permissionResult = await admPermissionUpdateApiPermissionStatus({ path: { apiPermissionId: this.apiPermissionForm.apiPermissionId }, data: { useYn: this.apiPermissionForm.useYn, reason: this.apiPermissionForm.reason } });
        this.setMessage("API 권한 사용 상태를 변경했습니다.");
      },
  async createApiPermission() {
        if (!this.apiPermissionForm.apiPermissionId || !this.apiPermissionForm.apiPath || !this.requireReason(this.apiPermissionForm.reason)) return;
        this.permissionResult = await admPermissionCreateApiPermission({ data: this.apiPermissionForm });
        this.setMessage("API 권한을 등록했습니다.");
      },
  async updateApiPermission() {
        if (!this.apiPermissionForm.apiPermissionId || !this.apiPermissionForm.apiPath || !this.requireReason(this.apiPermissionForm.reason)) return;
        this.permissionResult = await admPermissionUpdateApiPermission({ path: { apiPermissionId: this.apiPermissionForm.apiPermissionId }, data: this.apiPermissionForm });
        this.setMessage("API 권한을 수정했습니다.");
      },
  async loadOperators() {
        this.operatorResult = await admOperatorFindOperators();
      },
  async createOperator() {
        if (!this.canButton("OPERATOR_CREATE", "OPERATOR")) throw new Error("OPERATOR_CREATE 권한이 없습니다.");
        if (!this.operatorForm.operatorId || !this.operatorForm.operatorName || !this.operatorForm.password) {
          this.setMessage("운영자 ID, 이름, 초기 비밀번호가 필요합니다.");
          return;
        }
        if (!this.requireReason(this.operatorForm.reason)) return;
        const operationId = this.operatorForm.operationId || crypto.randomUUID();
        this.operatorForm.operationId = operationId;
        const payload = {
          operatorId: this.operatorForm.operatorId,
          operatorName: this.operatorForm.operatorName,
          operationId,
          mobileNo: this.operatorForm.mobileNo || null,
          officePhoneNo: this.operatorForm.officePhoneNo || null,
          password: this.operatorForm.password,
          reason: this.operatorForm.reason
        };
        try {
          this.operatorResult = await admOperatorCreateOperator({ data: payload });
          this.operatorForm.operationId = crypto.randomUUID();
        } catch (error) {
          // 전송 결과가 불명확한 경우 새 operationId를 만들지 않고 동일 ID로 결과를 먼저 확인합니다.
          try {
            this.operatorResult = await admOperatorFindCreateResult({ path: { operationId } });
            this.operatorForm.operationId = crypto.randomUUID();
          } catch (_lookupError) {
            throw error;
          }
        }
        this.setMessage("운영자를 등록했습니다. Role을 부여한 뒤 ACTIVE로 전환해야 로그인할 수 있습니다.");
      },
  clearOperatorRaw() {
        this.operatorRawResult = {};
        this.operatorRawReason = "";
        this.operatorRawTarget = null;
        this.operatorRawError = "";
        this.operatorRawLoading = false;
      },
  openOperatorRaw(operator: any) {
        this.clearOperatorRaw();
        this.operatorRawTarget = operator;
        this.operatorRawOpen = true;
      },
  closeOperatorRaw() {
        this.operatorRawOpen = false;
        this.clearOperatorRaw();
      },
  async viewOperatorRaw() {
        if (!this.canButton("OPERATOR_PII_RAW", "OPERATOR")) throw new Error("OPERATOR_PII_RAW 권한이 없습니다.");
        const operator = this.operatorRawTarget;
        const reason = (this.operatorRawReason || "").trim();
        if (!operator?.operatorId || !this.requireReason(reason)) return;
        this.operatorRawResult = {};
        this.operatorRawError = "";
        this.operatorRawLoading = true;
        try {
          this.operatorRawResult = await admOperatorRawContact({ path: { operatorId: operator.operatorId }, data: { reason } });
        } catch (error: any) {
          this.operatorRawResult = {};
          const status = Number(error?.status || error?.response?.status || 0);
          const transactionId = error?.transactionId || error?.data?.transactionId || "";
          if (status === 403) this.operatorRawError = "원문 연락처 조회 권한이 없습니다.";
          else if (status === 409) this.operatorRawError = "대상 상태가 변경되었습니다. 목록을 새로고침한 뒤 다시 시도하세요.";
          else if (status === 503) this.operatorRawError = "권한/감사/DB 저장소를 사용할 수 없습니다. 잠시 후 동일 사유로 다시 시도하세요.";
          else this.operatorRawError = error?.message || "원문 연락처 조회에 실패했습니다.";
          if (transactionId) this.operatorRawError += ` (transactionId: ${transactionId})`;
          throw error;
        } finally {
          this.operatorRawLoading = false;
        }
      },
  async activateOperator(operator: any) {
        if (!this.canButton("OPERATOR_STATUS_UPDATE", "OPERATOR")) throw new Error("OPERATOR_STATUS_UPDATE 권한이 없습니다.");
        const reason = (this.operatorForm.reason || "").trim();
        if (!this.requireReason(reason)) return;
        this.operatorResult = await admOperatorUpdateStatus({ path: { operatorId: operator.operatorId }, data: {
          accountStatus: "ACTIVE", expectedVersion: operator.versionNo, reason
        } });
        await this.loadOperators();
      },
  async loadPasswordPolicy() {
        this.passwordResult = await admOperatorPasswordPolicy();
      },
  async resetOperatorPassword() {
        if (!this.passwordForm.operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await admOperatorResetPassword({ path: { operatorId: this.passwordForm.operatorId }, data: {
          newPassword: this.passwordForm.newPassword, forceChange: this.passwordForm.forceChange, reason: this.passwordForm.reason
        } });
        this.setMessage("비밀번호 초기화를 요청했습니다.");
      },
  async unlockOperator() {
        if (!this.passwordForm.operatorId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await admOperatorUnlockOperator({ path: { operatorId: this.passwordForm.operatorId }, data: { reason: this.passwordForm.reason } });
        this.setMessage("계정 잠금 해제를 요청했습니다.");
      },
  async loadSessions() {
        this.passwordResult = await admOperatorFindSessions({ query: { operatorId: this.passwordForm.operatorId || undefined } });
      },
  async revokeSession() {
        if (!this.passwordForm.sessionId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await admOperatorRevokeSession({ path: { sessionId: this.passwordForm.sessionId }, data: { reason: this.passwordForm.reason } });
        this.setMessage("세션 강제 종료를 요청했습니다.");
      },
  async cleanupExpiredSessions() {
        if (!this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await admOperatorCleanupExpiredSessions({ data: { reason: this.passwordForm.reason } });
        this.setMessage("만료 세션 정리를 요청했습니다.");
      },
  async loadSecurity() {
        const ipAllowlist = await admSecurityFindIpAllowlist();
        const mfa = await admSecurityFindMfaStates();
        this.securityResult = { ipAllowlist, mfa };
      },
  async registerMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.secretRef || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await admSecurityRegisterMfa({ path: { operatorId: this.securityForm.operatorId }, data: { secretRef: this.securityForm.secretRef, reason: this.securityForm.reason } });
        this.setMessage("MFA 등록을 요청했습니다.");
      },
  async verifyMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.otpCode || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await admSecurityVerifyMfa({ path: { operatorId: this.securityForm.operatorId }, data: { otpCode: this.securityForm.otpCode, reason: this.securityForm.reason } });
        this.setMessage("MFA 검증을 요청했습니다.");
      }
} satisfies Record<string, any>;
