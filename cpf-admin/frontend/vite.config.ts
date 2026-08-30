import { fileURLToPath, URL } from "node:url";
import { configDefaults, defineConfig } from "vitest/config";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  base: "/adm/",
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
  build: {
    outDir: "../build/generated/frontend/static/adm",
    emptyOutDir: true,
    sourcemap: false,
    manifest: true,
    rolldownOptions: {
      output: {
        // Keep the initial ADM payload bounded through physical code splitting.  This avoids
        // masking an oversized bundle by merely raising Vite's warning threshold.
        codeSplitting: {
          groups: [
            {
              name: "element-plus",
              test: /node_modules[\\/]element-plus[\\/]/,
              priority: 30,
              minSize: 20_000,
              maxSize: 400_000
            },
            {
              name: "vue-runtime",
              test: /node_modules[\\/](@vue|vue|vue-router|pinia)[\\/]/,
              priority: 20,
              minSize: 20_000,
              maxSize: 400_000
            },
            {
              name: "vendor",
              test: /node_modules[\\/]/,
              priority: 10,
              minSize: 20_000,
              maxSize: 400_000
            }
          ]
        }
      }
    }
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/test-setup.ts"],
    exclude: [...configDefaults.exclude, "e2e/**"]
  }
});
