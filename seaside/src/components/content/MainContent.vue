<template>
  <main
      class="flex-1 flex flex-col bg-gray-100 dark:bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-10 py-2">
    <ContentTopNav/>
    <div class="xl:w-2/3">
      <ContentHeader :users="statistics.users" :feeds="statistics.feeds" :news="statistics.news"/>

      <template v-for="(card, idx) in news">
        <NewsCard :ref="card.data.id" :card="card" v-bind:key="card.data.id" @activate="activateNewsCard(idx)"/>
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
import UserService from "@/services/UserService";

@Component({
  components: {
    NewsCard,
    NewsListHeader,
    ContentHeader,
    ContentTopNav,
  }
})
export default class MainContent extends Vue {
  @Prop() statistics?: Statistics;

  private newsService: NewsService = new NewsService(process.env.VUE_APP_API_BASE_URL);
  private userService: UserService = new UserService(process.env.VUE_APP_API_BASE_URL);
  private news: NewsView[] = new Array(0);

  private activeNews = -1;
  private page = 0;

  private observer = new IntersectionObserver((entries) => {
    if (!entries[0].isIntersecting) {
      console.log("test");
      this.activateNewsCard(++this.activeNews)
    }
  }, {threshold: [1], rootMargin: "-50px 0px 0px 0px"});

  private subscriptions?: Subscription;

  getId(n: NewsView) {
    return n.data.id;
  }

  created(): void {
    this.loadNextNewsPage();
  }

  mounted(): void {
    window.addEventListener('keydown', this.onKeyDownListener, false);
  }

  loadNextNewsPage(): void {
    this.subscriptions = this.newsService.getNews(++this.page).pipe(
        map(ns => ns.map(n => ({data: n, isActive: false, keepMark: false}) as NewsView))
    ).subscribe(
        ns => {
          this.news.push(...ns);
          if (this.activeNews < 0) {
            this.$nextTick(() => this.observer.observe(this.$refs[this.news[0].data.id][0].$el));
          }
        },
        e => console.log(e)
    );
  }

  onKeyDownListener(event: KeyboardEvent): void {
    if (event.altKey) {
      return;
    }
    if (!event.altKey) {
      if (event.key === "n") {
        event.preventDefault();
        this.activateNewsCard(this.activeNews + 1);
        this.scrollToActivateNews();

      } else if (event.key === "k") {
        event.preventDefault();
        this.activateNewsCard(this.activeNews - 1);
        this.scrollToActivateNews();

      } else if (event.key === "m") {
        event.preventDefault();
        this.toggleRead(this.activeNews);
      }
    }
  }

  activateNewsCard(_idx: number): void {
    this.observer.disconnect();
    if (this.activeNews >= 0 && this.activeNews < this.news.length) {
      // Manage previous news
      this.news[this.activeNews].isActive = false;
      if (!this.news[this.activeNews].keepMark) {
        this.markNewsRead(this.activeNews, true);
      }
    }

    const idx = Math.max(-1, Math.min(_idx, this.news.length));
    this.activeNews = idx;
    if (idx >= this.news.length || idx < 0) {
      // Stop if last news
      return;
    }

    this.news[this.activeNews].isActive = true;
    this.observer.observe(this.$refs[this.news[this.activeNews].data.id][0].$el)
  }

  toggleRead(idx: number) {
    this.markNewsRead(idx, !this.news[idx].data.read);
    this.news[idx].keepMark = true;
  }

  private markNewsRead(idx: number, mark: boolean) {
    if (!this.userService.get()) {
      return;
    }
    const target = this.news[idx];
    if (target.data.read === mark) {
      return;
    }

    const markObs = (mark)
        ? this.newsService.mark(target.data.id, 'read')
        : this.newsService.unmark(target.data.id, 'read');

    markObs.subscribe(news => {
      this.$set(this.news, idx, {...target, data: news});
    });
  }

  private scrollToActivateNews() {
    if (this.activeNews >= this.news.length - 1 || this.activeNews < 0) {
      // Stop if last news
      return;
    }
    const current = this.news[this.activeNews];
    this.$nextTick(() => {
      const ref: Vue[] | undefined = this.$refs[current.data.id] as Vue[] | undefined;
      if (ref === undefined) {
        return;
      }
      (ref as Vue[])[0].$el.scrollIntoView(
          {block: 'center', scrollBehavior: 'smooth'} as ScrollIntoViewOptions);
    });
  }

  handleScroll(): void {
    if (this.activeNews < 0) {
      this.activateNewsCard(0);
    }
  }

  beforeDestroy(): void {
    window.removeEventListener('keydown', this.onKeyDownListener, false);
    this.subscriptions?.unsubscribe();
  }
}
</script>
