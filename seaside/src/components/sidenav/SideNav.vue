<template>
  <nav class="flex flex-col bg-gray-200 dark:bg-gray-900 md:w-64 px-10 pt-4 pb-6">

    <SideNavHeader :unread="baywatchStats.unread"/>

    <SideNavUserInfo :user="user"/>
    <SideNavStatistics :statistics="baywatchStats" :isLoggedIn="isLoggedIn"/>

    <SideNavTags v-if="isLoggedIn"/>
    <SideNavFeeds v-if="isLoggedIn"/>

    <SideNavImportantActions :isLoggedIn="isLoggedIn" @logout="logoutUser()"/>
  </nav>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavFeeds from './SideNavFeeds.vue';
import {Statistics} from "@/services/model/Statistics";
import SideNavStatistics from "@/components/sidenav/SideNavStatistics.vue";

import userService from "@/services/UserService";
import statsService from "@/services/StatsService";
import {User} from "@/services/model/User";

const SideNavTags = () => import('./SideNavTags.vue').then(m => m.default);
const SideNavUserInfo = () => import('./SideNavUserInfo.vue').then(m => m.default);

@Component({
  components: {
    SideNavStatistics,
    SideNavHeader,
    SideNavUserInfo,
    SideNavTags,
    SideNavFeeds,
    SideNavImportantActions,
  },
})
export default class SideNav extends Vue {
  private baywatchStats: Statistics = {
    users: 0,
    feeds: 0,
    news: 0,
    unread: 0
  };

  private user: User | null = null;

  get isLoggedIn(): boolean {
    return !!this.user;
  }

  mounted(): void {
    userService.get().subscribe({
      next: user => this.$nextTick(() => this.user = user),
      error: () => {
        this.$nextTick(() => this.user = null)
        this.upgradeStatistics();
      }
    });

    userService.listenUser(u => {
      this.user = u;
      this.upgradeStatistics();
    });
  }

  logoutUser(): void {
    userService.logout()
        .subscribe(() => this.$router.go(0));
  }

  private upgradeStatistics(): void {
    statsService.getBaywatchStats()
        .subscribe({
          next: stats => this.baywatchStats = stats,
          error: e => console.log(e)
        });
  }
}
</script>
