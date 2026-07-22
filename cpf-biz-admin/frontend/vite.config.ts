import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vitest/config";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  base: "/bza/",
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
  build: {
    outDir: "../build/generated/frontend/static/bza",
    emptyOutDir: true,
    sourcemap: false,
    manifest: true
  },
  test: {
    environment: "jsdom",
    globals: true
  }
});
