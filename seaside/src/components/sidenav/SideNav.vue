<template>
  <aside class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-10 flex flex-col flex-shrink-0 w-64 max-h-screen overflow-hidden
  transition-all transform bg-gray-900 shadow-lg lg:z-auto lg:static lg:shadow-none"
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
import {Options, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavFeeds from './SideNavFeeds.vue';
import SideNavStatistics from "@/components/sidenav/SideNavStatistics.vue";

import userService, {UserListener} from "@/services/UserService";
import statsService from "@/services/StatisticsService";
import {User} from "@/services/model/User";
import {SidenavState} from "@/store/sidenav/sidenav";
import {StatisticsState} from "@/store/statistics/statistics";
import {StatisticsMutation} from "@/store/statistics/StatisticsMutation.enum";
import {useStore} from "vuex";
import {setup} from "vue-class-component";
import {defineAsyncComponent} from "vue";

const SideNavTags = defineAsyncComponent(() => import('./SideNavTags.vue').then(m => m.default))
const SideNavUserInfo = defineAsyncComponent(() => import('./SideNavUserInfo.vue').then(m => m.default));

@Options({
  name: 'SideNav',
  components: {
    SideNavStatistics,
    SideNavHeader,
    SideNavUserInfo,
    SideNavTags,
    SideNavFeeds,
    SideNavImportantActions,
  },
})
export default class SideNav extends Vue implements UserListener {
  private store = setup(() => useStore());
  private state: SidenavState = setup(() => useStore().state.sidenav);
  private baywatchStats: StatisticsState = setup(() => useStore().state.statistics);

  private user: User | null = null;

  setup() {
    const store = useStore();
    return {
      store: store,
    }
  }

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

    userService.registerUserListener(this);
  }

  onUserChange(u: User): void {
    this.user = u;
    this.updateStatistics();
  }

  private updateStatistics(): void {
    statsService.get().subscribe(s => this.store.commit(StatisticsMutation.UPDATE, s));
  }

  logoutUser(): void {
    userService.logout()
        .subscribe(() => this.$router.go(0));
  }

  unmounted(): void {
    userService.unregisterUserListener(this);
  }
}
</script>
