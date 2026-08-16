/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DownloadArtifact {
  content?: Array<number>;
  fileName?: string;
}
