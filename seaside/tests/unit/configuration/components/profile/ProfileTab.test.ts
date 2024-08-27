import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import ProfileTab from '@/configuration/components/profile/ProfileTab.vue';

vi.mock('@/security/services/UserService', () => {
    return {
        userList: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('ProfileTab', () => {
    test('render config profile tab', () => {
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });
        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };
        const wrapper = mount(ProfileTab, {
            global: {
                plugins: [i18n],
                provide: { store: $store },
            },
        });
        expect(wrapper.find('img').exists()).toBe(true);
        expect(wrapper.find('img').attributes('src'))
            .toEqual('https://www.gravatar.com/avatar/0?s=96&d=retro');
    });
});