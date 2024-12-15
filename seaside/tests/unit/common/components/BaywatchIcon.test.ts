import { describe, expect, test } from 'vitest';
import { mount } from '@vue/test-utils';
import BaywatchIcon from '@/common/components/BaywatchIcon.vue';

describe('BaywatchIcon', () => {
    test('render Baywatch Icon', async () => {
        const wrapper = mount(BaywatchIcon);

        expect(wrapper.find('svg').exists()).toBe(true);
    });
});