export const accessMethods: Record<string, any> = {
  permission(menuId) {
        const found = this.authorizedMenus.find(menu => (menu.menuId || menu.id) === menuId);
        return found || { readAllowed: true, writeAllowed: true, deleteAllowed: true };
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
        const data = await this.parseResponse(response);
        if (!response.ok || !data.accessToken) {
          this.authMessage = JSON.stringify(data, null, 2);
          return;
        }
        this.token = data.accessToken;
        this.currentOperator = data.operator || {};
        this.authorizedMenus = data.menus || [];
        localStorage.setItem("admAccessToken", this.token);
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
          requestUser: operatorId,
          reason: form.reason
        });
        if (!result?.operatorId) {
          this.authMessage = result?.message || "비밀번호 변경에 실패했습니다.";
          return;
        }
        this.forcedPasswordForm.currentPassword = "";
        this.forcedPasswordForm.newPassword = "";
        this.forcedPasswordForm.newPasswordConfirm = "";
        this.clearToken("비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인하세요.");
      },
  async logout() {
        await this.sendJson("/adm/api/auth/logout", "POST");
        this.clearToken("로그아웃되었습니다.");
      },
  async searchMembers() {
        const params = this.buildParams(this.memberSearch);
        const data = await this.getJson(`/adm/api/members?${params.toString()}`);
        this.memberResult = { items: Array.isArray(data) ? data : data.items || [] };
      },
  async loadMemberDetail(memberId) {
        if (!memberId) return;
        this.memberDetail = await this.getJson(`/adm/api/members/${memberId}`);
        const member = this.memberDetail.member || {};
        this.memberForm.memberId = member.id || member.ID || memberId;
        this.memberForm.memberNo = member.member_no || member.MEMBER_NO || "";
        this.memberForm.customerNo = member.customer_no || member.CUSTOMER_NO || "";
        this.memberForm.loginId = member.login_id || member.LOGIN_ID || "";
        this.memberForm.name = member.name || member.NAME || "";
        this.memberForm.email = member.email || member.EMAIL || "";
        this.memberForm.mobileNo = member.mobile_no || member.MOBILE_NO || "";
        this.memberStatusForm.memberStatus = member.member_status || member.MEMBER_STATUS || "ACTIVE";
        this.memberStatusForm.lockYn = member.lock_yn || member.LOCK_YN || "N";
        this.memberStatusForm.withdrawYn = member.withdraw_yn || member.WITHDRAW_YN || "N";
      },
  memberPayload() {
        return {
          memberNo: this.memberForm.memberNo,
          customerNo: this.memberForm.customerNo,
          loginId: this.memberForm.loginId,
          name: this.memberForm.name,
          email: this.memberForm.email,
          mobileNo: this.memberForm.mobileNo,
          memberStatus: this.memberForm.memberStatus,
          channelCode: this.memberForm.channelCode,
          description: this.memberForm.description,
          requestUser: "admin-ui",
          reason: this.memberForm.reason
        };
      },
  async createMember() {
        if (!this.memberForm.loginId || !this.memberForm.name || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson("/adm/api/members", "POST", this.memberPayload());
        await this.searchMembers();
        this.setMessage("회원 등록을 완료했습니다.");
      },
  async updateMember() {
        if (!this.memberForm.memberId || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}`, "PUT", this.memberPayload());
        await this.searchMembers();
        this.setMessage("회원 수정을 완료했습니다.");
      },
  async updateMemberStatus() {
        if (!this.memberForm.memberId || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/status`, "PUT", {
          ...this.memberStatusForm,
          requestUser: "admin-ui",
          reason: this.memberForm.reason
        });
        await this.searchMembers();
        this.setMessage("회원 상태를 변경했습니다.");
      },
  async grantMemberRole() {
        if (!this.memberForm.memberId || !this.memberRoleForm.roleCode || !this.requireReason(this.memberRoleForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/roles`, "POST", this.memberRoleForm);
        this.setMessage("회원 권한을 부여했습니다.");
      },
  async revokeMemberRole() {
        if (!this.memberForm.memberId || !this.memberRoleForm.roleCode || !this.requireReason(this.memberRoleForm.reason)) return;
        const params = this.buildParams({
          serviceCode: this.memberRoleForm.serviceCode,
          reason: this.memberRoleForm.reason,
          requestUser: "admin-ui"
        });
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/roles/${this.memberRoleForm.roleCode}?${params.toString()}`, "DELETE");
        this.setMessage("회원 권한을 회수했습니다.");
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
          requestUser: "admin-ui",
          reason: this.permissionForm.reason
        });
        this.setMessage("메뉴 권한을 저장했습니다.");
      },
  async updateButtonPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.buttonId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/buttons/${this.permissionForm.buttonId}`, "PUT", {
          allowYn: this.permissionForm.deleteYn,
          requestUser: "admin-ui",
          reason: this.permissionForm.reason
        });
        this.setMessage("버튼 권한을 저장했습니다.");
      },
  async updateApiPermissionRole() {
        if (!this.permissionForm.roleId || !this.permissionForm.apiPermissionId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/api-permissions/${this.permissionForm.apiPermissionId}`, "PUT", {
          allowYn: this.permissionForm.deleteYn,
          requestUser: "admin-ui",
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
        this.operatorResult = await this.sendJson("/adm/api/operators", "POST", {
          operatorId: this.operatorForm.operatorId,
          operatorName: this.operatorForm.operatorName,
          password: this.operatorForm.password,
          roleIds: ["ADM_VIEWER"],
          requestUser: "admin-ui",
          reason: this.operatorForm.reason
        });
        this.setMessage("운영자를 등록했습니다.");
      },
  async loadPasswordPolicy() {
        this.passwordResult = await this.getJson("/adm/api/operators/password-policy");
      },
  async resetOperatorPassword() {
        if (!this.passwordForm.operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/password/reset`, "POST", {
          newPassword: this.passwordForm.newPassword,
          forceChange: this.passwordForm.forceChange,
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("비밀번호 초기화를 요청했습니다.");
      },
  async unlockOperator() {
        if (!this.passwordForm.operatorId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/unlock`, "POST", {
          requestUser: "admin-ui",
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
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("세션 강제 종료를 요청했습니다.");
      },
  async cleanupExpiredSessions() {
        if (!this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson("/adm/api/operators/sessions/cleanup-expired", "POST", {
          requestUser: "admin-ui",
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
          requestUser: "admin-ui",
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 등록을 요청했습니다.");
      },
  async verifyMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.otpCode || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson(`/adm/api/security/mfa/${this.securityForm.operatorId}/verify`, "POST", {
          otpCode: this.securityForm.otpCode,
          requestUser: "admin-ui",
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 검증을 요청했습니다.");
      }
};
