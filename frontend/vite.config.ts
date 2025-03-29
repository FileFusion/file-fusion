import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import UnoCSS from 'unocss/vite';
import Components from 'unplugin-vue-components/vite';
import Icons from 'unplugin-icons/vite';
import IconsResolver from 'unplugin-icons/resolver';
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers';
import checker from 'vite-plugin-checker';

export default defineConfig({
  server: {
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'https://localhost:18881',
        changeOrigin: true,
        secure: false
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(dirname(fileURLToPath(import.meta.url)), './src')
    }
  },
  build: {
    outDir: './target/classes/static'
  },
  plugins: [
    vue(),
    Components({
      dirs: ['./src/components'],
      resolvers: [
        NaiveUiResolver(),
        IconsResolver({
          prefix: false,
          enabledCollections: ['icon-park-outline', 'icon-park-solid'],
          alias: {
            i: 'icon-park-outline',
            'i-solid': 'icon-park-solid'
          }
        })
      ]
    }),
    Icons({
      compiler: 'vue3'
    }),
    UnoCSS(),
    checker({
      typescript: true,
      vueTsc: true,
      eslint: {
        lintCommand: 'eslint "**/*.{html,mjs,ts,vue}"',
        useFlatConfig: true
      },
      stylelint: {
        lintCommand: 'stylelint **/*.{css,vue}'
      }
    })
  ]
});
