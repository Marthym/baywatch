import { describe, expect, test, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { of } from 'rxjs';
import SideNav from '@/layout/components/sidenav/SideNav.vue';
import { router } from '@/router';
import { i18n } from '@/i18n';
import { store } from '@/store';
import { UPDATE_MUTATION } from '@/security/store/UserConstants';
import { User } from '@/security/model/User';

// avoid store update console logs
vi.hoisted(() => process.env.NODE_ENV = 'production');

vi.mock('@/techwatch/services/TagsService', () => {
    return {
        tagsListAll: vi.fn().mockImplementation(() => of(['jedi', 'sith'])),
    };
});
vi.mock('@/security/services/UserSettingsService', () => {
    return {
        userSettingsGet: vi.fn().mockImplementation(() => of({})),
    };
});

describe('SideNav', () => {
    test('renders with unauthenticated', () => {
        const wrapper = mount(SideNav, {
            global: {
                plugins: [i18n, router, store],
            },
        });
        expect(wrapper.find('input').exists()).toBe(false);
    });

    test('renders with authenticated', async () => {
        store.commit(UPDATE_MUTATION, {
            _id: '42',
            roles: ['USER'],
            mail: 'okenobi@jedi.com',
        } as User);
        const wrapper = mount(SideNav, {
            global: {
                plugins: [i18n, router, store],
            },
        });

        await flushPromises();

        expect(wrapper.findAll('button.badge').map(b => b.text()))
            .toEqual(['jedi', 'sith']);
    });
});