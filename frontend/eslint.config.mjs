import globals from 'globals';
import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import pluginVue from 'eslint-plugin-vue';
import eslintConfigPrettier from 'eslint-config-prettier';
import unocss from '@unocss/eslint-config/flat';
import vueEslintParser from 'vue-eslint-parser';

export default tseslint.config({
  ignores: ['node_modules/**', 'target/**'],
  languageOptions: { globals: globals.browser },
  extends: [
    eslint.configs.recommended,
    ...tseslint.configs.recommended,
    ...pluginVue.configs['flat/recommended'],
    {
      files: ['*.vue', '**/*.vue'],
      languageOptions: {
        parser: vueEslintParser,
        parserOptions: {
          sourceType: 'module',
          parser: {
            ts: tseslint.parser
          }
        }
      }
    },
    eslintConfigPrettier,
    unocss
  ],
  rules: {
    '@typescript-eslint/no-explicit-any': 'off'
  }
});
