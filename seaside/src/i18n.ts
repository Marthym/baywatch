import { createI18n } from 'vue-i18n';
import { en_US } from '@/locales/main-en_US';
import { fr_FR } from '@/locales/main-fr_FR';
import { nextTick } from 'vue';

export const i18n = createI18n({
    legacy: false,
    locale: Intl.DateTimeFormat().resolvedOptions().locale.replace('-', '_'),
    fallbackLocale: 'en_US',
    messages: { en_US, fr_FR },
});

export async function loadLocaleMessages(component) {
    try {
        const messages = await import(`@/locales/${component}-${i18n.global.locale.value}.ts`);
        i18n.global.mergeLocaleMessage(i18n.global.locale.value, messages[i18n.global.locale.value]);
    } catch (error) {
    }

    return nextTick();
}
