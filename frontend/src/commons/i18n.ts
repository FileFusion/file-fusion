import { createI18n } from 'vue-i18n';
import enUS from '@/assets/languages/en-US.json';
import zhCN from '@/assets/languages/zh-CN.json';

export enum SUPPORT_LANGUAGES {
  EN_US = 'en-US',
  ZH_CN = 'zh-CN'
}

type MessageSchema = typeof enUS;

function getDefaultLanguage(): SUPPORT_LANGUAGES {
  const language = localStorage.getItem('language') || navigator.language;
  const matchedLang = Object.values(SUPPORT_LANGUAGES).find(
    (lang) => lang.toLowerCase() === language.toLowerCase()
  );
  return matchedLang || SUPPORT_LANGUAGES.EN_US;
}

export const defaultLanguage = getDefaultLanguage();

const messages: Record<SUPPORT_LANGUAGES, MessageSchema> = {
  [SUPPORT_LANGUAGES.EN_US]: enUS,
  [SUPPORT_LANGUAGES.ZH_CN]: zhCN
};

export default createI18n<[MessageSchema], SUPPORT_LANGUAGES>({
  legacy: false,
  locale: defaultLanguage,
  fallbackLocale: SUPPORT_LANGUAGES.EN_US,
  messages: messages
});
