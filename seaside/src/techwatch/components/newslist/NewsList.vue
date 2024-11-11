<template>
  <div ref="newsList" class="max-w-5xl focus:outline-none flex flex-row flex-wrap">
    <template v-for="(card, idx) in news" :key="card.data.id">
      <NewsCard :ref="card.data.id" :card="card" :view-mode="viewMode"
                @activate="onClickNewActivate(idx)" @addFilter="onAddFilter" @clickTitle="markNewsRead(idx, true)">
        <template v-if="userStore.isAuthenticated" #actions>
          <div class="join -ml-2 lg:ml-0">
            <button v-if="card.data.state.read" :title="t('home.news.unread.tooltip')"
                    class="btn btn-xs btn-ghost join-item"
                    @click.stop="toggleRead(idx)">
              <EnvelopeOpenIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">{{ t('home.news.unread') }}</span>
            </button>
            <button v-else :title="t('home.news.read.tooltip')"
                    class="btn btn-xs btn-ghost join-item"
                    @click.stop="toggleRead(idx)">
              <EnvelopeIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">{{ t('home.news.read') }}</span>
            </button>
            <button :class="{'text-accent': card.data.state.keep}" :title="t('home.news.clip.tooltip')"
                    class="btn btn-xs btn-ghost join-item"
                    @click.stop="toggleNewsKeep(idx)">
              <PaperClipIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">{{ t('home.news.clip') }}</span>
            </button>
            <button :class="{'text-accent': card.data.state.shared}" class="btn btn-xs btn-ghost join-item"
                    @click.stop="toggleNewsShared(idx)">
              <ShareIcon class="h-5 w-5 cursor-pointer stroke-2"/>
              <span class="hidden lg:block">{{ t('home.news.share') }}</span>
            </button>
            <button class="btn btn-xs btn-ghost join-item" disabled="disabled">
              <FireIcon :class="{'text-warning': card.data.popularity?.score > 0}" class="w-6 h-6"/>
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
import { Component, Vue, Watch } from 'vue-facing-decorator';
import NewsCard from '@/techwatch/components/newslist/NewsCard.vue';
import { iif, Observable, Subject } from 'rxjs';
import { map, switchMap, take, tap } from 'rxjs/operators';
import { NewsView } from '@/techwatch/components/newslist/model/NewsView';
import ScrollActivable from '@/services/model/ScrollActivable';
import { InfiniteScrollBehaviour, useInfiniteScroll } from '@/services/InfiniteScrollBehaviour';
import { ScrollingActivationBehaviour, useScrollingActivation } from '@/services/ScrollingActivationBehaviour';
import InfiniteScrollable from '@/services/model/InfiniteScrollable';
import { Mark } from '@/techwatch/model/Mark.enum';
import { useStore } from 'vuex';
import {
  DECREMENT_UNREAD_MUTATION,
  FILTER_MUTATION,
  INCREMENT_UNREAD_MUTATION,
} from '@/techwatch/store/statistics/StatisticsConstants';
import { UserState } from '@/security/store/user';
import newsService, { newsMark, newsUnMark } from '@/techwatch/services/NewsService';
import {
  actionServiceRegisterFunction,
  actionServiceReload,
  actionServiceUnregisterFunction,
} from '@/common/services/ReloadActionService';
import { NewsSearchRequest } from '@/techwatch/model/NewsSearchRequest.type';
import { News } from '@/techwatch/model/News.type';
import { NEWS_FILTER_FEED_MUTATION, NewsStore } from '@/common/model/store/NewsStore.type';
import { Feed } from '@/techwatch/model/Feed.type';
import { EnvelopeIcon, EnvelopeOpenIcon, PaperClipIcon, ShareIcon } from '@heroicons/vue/24/outline';
import { FireIcon } from '@heroicons/vue/20/solid';
import { KeyboardController, listener, useKeyboardController } from '@/common/services/KeyboardController';
import { ref } from 'vue';
import { Ref, UnwrapRef } from '@vue/reactivity';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'NewsList',
  components: {
    PaperClipIcon, EnvelopeIcon, EnvelopeOpenIcon, ShareIcon, FireIcon,
    NewsCard,
  },
  setup() {
    const store = useStore();
    const newsList: Ref<UnwrapRef<HTMLElement>> = ref(HTMLElement.prototype);
    const { t } = useI18n();
    return {
      store: store,
      t: t,
      userStore: store.state.user,
      newsStore: store.state.news,
      activateOnScroll: useScrollingActivation(),
      infiniteScroll: useInfiniteScroll(),
      keyboardController: useKeyboardController(newsList),
      newsList,
    };
  },
})
export default class NewsList extends Vue implements ScrollActivable, InfiniteScrollable {
  private readonly store;
  private readonly t: unknown;
  private readonly userStore: UserState;
  private readonly newsStore: NewsStore;
  private readonly activateOnScroll: ScrollingActivationBehaviour;
  private readonly infiniteScroll: InfiniteScrollBehaviour;
  private keyboardController: KeyboardController;

  private news: NewsView[] = [];

  private activeNews = -1;

  get isAuthenticated(): boolean | undefined {
    return this.userStore.isAuthenticated;
  }

  get viewMode(): 'CARD' | 'MAGAZINE' {
    return this.userStore.newView;
  }

  @Watch('isAuthenticated')
  onAuthenticationChange(): void {
    this.loadNextPage().pipe(take(1)).subscribe({ next: el => this.observeFirst(el) });
  }

