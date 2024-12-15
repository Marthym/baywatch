import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { of } from 'rxjs';
import FeedsList from '@/configuration/components/feedslist/FeedsList.vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import LoginPage from '@/security/pages/LoginPage.vue';

vi.mock('@/configuration/services/FeedService', () => {
    return {
        default: {
            list: vi.fn().mockImplementation(() => of({
                totalPage: 1,
                currentPage: 1,
                data: of([]),
            })),
        },
    };
});

describe('FeedsList', () => {
    test('render feed list', async () => {
        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };
        const i18n = createI18n({
            legacy: false,
            missingWarn: false,
            messages: { 'en': {} },
        });
        const router = createRouter({
            history: createWebHashHistory(),
            routes: [
                { path: '/', component: LoginPage, name: 'test' },
            ],
        });
        const wrapper = mount(FeedsList, {
            global: {
                plugins: [i18n, router],
                provide: { store: $store },
            },
        });

        expect(wrapper.find('button').exists()).toBe(true);
    });
});