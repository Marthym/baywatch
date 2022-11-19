import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  plugins: [vue()],
  server: {
    port: 8080,
    cors: true,
    proxy: {
      '^/api': {
        target: 'http://localhost:8081',
        toProxy: true,
        timeout: 0,
      },
      '^/graphiql': {
        target: 'http://localhost:8081',
        toProxy: true,
        timeout: 0,
      },
      '^/gpi': {
        target: 'http://localhost:8081',
        toProxy: true,
        timeout: 0,
      },
      '^/img': {
        target: 'http://localhost:8082',
        toProxy: true,
        timeout: 0,
        rewrite: (path) => path.replace(/^\/img/, '')
      },
    },
  }
})
