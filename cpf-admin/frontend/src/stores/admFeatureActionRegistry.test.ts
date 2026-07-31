import { describe, expect, it } from "vitest";
import { composeAdmFeatureActions } from "./admFeatureActionRegistry";

describe("ADM feature action ownership", () => {
  it("combines actions from different feature owners", () => {
    const actions = composeAdmFeatureActions([
      { owner: "a", actions: { first: () => 1 } },
      { owner: "b", actions: { second: () => 2 } }
    ]);
    expect(actions.first()).toBe(1);
    expect(actions.second()).toBe(2);
  });

  it("fails closed when two features claim the same action", () => {
    expect(() => composeAdmFeatureActions([
      { owner: "a", actions: { save: () => 1 } },
      { owner: "b", actions: { save: () => 2 } }
    ])).toThrow(/ownership collision/);
  });
});
