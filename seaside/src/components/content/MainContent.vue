<template>
  <main
      class="flex-1 flex flex-col bg-gray-100 dark:bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-10 py-2">
    <ContentTopNav/>
    <div class="xl:w-2/3">
      <ContentHeader :users="statistics.users" :feeds="statistics.feeds" :news="statistics.news"/>
      <template v-for="card in news">
        <NewsCard :card="card" v-bind:key="card.id"/>
      </template>
    </div>

  </main>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import ContentTopNav from "./ContentTopNav.vue";
import ContentHeader from "./ContentHeader.vue";
import NewsListHeader from "@/components/content/NewsListHeader.vue";
import NewsCard from "@/components/content/NewsCard.vue";
import NewsService from "@/services/NewsService";
import {Subscription} from "rxjs";
import {News} from "@/services/model/News";
import {Statistics} from "@/services/model/Statistics";

@Component({
  components: {
    NewsCard,
    NewsListHeader,
    ContentHeader,
    ContentTopNav
  }
})
export default class MainContent extends Vue {
  private newsService: NewsService = new NewsService(process.env.VUE_APP_API_BASE_URL);
  private news: Array<News> = [];
  @Prop() statistics?: Statistics;

  private subscriptions?: Subscription;

  created(): void {
    this.subscriptions = this.newsService.getNews()
        .subscribe(
            ns => this.news = ns,
            e => console.log(e)
        );
  }

  beforeDestroy(): void {
    this.subscriptions?.unsubscribe();
  }
}
</script>
