/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ActionRule {
  actionCode?: string;
  method?: string;
  pathPattern?: string;
}
