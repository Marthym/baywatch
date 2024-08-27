import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { of } from 'rxjs';
import TopNavigationBar from '@/layout/components/TopNavigationBar.vue';
import { router } from '@/router';
import { i18n } from '@/i18n';
import { store } from '@/store';
import { UPDATE_MUTATION } from '@/security/store/UserConstants';
import { User } from '@/security/model/User';

// avoid store update console logs
vi.hoisted(() => process.env.NODE_ENV = 'production');

vi.mock('@/security/services/UserSettingsService', () => {
    return {
        userSettingsGet: vi.fn().mockImplementation(() => of({})),
    };
});

describe('TopNavigationBar', () => {
    test('renders with unauthenticated', () => {
        const wrapper = mount(TopNavigationBar, {
            global: {
                plugins: [i18n, router, store],
            },
        });
        expect(wrapper.find('input').exists()).toBe(false);
    });

    test('renders with authenticated', () => {
        store.commit(UPDATE_MUTATION, {
            _id: '42',
            roles: ['USER'],
        } as User);
        const wrapper = mount(TopNavigationBar, {
            global: {
                plugins: [i18n, router, store],
            },
        });
        expect(wrapper.find('input').exists()).toBe(true);
    });
});