import { defineConfig } from "orval";

export default defineConfig({
  cpf: {
    input: { target: process.env.CPF_OPENAPI_FILE || "./openapi/cpf-openapi.json" },
    output: {
      target: "./src/generated/cpf-api.ts",
      schemas: "./src/generated/model",
      client: "vue-query",
      mode: "tags-split",
      prettier: true,
      override: { mutator: { path: "./src/shared/orval-mutator.ts", name: "cpfOrvalRequest" } }
    }
  }
});
