import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { createRouter, createWebHashHistory } from 'vue-router';
import LoginPage from '@/security/pages/LoginPage.vue';
import NewsList from '@/techwatch/components/newslist/NewsList.vue';
import NewsCard from '@/techwatch/components/newslist/NewsCard.vue';
import { i18n } from '@/i18n';

describe('NewsList', () => {
    beforeEach(() => {
        // IntersectionObserver isn't available in test environment
        const mockIntersectionObserver = vi.fn();
        mockIntersectionObserver.mockReturnValue({
            observe: () => null,
            unobserve: () => null,
            disconnect: () => null,
        });
        window.IntersectionObserver = mockIntersectionObserver;
    });

    test('render news list', async () => {
        const $store = {
            state: { user: { user: { _id: '42' } } },
            commit: vi.fn(),
        };

        const wrapper = mount(NewsList, {
            global: {
                plugins: [i18n],
                provide: { store: $store },
            },
            data() {
                return {
                    news: [
                        { data: { id: '42', publication: '2024-08-30T22:42:00' } },
                        { data: { id: '43', publication: '2024-08-30T22:43:00' } },
                    ],
                };
            },
        });

        expect(wrapper.findAllComponents(NewsCard).length).toEqual(2);
    });
});