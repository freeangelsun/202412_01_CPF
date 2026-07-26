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
