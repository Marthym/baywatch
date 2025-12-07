import { createI18n } from 'vue-i18n';
import { datetimeFormat_en_US, en_US } from '@/locales/main_en-US';
import { datetimeFormat_fr_FR, fr_FR } from '@/locales/main_fr-FR';
import { nextTick } from 'vue';
import { NavigationGuardWithThis } from 'vue-router';

export const i18n = createI18n({
    legacy: false,
    locale: Intl.DateTimeFormat().resolvedOptions().locale,
    fallbackLocale: 'en-US',
    messages: { 'en-US': en_US, 'fr-FR': fr_FR },
    datetimeFormats: { 'en-US': datetimeFormat_en_US, 'fr-FR': datetimeFormat_fr_FR },
});

export const lazyloadTranslations: NavigationGuardWithThis<undefined> = async (to, from, next) => {
    const fallbackLocale = i18n.global.fallbackLocale.value as string;
    const currentLocale = i18n.global.locale.value as string;

    const localePageFile = `./locales/${String(to.name)}_${fallbackLocale}.ts`;
    try {
        const messagesFallback = import(/* @vite-ignore */`./locales/${String(to.name)}_${fallbackLocale}.ts`);
        const messages = import(/* @vite-ignore */`./locales/${String(to.name)}_${currentLocale}.ts`);
        await Promise.all([
            messagesFallback.then(msg => i18n.global.mergeLocaleMessage(
                fallbackLocale, msg[fallbackLocale.replace('-', '_')])),
            messages.then(msg => i18n.global.mergeLocaleMessage(
                currentLocale, msg[currentLocale.replace('-', '_')])),
        ]);
    } catch (error) {
        if (error instanceof Error) {
            console.debug('Error on loading locale file', localePageFile, error.message);
        } else {
            console.debug('Error on loading locale file', localePageFile, error);
        }
    }

    await nextTick();
    return next();
};