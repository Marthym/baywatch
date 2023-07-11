<template>
  <div class="max-w-5xl">
    <template v-for="(card, idx) in news" :key="card.data.id">
      <NewsCard :ref="card.data.id" :card="card" @activate="activateNewsCard(idx)" @addFilter="onAddFilter">
        <template #actions v-if="userStore.isAuthenticated">
          <div class="join -ml-2 lg:ml-0">
            <button v-if="card.data.state.read" @click.stop="toggleRead(idx)"
                    class="btn btn-xs btn-ghost join-item">
              <EnvelopeOpenIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">unread</span>
            </button>
            <button v-else @click.stop="toggleRead(idx)" class="btn btn-xs btn-ghost join-item">
              <EnvelopeIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">read</span>
            </button>
            <button @click.stop="toggleNewsKeep(idx)" class="btn btn-xs btn-ghost join-item"
                    :class="{'text-accent': card.data.state.keep}">
              <PaperClipIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">clip</span>
            </button>
            <button @click.stop="toggleNewsShared(idx)" class="btn btn-xs btn-ghost join-item"
                    :class="{'text-accent': card.data.state.shared}">
              <ShareIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">share</span>
            </button>
            <button class="btn btn-xs btn-ghost join-item" disabled="disabled">
              <FireIcon class="w-6 h-6" :class="{'text-warning': card.data.popularity?.score > 0}"/>
              <span v-if="card.data.popularity?.score > 0"
                    class="hidden lg:block text-warning">{{ card.data.popularity.score }}</span>
            </button>
          </div>
        </template>
      </NewsCard>
    </template>
  </div>
</template>
<script lang="ts">
import {Component, Vue, Watch} from 'vue-facing-decorator';
import NewsCard from "@/techwatch/components/newslist/NewsCard.vue";
import {iif, Observable, Subject} from "rxjs";
import {map, switchMap, take, tap} from "rxjs/operators";
import {NewsView} from "@/techwatch/components/newslist/model/NewsView";
import ScrollActivable from "@/services/model/ScrollActivable";
import {InfiniteScrollBehaviour, useInfiniteScroll} from "@/services/InfiniteScrollBehaviour";
import {ScrollingActivationBehaviour, useScrollingActivation} from "@/services/ScrollingActivationBehaviour";
import InfiniteScrollable from "@/services/model/InfiniteScrollable";
import {Mark} from "@/techwatch/model/Mark.enum";
import {useStore} from "vuex";
import {
  DECREMENT_UNREAD_MUTATION,
  FILTER_MUTATION,
  INCREMENT_UNREAD_MUTATION
} from "@/techwatch/store/statistics/StatisticsConstants";
import {UserState} from "@/store/user/user";
import newsService from "@/techwatch/services/NewsService";
import reloadActionService from "@/common/services/ReloadActionService";
import {NewsSearchRequest} from "@/techwatch/model/NewsSearchRequest.type";
import {News} from "@/techwatch/model/News.type";
import keyboardControl from '@/common/services/KeyboardControl';
import {NEWS_FILTER_FEED_MUTATION, NewsStore} from "@/common/model/store/NewsStore.type";
import {Feed} from "@/techwatch/model/Feed.type";
import {EnvelopeIcon, EnvelopeOpenIcon, PaperClipIcon, ShareIcon} from "@heroicons/vue/24/outline";
import {FireIcon} from "@heroicons/vue/20/solid";


@Component({
  name: 'NewsList',
  components: {
    PaperClipIcon, EnvelopeIcon, EnvelopeOpenIcon, ShareIcon, FireIcon,
    NewsCard,
  },
  setup() {
    const store = useStore();
    return {
      store: store,
      userStore: store.state.user,
      newsStore: store.state.news,
      activateOnScroll: useScrollingActivation(),
      infiniteScroll: useInfiniteScroll(),
    }
  }
})
export default class NewsList extends Vue implements ScrollActivable, InfiniteScrollable {
  private readonly store;
  private readonly userStore: UserState;
  private readonly newsStore: NewsStore;
  private readonly activateOnScroll: ScrollingActivationBehaviour;
  private readonly infiniteScroll: InfiniteScrollBehaviour;

  private news: NewsView[] = [];

  private activeNews = -1;

  get isAuthenticated(): boolean | undefined {
    return this.userStore.isAuthenticated;
  }

  @Watch('isAuthenticated')
  onAuthenticationChange(): void {
    this.loadNextPage().pipe(take(1)).subscribe({next: el => this.observeFirst(el)});
  }

  mounted(): void {
    this.activateOnScroll.connect(this);
    this.infiniteScroll.connect(this);

    if (this.userStore.isAuthenticated !== undefined) {
      this.onAuthenticationChange();
    }

    keyboardControl.registerListener("n", event => {
      event.preventDefault();
      this.activateNewsCard(this.activeNews + 1);
      this.scrollToActivateNews();
    }).registerListener("k", event => {
      event.preventDefault();
      this.activateNewsCard(this.activeNews - 1);
      this.scrollToActivateNews();
    }).registerListener("m", event => {
      event.preventDefault();
      this.toggleRead(this.activeNews);
    }).registerListener("c", event => {
      event.preventDefault();
      this.toggleNewsKeep(this.activeNews);
    }).registerListener("s", event => {
      event.preventDefault();
      this.toggleNewsShared(this.activeNews);
    });

    reloadActionService.registerReloadFunction((context) => {
      if (context === 'news' || context === '') {
        this.news = [];
        this.activeNews = -1;
        this.loadNextPage().pipe(take(1)).subscribe(el => this.observeFirst(el))
      }
    });
  }

