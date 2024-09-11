<template>
  <div class="overflow-x-auto mt-5">
    <SmartTable :active-page="activePage" :columns="t('config.feeds.table.headers')" :elements="feeds"
                :total-page="pagesNumber"
                actions="avieud"
                @add="addNewFeed()"
                @delete="idx => itemDelete(idx)"
                @deleteSelected="idx => bulkDelete(idx)"
                @edit="itemUpdate"
                @export="onClickExport"
                @import="onClickImport"
                @navigate="pageIdx => loadFeedPage(pageIdx).subscribe()"
                @view="itemView">
      <template #default="vFeed">
        <std class="grid grid-cols-1 lg:gap-x-4 md:grid-cols-12 auto-cols-auto">
          <FeedCard :view="vFeed.data"/>
        </std>
      </template>
    </SmartTable>

    <FeedEditor ref="feedEditor"/>
    <FileUploadWindow v-if="isFileUploadVisible" @upload="onOPMLUpload"/>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { filter, map, switchMap, take, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Feed } from '@/configuration/model/Feed.type';
import FeedEditor from '@/configuration/components/feedslist/FeedEditor.vue';
import feedsService, { feedUpdate } from '@/configuration/services/FeedService';
import opmlService from '@/techwatch/services/OpmlService';
import notificationService from '@/services/notification/NotificationService';
import { actionServiceRegisterFunction, actionServiceUnregisterFunction } from '@/common/services/ReloadActionService';
import { defineAsyncComponent } from 'vue';
import { AlertResponse, AlertType } from '@/common/components/alertdialog/AlertDialog.types';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { NEWS_FILTER_FEED_MUTATION } from '@/common/model/store/NewsStore.type';
import { Router, useRouter } from 'vue-router';
import { InformationCircleIcon } from '@heroicons/vue/24/outline';
import SmartTable from '@/common/components/smartTable/SmartTable.vue';
import stla from '@/common/components/smartTable/SmartTableLineAction.vue';
import std from '@/common/components/smartTable/SmartTableData.vue';
import { SmartTableView } from '@/common/components/smartTable/SmartTableView.interface';
import FeedCard from '@/common/components/FeedCard.vue';
import { useI18n } from 'vue-i18n';

const FileUploadWindow = defineAsyncComponent(() => import('@/common/components/FileUploadWindow.vue'));
const BASEURL = import.meta.env.VITE_API_BASE_URL;

@Component({
  name: 'FeedsList',
  components: {
    FeedCard,
    std,
    stla,
    SmartTable,
    InformationCircleIcon,
    FeedEditor,
    FileUploadWindow,
  },
  setup() {
    const store: Store<UserState> = useStore();
    const { t } = useI18n();
    return {
      store: store,
      router: useRouter(),
      t: t,
    };
  },
})
export default class FeedsList extends Vue {
  private t;
  private store: Store<UserState>;
  private router: Router;
  private feedEditor!: FeedEditor;
  private feeds: SmartTableView<Feed>[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private isFileUploadVisible = false;

  mounted(): void {
    this.loadFeedPage(0).subscribe({
      next: () => actionServiceRegisterFunction(context => {
        if (context === '' || context === 'feed') {
          this.loadFeedPage(this.activePage).subscribe();
        }
      }),
    });
    this.feedEditor = this.$refs.feedEditor as FeedEditor;
  }

  loadFeedPage(page: number): Observable<SmartTableView<Feed>[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return feedsService.list({ _p: resolvedPage }).pipe(
        switchMap(feedPage => {
          this.pagesNumber = feedPage.totalPage;
          this.activePage = feedPage.currentPage;
          return feedPage.data;
        }),
        map(fs => fs.map(f =>
            this.modelToView({ icon: new URL(new URL(f.location).origin + '/favicon.ico'), ...f }))),
        tap(fs => this.feeds = fs),
    );
  }

  modelToView(feed: Feed): SmartTableView<Feed> {
    return {
      data: feed,
      isSelected: false,
      isEditable: true,
    };
  }

  unmounted(): void {
    actionServiceUnregisterFunction();
  }

  private addNewFeed(): void {
    this.feedEditor.openEmpty().pipe(
        take(1),
        switchMap(feed => feedsService.add(feed)),
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('Feed successfully subscribed.'),
      error: e => {
        notificationService.pushSimpleError(e.message);
      },
    });
  }

  private itemView(idx: number): void {
    const item = this.feeds[idx].data;
    if (!item) {
      notificationService.pushSimpleError(`Unable to edit element at index ${idx}`);
      return;
    }
    this.store.commit(NEWS_FILTER_FEED_MUTATION, { id: item._id, label: item.name });
    this.router.push('/news');
  }

  private itemUpdate(idx: number): void {
    const item = this.feeds[idx].data;
    if (!item) {
      notificationService.pushSimpleError(`Unable to edit element at index ${idx}`);
      return;
    }
    this.feedEditor.openFeed({ ...item }).pipe(
        take(1),
        switchMap((feed: Feed) => feedUpdate(feed._id, feed)),
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('Feed updated successfully.'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to update feed !');
      },
    });
  }

  private itemDelete(idx: number): void {
    const message = `Unsubscribe for feed <br/> <b>${this.feeds[idx].data.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.remove(this.feeds[idx].data._id)),
        tap(() => this.feeds.splice(idx, 1)),
    ).subscribe({
      next: feed => notificationService.pushSimpleOk(`Feed ${feed._id.substring(0, 10)} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to update feed !');
      },
    });
  }

  private bulkDelete(ids: number[]): void {
    if (ids.length == 0) {
      return;
    } else if (ids.length == 1) {
      return this.itemDelete(ids[0]);
    }
    const message = `Remove the ${ids.length} selected subscriptions ?`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.bulkRemove(ids.map(index => this.feeds[index].data._id))),
        tap(() =>
            ids.forEach(idx =>
                this.feeds.splice(idx, 1))),
    ).subscribe({
      next: () => notificationService.pushSimpleOk(`Unsubscribe successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to unsubscribe to the selected feeds !');
      },
    });
  }

  private onClickExport(): void {
    const link = document.createElement('a');
    link.href = `${BASEURL}/opml/export/baywatch.opml`;
    link.target = '_blank';
    link.download = 'baywatch.opml';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  private onClickImport(): void {
    this.isFileUploadVisible = true;
  }

  private onOPMLUpload(path: File | undefined): void {
    this.isFileUploadVisible = false;
    if (path === undefined) {
      return;
    }
    opmlService.upload(path).pipe(
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('OPML loaded successfully.'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to load OPML file !');
      },
    });
  }
}
</script>