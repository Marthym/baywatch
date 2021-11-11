<template>
  <!--    <div-->
  <!--        x-show.in.out.opacity="isSidebarOpen"-->
  <!--        class="fixed inset-0 z-10 bg-black bg-opacity-20 backdrop-blur-md lg:hidden"-->
  <!--    ></div>-->
  <aside
      class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-10 flex flex-col flex-shrink-0 w-64 max-h-screen overflow-hidden
  transition-all transform bg-gray-200 dark:bg-gray-900 shadow-lg lg:z-auto lg:static lg:shadow-none"
      :class="{'-translate-x-full lg:translate-x-0': !open}">
    <SideNavHeader :unread="baywatchStats.unread" @toggleSidenav="$emit('toggleSidenav')"/>

    <SideNavUserInfo :user="user"/>
    <SideNavStatistics :statistics="baywatchStats" :isLoggedIn="isLoggedIn"/>

    <SideNavTags v-if="isLoggedIn"/>
    <SideNavFeeds v-if="isLoggedIn"/>

    <SideNavImportantActions :isLoggedIn="isLoggedIn" @logout="logoutUser()"/>
  </aside>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
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
  @Prop() open!: boolean;

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
    this.baywatchStats = statsService.getBaywatchStats();
    userService.get().subscribe({
      next: user => this.$nextTick(() => this.user = user),
      error: () => {
        this.$nextTick(() => this.user = null)
        statsService.update();
      }
    });

    userService.listenUser(u => {
      this.user = u;
      statsService.update();
    });
  }

  logoutUser(): void {
    userService.logout()
        .subscribe(() => this.$router.go(0));
  }
}
</script>
