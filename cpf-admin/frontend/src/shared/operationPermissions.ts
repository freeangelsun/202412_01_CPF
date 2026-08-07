export function parseOperationPermissions(raw: string | undefined): ReadonlySet<string> {
  return new Set((raw ?? "").split(",").map(value => value.trim()).filter(Boolean));
}
export function canInvokeOperation(permissions: ReadonlySet<string>, operationId: string): boolean {
  return permissions.has(operationId);
}
