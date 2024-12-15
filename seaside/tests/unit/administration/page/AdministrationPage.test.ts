import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import AdministrationPage from '@/administration/page/AdministrationPage.vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import { routes as adminRoutes } from '@/administration/router';
import LoginPage from '@/security/pages/LoginPage.vue';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';
import { of } from 'rxjs';

vi.mock('@/security/services/UserService', () => {
    return {
        userList: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('AdministrationPage', () => {
    test('render user /admin page', async () => {

        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });

        const router = createRouter({
            history: createWebHashHistory(),
            routes: [
                ...adminRoutes,
                { path: '/', component: LoginPage, name: 'test' },
            ],
        });
        await router.push('/admin');
        await router.isReady();
        const wrapper = mount(AdministrationPage, {
            global: {
                plugins: [i18n, router],
                provide: { store: $store },
            },
        });
        expect(wrapper.find('nav').exists()).toBe(true);
        expect(wrapper.findComponent(UserAdminTab).isVisible()).toBe(true);
    });
});