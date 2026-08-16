import type { RawActionRule } from './rawActionRule';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RawDefinition {
  actionRules?: Array<RawActionRule>;
  apiResourceGroups?: Record<string, string>;
  menuGroups?: Array<string>;
  owner?: string;
  permissionAliases?: Record<string, string>;
  schemaVersion: number;
  sourceProjection?: string;
}
