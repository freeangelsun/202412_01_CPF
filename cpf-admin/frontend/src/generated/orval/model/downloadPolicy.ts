/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DownloadPolicy {
  description?: string;
  downloadType?: string;
  includeSensitiveAllowed: boolean;
  menuId?: string;
}
