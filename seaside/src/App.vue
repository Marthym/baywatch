<template>
  <div class="md:h-screen flex md:flex-row flex-col overflow-hidden">
    <SideNav :statistics="baywatchStats"/>
    <main class="flex-1 flex flex-col
      bg-gray-100 dark:bg-gray-700
      transition duration-500 ease-in-out
      overflow-y-auto px-10 py-2">
      <ContentTopNav/>
      <router-view></router-view>
    </main>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import router from "./router";
import StatsService from "@/services/StatsService";
import {Statistics} from "@/services/model/Statistics";
import {Subscription} from "rxjs";
import ContentTopNav from "@/components/topnav/ContentTopNav.vue";

const SideNav = () => import('@/components/sidenav/SideNav.vue');

@Component({
  components: {
    ContentTopNav,
    SideNav,
  },
  router,
})
export default class App extends Vue {
  private statsService: StatsService = new StatsService(process.env.VUE_APP_API_BASE_URL);
  private baywatchStats: Statistics = {
    users: 0,
    feeds: 0,
    news: 0,
    unread: 0
  };

  private subscriptions?: Subscription;

  created(): void {
    this.subscriptions = this.statsService.getBaywatchStats()
        .subscribe(
            stats => this.baywatchStats = stats,
            e => console.log(e)
        );
  }

  beforeDestroy(): void {
    this.subscriptions?.unsubscribe();
  }
}
</script>

<style>

</style>
