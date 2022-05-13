<template>
  <div class="max-w-5xl">
    <template v-for="(card, idx) in news" :key="card.data.id">
      <NewsCard :ref="card.data.id" :card="card" @activate="activateNewsCard(idx)">
        <template #actions v-if="userStore.isAuthenticated">
          <div class="btn-group">
            <button v-if="card.data.read" @click.stop="markNewsRead(idx, false)" class="btn btn-xs btn-ghost">
              <svg class="h-5 w-5 cursor-pointer" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                   stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 19v-8.93a2 2 0 01.89-1.664l7-4.666a2 2 0 012.22 0l7 4.666A2 2 0 0121 10.07V19M3 19a2 2 0 002 2h14a2 2 0 002-2M3 19l6.75-4.5M21 19l-6.75-4.5M3 10l6.75 4.5M21 10l-6.75 4.5m0 0l-1.14.76a2 2 0 01-2.22 0l-1.14-.76"/>
              </svg>
            </button>
            <button v-else @click.stop="markNewsRead(idx, true)" class="btn btn-xs btn-ghost">
              <svg class="h-5 w-5 cursor-pointer" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                   stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </button>
            <button @click.stop="toggleNewsShared(idx)" class="btn btn-xs btn-ghost">
              <svg class="h-5 w-5 cursor-pointer" :class="{'text-red-400': card.data.shared}"
                   xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
              </svg>
            </button>
            <button class="btn btn-xs btn-ghost" disabled="disabled">
              <svg class="w-6 h-6" :class="{'text-warning': card.popularity > 0}" fill="currentColor"
                   viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd"
                      d="M12.395 2.553a1 1 0 00-1.45-.385c-.345.23-.614.558-.822.88-.214.33-.403.713-.57 1.116-.334.804-.614 1.768-.84 2.734a31.365 31.365 0 00-.613 3.58 2.64 2.64 0 01-.945-1.067c-.328-.68-.398-1.534-.398-2.654A1 1 0 005.05 6.05 6.981 6.981 0 003 11a7 7 0 1011.95-4.95c-.592-.591-.98-.985-1.348-1.467-.363-.476-.724-1.063-1.207-2.03zM12.12 15.12A3 3 0 017 13s.879.5 2.5.5c0-1 .5-4 1.25-4.5.5 1 .786 1.293 1.371 1.879A2.99 2.99 0 0113 13a2.99 2.99 0 01-.879 2.121z"
                      clip-rule="evenodd"></path>
              </svg>
              <span v-if="card.popularity > 0" class="text-warning">{{ card.popularity }}</span>
            </button>
          </div>
        </template>
      </NewsCard>
    </template>
  </div>
</template>
<script lang="ts">
import {Options, Vue, Watch} from 'vue-property-decorator';
import NewsCard from "@/techwatch/components/newslist/NewsCard.vue";
import {iif, Observable, Subject} from "rxjs";
import {map, switchMap, take, tap} from "rxjs/operators";
import {NewsView} from "@/techwatch/components/newslist/model/NewsView";
import ScrollActivable from "@/services/model/ScrollActivable";
import {useInfiniteScroll} from "@/services/InfiniteScrollBehaviour";
import {useScrollingActivation} from "@/services/ScrollingActivationBehaviour";
import InfiniteScrollable from "@/services/model/InfiniteScrollable";
import {Mark} from "@/techwatch/model/Mark.enum";
import {Feed} from "@/techwatch/model/Feed";
import {ConstantFilters} from "@/constants";
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {
  DECREMENT_UNREAD_MUTATION,
  FILTER_MUTATION,
  INCREMENT_UNREAD_MUTATION
} from "@/techwatch/store/statistics/StatisticsConstants";
import {UserState} from "@/store/user/user";

import feedService, {FeedService} from "@/configuration/services/FeedService";
import newsService, {NewsService} from "@/techwatch/services/NewsService";
import {NewsStore} from "@/techwatch/store/news/news";
import popularNewsService from "@/techwatch/services/PopularNewsService";
import {Popularity} from "@/techwatch/model/Popularity.type";


@Options({
  name: 'NewsList',
  components: {
    NewsCard,
  }
})
export default class NewsList extends Vue implements ScrollActivable, InfiniteScrollable {

  private readonly store = setup(() => useStore());
  private readonly userStore: UserState = setup(() => useStore().state.user);
  private readonly newsStore: NewsStore = setup(() => useStore().state.news);
  private readonly activateOnScroll = setup(() => useScrollingActivation());
  private readonly infiniteScroll = setup(() => useInfiniteScroll());

  private news: NewsView[] = [];
  private feeds = new Map<string, Feed>();

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

    window.addEventListener('keydown', this.onKeyDownListener, false);

