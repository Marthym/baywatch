import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n, I18n } from 'vue-i18n';
import { of } from 'rxjs';
import { createRouter, createWebHashHistory, Router } from 'vue-router';
import UserEditor from '@/administration/component/usereditor/UserEditor.vue';
import { userCreate, userGet } from '@/security/services/UserService';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { User } from '@/security/model/User';

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

vi.mock('@/security/services/PasswordService', () => ({
    passwordGenerate: vi.fn(),
    passwordAnonymousCheckStrength: vi.fn(),
}));

describe('UserEditor.vue', () => {
    const i18n = createI18n({
        legacy: false,
        missingWarn: false,
        messages: {
            en: {
                'admin.users.editor.title.create': 'Create User',
                'admin.users.editor.title.update': 'Update User {login}',
                'admin.users.editor.button.generate': 'generate',
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

    test('in creation mode, must display creation form', async () => {
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

    test('should fail saving user', async () => {
        vi.mocked(userCreate).mockReturnValue(of({ login: 'neo' } as User));
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

        await wrapper.find('button._js_btn-save').trigger('click');
        expect(userCreate).not.toHaveBeenCalled();

        wrapper.setData({
            modelValue: {
                login: 'neo',
                name: 'Neo',
                mail: 'neo@matrix.io',
                password: 'Sup3r!Pass',
                passwordConfirm: 'Sup3r!Pass',
                roles: ['USER'],
            },
        });

        await wrapper.find('button._js_btn-save').trigger('click');

        expect(userCreate).toHaveBeenCalledWith({
            login: 'neo',
            name: 'Neo',
            mail: 'neo@matrix.io',
            password: 'Sup3r!Pass',
            roles: ['USER'],
        });
        const loginInput = wrapper.find('input[type="text"]').element as HTMLInputElement;
        expect(loginInput.disabled).toBe(false);
    });

    test('in edit mode, should load user data', async () => {
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

    test('onCancel should redirect to user list', async () => {
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

describe('UserEditor, password management', () => {
    const i18n = createI18n({
        legacy: false,
        missingWarn: false,
        messages: {
            en: {
                'admin.users.editor.title.create': 'Create User',
                'admin.users.editor.title.update': 'Update User {login}',
                'admin.users.editor.button.generate': 'generate',
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

    const mountWrapper = async (i18n: I18n<any>, router: Router) => {
        const vueWrapper = mount(UserEditor, {
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
        await vueWrapper.vm.$nextTick();
        await vueWrapper.vm.$nextTick();
        return vueWrapper;
    }

    beforeEach(async () => {
        vi.clearAllMocks();
        vi.mocked(passwordGenerate).mockReturnValue(of(['random-password']));
        vi.stubGlobal('crypto', {
            getRandomValues: (arr: Uint32Array) => {
                arr[0] = 0;
                return arr;
            },
        });
    });

    test('should generate password on click', async () => {
        await router.push('/admin/users/new');
        const wrapper = await mountWrapper(i18n, router);

        const passwordInput = wrapper.find('input[type="password"]');
        const generateButton = wrapper.find('button.btn-soft');
        expect(generateButton.text()).toEqual('generate');
        await generateButton.trigger('click');

        expect(passwordGenerate).toHaveBeenCalledWith(20);
        expect(wrapper.find('h2').text()).toBe('Create User');
        expect(passwordInput.classes()).not.toContain('input-error');
        expect((passwordInput.element as HTMLInputElement).value).toEqual('random-password');
    });

    test('must display error when password is weak and remove it when it is correct', async () => {
        await router.push('/admin/users/new');
        const wrapper = await mountWrapper(i18n, router);

        const weakResponse = of({ isSecure: false, message: 'Too weak', entropy: 1 });
        const okResponse = of({ isSecure: true, message: '', entropy: 10 });
        vi.mocked(passwordAnonymousCheckStrength)
            .mockReturnValueOnce(weakResponse)
            .mockReturnValueOnce(okResponse);


        const loginInput = await wrapper.find('input[type="text"]');
        const passwordInput = wrapper.find('input[type="password"].join-item');

        await loginInput.setValue('neo');
        await passwordInput.setValue('12345678');
        await passwordInput.trigger('blur');

        expect(passwordAnonymousCheckStrength).toHaveBeenCalled();
        expect(wrapper.find('p.text-error').text()).toEqual('Too weak');

        await passwordInput.setValue('Correct#12345');
        await passwordInput.trigger('blur');

        expect(wrapper.find('p.text-error').exists()).toBe(false);
    });

    test('should blur confirm and display error', async () => {
        await router.push('/admin/users/new');
        const wrapper = await mountWrapper(i18n, router);

        const passwordInput = wrapper.find('input[type="password"]');
        const confirmInput = wrapper.find('input._js_input-confirm[type="password"]');

        await passwordInput.setValue('12345678');
        await confirmInput.setValue('1234567');
        await confirmInput.trigger('blur');

        expect(passwordInput.classes()).not.toContain('input-error');
        expect(confirmInput.classes()).toContain('input-error');

        await confirmInput.setValue('12345678');
        expect(passwordInput.classes()).not.toContain('input-error');
        expect(confirmInput.classes()).not.toContain('input-error');
    });
});
