export type AdmActionMap = Record<string, (...args: any[]) => unknown>;
export interface AdmFeatureActionGroup { owner: string; actions: AdmActionMap; }

export function composeAdmFeatureActions(groups: readonly AdmFeatureActionGroup[]): AdmActionMap {
  const composed: AdmActionMap = {};
  const owners = new Map<string, string>();
  for (const group of groups) {
    for (const [name, action] of Object.entries(group.actions)) {
      const previous = owners.get(name);
      if (previous) {
        throw new Error(`ADM action ownership collision: ${name} (${previous}, ${group.owner})`);
      }
      if (typeof action !== "function") {
        throw new Error(`ADM action must be a function: ${group.owner}.${name}`);
      }
      owners.set(name, group.owner);
      composed[name] = action;
    }
  }
  return Object.freeze(composed);
}
