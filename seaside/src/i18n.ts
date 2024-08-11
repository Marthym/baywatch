import { createI18n } from 'vue-i18n';
import { localeMainEn } from '@/locales/main-en';
import { localeMainFr } from '@/locales/main-fr';

//TODO: Move locales into common/locales and create a locales file in each module
//TODO: Add lazy loading for components

export const i18n = createI18n({
    legacy: false,
    locale: navigator.language,
    fallbackLocale: 'en',
    messages: {
        en: localeMainEn,
        fr: localeMainFr,
    },
});