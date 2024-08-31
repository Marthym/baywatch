import { describe, expect, test } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import FileUploadWindow from '@/common/components/FileUploadWindow.vue';

describe('FileUploadWindow', () => {
    test('render file upload window', async () => {
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });

        const wrapper = mount(FileUploadWindow, {
            global: {
                plugins: [i18n],
            },
        });

        expect(wrapper.find('button').exists()).toBe(true);
    });
});