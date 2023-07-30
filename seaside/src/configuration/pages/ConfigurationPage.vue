<template>
  <nav class="tabs tabs-boxed">
    <a class="tab" :class="{'tab-active': 'FeedsList' === activeTab}"
       @click.prevent="onChangeTab('FeedsList')">Feeds</a>
    <a class="tab" :class="{'tab-active': 'ProfileTab' === activeTab}"
       @click.prevent="onChangeTab('ProfileTab')">Profile</a>
  </nav>
  <component :is="activeTab"></component>
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