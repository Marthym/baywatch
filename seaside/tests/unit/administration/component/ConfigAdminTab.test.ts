import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of, throwError } from 'rxjs';

import ConfigAdminTab from '@/administration/component/ConfigAdminTab.vue';
import {
    adminAppConfigurationMailSmtp,
    adminAppConfigurationMailSmtpUpdate,
} from '@/administration/services/AddConfigurationService';
import notificationService from '@/services/notification/NotificationService';

vi.mock('@/administration/services/AddConfigurationService', () => {
    return {
        adminAppConfigurationMailSmtp: vi.fn(),
        adminAppConfigurationMailSmtpUpdate: vi.fn(),
    };
});

vi.mock('@/services/notification/NotificationService', () => {
    return {
        default: {
            pushSimpleOk: vi.fn(),
            pushSimpleError: vi.fn(),
        },
    };
});

const i18n = createI18n({
    legacy: false,
    missingWarn: false,
    messages: {
        en: {
            'admin.config.mail.title': 'Config form',
            'dialog.cancel': 'cancel',
            'dialog.save': 'save',
            'admin.config.mail.messages.loadingError': 'loading error',
            'admin.config.mail.messages.updateSuccess': 'update ok',
            'admin.config.mail.messages.updateError': 'update error',
            'admin.config.mail.messages.formValidationError': 'form invalid',
        },
    },
});

const validConfig = () => ({
    host: 'smtp.example.test',
    port: 587,
    secure: false,
    username: 'user123',
    password: 'secret123',
    requireTls: true,
    from: 'noreply@example.test',
    ssl: { protocols: 'TLSv1.3', checkserveridentity: true },
    pollingIntervalSeconds: 10,
});

describe('ConfigAdminTab', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, 'error').mockImplementation(() => undefined);
        vi.spyOn(console, 'debug').mockImplementation(() => undefined);
    });

    test('rend le titre et charge la configuration au montage (et hydrate le formulaire)', async () => {
        vi.mocked(adminAppConfigurationMailSmtp).mockReturnValueOnce(of(validConfig()));

        const wrapper = mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });

        expect(wrapper.find('section > h2').text()).toBe('Config form');
        expect(adminAppConfigurationMailSmtp).toHaveBeenCalledTimes(1);

        await wrapper.vm.$nextTick();

        const fieldsets = wrapper.findAll('fieldset');
        const connection = fieldsets[0];
        const hostInput = connection.find('input[type="text"]');
        const portInput = connection.find('input[type="number"]');
        const secureToggle = connection.find('input[type="checkbox"]');

        expect((hostInput.element as HTMLInputElement).value).toBe('smtp.example.test');
        expect((portInput.element as HTMLInputElement).value).toBe('587');
        expect((secureToggle.element as HTMLInputElement).checked).toBe(false);
    });

    test('si le chargement échoue, affiche une notification d’erreur', async () => {
        vi.mocked(adminAppConfigurationMailSmtp).mockReturnValueOnce(
            throwError(() => new Error('boom')),
        );

        mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });

        expect(adminAppConfigurationMailSmtp).toHaveBeenCalledTimes(1);
        expect(notificationService.pushSimpleError).toHaveBeenCalledWith('loading error');
        expect(console.error).toHaveBeenCalled(); // log de error.message dans le composant
    });

    test('bouton cancel : recharge la config serveur (2e appel) et écrase les modifications locales', async () => {
        vi.mocked(adminAppConfigurationMailSmtp)
            .mockReturnValueOnce(of(validConfig()))
            .mockReturnValueOnce(of({ ...validConfig(), host: 'smtp.reset.test' }));

        const wrapper = mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });

        await wrapper.vm.$nextTick();

        const hostInput = wrapper.findAll('fieldset')[0].find('input[type="text"]');
        await hostInput.setValue('local-change.test');
        expect((hostInput.element as HTMLInputElement).value).toBe('local-change.test');

        const cancelBtn = wrapper.findAll('button').find((b) => b.text() === 'cancel');
        expect(cancelBtn).toBeTruthy();

        await cancelBtn!.trigger('click');
        await wrapper.vm.$nextTick();

        expect(adminAppConfigurationMailSmtp).toHaveBeenCalledTimes(2);
        expect((hostInput.element as HTMLInputElement).value).toBe('smtp.reset.test');
    });

    test('bouton save : config valide => appelle update + notifie OK + hydrate les valeurs retournées', async () => {
        vi.mocked(adminAppConfigurationMailSmtp).mockReturnValueOnce(of(validConfig()));
        vi.mocked(adminAppConfigurationMailSmtpUpdate).mockImplementationOnce((payload) =>
            of({ ...(payload as any), host: 'smtp.updated.test' }),
        );

        const wrapper = mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });
        await wrapper.vm.$nextTick();

        const saveBtn = wrapper.findAll('button').find((b) => b.text() === 'save');
        expect(saveBtn).toBeTruthy();

        await saveBtn!.trigger('click');
        await wrapper.vm.$nextTick();

        expect(adminAppConfigurationMailSmtpUpdate).toHaveBeenCalledTimes(1);
        expect(notificationService.pushSimpleOk).toHaveBeenCalledWith('update ok');

        const hostInput = wrapper.findAll('fieldset')[0].find('input[type="text"]');
        expect((hostInput.element as HTMLInputElement).value).toBe('smtp.updated.test');
    });

    test('should fail form validation and clear error', async () => {
        vi.mocked(adminAppConfigurationMailSmtp).mockReturnValueOnce(of(validConfig()));

        const wrapper = mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });
        await wrapper.vm.$nextTick();

        // Rend la config invalide (schéma: host string, username minLength(3), from email, pollingIntervalSeconds minValue(2), ssl.protocols nonEmpty etc.)
        const connection = wrapper.findAll('fieldset')[0];
        const portInput = connection.find('input[type="number"]');
        await portInput.setValue(-1); // invalide (port negatif => parse devrait échouer)

        const saveBtn = wrapper.findAll('button')
            .find((b) => b.text() === 'save');
        await saveBtn!.trigger('click');
        await wrapper.vm.$nextTick();

        expect(adminAppConfigurationMailSmtpUpdate).not.toHaveBeenCalled();
        expect(notificationService.pushSimpleError).toHaveBeenCalledWith('form invalid');

        // Si l’erreur "host" est remontée par valibot, la classe input-error doit apparaître
        expect(portInput.classes()).toContain('input-error');

        // clearError('host') est appelé sur @input: retaper quelque chose doit retirer l’erreur (donc la classe)
        await portInput.setValue('smtp.fixed.test');
        await wrapper.vm.$nextTick();

        expect(portInput.classes()).not.toContain('input-error');
    });

    test('bouton save : update en erreur => notifie erreur', async () => {
        vi.mocked(adminAppConfigurationMailSmtp).mockReturnValueOnce(of(validConfig()));
        vi.mocked(adminAppConfigurationMailSmtpUpdate).mockReturnValueOnce(
            throwError(() => new Error('update failed')),
        );

        const wrapper = mount(ConfigAdminTab, {
            global: { plugins: [i18n] },
        });
        await wrapper.vm.$nextTick();

        const saveBtn = wrapper.findAll('button').find((b) => b.text() === 'save');
        await saveBtn!.trigger('click');
        await wrapper.vm.$nextTick();

        expect(adminAppConfigurationMailSmtpUpdate).toHaveBeenCalledTimes(1);
        expect(notificationService.pushSimpleError).toHaveBeenCalledWith('update error');
        expect(console.error).toHaveBeenCalled(); // log err dans le composant
    });
});