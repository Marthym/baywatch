import { describe, expect, test } from 'vitest';
import { mount } from '@vue/test-utils';
import ChangePasswordModal from '@/configuration/components/profile/ChangePasswordModal.vue';
import { createI18n } from 'vue-i18n';
import ModalWindow from '@/common/components/ModalWindow.vue';

describe('ChangePasswordModal', () => {
    test('renders a div', async () => {
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });
        const wrapper = mount(ChangePasswordModal, { global: { plugins: [i18n] } });
        const modalWrapper = wrapper.findComponent(ModalWindow);
        expect(modalWrapper.exists()).toBe(true);
        expect(modalWrapper.isVisible()).toBe(false);

        await wrapper.setProps({ isOpen: true });
        expect(modalWrapper.isVisible()).toBe(false);
        expect(modalWrapper.find('button').exists()).toBe(true);
    });
});