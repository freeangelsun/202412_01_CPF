import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
const MBW_FRONTEND_DEV_PORT = Number(process.env.MBW_FRONTEND_DEV_PORT ?? '5173')

export default defineConfig({
  plugins: [vue()],
  server: { port: MBW_FRONTEND_DEV_PORT, proxy: { '/api/v1/backoffice': { target: process.env.MBW_WEB_DEV_URL ?? 'http://127.0.0.1:8090', changeOrigin: true } } }
})
