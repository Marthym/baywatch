import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import { teamsList } from '@/teams/services/Teams.service';
import TeamsPage from '@/teams/pages/TeamsPage.vue';

vi.mock('@/teams/services/Teams.service', () => {
    return {
        teamsList: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('TeamsPage', () => {
    test('render team page', () => {
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

        const wrapper = mount(TeamsPage, {
            global: {
                plugins: [i18n],
                provide: { store: $store },
            },
        });
        expect(wrapper.find('table').exists()).toBe(true);
        expect(wrapper.find('table').attributes('aria-describedby')).toEqual('smarttable.aria.usersList');
        expect(teamsList).toHaveBeenCalledWith(0);
    });
});