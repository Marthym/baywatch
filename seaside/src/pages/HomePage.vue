<template>
  <div class="md:h-screen flex md:flex-row flex-col overflow-hidden">
    <SideNav :statistics="baywatchStats"/>
    <MainContent/>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {Subscription} from "rxjs";
import StatsService from "@/services/StatsService";
import {Statistics} from "@/services/model/Statistics";

const SideNav = () => import('../components/sidenav/SideNav.vue');
const MainContent = () => import('../components/content/MainContent.vue');

@Component({
  components: {
    SideNav,
    MainContent,
  },
})
export default class Home extends Vue {
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