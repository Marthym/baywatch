import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import { createRouter, createWebHashHistory } from 'vue-router';
import UserEditor from '@/administration/component/usereditor/UserEditor.vue';
import { userGet } from '@/security/services/UserService';

// Mock des services
vi.mock('@/security/services/UserService', () => ({
    userGet: vi.fn(),
    userCreate: vi.fn(),
    userUpdate: vi.fn(),
}));

vi.mock('@/services/notification/NotificationService', () => ({
    default: {
        pushSimpleOk: vi.fn(),
        pushSimpleError: vi.fn(),
    },
}));

describe('UserEditor.vue', () => {
    const i18n = createI18n({
        legacy: false,
        missingWarn: false,
        messages: {
            en: {
                'admin.users.editor.title.create': 'Create User',
                'admin.users.editor.title.update': 'Update User {login}',
                'admin.users.login': 'Login',
                'dialog.save': 'Save',
                'dialog.cancel': 'Cancel',
            },
        },
    });

    const router = createRouter({
        history: createWebHashHistory(),
        routes: [
            {
                path: `/admin/users/:userId`,
                name: 'admin-users-editor',
                component: UserEditor,
            },
            {
                path: '/admin/users',
                name: 'admin-users',
                component: { template: '<div>List</div>' },
            },
        ],
    });

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('doit afficher le mode création quand l\'id est "new"', async () => {
        await router.push('/admin/users/new');
        const wrapper = mount(UserEditor, {
            global: {
                plugins: [i18n, router],
                stubs: {
                    'curtain-modal': {
                        template: '<div><slot :close="() => {}" /></div>',
                    },
                    'EyeIcon': true,
                    'EyeSlashIcon': true,
                    'UserRoleInput': true,
                },
            },
        });

        // On attend que le mounted() et le nextTick() soient passés
        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('h2').text()).toBe('Create User');
        const loginInput = wrapper.find('input[type="text"]').element as HTMLInputElement;
        expect(loginInput.disabled).toBe(false);
    });

    test('doit charger les données utilisateur en mode édition', async () => {
        const mockUser = {
            login: 'jdoe',
            name: 'John Doe',
            mail: 'john@doe.com',
            roles: ['01AN4V0V2D8V1X9ZX3E326XSJK'],
        };

        (userGet as any).mockReturnValue(of(mockUser));

        await router.push('/admin/users/123');

        const wrapper = mount(UserEditor, {
            global: {
                plugins: [i18n, router],
                stubs: {
                    'curtain-modal': {
                        template: '<div><slot :close="() => {}" /></div>',
                    },
                    'UserRoleInput': true,
                },
            },
        });

        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick(); // Deuxième tick pour l'async du service

        expect(userGet).toHaveBeenCalledWith('123');
        expect(wrapper.find('h2').text()).toContain('jdoe');

        // Le login doit être désactivé en édition
        const loginInput = wrapper.find('input[type="text"]');
        expect((loginInput.element as HTMLInputElement).disabled).toBe(true);
    });

    test('onCancel doit rediriger vers la liste des utilisateurs', async () => {
        await router.push('/admin/users/123');

        const pushSpy = vi.spyOn(router, 'push');

        const wrapper = mount(UserEditor, {
            global: {
                plugins: [i18n, router],
                stubs: { 'curtain-modal': { template: '<div><slot /></div>' }, 'UserRoleInput': true },
            },
        });

        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick();
        
        await wrapper.find('div.card-actions button.btn:not(.btn-primary)').trigger('click');

        expect(pushSpy).toHaveBeenCalledWith({ name: 'admin-users' });
    });
});