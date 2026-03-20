import { defineConfig } from 'vite'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const API_TARGET = process.env.API_TARGET || 'http://localhost:8080'
const __dirname = path.dirname(fileURLToPath(import.meta.url))
const FRONTEND_ROOT = path.resolve(__dirname, 'frontend')
const HTML_INPUTS = Object.fromEntries(
  fs
    .readdirSync(FRONTEND_ROOT)
    .filter((name) => name.toLowerCase().endsWith('.html'))
    .map((name) => [name.replace(/\.html$/i, ''), path.resolve(FRONTEND_ROOT, name)])
)
const STATIC_FRONTEND_SCRIPTS = fs
  .readdirSync(FRONTEND_ROOT)
  .filter((name) => name.toLowerCase().endsWith('.js'))

const copyPlainScriptFiles = () => ({
  name: 'copy-plain-script-files',
  writeBundle() {
    const outDir = path.resolve(__dirname, 'dist')
    fs.mkdirSync(outDir, { recursive: true })
    STATIC_FRONTEND_SCRIPTS.forEach((name) => {
      const src = path.resolve(FRONTEND_ROOT, name)
      const dest = path.resolve(outDir, name)
      fs.copyFileSync(src, dest)
    })
  }
})

export default defineConfig({
  root: 'frontend',
  plugins: [copyPlainScriptFiles()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true,
        secure: false
      }
    }
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true,
    rollupOptions: {
      input: HTML_INPUTS
    }
  }
})
