import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n, I18n } from 'vue-i18n';
import { createRouter, createWebHashHistory, Router } from 'vue-router';
import CreateAccountComponent from '@/security/components/CreateAccountComponent.vue';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { of, throwError } from 'rxjs';
import { userCreate } from '@/security/services/UserService';
import { User } from '@/security/model/User';
import notificationService from '@/services/notification/NotificationService';

vi.mock('@/security/services/PasswordService', () => ({
    passwordGenerate: vi.fn(),
    passwordAnonymousCheckStrength: vi.fn(),
}));

vi.mock('@/security/services/UserService', () => ({
    userCreate: vi.fn(),
}));

vi.mock('@/services/notification/NotificationService', () => ({
    default: {
        pushSimpleOk: vi.fn(),
        pushSimpleError: vi.fn(),
    },
}));

describe('CreateAccountComponent', () => {
    const i18n = createI18n({
        legacy: false,
        missingWarn: false,
        messages: {
            'en': {
                'security.register.dialog.register': 'Register',
                'security.register.title': 'Register Curtain Title',
                'security.register.generate': 'Generate',
                'security.register.message.save.successfully': 'Saved',
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

    const closeSpy = vi.fn();

    const mountWrapper = async (i18n: I18n<any>, router: Router, close: Function) => {
        const vueWrapper = mount(CreateAccountComponent, {
            global: {
                plugins: [i18n, router],
                stubs: {
                    'curtain-modal': { template: '<div><slot :close="close" /></div>', methods: { close: close } },
                    EyeIcon: true,
                    EyeSlashIcon: true,
                },
            },
        });
        await vueWrapper.vm.$nextTick();
        return vueWrapper;
    };

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(passwordGenerate).mockReturnValue(of(['random-password']));
        vi.stubGlobal('crypto', {
            getRandomValues: vi.fn(),
        });

        const weakResponse = of({ isSecure: false, message: 'Too weak', entropy: 1 });
        const okResponse = of({ isSecure: true, message: '', entropy: 10 });
        vi.mocked(passwordAnonymousCheckStrength)
            .mockReturnValueOnce(weakResponse)
            .mockReturnValueOnce(okResponse);

        vi.mocked(passwordAnonymousCheckStrength).mockReturnValue(of({ isSecure: true, message: '', entropy: 10 }));
        vi.mocked(userCreate).mockReturnValue(of({ login: 'neo' } as User));
    })

    test('render feeds register window', async () => {
        const wrapper = await mountWrapper(i18n, router, closeSpy);
        const element = await vi.waitFor(
            () => wrapper.find('h2').exists()
                ? Promise.resolve(wrapper.find('h2'))
                : Promise.reject(),
        );

        expect(element.text()).toEqual('Register Curtain Title');

        expect(wrapper.find('button.btn-primary').text()).toEqual('Register');
    });

    test('click generate button should generate password and clear error', async () => {
        const wrapper = await mountWrapper(i18n, router, closeSpy);

        await wrapper.find('input[type="text"]').setValue('neo');
        await wrapper.find('input[type="password"].join-item').trigger('focus');
        await wrapper.find('input[type="password"].join-item').trigger('blur');
        await wrapper.vm.$nextTick();

        const passwordInput = wrapper.find('input[type="password"].join-item');
        await passwordInput.setValue('');
        await passwordInput.trigger('blur');
        await wrapper.vm.$nextTick();

        expect(passwordInput.classes()).toContain('input-error');

        const generateButton = wrapper.find('button.btn-soft');
        expect(generateButton.text()).toEqual('Generate');
        await generateButton.trigger('click');

        expect(passwordGenerate).toHaveBeenCalledWith(20);
        expect(passwordInput.classes()).not.toContain('input-error');
        expect((passwordInput.element as HTMLInputElement).value).toEqual('random-password');
    });

    test('must display error when password is weak and remove it when it is correct', async () => {
        const wrapper = await mountWrapper(i18n, router, closeSpy);

        await wrapper.find('input[type="text"]').setValue('neo');
        const passwordInput = wrapper.find('input[type="password"].join-item');

        await passwordInput.setValue('12345678');
        await passwordInput.trigger('blur');

        expect(passwordAnonymousCheckStrength).toHaveBeenCalled();
        expect(wrapper.find('p.text-error').text()).toEqual('Too weak');

        await passwordInput.setValue('Correct#12345');
        await passwordInput.trigger('blur');

        expect(wrapper.find('p.text-error').exists()).toBe(false);
    });

    test('must create the account and close the modal on success', async () => {
        const wrapper = await mountWrapper(i18n, router, closeSpy);
        const saveButton = wrapper.find('button.btn-primary');

        await saveButton.trigger('click');

        expect(notificationService.pushSimpleError).toHaveBeenCalledWith('security.register.message.formValidationError');
        expect(wrapper.find('span.text-error').text()).toEqual('security.register.message.login.too.short');
        expect(closeSpy).not.toHaveBeenCalled();

        wrapper.setData({
            account: {
                login: 'neo',
                name: 'Neo',
                mail: 'neo@matrix.io',
                password: 'Sup3r!Pass',
                passwordConfirm: 'Sup3r!Pass',
            },
        });

        await saveButton.trigger('click');

        expect(userCreate).toHaveBeenCalledWith({
            login: 'neo',
            name: 'Neo',
            mail: 'neo@matrix.io',
            password: 'Sup3r!Pass',
            roles: [],
        });
        expect(notificationService.pushSimpleOk).toHaveBeenCalledWith('Saved');
        expect(closeSpy).toHaveBeenCalled();
    });

    test('must display API errors and not close the modal', async () => {

        vi.mocked(userCreate).mockReturnValue(
            throwError(() => ({ message: 'Already exists', properties: ['login'] })),
        );
        const wrapper = await mountWrapper(i18n, router, closeSpy);

        wrapper.setData({
            account: {
                login: 'neo',
                name: 'Neo',
                mail: 'neo@matrix.io',
                password: 'Sup3r!Pass',
                passwordConfirm: 'Sup3r!Pass',
            },
        });

        await wrapper.find('button.btn-primary').trigger('click');

        expect(notificationService.pushSimpleError).toHaveBeenCalledWith('Already exists');
        expect(wrapper.find('span.text-error').text()).toEqual('Already exists');
        expect(closeSpy).not.toHaveBeenCalled();
    });

    test('should compare password and confirm password', async () => {
        const wrapper = await mountWrapper(i18n, router, closeSpy);

        const passwordInput = wrapper.find('input._js_password-input');
        const confirmPasswordInput = wrapper.find('input._js_password-confirm-input');

        await passwordInput.setValue('Sup3r!Pass');
        await confirmPasswordInput.setValue('wrong-password');
        await confirmPasswordInput.trigger('blur');

        expect(passwordInput.classes()).not.toContain('input-error');
        expect(confirmPasswordInput.classes()).toContain('input-error');

        await confirmPasswordInput.setValue('Sup3r!Pass');
        await confirmPasswordInput.trigger('blur');

        expect(passwordInput.classes()).not.toContain('input-error');
        expect(confirmPasswordInput.classes()).not.toContain('input-error');
    })
});