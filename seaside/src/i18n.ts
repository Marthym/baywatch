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

export const lazyloadTranslations: NavigationGuardWithThis<NavigationGuardWithThis<boolean>> = async (to, from, next) => {
    const localePageFile = `./locales/${to.name}_${i18n.global.fallbackLocale.value}.ts`;
    try {
        const messagesFallback = import(`./locales/${to.name}_${i18n.global.fallbackLocale.value}.ts`);
        const messages = import(`./locales/${to.name}_${i18n.global.locale.value}.ts`);
        await Promise.all([
            messagesFallback.then(msg => i18n.global.mergeLocaleMessage(
                i18n.global.fallbackLocale.value, msg[i18n.global.fallbackLocale.value.replace('-', '_')])),
            messages.then(msg => i18n.global.mergeLocaleMessage(
                i18n.global.locale.value, msg[i18n.global.locale.value.replace('-', '_')])),
        ]);
    } catch (error) {
        console.debug('Error on loading locale file', localePageFile, error.message);
    }

    await nextTick();
    return next();
};