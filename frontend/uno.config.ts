import { defineConfig } from 'unocss';
import { presetUno } from 'unocss';
import presetRemToPx from '@unocss/preset-rem-to-px';
import cssRules from './src/commons/css-rules';

export default defineConfig({
  presets: [presetUno(), presetRemToPx()],
  rules: cssRules
});
