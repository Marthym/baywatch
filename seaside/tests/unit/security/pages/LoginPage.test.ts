import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import LoginPage from '@/security/pages/LoginPage.vue';
import { createRouter, createWebHashHistory } from 'vue-router';

describe('LoginPage', () => {
    test('render', async () => {
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });
        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };
        const wrapper = mount(LoginPage, {
            global: {
                plugins: [i18n, createRouter({
                    history: createWebHashHistory(),
                    routes: [
                        { path: '/', component: LoginPage, name: 'test' }
                    ],
                })],
                provide: { store: $store },
            },
        });
        expect(wrapper.find('button').exists()).toBe(true);
    });
});