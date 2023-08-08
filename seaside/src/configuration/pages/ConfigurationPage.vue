<template>
  <nav class="tabs tabs-boxed">
    <router-link to="/config/feeds" class="tab" active-class="tab-active">Feeds</router-link>
    <router-link to="/config/profile" class="tab" active-class="tab-active">Profile</router-link>
  </nav>
  <router-view></router-view>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import FeedsList from '@/configuration/components/feedslist/FeedsList.vue';
import { defineAsyncComponent } from 'vue';

const ProfileTab = defineAsyncComponent(() => import('@/configuration/components/profile/ProfileTab.vue'));

type Tab = 'FeedsList' | 'ProfileTab';

@Component({
  name: 'ConfigurationPage',
  components: {
    FeedsList, ProfileTab,
  },
})
export default class ConfigurationPage extends Vue {
  private activeTab: Tab = 'FeedsList';

  private onChangeTab(toBeActivate: Tab): void {
    this.activeTab = toBeActivate;
  }
}
</script>