    newsService.registerReloadFunction(() => {
      this.news = [];
      this.activeNews = -1;
      this.loadNextPage().pipe(take(1)).subscribe(el => this.observeFirst(el))
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

  private buildNewsQueryString(): URLSearchParams {
    const query = new URLSearchParams(NewsService.DEFAULT_QUERY);
    if (this.isAuthenticated) {
      if (this.newsStore.unread) {
        query.append('read', 'false');
      }
      if (this.newsStore.popular) {
        query.append('popular', 'true');
      }
      if (this.newsStore.tags.length > 0) {
        this.newsStore.tags.forEach(tag => query.append('tags', `∋${tag}`));
      }
    }
    return query;
  }

  loadNextPage(): Observable<Element> {
    const query = this.buildNewsQueryString();

    const lastIndex = this.news.length - 1;
    if (this.news.length > 0) {
      let toSkip = 0;
      const lastNewsView = this.news[lastIndex];
      // Find unread news at the same date to add offset in query, avoid duplicate
      for (let i = this.news.length - 1; i >= 0; i--) {
        if (!this.news[i].data.read && this.news[i].data.publication === lastNewsView.data.publication) {
          toSkip++;
        } else {
          break;
        }
      }
      query.append('publication', `≤${lastNewsView.data.publication}`)
      if (toSkip > 0) {
        query.append(ConstantFilters.FROM_PAGE, String(toSkip))
      }
    }
    const elements = new Subject<Element>();
    let unreadCount = this.news.filter(n => !n.data.read).length;
    newsService.getNews(undefined, query).pipe(
        switchMap(ns => {
          this.store.commit(FILTER_MUTATION, ns.total + unreadCount);
          return ns.data;
        }),
        map(ns => ns.map(n => ({data: n, feeds: [], isActive: false, keepMark: false, popularity: 0}) as NewsView)),
        tap(ns => this.news.push(...ns))
    ).subscribe({
      next: ns => {
        this.$nextTick(() => {
          const feeds = new Map<string, string[]>();
          ns.forEach(n => {
            feeds.set(n.data.id, n.data.feeds);
            return elements.next(this.getRefElement(n.data.id));
          });
          elements.complete();
          this.loadPopularity(lastIndex, Array.from(feeds.keys()));
          this.loadFeeds(lastIndex, feeds);
        });
      },
      error: e => elements.next(e)
    });
    return elements.asObservable();
  }

  /**
   * Retrieve the popularity data from server for the latest loaded news
   *
   * @param fromIdx The start index in news array
   * @param ids The ids of last loaded news
   */
  loadPopularity(fromIdx: number, ids: string[]): void {
    popularNewsService.get(ids).subscribe({
      next: pops => {
        for (let i = Math.max(fromIdx, 0); i < this.news.length; i++) {
          let popularity: Popularity | undefined = pops.find(p => p.id === this.news[i].data.id);
          if (popularity !== undefined) {
            this.news[i].popularity = popularity.popularity;
          }
        }
      }
    });
  }

  loadFeeds(fromIdx: number, ids: Map<string, string[]>): void {
    const feedIds = this.updateNewsView(fromIdx, ids);

    const query = new URLSearchParams(FeedService.DEFAULT_QUERY);
    feedIds.forEach(f => query.append('id', f));
    feedService.list(-1, query).pipe(
        switchMap(page => page.data)
    ).subscribe(fs => {
      for (const f of fs) {
        this.feeds.set(f.id, f);
      }
      this.updateNewsView(fromIdx, ids);
    });
  }

  /**
   * For each news from the index to the end of the array, update the feed with the uptodate feed map
   *
   * @param fromIdx The start index for looping in news array
   * @param ids The list of couple of News Id and Feed Id
   * @return A Set containing Feed Id not found in feeds map
   */
  private updateNewsView(fromIdx: number, ids: Map<string, string[]>): Set<string> {
    const feedIds = new Set<string>();
    for (let i = Math.max(fromIdx, 0); i < this.news.length; i++) {
      const news = this.news[i];
      const receivedFeedIds: string[] | undefined = ids.get(news.data.id);
      if (receivedFeedIds === undefined) continue;

      receivedFeedIds.forEach(feedId => {
        const feed: Feed | undefined = this.feeds.get(feedId);
        if (feed === undefined) {
          feedIds.add(feedId);
        } else {
          if (!news.feeds.includes(feed.name))
            news.feeds.push(feed.name);
        }
      });
    }
    return feedIds;
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
    this.markNewsRead(idx, !this.news[idx].data.read);
    this.news[idx].keepMark = true;
  }

  private markNewsRead(idx: number, mark: boolean) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    if (target.data.read === mark) {
      return;
    }

    iif(() => mark,
        newsService.mark(target.data.id, Mark.READ).pipe(
            tap(() => this.store.commit(DECREMENT_UNREAD_MUTATION))),
        newsService.unmark(target.data.id, Mark.READ).pipe(
            tap(() => this.store.commit(INCREMENT_UNREAD_MUTATION))),
    ).subscribe(state => {
      this.news[idx].data.read = state.read;
    });
  }

  private toggleNewsShared(idx: number) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    const markObs = (!target.data.shared)
        ? newsService.mark(target.data.id, Mark.SHARED)
        : newsService.unmark(target.data.id, Mark.SHARED);

    markObs.subscribe(state => {
      this.news[idx].data.shared = state.shared;
      if (state.shared) {
        this.news[idx].popularity += 1;
      } else {
        this.news[idx].popularity -= 1;
      }
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

  // noinspection JSUnusedGlobalSymbols
  unmounted(): void {
    this.activateOnScroll.disconnect();
    this.infiniteScroll.disconnect();
    window.removeEventListener('keydown', this.onKeyDownListener, false);
  }

}
</script>
