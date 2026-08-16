import type { CpfChannelPolicyPackage } from './cpfChannelPolicyPackage';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmChannelPackageImportRequest {
  dryRun: boolean;
  policyPackage: CpfChannelPolicyPackage;
  reason: string;
}
