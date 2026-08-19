import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
const bzaFrontendDevPort = Number(process.env.BZA_FRONTEND_DEV_PORT ?? '5173')

export default defineConfig({
  plugins: [vue()],
  server: { port: bzaFrontendDevPort, proxy: { '/api/bza': { target: process.env.BZA_CHANNEL_DEV_URL ?? 'http://127.0.0.1:8090', changeOrigin: true } } }
})
