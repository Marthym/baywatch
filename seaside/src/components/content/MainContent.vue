<template>
  <main
      class="flex-1 flex flex-col bg-gray-100 dark:bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-10 py-2">
    <ContentTopNav/>
    <div class="xl:w-2/3">
      <ContentHeader :users="statistics.users" :feeds="statistics.feeds" :news="statistics.news"/>
      <template v-for="card in news">
        <NewsCard :ref="card.data.id" :card="card" v-bind:key="card.data.id"/>
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
import {Statistics} from "@/services/model/Statistics";
import {map} from "rxjs/operators";
import {NewsView} from "@/components/content/model/NewsView";

@Component({
  components: {
    NewsCard,
    NewsListHeader,
    ContentHeader,
    ContentTopNav
  }
})
export default class MainContent extends Vue {
  @Prop() statistics?: Statistics;

  private newsService: NewsService = new NewsService(process.env.VUE_APP_API_BASE_URL);
  private news: Array<NewsView> = [];
  private activeNews = -1;


  private subscriptions?: Subscription;

  created(): void {
    this.subscriptions = this.newsService.getNews().pipe(
        map(ns => ns.map(n => ({data: n, isActive: false, isRead: false}) as NewsView))
    ).subscribe(
        ns => this.news = ns,
        e => console.log(e)
    );
  }

  mounted(): void {
    window.addEventListener('keydown', event => {
      if (event.altKey) {
        return;
      }
      if (event.key === "n" && !event.altKey) {
        event.preventDefault();
        this.nextNews();
      }
    })
  }

  nextNews(): void {
    if (this.activeNews >= 0) {
      // Manage previous news
      this.news[this.activeNews].isActive = false;
      this.news[this.activeNews].isRead = true;
    }
    if (this.activeNews >= this.news.length - 1) {
      // Stop if last news
      return;
    }

    this.activeNews += 1;
    const current = this.news[this.activeNews];
    current.isActive = true;
    this.$nextTick(() => {
      (this.$refs[current.data.id] as Vue[])[0].$el.scrollIntoView(
          {block: 'center', scrollBehavior: 'smooth'} as ScrollIntoViewOptions);
    });
  }

  beforeDestroy(): void {
    this.subscriptions?.unsubscribe();
  }
}
</script>
