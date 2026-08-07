import manifest from "../../../../cpf-tools/db/metadata/bza-permission-manifest.json";

const aliases = manifest.permissionAliases as Record<string, string>;
const menuGroups = new Set(manifest.menuGroups);

export function canonicalBzaMenuCode(value: string): string {
  const raw = value.trim().toUpperCase();
  const normalized = raw.startsWith("BZA_") ? raw.substring(4) : raw;
  return aliases[normalized] || normalized;
}

export function isCanonicalBzaMenuCode(value: string): boolean {
  return menuGroups.has(canonicalBzaMenuCode(value));
}


type BzaActionRule = { method: string; pathPattern: string; actionCode: string };
export type BzaOperationPermission = { menuCode: string; actionCode: string };

function normalizeApiPath(value: string): string {
  return value
    .replace(/^https?:\/\/[^/]+/i, "")
    .replace(/^\/?bza\/api\/?/i, "")
    .replace(/^\/?api\/?/i, "")
    .replace(/^\/+|\/+$/g, "");
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function pathPatternMatches(pattern: string, path: string): boolean {
  const patternSegments = normalizeApiPath(pattern).split("/").filter(Boolean);
  const pathSegments = normalizeApiPath(path).split("/").filter(Boolean);
  function match(pi: number, si: number): boolean {
    if (pi === patternSegments.length) return si === pathSegments.length;
    const token = patternSegments[pi];
    if (token === "**") {
      for (let next = si; next <= pathSegments.length; next += 1) {
        if (match(pi + 1, next)) return true;
      }
      return false;
    }
    if (si >= pathSegments.length) return false;
    if (token === "*") return match(pi + 1, si + 1);
    const normalizedToken = token.replace(/\{[^}]+\}/g, "*");
    const expression = new RegExp(`^${escapeRegExp(normalizedToken).replace(/\\\*/g, "[^/]+")}$`, "i");
    return expression.test(pathSegments[si]) && match(pi + 1, si + 1);
  }
  return match(0, 0);
}

function resolveMenuCode(path: string): string | null {
  const normalized = normalizeApiPath(path);
  const resourceGroups = manifest.apiResourceGroups as Record<string, string>;
  const entries = Object.entries(resourceGroups).sort((left, right) => right[0].length - left[0].length);
  const match = entries.find(([resource]) => normalized === resource || normalized.startsWith(`${resource}/`));
  return match ? canonicalBzaMenuCode(match[1]) : null;
}

export function resolveBzaOperationPermission(method: string, path: string): BzaOperationPermission | null {
  const normalizedMethod = method.trim().toUpperCase();
  const normalizedPath = normalizeApiPath(path);
  const menuCode = resolveMenuCode(normalizedPath);
  if (!menuCode || !menuGroups.has(menuCode)) return null;
  const rules = manifest.actionRules as BzaActionRule[];
  const rule = rules.find(item => item.method.toUpperCase() === normalizedMethod && pathPatternMatches(item.pathPattern, normalizedPath));
  return rule ? { menuCode, actionCode: rule.actionCode.trim().toUpperCase() } : null;
}
