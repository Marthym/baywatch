import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import ConfigAdminTab from '@/administration/component/ConfigAdminTab.vue';
import { adminAppConfigurationMailSmtp } from '@/administration/services/AddConfigurationService';

vi.mock('@/administration/services/AddConfigurationService', () => {
    return {
        adminAppConfigurationMailSmtp: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('ConfigAdminTab', () => {
    test('render config admin tab', () => {

        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: {
                'en': { 'admin.config.mail.title': 'Config form' },
            },
        });

        const wrapper = mount(ConfigAdminTab, {
            global: {
                plugins: [i18n],
            },
        });
        expect(wrapper.find('section > h2').exists()).toBe(true);
        expect(wrapper.find('section > h2').text()).toEqual('Config form');
        expect(adminAppConfigurationMailSmtp).toHaveBeenCalledWith();
    });
});