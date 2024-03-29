<template>
  <div class="overflow-x-auto mt-5">
    <FeedActions v-if="userState.isAuthenticated" :delete-enable="checkState"
                 @clickAdd="addNewFeed()" @clickImport="this.isFileUploadVisible = true" @clickDelete="bulkDelete()"/>
    <table class="table w-full" aria-describedby="Feeds list">
      <thead class="top-0 z-50">
      <tr>
        <th scope="col" v-if="userState.isAuthenticated" class="w-1">
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState" @change="onSelectAll()"/>
          </label>
        </th>
        <th scope="colgroup">
          <div class="grid grid-cols-1 md:grid-cols-12">
            <div class="md:col-span-3 my-auto">Name</div>
            <div class="md:col-span-7 my-auto">Link / Categories</div>
            <div class="md:col-span-2 join justify-end" v-if="pagesNumber > 1">
              <button v-for="i in pagesNumber" :key="i"
                      :class="{'btn-active': activePage === i-1}" class="join-item btn btn-sm"
                      v-on:click="loadFeedPage(i-1).subscribe()">
                {{ i }}
              </button>
            </div>
          </div>
        </th>
      </tr>
      </thead>
      <tbody>
      <template v-if="feeds.length > 0" v-for="vFeed in this.feeds" v-bind:key="vFeed.data._id">
        <tr>
          <th scope="row" v-if="userState.isAuthenticated">
            <label>
              <input type="checkbox" class="checkbox" v-model="vFeed.isSelected">
              <span class="checkbox-mark"></span>
            </label>
          </th>
          <td class="grid grid-cols-1 md:grid-cols-12 auto-cols-auto">
            <FeedListItem :ref="vFeed.data._id" :view="vFeed"
                          @item-view="itemView" @item-update="itemUpdate" @item-delete="itemDelete"/>
          </td>
        </tr>
      </template>
      <template v-else>
        <tr>
          <td colspan="2" class="text-center">
            No feed in the list
          </td>
        </tr>
      </template>
      </tbody>
      <tfoot>
      <tr>
        <th scope="col" v-if="userState.isAuthenticated"></th>
        <th scope="colgroup">
          <div class="grid grid-cols-1 md:grid-cols-12">
            <div class="md:col-span-3 my-auto">Name</div>
            <div class="md:col-span-7 my-auto">Link / Categories</div>
            <div class="md:col-span-2 join justify-end" v-if="pagesNumber > 1">
              <button v-for="i in pagesNumber" :key="i"
                      :class="{'btn-active': activePage === i-1}" class="join-item btn btn-sm"
                      v-on:click="loadFeedPage(i-1).subscribe()">
                {{ i }}
              </button>
            </div>
          </div>
        </th>
      </tr>
      </tfoot>
    </table>
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

const FileUploadWindow = defineAsyncComponent(() => import('@/common/components/FileUploadWindow.vue'));

@Component({
  name: 'FeedsList',
  components: { FeedActions, FeedEditor, FeedListItem, FileUploadWindow },
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
  private feeds: FeedView[] = new Array(0);
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
        map(fs => fs.map(f => this.modelToView(f))),
        tap(fs => this.feeds = fs),
    );
  }

  private onSelectAll(): void {
    const current = this.checkState;
    this.feeds.forEach(f => f.isSelected = !current);
  }

  modelToView(feed: Feed): FeedView {
    return {
      icon: new URL(feed.location).origin + '/favicon.ico',
      data: feed,
      isSelected: false,
    } as FeedView;
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

  private itemDelete(itemId: string): void {
    const idx = this.feeds.findIndex(fv => fv.data._id === itemId);
    const message = `Supprimer l’abonnement au fil <br/> <b>${this.feeds[idx].data.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.remove(itemId)),
        tap(() => {
          const concernedIndexes = this.feeds.findIndex(fv => fv.data._id === itemId);
          this.feeds.splice(concernedIndexes, 1);
        }),
    ).subscribe({
      next: feed => notificationService.pushSimpleOk(`Feed ${feed._id.substring(0, 10)} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de mettre à jour le fil');
      },
    });
  }

  private bulkDelete(): void {
    const ids = this.feeds.filter(f => f.isSelected).map(f => f.data._id);
    if (ids.length == 0) {
      return;
    } else if (ids.length == 1) {
      return this.itemDelete(ids[0]);
    }
    const message = `Supprimer les ${ids.length} abonnements sélectionnés ?`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.bulkRemove(ids)),
        tap(() => {
          ids.forEach(id => {
            const idx = this.feeds.findIndex(fv => fv.data._id === id);
            this.feeds.splice(idx, 1);
          });
        }),
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