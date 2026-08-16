import { admFeatureRoutes } from "./routes";

export function missingAdmFeatureRoutes(menuIds: string[]): string[] {
  return menuIds.filter(menuId => !admFeatureRoutes[menuId]);
}
