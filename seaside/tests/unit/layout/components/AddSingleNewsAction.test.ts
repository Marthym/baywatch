import { describe, expect, test, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { of } from 'rxjs';
import AddSingleNewsAction from '@/layout/components/AddSingleNewsAction.vue';

// avoid store update console logs
vi.hoisted(() => process.env.NODE_ENV = 'production');

vi.mock('@/security/services/UserSettingsService', () => {
    return {
        userSettingsGet: vi.fn().mockImplementation(() => of({})),
    };
});

describe('AddSingleNewsAction', () => {
    test('renders component', () => {
        const wrapper = mount(AddSingleNewsAction, {});
        expect(wrapper.find('input').exists()).toBe(true);
        expect(wrapper.find('button').exists()).toBe(true);
    });
});