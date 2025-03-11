import { createI18n } from 'vue-i18n';
import enUS from '@/assets/languages/en-US.json';
import zhCN from '@/assets/languages/zh-CN.json';
import router, { getRouteTitle } from '@/router';

export enum SUPPORT_LANGUAGES {
  EN_US = 'en-US',
  ZH_CN = 'zh-CN'
}

type MessageSchema = typeof enUS;

const messages: Record<SUPPORT_LANGUAGES, MessageSchema> = {
  [SUPPORT_LANGUAGES.EN_US]: enUS,
  [SUPPORT_LANGUAGES.ZH_CN]: zhCN
};

function getDefaultLanguage(): SUPPORT_LANGUAGES {
  const language = localStorage.getItem('language') || navigator.language;
  const matchedLang = Object.values(SUPPORT_LANGUAGES).find(
    (lang) => lang.toLowerCase() === language.toLowerCase()
  );
  return matchedLang || SUPPORT_LANGUAGES.EN_US;
}

export function languageChange(language: SUPPORT_LANGUAGES) {
  (<any>i18n.global.locale).value = language;
  document.documentElement.setAttribute('lang', language);
  document.title = getRouteTitle(router.currentRoute.value);
}

export const defaultLanguage: SUPPORT_LANGUAGES = getDefaultLanguage();

const i18n = createI18n<[MessageSchema], SUPPORT_LANGUAGES>({
  legacy: false,
  locale: defaultLanguage,
  fallbackLocale: SUPPORT_LANGUAGES.EN_US,
  messages: messages
});
export default i18n;
