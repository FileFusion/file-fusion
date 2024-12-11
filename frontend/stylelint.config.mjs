/** @type {import('stylelint').Config} */
export default {
  ignoreFiles: ['node_modules/**', 'target/**'],
  extends: [
    'stylelint-config-recommended-vue',
    'stylelint-config-standard',
    'stylelint-config-recess-order'
  ]
};
