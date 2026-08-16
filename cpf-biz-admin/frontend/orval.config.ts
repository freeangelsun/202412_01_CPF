import { defineConfig } from "orval";

export default defineConfig({
  cpf: {
    input: { target: process.env.CPF_OPENAPI_FILE || "./openapi/cpf-openapi.json" },
    output: {
      target: "./src/generated/orval/cpf-api.ts",
      schemas: "./src/generated/orval/model",
      client: "vue-query",
      mode: "single",
      clean: true,
      prettier: true,
      override: { mutator: { path: "./src/shared/orval-mutator.ts", name: "cpfOrvalRequest" } }
    }
  }
});
