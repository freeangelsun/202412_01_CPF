import js from "@eslint/js";
import tsParser from "@typescript-eslint/parser";
import pluginVue from "eslint-plugin-vue";

export default [
  { ignores: ["dist/**", "coverage/**"] },
  js.configs.recommended,
  ...pluginVue.configs["flat/essential"],
  {
    files: ["**/*.ts"],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module"
      },
      globals: {
        window: "readonly",
        document: "readonly",
        localStorage: "readonly",
        sessionStorage: "readonly",
        fetch: "readonly",
        URLSearchParams: "readonly",
        Blob: "readonly",
        URL: "readonly",
        alert: "readonly",
        confirm: "readonly",
        crypto: "readonly",
        navigator: "readonly",
        prompt: "readonly",
        setTimeout: "readonly"
      }
    },
    rules: {
      "no-undef": "off",
      "no-unused-vars": "off",
      "vue/multi-word-component-names": "off"
    }
  },
  {
    files: ["**/*.vue"],
    languageOptions: {
      parserOptions: {
        parser: tsParser,
        ecmaVersion: "latest",
        sourceType: "module"
      },
      globals: {
        window: "readonly",
        document: "readonly",
        localStorage: "readonly",
        sessionStorage: "readonly",
        fetch: "readonly",
        URLSearchParams: "readonly",
        Blob: "readonly",
        URL: "readonly",
        alert: "readonly",
        confirm: "readonly",
        crypto: "readonly",
        navigator: "readonly",
        prompt: "readonly",
        setTimeout: "readonly"
      }
    },
    rules: {
      "no-undef": "off",
      "no-unused-vars": "off",
      "vue/multi-word-component-names": "off"
    }
  }
];
