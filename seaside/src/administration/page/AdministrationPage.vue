<template>
  <nav role="navigation" class="tabs tabs-boxed place-content-start">
    <a role="tab" class="tab" :class="{'tab-active': 'UserAdminTab' === activeTab}"
       @click.prevent="onChangeTab('UserAdminTab')">Users</a>
    <a role="tab" class="tab" :class="{'tab-active': 'ConfigAdminTab' === activeTab}"
       @click.prevent="onChangeTab('ConfigAdminTab')">Configuration</a>
    <a role="tab" class="tab" :class="{'tab-active': 'StatisticsAdminTab' === activeTab}"
       @click.prevent="onChangeTab('StatisticsAdminTab')">Statistiques</a>
  </nav>
  <component :is="activeTab"></component>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';
import ConfigAdminTab from '@/administration/component/ConfigAdminTab.vue';
import { defineAsyncComponent } from 'vue';

const StatisticsAdminTab = defineAsyncComponent(() => import('@/administration/component/StatisticsAdminTab.vue'));

type Tab = 'UserAdminTab' | 'ConfigAdminTab' | 'StatisticsAdminTab';

@Component({
  name: 'AdministrationPage',
  components: {
    UserAdminTab,
    ConfigAdminTab,
    StatisticsAdminTab,
  },
})
export default class AdministrationPage extends Vue {
  private activeTab: Tab = 'UserAdminTab';

  private onChangeTab(toBeActivate: Tab): void {
    this.activeTab = toBeActivate;
  }
}
</script>