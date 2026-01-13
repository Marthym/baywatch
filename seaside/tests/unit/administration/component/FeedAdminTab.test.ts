import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import FeedsAdminTab from '@/administration/component/FeedsAdminTab.vue';
import { adminFeedFind } from '@/administration/services/FeedAdministrationService';
import { createRouter, createWebHashHistory } from 'vue-router';
import AdminFeedEditor from '@/administration/component/usereditor/AdminFeedEditor.vue';

vi.mock('@/administration/services/FeedAdministrationService', () => {
    return {
        adminFeedFind: vi.fn().mockImplementation(() => of({ data: [] })),
    };
});

describe('FeedsAdminTab', () => {
    test('render feeds admin tab', () => {

        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: {
                'en': {
                    'admin.feeds.tab.feeds.list': 'Liste des feeds',
                },
            },
        });

        const router = createRouter({
            history: createWebHashHistory(),
            routes: [
                {
                    path: '/feeds', component: FeedsAdminTab, name: 'admin-feeds', children: [
                        { path: ':id', component: AdminFeedEditor, name: 'admin-feed-editor' },
                    ],
                },
                { path: '/:catchAll(.*)*', redirect: '/feeds' },
            ],
        });

        const wrapper = mount(FeedsAdminTab, {
            global: {
                plugins: [i18n, router],
            },
        });
        expect(wrapper.find('ul').exists()).toBe(true);
        expect(wrapper.find('div').attributes('class')).toEqual('grow');
        expect(wrapper.find('div').text()).toEqual('Liste des feeds');
        expect(adminFeedFind).toHaveBeenCalledWith({ _p: 0, _pp: 20 });
    });
});