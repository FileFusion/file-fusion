import type { Rule } from 'unocss';

const cssRules: Rule[] = [
  ['bg-primary', { 'background-color': 'var(--primary-color)' }],
  ['content-min-height', { 'min-height': 'calc(100vh - 172px)' }],
  ['border-color', { 'border-color': 'var(--border-color)' }]
];

export default cssRules;
