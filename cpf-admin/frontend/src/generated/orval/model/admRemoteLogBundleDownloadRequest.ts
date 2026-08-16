/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRemoteLogBundleDownloadRequest {
  artifactIds?: Array<string>;
  reason?: string;
}
