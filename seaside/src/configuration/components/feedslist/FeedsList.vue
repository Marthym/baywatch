<template>
  <div class="overflow-x-auto mt-5">
    <SmartTable columns="Name / Link / Categories / Actions" :elements="feeds" actions="avieud"
                @add="addNewFeed()"
                @delete="idx => itemDelete(idx)"
                @deleteSelected="idx => bulkDelete(idx)">
      <template #default="vFeed">
        <std class="grid grid-cols-1 lg:gap-x-4 md:grid-cols-12 auto-cols-auto">
          <FeedCard :view="{...vFeed.data, icon: vFeed.data.icon}"/>
        </std>
      </template>
    </SmartTable>

    <FeedEditor ref="feedEditor"/>
    <FileUploadWindow v-if="isFileUploadVisible" @upload="onOPMLUpload"/>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import FeedListItem from '@/configuration/components/feedslist/FeedsListItem.vue';
import { FeedView } from '@/configuration/components/feedslist/model/FeedView';
import { filter, map, switchMap, take, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Feed } from '@/configuration/model/Feed.type';
import FeedEditor from '@/configuration/components/feedslist/FeedEditor.vue';
import feedsService from '@/configuration/services/FeedService';
import opmlService from '@/techwatch/services/OpmlService';
import notificationService from '@/services/notification/NotificationService';
import { actionServiceRegisterFunction, actionServiceUnregisterFunction } from '@/common/services/ReloadActionService';
import { defineAsyncComponent } from 'vue';
import { AlertResponse, AlertType } from '@/common/components/alertdialog/AlertDialog.types';
import FeedActions from '@/configuration/components/feedslist/FeedActions.vue';
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

const FileUploadWindow = defineAsyncComponent(() => import('@/common/components/FileUploadWindow.vue'));

@Component({
  name: 'FeedsList',
  components: {
    FeedCard,
    std,
    stla,
    SmartTable,
    InformationCircleIcon,
    FeedActions,
    FeedEditor,
    FeedListItem,
    FileUploadWindow,
  },
  setup() {
    const store: Store<UserState> = useStore();
    return {
      store: store,
      userState: store.state.user,
      router: useRouter(),
    };
  },
})
export default class FeedsList extends Vue {
  private store: Store<UserState>;
  private userState: UserState;
  private router: Router;
  private feedEditor!: FeedEditor;
// noinspection JSMismatchedCollectionQueryUpdate
  private feeds: SmartTableView<Feed>[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private isFileUploadVisible = false;

  get checkState(): boolean {
    const isOneSelected = this.feeds.find(f => f.isSelected) !== undefined;
    if (this.$refs['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.feeds.find(f => !f.isSelected) !== undefined;
    return isOneSelected;
  }

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

  loadFeedPage(page: number): Observable<FeedView[]> {
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

  private onSelectAll(): void {
    const current = this.checkState;
    this.feeds.forEach(f => f.isSelected = !current);
  }

  modelToView(feed: Feed): SmartTableView<Feed> {
    return {
      data: feed,
      isSelected: false,
      isEditable: true,
    };
  }

  private addNewFeed(): void {
    this.feedEditor.openEmpty().pipe(
        take(1),
        switchMap(feed => feedsService.add(feed)),
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('Fil ajouté avec succès'),
      error: e => {
        notificationService.pushSimpleError(e.message);
      },
    });
  }

  private itemView(item: Feed): void {
    this.store.commit(NEWS_FILTER_FEED_MUTATION, { id: item._id, label: item.name });
    this.router.push('/news');
  }

  private itemUpdate(item: Feed): void {
    this.feedEditor.openFeed({ ...item }).pipe(
        take(1),
        switchMap(feed => feedsService.update(feed, item.location !== feed.url)),
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('Mis à jour avec succès'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de mettre à jour le fil');
      },
    });
  }

  private itemDelete(idx: number): void {
    const message = `Supprimer l’abonnement au fil <br/> <b>${this.feeds[idx].data.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.remove(this.feeds[idx].data._id)),
        tap(() => this.feeds.splice(idx, 1)),
    ).subscribe({
      next: feed => notificationService.pushSimpleOk(`Feed ${feed._id.substring(0, 10)} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de mettre à jour le fil');
      },
    });
  }

  private bulkDelete(ids: number[]): void {
    if (ids.length == 0) {
      return;
    } else if (ids.length == 1) {
      return this.itemDelete(ids[0]);
    }
    const message = `Supprimer les ${ids.length} abonnements sélectionnés ?`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.bulkRemove(ids.map(index => this.feeds[index].data._id))),
        tap(() =>
            ids.forEach(idx =>
                this.feeds.splice(idx, 1))),
    ).subscribe({
      next: () => notificationService.pushSimpleOk(`Désinscription réalisé avec succès !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de se désinscrire des feeds sélectionnés !');
      },
    });
  }

  private onOPMLUpload(path: File | undefined): void {
    this.isFileUploadVisible = false;
    if (path === undefined) {
      return;
    }
    opmlService.upload(path).pipe(
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('OPML chargé avec succès'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de charger le fichier');
      },
    });
  }

  // noinspection JSUnusedGlobalSymbols
  unmounted(): void {
    actionServiceUnregisterFunction();
  }
}
</script>