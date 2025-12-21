import { createI18n, Locale } from 'vue-i18n';
import { datetimeFormat_en_US, en_US } from '@/locales/main_en-US';
import { datetimeFormat_fr_FR, fr_FR } from '@/locales/main_fr-FR';
import { nextTick } from 'vue';
import { NavigationGuardWithThis } from 'vue-router';

const SUPPORTED_LOCALES: Locale[] = ['fr-FR', 'en-US'];
const FALLBACK_LOCALE: Locale = 'en-US';

/**
 * Récupère la locale du navigateur et vérifie si elle est supportée.
 * Si le navigateur renvoie 'fr', on essaye de mapper vers 'fr-FR'.
 */
export function getBrowserLocale(): Locale {
    const browserLocale = navigator.language;

    if (SUPPORTED_LOCALES.includes(browserLocale)) {
        return browserLocale;
    }

    const shortLocale = browserLocale.split('-')[0];
    const matchedLocale = SUPPORTED_LOCALES.find(l => l.startsWith(shortLocale));

    if (matchedLocale) {
        return matchedLocale;
    }

    return FALLBACK_LOCALE;
}

export const i18n = createI18n({
    legacy: false,
    locale: getBrowserLocale(),
    fallbackLocale: FALLBACK_LOCALE,
    messages: { 'en-US': en_US, 'fr-FR': fr_FR },
    datetimeFormats: { 'en-US': datetimeFormat_en_US, 'fr-FR': datetimeFormat_fr_FR },
});

export const lazyloadTranslations: NavigationGuardWithThis<undefined> = async (to, from, next) => {
    const localePageFile = `./locales/${String(to.name)}_${i18n.global.fallbackLocale.value}.ts`;
    try {
        const messagesFallback = import(`./locales/${String(to.name)}_${i18n.global.fallbackLocale.value}.ts`);
        const messages = import(`./locales/${String(to.name)}_${i18n.global.locale.value}.ts`);
        await Promise.all([
            messagesFallback.then(msg => i18n.global.mergeLocaleMessage(
                i18n.global.fallbackLocale.value, msg[i18n.global.fallbackLocale.value.replace('-', '_')])),
            messages.then(msg => i18n.global.mergeLocaleMessage(
                i18n.global.locale.value, msg[i18n.global.locale.value.replace('-', '_')])),
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

export type TranslatorFunction = (key: string) => string;