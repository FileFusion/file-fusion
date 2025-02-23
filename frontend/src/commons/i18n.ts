import { createI18n } from 'vue-i18n';
import enUS from '@/assets/languages/en-US.json';
import zhCN from '@/assets/languages/zh-CN.json';

type MessageSchema = typeof enUS;

export enum SUPPORT_LANGUAGES {
  EN_US = 'en-US',
  ZH_CN = 'zh-CN'
}

function getDefaultLanguage(): SUPPORT_LANGUAGES {
  const language = localStorage.getItem('language') || navigator.language;
  if (language) {
    for (const supportLanguage of Object.values(SUPPORT_LANGUAGES)) {
      if (language.toLowerCase() === supportLanguage.toLowerCase()) {
        return supportLanguage;
      }
    }
  }
  return SUPPORT_LANGUAGES.EN_US;
}

export const DefaultLanguage = getDefaultLanguage();

export default createI18n<[MessageSchema], SUPPORT_LANGUAGES>({
  legacy: false,
  locale: DefaultLanguage,
  fallbackLocale: SUPPORT_LANGUAGES.EN_US,
  messages: {
    'en-US': enUS,
    'zh-CN': zhCN
  }
});
