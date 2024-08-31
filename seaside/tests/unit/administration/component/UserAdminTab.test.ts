import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';
import { of } from 'rxjs';
import { userList } from '@/security/services/UserService';

vi.mock('@/security/services/UserService', () => {
    return {
        userList: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('UserAdminTab', () => {
    test('render user admin tab', () => {

        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });

        const wrapper = mount(UserAdminTab, {
            global: {
                plugins: [i18n],
            },
        });
        expect(wrapper.find('table').exists()).toBe(true);
        expect(wrapper.find('table').attributes('aria-describedby')).toEqual('User List');
        expect(userList).toHaveBeenCalledWith(0);
    });
});