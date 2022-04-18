<template>
  <nav class="tabs tabs-boxed">
    <a class="tab" :class="{'tab-active': 'UserAdminTab' === activeTab}"
       @click.prevent="onChangeTab('UserAdminTab')">Users</a>
    <a class="tab" :class="{'tab-active': 'ConfigAdminTab' === activeTab}"
       @click.prevent="onChangeTab('ConfigAdminTab')">Configuration</a>
    <a class="tab" :class="{'tab-active': 'StatisticsAdminTab' === activeTab}"
       @click.prevent="onChangeTab('StatisticsAdminTab')">Statistiques</a>
  </nav>
  <component :is="activeTab"></component>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';
import ConfigAdminTab from '@/administration/component/ConfigAdminTab.vue';
import {defineAsyncComponent} from "vue";

const StatisticsAdminTab = defineAsyncComponent(() => import('@/administration/component/StatisticsAdminTab.vue'));

type Tab = 'UserAdminTab' | 'ConfigAdminTab' | 'StatisticsAdminTab';

@Options({
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