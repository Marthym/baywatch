<template>
  <!--    <div-->
  <!--        x-show.in.out.opacity="isSidebarOpen"-->
  <!--        class="fixed inset-0 z-10 bg-black bg-opacity-20 backdrop-blur-md lg:hidden"-->
  <!--    ></div>-->
  <aside
      class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-10 flex flex-col flex-shrink-0 w-64 max-h-screen overflow-hidden
  transition-all transform bg-gray-200 dark:bg-gray-900 shadow-lg lg:z-auto lg:static lg:shadow-none"
      :class="{'-translate-x-full lg:translate-x-0': !state.open}">
    <SideNavHeader :unread="baywatchStats.unread"/>

    <SideNavUserInfo :user="user"/>
    <SideNavStatistics :statistics="baywatchStats" :isLoggedIn="isLoggedIn"/>

    <SideNavTags v-if="isLoggedIn"/>
    <SideNavFeeds v-if="isLoggedIn"/>

    <SideNavImportantActions :isLoggedIn="isLoggedIn" @logout="logoutUser()"/>
  </aside>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavFeeds from './SideNavFeeds.vue';
import SideNavStatistics from "@/components/sidenav/SideNavStatistics.vue";

import userService from "@/services/UserService";
import statsService from "@/services/StatisticsService";
import {User} from "@/services/model/User";
import {SidenavState} from "@/store/sidenav/sidenav";
import {STATISTICS_MUTATION_UPDATE, StatisticsState} from "@/store/statistics/statistics";

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
  private state: SidenavState = this.$store.state.sidenav;

  private baywatchStats: StatisticsState = this.$store.state.statistics;

  private user: User | null = null;

  get isLoggedIn(): boolean {
    return !!this.user;
  }

  mounted(): void {
    this.updateStatistics();
    userService.get().subscribe({
      next: user => this.$nextTick(() => this.user = user),
      error: () => {
        this.$nextTick(() => this.user = null)
        this.updateStatistics();
      }
    });

    userService.listenUser(u => {
      this.user = u;
      this.updateStatistics();
    });
  }

  private updateStatistics(): void {
    statsService.get().subscribe(s => this.$store.commit(STATISTICS_MUTATION_UPDATE, s));
  }

  logoutUser(): void {
    userService.logout()
        .subscribe(() => this.$router.go(0));
  }
}
</script>
