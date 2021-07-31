<template>
  <div class="xl:w-2/3">

    <template v-for="(card, idx) in news">
      <NewsCard :ref="card.data.id" :card="card" v-bind:key="card.data.id" @activate="activateNewsCard(idx)">
        <template #actions v-if="isAuthenticated">
          <svg class="action-icon p-0.5" v-if="!card.data.read" @click.stop="markNewsRead(idx, true)"
               xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 19v-8.93a2 2 0 01.89-1.664l7-4.666a2 2 0 012.22 0l7 4.666A2 2 0 0121 10.07V19M3 19a2 2 0 002 2h14a2 2 0 002-2M3 19l6.75-4.5M21 19l-6.75-4.5M3 10l6.75 4.5M21 10l-6.75 4.5m0 0l-1.14.76a2 2 0 01-2.22 0l-1.14-.76"/>
          </svg>
          <svg class="action-icon p-0.5" v-else @click.stop="markNewsRead(idx, false)"
               xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
          </svg>
          <svg class="action-icon p-0.5" @click.stop="toggleNewsShared(idx)"
               v-bind:class="{'text-red-600 dark:text-red-400': card.data.shared}"
               xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </template>
      </NewsCard>
    </template>
  </div>
</template>
<style>
@layer components {
  .action-icon {
    @apply h-5 w-5 cursor-pointer hover:text-green-500 dark:hover:text-green-200;
  }
}
</style>
<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import NewsCard from "@/components/newslist/NewsCard.vue";
import {Observable, of, Subject} from "rxjs";
import {catchError, map, switchMap, take, tap} from "rxjs/operators";
import {NewsView} from "@/components/newslist/model/NewsView";
import ScrollingActivationBehaviour from "@/services/ScrollingActivationBehaviour";
import ScrollActivable from "@/services/model/ScrollActivable";
import InfiniteScrollBehaviour from "@/services/InfiniteScrollBehaviour";
import InfiniteScrollable from "@/services/model/InfiniteScrollable";
import {Mark} from "@/services/model/Mark.enum";
import {Feed} from "@/services/model/Feed";
import feedService, {FeedService} from "@/services/FeedService";
import newsService, {NewsService} from "@/services/NewsService";
import userService from '@/services/UserService';

@Component({
  components: {
    NewsCard,
  }
})
export default class MainContent extends Vue implements ScrollActivable, InfiniteScrollable {

  private readonly activateOnScroll = ScrollingActivationBehaviour.apply(this);
  private readonly infiniteScroll = InfiniteScrollBehaviour.apply(this);
  private news: NewsView[] = new Array(0);
  private feeds = new Map<string, Feed>();

  private activeNews = -1;
  private page = 0;
  private isAuthenticated = false;

  mounted(): void {
    userService.get().pipe(
        tap(() => this.isAuthenticated = true),
        catchError(() => {
          this.isAuthenticated = false;
          return of({});
        }),
        switchMap(() => this.loadNextPage()),
        take(1),
    ).subscribe(el => {
      this.activateOnScroll.observe(el);
      if (this.news.length > 3) {
        this.infiniteScroll.observe(this.getRefElement(this.news[this.news.length - 3].data.id));
      }
    });

    window.addEventListener('keydown', this.onKeyDownListener, false);
  }

  loadNextPage(): Observable<Element> {
    const query = new URLSearchParams(NewsService.DEFAULT_QUERY);
    if (this.isAuthenticated) {
      query.append('read', 'false');
    }
    const elements = new Subject<Element>();
    const currentPage = ++this.page;
    newsService.getNews(currentPage, query).pipe(
        map(ns => ns.map(n => ({data: n, feeds: [], isActive: false, keepMark: false}) as NewsView)),
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
          this.loadFeeds(currentPage, feeds);
        });
      },
      error: e => elements.next(e)
    });
    return elements.asObservable();
  }

  loadFeeds(page: number, ids: Map<string, string[]>): void {
    const fromIdx = this.news.length - Math.round(this.news.length / page);
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
   * For each news from the index to the and of the array, update the feed with the uptodate feed map
   *
   * @param fromIdx The start index for looping in news array
   * @param ids The list of couple of News Id and Feed Id
   * @private A Set containing Feed Id not found in feeds map
   */
  private updateNewsView(fromIdx: number, ids: Map<string, string[]>): Set<string> {
    const feedIds = new Set<string>();
    for (let i = fromIdx; i < this.news.length; i++) {
      const news = this.news[i];
      const receivedFeedIds = ids.get(news.data.id);
      if (receivedFeedIds === undefined) continue;

      receivedFeedIds.forEach(feedId => {
        const feed = this.feeds.get(feedId);
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

    const markObs = (mark)
        ? newsService.mark(target.data.id, Mark.READ)
        : newsService.unmark(target.data.id, Mark.READ);

    markObs.subscribe(news => {
      this.$set(this.news, idx, {...target, data: news});
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

    markObs.subscribe(news => {
      this.$set(this.news, idx, {...target, data: news});
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
    return (vueRef as Vue[])[0].$el
  }

  beforeDestroy(): void {
    window.removeEventListener('keydown', this.onKeyDownListener, false);
  }
}
</script>
