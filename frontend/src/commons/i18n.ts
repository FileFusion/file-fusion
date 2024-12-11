import { createI18n } from 'vue-i18n';
import enUS from '@/assets/languages/en-US.json';
import zhCN from '@/assets/languages/zh-CN.json';

type MessageSchema = typeof enUS;

export enum SupportLanguages {
  EN_US = 'en-US',
  ZH_CN = 'zh-CN'
}

function getDefaultLanguage(): SupportLanguages {
  const language = localStorage.getItem('language') || navigator.language;
  if (language) {
    for (const supportLanguage of Object.values(SupportLanguages)) {
      if (language.toLowerCase() === supportLanguage.toLowerCase()) {
        return supportLanguage;
      }
    }
  }
  return SupportLanguages.EN_US;
}

export const DefaultLanguage = getDefaultLanguage();

export default createI18n<[MessageSchema], SupportLanguages>({
  legacy: false,
  locale: DefaultLanguage,
  fallbackLocale: SupportLanguages.EN_US,
  messages: {
    'en-US': enUS,
    'zh-CN': zhCN
  }
});
