import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { createRouter, createWebHashHistory } from 'vue-router';
import CreateAccountComponent from '@/security/components/CreateAccountComponent.vue';

describe('CreateAccountComponent', () => {
    test('render feeds register window', async () => {

        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: {
                'en': {
                    'security.register.dialog.register': 'Register',
                    'security.register.title': 'Register Curtain Title',
                },
            },
        });

        const router = createRouter({
            history: createWebHashHistory(),
            routes: [
                { path: '/', name: 'page-login', component: CreateAccountComponent },
                { path: '/register', component: CreateAccountComponent, name: 'security-register' },
            ],
        });

        const wrapper = mount(CreateAccountComponent, {
            global: {
                plugins: [i18n, router],
            },
        });

        const element = await vi.waitFor(
            () => wrapper.find('h2').exists()
                ? Promise.resolve(wrapper.find('h2'))
                : Promise.reject(),
        );

        expect(element.text()).toEqual('Register Curtain Title');

        expect(wrapper.find('button.btn-primary').text()).toEqual('Register');
    });
});