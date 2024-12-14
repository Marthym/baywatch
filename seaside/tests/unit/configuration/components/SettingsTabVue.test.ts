import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import SettingsTab from '@/configuration/components/SettingsTab.vue';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import { en_US } from '@/locales/config-settings_en-US';

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
                'en': en_US,
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