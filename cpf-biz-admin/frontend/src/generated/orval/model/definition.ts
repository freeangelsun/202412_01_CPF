import type { ActionRule } from './actionRule';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Definition {
  actionRules?: Array<ActionRule>;
  apiResourceGroups?: Record<string, string>;
  menuGroups?: Array<string>;
  owner?: string;
  permissionAliases?: Record<string, string>;
  schemaVersion: number;
  sourceProjection?: string;
}
