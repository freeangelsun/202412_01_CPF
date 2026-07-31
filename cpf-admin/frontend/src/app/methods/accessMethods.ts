
export const accessMethods: Record<string, any> = {
  permission(menuId) {
        const found = this.authorizedMenus.find(menu => (menu.menuId || menu.id) === menuId);
        return found || { readAllowed: false, writeAllowed: false, deleteAllowed: false };
      },
  async login() {
        if (!this.loginForm.operatorId || !this.loginForm.password) {
          this.authMessage = "운영자 ID와 비밀번호를 입력하세요.";
          return;
        }
        const response = await fetch("/adm/api/auth/login", {
          method: "POST",
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: JSON.stringify(this.loginForm)
        });
        const data = await this.parseResponse(response, false);
        if (!response.ok || !data.operator?.operatorId) {
          this.authMessage = JSON.stringify(data, null, 2);
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
        const result = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operatorId)}/password`, "POST", {
          currentPassword: form.currentPassword,
          newPassword: form.newPassword,
          newPasswordConfirm: form.newPasswordConfirm,
          reason: form.reason
        });
        if (!result?.operatorId) {
          this.authMessage = result?.message || "비밀번호 변경에 실패했습니다.";
          return;
        }
        this.forcedPasswordForm.currentPassword = "";
        this.forcedPasswordForm.newPassword = "";
        this.forcedPasswordForm.newPasswordConfirm = "";
        this.clearSession("비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인하세요.");
      },
  async logout() {
        try {
          await this.sendJson("/adm/api/auth/logout", "POST");
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
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/menus/${this.permissionForm.menuId}`, "PUT", {
          readYn: this.permissionForm.readYn,
          writeYn: this.permissionForm.writeYn,
          deleteYn: this.permissionForm.deleteYn,
          reason: this.permissionForm.reason
        });
        this.setMessage("메뉴 권한을 저장했습니다.");
      },
  async updateButtonPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.buttonId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/buttons/${this.permissionForm.buttonId}`, "PUT", {
          allowYn: this.permissionForm.buttonAllowYn,
          reason: this.permissionForm.reason
        });
        this.setMessage("버튼 권한을 저장했습니다.");
      },
  async updateApiPermissionRole() {
        if (!this.permissionForm.roleId || !this.permissionForm.apiPermissionId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/api-permissions/${this.permissionForm.apiPermissionId}`, "PUT", {
          allowYn: this.permissionForm.apiAllowYn,
          reason: this.permissionForm.reason
        });
        this.setMessage("API 권한을 저장했습니다.");
      },
  async createApiPermission() {
        if (!this.apiPermissionForm.apiPermissionId || !this.apiPermissionForm.apiPath || !this.requireReason(this.apiPermissionForm.reason)) return;
        this.permissionResult = await this.sendJson("/adm/api/permissions/api-permissions", "POST", this.apiPermissionForm);
        this.setMessage("API 권한을 등록했습니다.");
      },
  async updateApiPermission() {
        if (!this.apiPermissionForm.apiPermissionId || !this.apiPermissionForm.apiPath || !this.requireReason(this.apiPermissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/api-permissions/${this.apiPermissionForm.apiPermissionId}`, "PUT", this.apiPermissionForm);
        this.setMessage("API 권한을 수정했습니다.");
      },
  async loadOperators() {
        this.operatorResult = await this.getJson("/adm/api/operators");
      },
  async createOperator() {
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
          this.operatorResult = await this.sendJson("/adm/api/operators", "POST", payload);
          this.operatorForm.operationId = crypto.randomUUID();
        } catch (error) {
          // 전송 결과가 불명확한 경우 새 operationId를 만들지 않고 동일 ID로 결과를 먼저 확인합니다.
          try {
            this.operatorResult = await this.getJson(`/adm/api/operators/operations/${encodeURIComponent(operationId)}`);
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
        const operator = this.operatorRawTarget;
        const reason = (this.operatorRawReason || "").trim();
        if (!operator?.operatorId || !this.requireReason(reason)) return;
        this.operatorRawResult = {};
        this.operatorRawError = "";
        this.operatorRawLoading = true;
        try {
          this.operatorRawResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operator.operatorId)}/contacts/raw`, "POST", { reason });
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
        const reason = (this.operatorForm.reason || "").trim();
        if (!this.requireReason(reason)) return;
        this.operatorResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operator.operatorId)}/status`, "PUT", {
          accountStatus: "ACTIVE",
          expectedVersion: operator.versionNo,
          reason
        });
        await this.loadOperators();
      },
  async loadPasswordPolicy() {
        this.passwordResult = await this.getJson("/adm/api/operators/password-policy");
      },
  async resetOperatorPassword() {
        if (!this.passwordForm.operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/password/reset`, "POST", {
          newPassword: this.passwordForm.newPassword,
          forceChange: this.passwordForm.forceChange,
          reason: this.passwordForm.reason
        });
        this.setMessage("비밀번호 초기화를 요청했습니다.");
      },
  async unlockOperator() {
        if (!this.passwordForm.operatorId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/unlock`, "POST", {
          reason: this.passwordForm.reason
        });
        this.setMessage("계정 잠금 해제를 요청했습니다.");
      },
  async loadSessions() {
        const params = this.buildParams({ operatorId: this.passwordForm.operatorId });
        this.passwordResult = await this.getJson(`/adm/api/operators/sessions?${params.toString()}`);
      },
  async revokeSession() {
        if (!this.passwordForm.sessionId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/sessions/${this.passwordForm.sessionId}/revoke`, "POST", {
          reason: this.passwordForm.reason
        });
        this.setMessage("세션 강제 종료를 요청했습니다.");
      },
  async cleanupExpiredSessions() {
        if (!this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson("/adm/api/operators/sessions/cleanup-expired", "POST", {
          reason: this.passwordForm.reason
        });
        this.setMessage("만료 세션 정리를 요청했습니다.");
      },
  async loadSecurity() {
        const ipAllowlist = await this.getJson("/adm/api/security/ip-allowlist");
        const mfa = await this.getJson("/adm/api/security/mfa");
        this.securityResult = { ipAllowlist, mfa };
      },
  async registerMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.secretRef || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson(`/adm/api/security/mfa/${this.securityForm.operatorId}/register`, "POST", {
          secretRef: this.securityForm.secretRef,
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 등록을 요청했습니다.");
      },
  async verifyMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.otpCode || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson(`/adm/api/security/mfa/${this.securityForm.operatorId}/verify`, "POST", {
          otpCode: this.securityForm.otpCode,
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 검증을 요청했습니다.");
      }
};
