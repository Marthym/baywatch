import { createI18n } from 'vue-i18n';
import { en_US } from '@/locales/main-en_US';
import { fr_FR } from '@/locales/main-fr_FR';
import { nextTick } from 'vue';
import { router } from '@/router';

type MessageSchema = typeof en_US;

export const i18n = createI18n<[MessageSchema], en_US | fr_FR>({
    legacy: false,
    locale: Intl.DateTimeFormat().resolvedOptions().locale.replace('-', '_'),
    fallbackLocale: 'en_US',
    messages: { en_US, fr_FR },
});

router.beforeEach(async (to, from, next) => {
    const localePageFile = `./locales/${to.name}-${i18n.global.locale.value}.ts`;
    try {
        const messages = await import(`./locales/${to.name}-${i18n.global.locale.value}.ts`);
        i18n.global.mergeLocaleMessage(i18n.global.locale.value, messages[i18n.global.locale.value]);
    } catch (error) {
        console.debug('Error on loading ', localePageFile, error);
    }

    await nextTick();
    return next();
});