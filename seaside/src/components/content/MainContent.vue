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
import ScrollingActivationBehaviour from "@/services/ScrollingActivationBehaviour";
import ScrollingActivation from "@/services/model/ScrollingActivation";

@Component({
  components: {
    NewsCard,
    NewsListHeader,
    ContentHeader,
    ContentTopNav,
  }
})
export default class MainContent extends Vue implements ScrollingActivation {
  @Prop() statistics?: Statistics;

  private readonly activateOnScroll = ScrollingActivationBehaviour.apply(this);
  private newsService: NewsService = new NewsService(process.env.VUE_APP_API_BASE_URL);
  private userService: UserService = new UserService(process.env.VUE_APP_API_BASE_URL);
  private news: NewsView[] = new Array(0);

  private activeNews = -1;
  private page = 0;

  private subscriptions?: Subscription;

  mounted(): void {
    this.loadNextNewsPage();
    window.addEventListener('keydown', this.onKeyDownListener, false);
  }

  loadNextNewsPage(): void {
    this.subscriptions = this.newsService.getNews(++this.page).pipe(
        map(ns => ns.map(n => ({data: n, isActive: false, keepMark: false}) as NewsView))
    ).subscribe(
        ns => {
          this.news.push(...ns);
          if (this.activeNews < 0) {
            this.$nextTick(() => this.activateOnScroll.observe(this.getRefElement(this.news[0].data.id)));
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

  activateElement(incr: number): Element {
    this.activateNewsCard(this.activeNews + incr)
    return this.getRefElement(this.news[this.activeNews].data.id);
  }

  activateNewsCard(_idx: number): void {
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
    this.activateOnScroll.observe(this.getRefElement(this.news[this.activeNews].data.id))
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
      this.getRefElement(current.data.id).scrollIntoView(
          {block: 'center', scrollBehavior: 'smooth'} as ScrollIntoViewOptions);
    });
  }

  getRefElement(ref: string): Element {
    const vueRef: Vue[] | undefined = this.$refs[ref] as Vue[] | undefined;
    if (vueRef === undefined) {
      throw new Error(`Element with ref ${ref} not found !`);
    }
    return (vueRef as Vue[])[0].$el
  }

  beforeDestroy(): void {
    console.log("beforeDestroy MainContent");
    window.removeEventListener('keydown', this.onKeyDownListener, false);
    this.subscriptions?.unsubscribe();
  }
}
</script>
