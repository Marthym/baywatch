import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import TeamEditor from '@/teams/components/TeamEditor.vue';
import CurtainModal from '@/common/components/CurtainModal.vue';

describe('TeamsPage', () => {
    test('render team page', async () => {
        const $store = {
            state: { user: { user: { _id: '42' } } },
            getters: {
                'user/hasRoleUser': true,
            },
        };
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });

        const wrapper = mount(TeamEditor, {
            props: {
                modelValue: {
                    data: { name: 'Rogue One' },
                },
            },
            global: {
                plugins: [i18n],
                provide: { store: $store },
            },
        });

        const curtainWrapper = wrapper.findComponent(CurtainModal);
        const element = await vi.waitFor(
            () => curtainWrapper.find('h2').exists()
                ? Promise.resolve(curtainWrapper.find('h2'))
                : Promise.reject(),
        );

        expect(element.text()).toEqual('Edit Team');
        expect(wrapper.findAll('button').length).toEqual(1);

        await wrapper.setProps({
            modelValue: {
                isEditable: true,
                data: { name: 'Rogue One' },
            },
        });
        expect(wrapper.findAll('button').length).toEqual(2);
    });
});