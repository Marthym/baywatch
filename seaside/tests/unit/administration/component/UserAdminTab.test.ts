import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n, I18n } from 'vue-i18n';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';
import { of } from 'rxjs';
import { userList } from '@/security/services/UserService';
import { createRouter, createWebHashHistory, Router } from 'vue-router';
import UserEditor from '@/administration/component/usereditor/UserEditor.vue';

vi.mock('@/security/services/UserService', () => {
    return {
        userList: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('UserAdminTab', () => {
    const i18n = createI18n({
        legacy: false,
        missingWarn: false,
        messages: { 'en': {} },
    });
    const router = createRouter({
        history: createWebHashHistory(),
        routes: [
            {
                path: '/admin/users', component: UserAdminTab, name: 'admin-users', children: [
                    { path: ':userId', component: UserEditor, name: 'admin-users-editor' },
                ],
            },
            { path: '/:catchAll(.*)*', redirect: '/admin/users/news' },
        ],
    });
    const mountWrapper = (i18n: I18n<any>, router: Router) => mount(UserAdminTab, {
        global: {
            plugins: [i18n, router],
        },
    });

    test('render user admin tab', () => {
        const wrapper = mountWrapper(i18n, router);
        expect(wrapper.find('table').exists()).toBe(true);
        expect(wrapper.find('table').attributes('aria-describedby')).toEqual('User List');
        expect(userList).toHaveBeenCalledWith(0);
    });

    test('should submit user form', async () => {
        const wrapper = mountWrapper(i18n, router);
    });
});