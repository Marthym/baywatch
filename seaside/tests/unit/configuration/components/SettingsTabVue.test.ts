import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import SettingsTab from '@/configuration/components/SettingsTab.vue';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';

vi.mock('@/security/services/UserSettingsService', () => {
    return {
        userSettingsGet: vi.fn().mockImplementation(() => of({})),
    };
});

describe('SettingsTab', () => {
    test('renders a div', () => {
        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };
        const i18n = createI18n({
            legacy: false,
            messages: {
                'en': {
                    'config.settings.form.preferredLocale': 'en',
                    'config.settings.form.action.save': 'Save',
                },
            },
        });

        const wrapper = mount(SettingsTab, {
            global: {
                plugins: [i18n],
                provide: { store: $store },
            },
        });
        expect(wrapper.find('select').exists()).toBe(true);
    });
});