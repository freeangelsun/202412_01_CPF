/**
 * Explicit EDU-only fixture. Product builds do not receive educational IDs unless
 * VITE_CPF_EDU_PROFILE=true is supplied by an EDU runtime profile.
 */
export function createAdmEducationFixture() {
  return Object.freeze({
    batch: Object.freeze({
      jobId: "CPF_EDU_TASKLET_JOB",
      jobName: "CPF EDU Tasklet Job",
      jobType: "TASKLET",
      scheduleId: "CPF_EDU_TASKLET_DAILY",
      jobParameters: JSON.stringify({ edu: true }),
      description: "ADM batch education data",
    }),
    centerCut: Object.freeze({
      centerCutJobId: "CPF_REF_CENTER_CUT_SAMPLE_JOB",
    }),
  });
}
