<template>
  <main class="flex-1 flex flex-col bg-gray-100 dark:bg-gray-700 transition duration-500 ease-in-out overflow-y-auto">
    <div class="mx-10 my-2">
      <ContentTopNav/>
      <ContentHeader/>
<!--      <NewsListHeader/>-->

      <template v-for="card in news">
        <NewsCard :card="card" v-bind:key="card.id"/>
      </template>
      <!--      <NewsCard/>-->
      <!--      <NewsCard/>-->
      <!--      <NewsCard/>-->
      <!--      <NewsCard/>-->

    </div>

  </main>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import ContentTopNav from "./ContentTopNav.vue";
import ContentHeader from "./ContentHeader.vue";
import NewsListHeader from "@/components/content/NewsListHeader.vue";
import NewsCard from "@/components/content/NewsCard.vue";
import NewsService from "@/services/NewsService";
import {Subscription} from "rxjs";

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

  private subscriptions?: Subscription;

  mounted(): void {
    console.log("mounted: ", process.env.VUE_APP_API_BASE_URL);
    this.subscriptions = this.newsService.getNews()
        .subscribe(
            ns => this.news = ns,
            () => console.log("complete")
        );
  }

  beforeDestroy(): void {
    this.subscriptions?.unsubscribe();
  }
}
</script>
