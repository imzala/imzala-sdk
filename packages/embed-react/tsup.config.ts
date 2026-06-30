import { defineConfig } from 'tsup';
export default defineConfig({
  entry: ['src/index.tsx'],
  format: ['esm', 'cjs'],
  platform: 'browser',
  target: 'es2019',
  dts: true,
  clean: true,
  external: ['react', 'react/jsx-runtime'],
});
