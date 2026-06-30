import { defineConfig } from 'tsup';
export default defineConfig({
  entry: ['src/index.ts'],
  format: ['esm', 'cjs', 'iife'],
  globalName: 'ImzalaEmbed',
  platform: 'browser',
  target: 'es2019',
  dts: true,
  minify: true,
  clean: true,
});