  mounted(): void {
    this.activateOnScroll.connect(this);
    this.infiniteScroll.connect(this);

    if (this.userStore.isAuthenticated !== undefined) {
      this.onAuthenticationChange();
    }

    this.keyboardController.register(
        listener('n', event => {
          event.preventDefault();
          this.applyNewsAutoRead();
          this.applyNewsCardActivation(this.activeNews + 1);
          this.scrollToActivateNews('center');
        }),
        listener('k', event => {
          event.preventDefault();
          this.applyNewsAutoRead();
          this.applyNewsCardActivation(this.activeNews - 1);
          this.scrollToActivateNews('center');
        }),
        listener('m', event => {
          event.preventDefault();
          this.toggleRead(this.activeNews);
        }),
        listener('c', event => {
          event.preventDefault();
          this.toggleNewsKeep(this.activeNews);
        }),
        listener('s', event => {
          event.preventDefault();
          this.toggleNewsShared(this.activeNews);
        }),
    ).start();

    actionServiceRegisterFunction((context) => {
      if (context === 'news' || context === '') {
        this.news = [];
        this.activeNews = -1;
        this.loadNextPage().pipe(take(1)).subscribe(el => this.observeFirst(el));
      }
    });
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
        tap(ns => this.news.push(...ns)),
    ).subscribe({
      next: ns => {
        this.$nextTick(() => {
          ns.forEach(n => {
            return elements.next(this.getRefElement(n.data.id));
          });
          elements.complete();
        });
      },
      error: e => elements.next(e),
    });
    return elements.asObservable();
  }

  onClickNewActivate(idx: number): void {
    this.applyNewsAutoRead();
    this.applyNewsCardActivation(idx);
  }

  onScrollActivation(incr: number): Element {
    this.applyNewsAutoRead();
    switch (this.viewMode) {
      case 'MAGAZINE':
        this.applyNewsCardActivation(this.activeNews + incr);
        const newsView = this.news[this.activeNews];
        if (newsView) {
          return this.getRefElement(newsView.data.id);
        } else {
          return {} as Element;
        }
      case 'CARD':
        this.applyNewsCardActivation(this.activeNews + incr);
        return {} as Element;
      default:
    }
  }

  toggleRead(idx: number): void {
    this.markNewsRead(idx, !this.news[idx].data.state.read);
    this.news[idx].keepMark = true;
  }

  getRefElement(ref: string): Element {
    const vueRef: Vue[] | undefined = this.$refs[ref] as Vue[] | undefined;
    if (vueRef === undefined) {
      throw new Error(`Element with ref ${ref} not found !`);
    }
    return (vueRef)[0].$el;
  }

  beforeUnmount(): void {
    this.keyboardController.purge();

  }

  unmounted(): void {
    this.activateOnScroll.disconnect();
    this.infiniteScroll.disconnect();
    actionServiceUnregisterFunction();
  }

  private applyNewsAutoRead(): void {
    if (this.activeNews >= 0) {
      if (!this.news[this.activeNews].keepMark && this.userStore.autoread) {
        this.markNewsRead(this.activeNews, true);
      }
    }
  }

  private applyNewsCardActivation(_idx: number): void {
    if (this.activeNews >= 0 && this.activeNews < this.news.length) {
      // Manage previous news
      this.news[this.activeNews].isActive = false;
    }

    const idx = Math.max(-1, Math.min(_idx, this.news.length));
    this.activeNews = idx;
    if (idx >= this.news.length || idx < 0) {
      // Stop if last news
      return;
    }

    this.news[this.activeNews].isActive = true;
    this.activateOnScroll.observe(this.getRefElement(this.news[this.activeNews].data.id));
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

  private markNewsRead(idx: number, mark: boolean) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    if (!target) {
      return;
    }
    if (target.data.state.read === mark) {
      return;
    }

    iif(() => mark,
        newsMark(target.data.id, Mark.READ).pipe(
            tap(() => {
              this.news[idx].data.state.read = true;
              this.store.commit(DECREMENT_UNREAD_MUTATION);
            })),
        newsUnMark(target.data.id, Mark.READ).pipe(
            tap(() => {
              this.news[idx].data.state.read = false;
              this.store.commit(INCREMENT_UNREAD_MUTATION);
            })),
    ).subscribe(state => {
      this.news[idx].data.state.read = state.read;
    });
  }

  private toggleNewsShared(idx: number) {
    if (!this.isAuthenticated) {
      return;
    }
    const target = this.news[idx];
    if (!target) {
      return;
    }
    const markObs = (!target.data.state.shared)
        ? newsMark(target.data.id, Mark.SHARED)
        : newsUnMark(target.data.id, Mark.SHARED);

    markObs.subscribe(state => {
      if (!this.news[idx].data.popularity) {
        this.news[idx].data.popularity = { score: 0, fans: [] };
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
    if (!target) {
      return;
    }
    const markObs = (!target.data.state.keep)
        ? newsMark(target.data.id, Mark.KEEP)
        : newsUnMark(target.data.id, Mark.KEEP);

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
  private scrollToActivateNews(block: string = 'center') {
    if (this.activeNews >= this.news.length - 2 || this.activeNews < 0) {
      // Stop if last news
      return;
    }
    const current = this.news[this.activeNews];
    this.$nextTick(() => {
      this.getRefElement(current.data.id).scrollIntoView(
          { block: block, behavior: 'smooth' } as ScrollIntoViewOptions);
    });
  }

  private onAddFilter(event: { type: string, entity: Feed }): void {
    this.store.commit(NEWS_FILTER_FEED_MUTATION, { id: event.entity._id, label: event.entity.name });
    actionServiceReload('news');
  }

}
</script>
