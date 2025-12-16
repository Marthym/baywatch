import { beforeEach, describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import NewsList from '@/techwatch/components/newslist/NewsList.vue';
import NewsCard from '@/techwatch/components/newslist/NewsCard.vue';
import { i18n } from '@/i18n';

describe('NewsList', () => {
    beforeEach(() => {
        // IntersectionObserver isn't available in test environment
        const IntersectionObserverMock = vi.fn(function (
            this: IntersectionObserver,
            _callback: IntersectionObserverCallback,
            _options?: IntersectionObserverInit,
        ) {
            this.observe = vi.fn();
            this.unobserve = vi.fn();
            this.disconnect = vi.fn();
            this.takeRecords = vi.fn(() => []);
        });

        Object.defineProperty(globalThis, 'IntersectionObserver', {
            value: IntersectionObserverMock,
            writable: true,
            configurable: true,
        });
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