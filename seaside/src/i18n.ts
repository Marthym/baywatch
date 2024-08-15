import { createI18n } from 'vue-i18n';
import { en_US } from '@/locales/main_en-US';
import { fr_FR } from '@/locales/main_fr-FR';
import { nextTick } from 'vue';
import { router } from '@/router';

type MessageSchema = typeof en_US;

const datetimeFormats = {
    'en-US': {
        short: {
            year: 'numeric', month: 'short', day: 'numeric',
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric',
            weekday: 'short', hour: 'numeric', minute: 'numeric',
        },
    },
    'fr-FR': {
        short: {
            year: 'numeric', month: 'short', day: 'numeric',
        },
        long: {
            year: 'numeric', month: 'short', day: '2-digit',
            hour: '2-digit', minute: '2-digit',
        },
    },
};

export const i18n = createI18n<[MessageSchema], en_US | fr_FR>({
    legacy: false,
    locale: Intl.DateTimeFormat().resolvedOptions().locale,
    fallbackLocale: 'en-US',
    messages: { 'en-US': en_US, 'fr-FR': fr_FR },
    datetimeFormats: datetimeFormats,
});

router.beforeEach(async (to, from, next) => {
    const localePageFile = `./locales/${to.name}-${i18n.global.locale.value}.ts`;
    try {
        const messagesFallback = import(`./locales/${to.name}_${i18n.global.fallbackLocale.value}.ts`);
        const messages = import(`./locales/${to.name}_${i18n.global.locale.value}.ts`);
        await Promise.all([
            messagesFallback.then(msg => i18n.global.mergeLocaleMessage(i18n.global.locale.value, msg[i18n.global.fallbackLocale.value.replace('-', '_')])),
            messages.then(msg => i18n.global.mergeLocaleMessage(i18n.global.locale.value, msg[i18n.global.locale.value.replace('-', '_')])),
        ]);
    } catch (error) {
        console.debug('Error on loading locale file : ', error.message);
    }

    await nextTick();
    return next();
});