  private observeFirst(el: Element): void {
    if (this.isAuthenticated) {
      this.activateOnScroll.observe(el);
      if (this.news.length >= 19) {
        this.infiniteScroll.observe(this.getRefElement(this.news[this.news.length - 3].data.id));
      }
    }
  }

  private buildNewsQueryString(): NewsSearchRequest {
    const query: NewsSearchRequest = {};
    if (!this.isAuthenticated) {
      return query;
    }

    if (this.newsStore.unread) {
      query.read = false;
    }
    if (this.newsStore.popular) {
      query.popular = true;
    }
    if (this.newsStore.keep) {
      query.keep = true;
    }
    if (this.newsStore.tags.length > 0) {
      query.tags = [];
      this.newsStore.tags.forEach(tag => query.tags?.push(`∋${tag}`));
    }
    if (this.newsStore.feed) {
      query.feeds = [this.newsStore.feed.id];
    }

    const lastIndex = this.news.length - 1;
    if (this.news.length <= 0) {
      return query;
    }

    let toSkip = 0;
    const lastNewsView = this.news[lastIndex];
    // Find unread news at the same date to add offset in query, avoid duplicate
    for (let i = this.news.length - 1; i >= 0; i--) {
      if (!this.news[i].data.state.read && this.news[i].data.publication === lastNewsView.data.publication) {
        toSkip++;
      } else {
        break;
      }
    }
    query.publication = `≤${lastNewsView.data.publication}`;
    if (toSkip > 0) {
      query._from = toSkip;
    }

    return query;
  }

  loadNextPage(): Observable<Element> {
    const query = this.buildNewsQueryString();

    const elements = new Subject<Element>();
    let unreadCount = this.news.filter(n => !n.data.state.read).length;
    const newsResponse = (this.isAuthenticated) ? newsService.getNews(query) : newsService.getAnonymousNews();
    newsResponse.pipe(
        switchMap(ns => {
          this.store.commit(FILTER_MUTATION, ns.total + unreadCount);
          return ns.data;
        }),
        map((ns: News[]) => ns.map(n => ({
          data: n,
          isActive: false,
          keepMark: n.state?.keep || false,
          sizes: n.imgm ? '(max-width: 1024px) 268px, 240px' : '',
          srcset: n.imgm ? `${n.imgm} 268w, ${n.imgd} 240w` : '',
        }) as NewsView)),
        tap(ns => this.news.push(...ns))
    ).subscribe({
      next: ns => {
        this.$nextTick(() => {
          ns.forEach(n => {
            return elements.next(this.getRefElement(n.data.id));
          });
          elements.complete();
        });
      },
      error: e => elements.next(e)
    });
    return elements.asObservable();
  }

  activateElement(incr: number): Element {
    this.activateNewsCard(this.activeNews + incr);
    const newsView = this.news[this.activeNews];
    if (newsView) {
      return this.getRefElement(newsView.data.id);
    } else {
      return {} as Element;
    }
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

  toggleRead(idx: number): void {
    this.markNewsRead(idx, !this.news[idx].data.state.read);
    this.news[idx].keepMark = true;
  }

  private markNewsRead(idx: number, mark: boolean) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    if (target.data.state.read === mark) {
      return;
    }

    iif(() => mark,
        newsService.mark(target.data.id, Mark.READ).pipe(
            tap(() => this.store.commit(DECREMENT_UNREAD_MUTATION))),
        newsService.unmark(target.data.id, Mark.READ).pipe(
            tap(() => this.store.commit(INCREMENT_UNREAD_MUTATION))),
    ).subscribe(state => {
      this.news[idx].data.state.read = state.read;
    });
  }

  private toggleNewsShared(idx: number) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    const markObs = (!target.data.state.shared)
        ? newsService.mark(target.data.id, Mark.SHARED)
        : newsService.unmark(target.data.id, Mark.SHARED);

    markObs.subscribe(state => {
      if (!this.news[idx].data.popularity) {
        this.news[idx].data.popularity = {score: 0, fans: []};
      }
      this.news[idx].data.state.shared = state.shared;
      if (state.shared) {
        this.news[idx].data.popularity.score += 1;
      } else {
        this.news[idx].data.popularity.score -= 1;
      }
    });
  }

  private toggleNewsKeep(idx: number) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    const markObs = (!target.data.state.keep)
        ? newsService.mark(target.data.id, Mark.KEEP)
        : newsService.unmark(target.data.id, Mark.KEEP);

    markObs.subscribe(state => {
      this.news[idx].data.state.keep = state.keep;
      this.news[idx].keepMark = state.keep;
    });
  }

  /**
   * Scroll to the activated news.
   *
   * @remarks
   * Move the viewport to center the news juste after the activated news. This move the current activate news
   * to the top of the viewport.
   *
   * {@link scrollIntoView} only allow to move center top or bottom, not to middle top.
   *
   * @private
   */
  private scrollToActivateNews() {
    if (this.activeNews >= this.news.length - 2 || this.activeNews < 0) {
      // Stop if last news
      return;
    }
    const current = this.news[this.activeNews + 1];
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
    return (vueRef)[0].$el
  }

  private onAddFilter(event: { type: string, entity: Feed }): void {
    this.store.commit(NEWS_FILTER_FEED_MUTATION, {id: event.entity.id, label: event.entity.name});
    reloadActionService.reload('news');
  }

  // noinspection JSUnusedGlobalSymbols
  unmounted(): void {
    this.activateOnScroll.disconnect();
    this.infiniteScroll.disconnect();
    keyboardControl.unregisterListener("n", "m", "k");
    reloadActionService.unregisterReloadFunction();
  }

}
</script>
