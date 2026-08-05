/** Runtime control-plane target selector. */
export interface CpfRuntimeTargetSelector {
  environment?: string;
  serviceId?: string;
  groupId?: string;
  instanceIds?: Array<string>;
  excludeInstanceIds?: Array<string>;
  labels?: Record<string, string>;
  zone?: string;
  cell?: string;
  includeDraining: boolean;
  includeMaintenance: boolean;
  allowAll: boolean;
}
