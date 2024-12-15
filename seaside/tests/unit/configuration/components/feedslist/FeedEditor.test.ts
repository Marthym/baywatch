import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import FeedEditor from '@/configuration/components/feedslist/FeedEditor.vue';
import { of } from 'rxjs';

vi.mock('@/techwatch/services/TagsService', () => {
    return {
        tagsListAll: vi.fn().mockImplementation(() => of(['jedi', 'sith'])),
    };
});

describe('FeedEditor', () => {
    test('render feed editor', async () => {
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });

        const wrapper = mount(FeedEditor, {
            props: {
                modelValue: {
                    data: { name: 'Rogue One' },
                },
            },
            data() {
                return {
                    isOpened: true,
                };
            },
            global: {
                plugins: [i18n],
            },
        });

        expect(wrapper.find('button').exists()).toBe(true);
    });
});