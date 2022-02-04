<template>
  <nav class="tabs tabs-boxed">
    <a class="tab" :class="{'tab-active': 'UserAdminTab' === activeTab}"
       @click.prevent="onChangeTab('UserAdminTab')">Users</a>
    <a class="tab" :class="{'tab-active': 'ConfigAdminTab' === activeTab}"
       @click.prevent="onChangeTab('ConfigAdminTab')">Configuration</a>
  </nav>
  <component :is="activeTab"></component>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import UserAdminTab from '@/administration/component/UserAdminTab.vue';

type Tab = 'UserAdminTab' | 'ConfigAdminTab';

@Options({
  name: 'AdministrationPage',
  components: {
    UserAdminTab,
  },
})
export default class AdministrationPage extends Vue {
  private activeTab: Tab = 'UserAdminTab';

  private onChangeTab(toBeActivate: Tab): void {
    this.activeTab = toBeActivate;
  }
}
</script>