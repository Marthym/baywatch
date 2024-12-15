<template>
  <div class="pb-2 border-b border-primary text-center text-primary-content text-lg font-semibold">
    <BaywatchIcon/>
    <span class="capitalize ml-4 text-primary">{{ t('main.application') }}</span>

    <button @click="toggleSidenav" class="btn btn-square btn-sm btn-ghost lg:hidden absolute left-0 top-0 m-2"
            aria-label="Close Side Menu">
      <XMarkIcon class="h-6 w-6"/>
    </button>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { SidenavMutation } from '@/store/sidenav/SidenavMutation.enum';
import { useStore } from 'vuex';
import { XMarkIcon } from '@heroicons/vue/20/solid';
import { useI18n } from 'vue-i18n';
import BaywatchIcon from '@/common/components/BaywatchIcon.vue';

@Component({
  name: 'SideNavHeader', components: {
    BaywatchIcon,
    XMarkIcon,
  },
  setup() {
    const { t } = useI18n();
    return {
      store: useStore(),
      t: t,
    };
  },
})
export default class SideNavHeader extends Vue {
  private store;
  private t;

  private toggleSidenav(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }
}
</script>