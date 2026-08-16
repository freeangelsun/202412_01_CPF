/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RawActionRule {
  actionCode?: string;
  method?: string;
  pathPattern?: string;
}
