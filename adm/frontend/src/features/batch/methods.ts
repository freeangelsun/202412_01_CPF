export const batchMethods: Record<string, any> = {
  async loadStandardExecutions() {
        const params = this.buildParams(this.standardExecutionSearch);
        params.delete("selectedId");
        this.standardExecutionResult = await this.getJson(`/adm/api/standard-executions?${params.toString()}`);
      },
  async loadStandardExecutionDetail(standardExecutionId) {
        if (!standardExecutionId) return;
        this.standardExecutionSearch.selectedId = standardExecutionId;
        this.standardExecutionDetail = await this.getJson(`/adm/api/standard-executions/${encodeURIComponent(standardExecutionId)}`);
      },
  selectChannelExecutionPolicy(item) {
        this.channelPolicyForm = {
          ...this.channelPolicyForm,
          ...item,
          reason: "거래별 채널 정책 변경",
          requestUser: this.currentOperator.operatorId || "admin-ui"
        };
      },
  async saveChannelExecutionPolicy() {
        if (!this.requireReason(this.channelPolicyForm.reason) || !this.channelPolicyForm.policyKey) return;
        const { policyKey, ...body } = this.channelPolicyForm;
        this.channelSnapshot = await this.sendJson(
          `/adm/api/channels/policies/${encodeURIComponent(policyKey)}`, "PUT", body
        );
        this.setMessage(`채널 실행 정책을 저장했습니다. key=${policyKey}`);
      },
  async createRemoteLogBundleJob() {
        if (this.remoteLogSelectedIds.length === 0 || !this.requireReason(this.remoteLogSearch.reason)) return;
        this.remoteLogBundleGrant = {};
        this.remoteLogBundleJob = await this.sendJson('/adm/api/remote-logs/bundle-jobs', 'POST', {
          artifactIds: this.remoteLogSelectedIds,
          reason: this.remoteLogSearch.reason
        });
        this.setMessage('비동기 로그 ZIP 작업을 등록했습니다.');
        await this.pollRemoteLogBundleJob();
      },
  async loadRemoteLogBundleJob() {
        if (!this.remoteLogBundleJob?.jobId) return;
        this.remoteLogBundleJob = await this.getJson(
          `/adm/api/remote-logs/bundle-jobs/${encodeURIComponent(this.remoteLogBundleJob.jobId)}`
        );
      },
  async pollRemoteLogBundleJob() {
        for (let attempt = 0; attempt < 60; attempt += 1) {
          await this.loadRemoteLogBundleJob();
          if (['COMPLETED', 'FAILED'].includes(this.remoteLogBundleJob.status)) {
            const failedCount = this.remoteLogBundleJob.failedArtifactIds?.length || 0;
            this.setMessage(this.remoteLogBundleJob.status === 'COMPLETED'
              ? `비동기 로그 ZIP 생성이 완료되었습니다. 부분 실패 ${failedCount}건`
              : `비동기 로그 ZIP 생성에 실패했습니다: ${this.remoteLogBundleJob.errorMessage || '원인 미상'}`);
            return;
          }
          await new Promise(resolve => window.setTimeout(resolve, 1000));
        }
        this.setMessage('비동기 로그 ZIP 작업이 계속 처리 중입니다. 상태 새로고침으로 확인하세요.');
      },
  async downloadRemoteLogBundleJob() {
        if (this.remoteLogBundleJob?.status !== 'COMPLETED' || !this.requireReason(this.remoteLogSearch.reason)) return;
        const jobId = encodeURIComponent(this.remoteLogBundleJob.jobId);
        this.remoteLogBundleGrant = await this.sendJson(
          `/adm/api/remote-logs/bundle-jobs/${jobId}/download-tokens`,
          'POST',
          { reason: this.remoteLogSearch.reason }
        );
        const params = this.buildParams({
          token: this.remoteLogBundleGrant.token,
          reason: this.remoteLogSearch.reason
        });
        const response = await fetch(`/adm/api/remote-logs/bundle-jobs/${jobId}/download?${params.toString()}`, {
          headers: this.apiHeaders()
        });
        if (!response.ok) {
          await this.parseResponse(response);
          return;
        }
        const disposition = response.headers.get('Content-Disposition') || '';
        const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
        const blobUrl = URL.createObjectURL(await response.blob());
        const anchor = document.createElement('a');
        anchor.href = blobUrl;
        anchor.download = encodedName ? decodeURIComponent(encodedName) : 'cpf-remote-logs-async.zip';
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();
        URL.revokeObjectURL(blobUrl);
        this.remoteLogBundleGrant = {};
        this.setMessage('비동기 로그 ZIP을 1회성 token으로 다운로드했습니다.');
      },
  async loadBatchJobLogDetail() {
        const search = this.reliabilitySearch;
        if (!search.businessDate || !search.jobName || !search.jobInstanceId) {
          this.setMessage("업무일자, Job 이름, JobInstance ID를 입력하세요.");
          return;
        }
        const path = [search.businessDate, search.jobName, search.jobInstanceId]
          .map(value => encodeURIComponent(value))
          .join("/");
        this.reliabilityResult = {
          ...this.reliabilityResult,
          batchJobLogDetail: await this.getJson(`/adm/api/reliability/batch-job-logs/${path}?maxRecords=200`)
        };
        this.setMessage("BAT JobInstance 로그 상세를 조회했습니다.");
      },
  async loadBatch() {
        const [jobs, executions, schedules, instances, workers, relations, targets, locks, ghostCandidates, operations, steps, calendar] = await Promise.all([
          this.getJson("/adm/api/batch/jobs"),
          this.getJson("/adm/api/batch/executions?limit=50"),
          this.getJson("/adm/api/batch/schedules"),
          this.getJson("/adm/api/batch/instances"),
          this.getJson(`/adm/api/batch/workers?${this.buildParams({ heartbeatTimeoutSeconds: this.batchForm.heartbeatTimeoutSeconds }).toString()}`),
          this.getJson(`/adm/api/batch/relations?${this.buildParams({ jobId: this.batchForm.jobId }).toString()}`),
          this.getJson(`/adm/api/batch/execution-targets?${this.buildParams({ jobId: this.batchForm.jobId, dispatchStatus: this.batchForm.dispatchStatus, limit: 50 }).toString()}`),
          this.getJson(`/adm/api/batch/locks?${this.buildParams({ jobId: this.batchForm.jobId }).toString()}`),
          this.getJson(`/adm/api/batch/ghost-candidates?${this.buildParams({ heartbeatTimeoutSeconds: this.batchForm.heartbeatTimeoutSeconds }).toString()}`),
          this.getJson(`/adm/api/batch/operations?${this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, limit: 50 }).toString()}`),
          this.getJson(`/adm/api/batch/steps?${this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, limit: 50 }).toString()}`),
          this.getJson(`/adm/api/batch/calendar?${this.buildParams({ calendarId: this.batchForm.calendarId }).toString()}`)
        ]);
        this.batchResult = { jobs, executions, schedules, instances, workers, relations, targets, locks, ghostCandidates, operations, steps, calendar };
      },
  async loadCenterCut() {
        const jobId = this.centerCutForm.centerCutJobId;
        const targetParams = this.buildParams({
          statusCode: this.centerCutForm.statusCode,
          limit: this.centerCutForm.limit || 100
        });
        const resultParams = this.buildParams({
          resultStatus: this.centerCutForm.resultStatus,
          limit: this.centerCutForm.limit || 100
        });
        const [jobs, detail, parameters, summary, targets, results] = await Promise.all([
          this.getJson("/adm/api/center-cut/jobs"),
          this.getJson(`/adm/api/center-cut/jobs/${jobId}`),
          this.getJson(`/adm/api/center-cut/jobs/${jobId}/parameters`),
          this.getJson(`/adm/api/center-cut/jobs/${jobId}/summary`),
          this.getJson(`/adm/api/center-cut/jobs/${jobId}/targets?${targetParams.toString()}`),
          this.getJson(`/adm/api/center-cut/jobs/${jobId}/results?${resultParams.toString()}`)
        ]);
        this.centerCutResult = { jobs, detail, parameters, summary, targets, results };
        this.setMessage("Center-Cut 관제 정보를 조회했습니다.");
      },
  async loadCenterCutJobDetail() {
        if (!this.centerCutForm.centerCutJobId) return;
        this.centerCutResult = await this.getJson(`/adm/api/center-cut/jobs/${this.centerCutForm.centerCutJobId}`);
        this.setMessage("Center-Cut Job 상세를 조회했습니다.");
      },
  async loadCenterCutTargets() {
        if (!this.centerCutForm.centerCutJobId) return;
        const params = this.buildParams({
          statusCode: this.centerCutForm.statusCode,
          limit: this.centerCutForm.limit || 100
        });
        this.centerCutResult = {
          targets: await this.getJson(`/adm/api/center-cut/jobs/${this.centerCutForm.centerCutJobId}/targets?${params.toString()}`)
        };
        this.setMessage("Center-Cut target 목록을 조회했습니다.");
      },
  async loadCenterCutResults() {
        if (!this.centerCutForm.centerCutJobId) return;
        const params = this.buildParams({
          resultStatus: this.centerCutForm.resultStatus,
          limit: this.centerCutForm.limit || 100
        });
        this.centerCutResult = {
          results: await this.getJson(`/adm/api/center-cut/jobs/${this.centerCutForm.centerCutJobId}/results?${params.toString()}`)
        };
        this.setMessage("Center-Cut result 목록을 조회했습니다.");
      },
  async loadCenterCutResultDetail(resultId) {
        if (!resultId) return;
        this.centerCutResult = {
          resultDetail: await this.getJson(`/adm/api/center-cut/results/${resultId}`)
        };
        this.setMessage("Center-Cut result 상세를 조회했습니다.");
      },
  async registerBatchJob() {
        if (!this.batchForm.jobId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/jobs", "POST", {
          jobId: this.batchForm.jobId,
          jobName: this.batchForm.jobName,
          jobType: this.batchForm.jobType,
          description: this.batchForm.description,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 Job을 등록했습니다.");
      },
  async runBatchJob() {
        if (!this.batchForm.jobId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/jobs/${this.batchForm.jobId}/run`, "POST", {
          jobParameters: this.batchForm.jobParameters,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 수동 실행을 요청했습니다.");
      },
  async retryBatchExecution() {
        if (!this.batchForm.executionId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/executions/${this.batchForm.executionId}/retry`, "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 재수행을 요청했습니다.");
      },
  async stopBatchExecution() {
        if (!this.batchForm.executionId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/executions/${this.batchForm.executionId}/stop`, "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 중지를 요청했습니다.");
      },
  async simulateBatchSchedule() {
        if (!this.batchForm.scheduleId) return;
        const params = this.buildParams({ baseDate: this.batchForm.businessDate, days: this.batchForm.simulationDays || 14 });
        this.batchResult = { simulation: await this.getJson(`/adm/api/batch/schedules/${this.batchForm.scheduleId}/simulation?${params.toString()}`) };
        this.setMessage("배치 수행 시뮬레이션을 조회했습니다.");
      },
  async loadBatchRelations() {
        const params = this.buildParams({ jobId: this.batchForm.jobId });
        this.batchResult = { relations: await this.getJson(`/adm/api/batch/relations?${params.toString()}`) };
      },
  async loadBatchTargets() {
        const params = this.buildParams({ jobId: this.batchForm.jobId, dispatchStatus: this.batchForm.dispatchStatus, limit: 100 });
        this.batchResult = { targets: await this.getJson(`/adm/api/batch/execution-targets?${params.toString()}`) };
      },
  async loadBatchWorkers() {
        const params = this.buildParams({ heartbeatTimeoutSeconds: this.batchForm.heartbeatTimeoutSeconds });
        this.batchResult = { workers: await this.getJson(`/adm/api/batch/workers?${params.toString()}`) };
        this.setMessage("배치 worker heartbeat를 조회했습니다.");
      },
  async loadBatchLocks() {
        const params = this.buildParams({ jobId: this.batchForm.jobId });
        this.batchResult = { locks: await this.getJson(`/adm/api/batch/locks?${params.toString()}`) };
        this.setMessage("배치 lock을 조회했습니다.");
      },
  async releaseBatchLock() {
        if (!this.batchForm.lockKey || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/locks/release", "POST", {
          lockKey: this.batchForm.lockKey,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 lock 강제 해제를 요청했습니다.");
      },
  async loadBatchGhostCandidates() {
        const params = this.buildParams({ heartbeatTimeoutSeconds: this.batchForm.heartbeatTimeoutSeconds });
        this.batchResult = { ghostCandidates: await this.getJson(`/adm/api/batch/ghost-candidates?${params.toString()}`) };
        this.setMessage("배치 ghost 후보를 조회했습니다.");
      },
  async actBatchGhost() {
        if (!this.batchForm.executionId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/ghost-candidates/${this.batchForm.executionId}/actions`, "POST", {
          actionType: this.batchForm.ghostActionType,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 ghost 조치를 요청했습니다.");
      },
  async loadBatchOperations() {
        const params = this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, limit: 100 });
        this.batchResult = { operations: await this.getJson(`/adm/api/batch/operations?${params.toString()}`) };
        this.setMessage("배치 운영 작업 로그를 조회했습니다.");
      },
  async loadBatchSteps() {
        const params = this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, limit: 100 });
        this.batchResult = { steps: await this.getJson(`/adm/api/batch/steps?${params.toString()}`) };
        this.setMessage("배치 Step 실행 이력을 조회했습니다.");
      },
  async loadBatchJobDetail() {
        if (!this.batchForm.jobId) return;
        this.batchResult = await this.getJson(`/adm/api/batch/jobs/${this.batchForm.jobId}`);
        this.setMessage("배치 Job 상세를 조회했습니다.");
      },
  async runBatchSchedulerOnce() {
        if (!this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/scheduler/run-once", "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 스케줄러 1회 실행을 요청했습니다.");
      }
};
