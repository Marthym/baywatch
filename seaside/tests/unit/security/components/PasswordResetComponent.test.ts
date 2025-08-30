import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { createRouter, createWebHashHistory } from 'vue-router';
import PasswordResetComponent from '@/security/components/PasswordResetComponent.vue';

describe('PasswordResetComponent', () => {
    test('render password reset curtain', async () => {

        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: {
                'en': {
                    'security.passreset.dialog.submit': 'Reset',
                    'security.passreset.title': 'Reset your password',
                },
            },
        });

        const router = createRouter({
            history: createWebHashHistory(),
            routes: [
                { path: '/', name: 'page-login', component: PasswordResetComponent },
                { path: '/register', component: PasswordResetComponent, name: 'security-register' },
            ],
        });

        const wrapper = mount(PasswordResetComponent, {
            global: {
                plugins: [i18n, router],
            },
        });

        const element = await vi.waitFor(
            () => wrapper.find('h2').exists()
                ? Promise.resolve(wrapper.find('h2'))
                : Promise.reject(),
        );

        expect(element.text()).toEqual('Reset your password');

        expect(wrapper.find('button.btn-primary').text()).toEqual('Reset');
    });
});