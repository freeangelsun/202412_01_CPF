import { describe, expect, it } from "vitest";
import { admFeatureRoutes, admGroupLabels, featureGroupForMenu } from "./routes";

describe("ADM information architecture", () => {
  it("uses home plus five top-level operation groups", () => {
    expect(Object.values(admGroupLabels)).toEqual(["홈","온라인 운영","배치 운영","연계 관리","통합 관제","프레임워크 관리"]);
  });
  it("places gateway and batch features under their single owner groups", () => {
    expect(featureGroupForMenu("gateway-routes")).toBe("online");
    expect(featureGroupForMenu("batch-job-packs")).toBe("batch");
    expect(featureGroupForMenu("gateway-log-policies")).toBe("online");
  });
  it("has a component for every declared route", () => {
    expect(Object.values(admFeatureRoutes).every(route => !!route.component)).toBe(true);
  });
});
