import { format as formatFns } from 'date-fns';
import { mainStore } from '@/store';
import { enUS, zhCN } from 'date-fns/locale';
import { SUPPORT_LANGUAGES } from '@/commons/i18n.ts';

const LOCALE_MAP = {
  [SUPPORT_LANGUAGES.ZH_CN]: zhCN,
  [SUPPORT_LANGUAGES.EN_US]: enUS
} as const;

function format(date: number | Date, formatStr = 'PP HH:mm:ss'): string {
  const mStore = mainStore(window.$pinia);
  const language = mStore.getLanguage;
  return formatFns(date, formatStr, {
    locale: LOCALE_MAP[language]
  });
}

export { format };
