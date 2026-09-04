import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const MBW_FRONTEND_DEV_PORT = Number(process.env.MBW_FRONTEND_DEV_PORT ?? '5173')
// Backoffice Web 은 MBW Channel Front(BFF)이며 자기 자신이 /api/v1/backoffice/** 를 서비스한다
// (cpf-backoffice-web application.yml: MBW_WEB_PORT, 기본 8092).
// dev proxy 가 ADM 포트를 가리키면 개발자는 Channel Front 대신 Platform Control Plane 을 호출하게 된다.
const MBW_WEB_DEV_URL = process.env.MBW_WEB_DEV_URL ?? 'http://127.0.0.1:8092'

export default defineConfig({
  plugins: [vue()],
  // 공개 Runtime 은 이 번들을 Spring Boot 정적 리소스로 서비스한다. ADM(/adm/)과 경로가 겹치지
  // 않도록 Channel Front 자기 경로를 쓴다.
  base: '/mbw/',
  build: {
    outDir: '../build/generated/frontend/static/mbw',
    emptyOutDir: true
  },
  server: {
    port: MBW_FRONTEND_DEV_PORT,
    proxy: { '/api/v1/backoffice': { target: MBW_WEB_DEV_URL, changeOrigin: true } }
  }
})
