<template>
  <div class="overflow-x-auto mt-5">
    <FeedActions v-if="userState.isAuthenticated" :delete-enable="checkState"
                 @clickAdd="addNewFeed" @clickImport="this.isFileUploadVisible = true" @clickDelete="bulkDelete()"/>
    <table class="table w-full" aria-describedby="Feeds list">
      <thead>
      <tr>
        <th scope="col" v-if="userState.isAuthenticated">
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState" @change="onSelectAll()"/>
          </label>
        </th>
        <th scope="colgroup">
          <div class="grid grid-cols-1 md:grid-cols-12">
            <div class="md:col-span-3 my-auto">Name</div>
            <div class="md:col-span-7 my-auto">Link / Categories</div>
            <div class="md:col-span-2 btn-group justify-end" v-if="pagesNumber > 1">
              <button v-for="i in pagesNumber" :key="i"
                      :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                      v-on:click="loadFeedPage(i-1).subscribe()">
                {{ i }}
              </button>
            </div>
          </div>
        </th>
      </tr>
      </thead>
      <tbody>
      <template v-if="feeds.length > 0" v-for="vFeed in this.feeds" v-bind:key="vFeed.data.id">
        <tr>
          <th scope="row" v-if="userState.isAuthenticated">
            <label>
              <input type="checkbox" class="checkbox" v-model="vFeed.isSelected">
              <span class="checkbox-mark"></span>
            </label>
          </th>
          <td class="grid grid-cols-1 md:grid-cols-12 auto-cols-auto">
            <FeedListItem :ref="vFeed.data.id" :view="vFeed"
                          @item-update="itemUpdate" @item-delete="itemDelete"/>
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
            <div class="md:col-span-2 btn-group justify-end" v-if="pagesNumber > 1">
              <button v-for="i in pagesNumber" :key="i"
                      :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
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
import {Options, Vue} from 'vue-property-decorator';
import FeedListHeader from "@/techwatch/components/feedslist/FeedListHeader.vue";
import FeedListItem from "@/techwatch/components/feedslist/FeedListItem.vue";
import {FeedView} from "@/techwatch/components/feedslist/model/FeedView";
import {filter, map, switchMap, take, tap} from "rxjs/operators";
import {Observable} from "rxjs";
import {Feed} from "@/techwatch/model/Feed";
import FeedEditor from "@/techwatch/components/feedslist/FeedEditor.vue";
import feedsService from "@/techwatch/services/FeedService";
import opmlService from "@/techwatch/services/OpmlService";
import notificationService from "@/services/notification/NotificationService";
import {defineAsyncComponent} from "vue";
import {AlertResponse, AlertType} from "@/components/shared/alertdialog/AlertDialog.types";
import FeedActions from "@/techwatch/components/feedslist/FeedActions.vue";
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {UserState} from "@/store/user/user";

const FileUploadWindow = defineAsyncComponent(() => import('@/components/shared/FileUploadWindow.vue'));

@Options({
  name: 'FeedsList',
  components: {FeedActions, FeedEditor, FeedListItem, FeedListHeader, FileUploadWindow},
})
export default class FeedsList extends Vue {
  private userState: UserState = setup(() => useStore().state.user);
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
    this.loadFeedPage(0).subscribe();
    this.feedEditor = this.$refs.feedEditor as FeedEditor;
  }

  loadFeedPage(page: number): Observable<FeedView[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return feedsService.list(resolvedPage).pipe(
        switchMap(feedPage => {
          this.pagesNumber = feedPage.totalPage;
          this.activePage = feedPage.currentPage;
          return feedPage.data;
        }),
        map(fs => fs.map(f => this.modelToView(f))),
        tap(fs => this.feeds = fs)
    )
  }

  private onSelectAll(): void {
    const current = this.checkState;
    this.feeds.forEach(f => f.isSelected = !current);
  }

  modelToView(feed: Feed): FeedView {
    return {
      icon: new URL(feed.url).origin + '/favicon.ico',
      data: feed,
      isSelected: false
    } as FeedView
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
      }
    });
  }

  private itemUpdate(item: Feed): void {
    this.feedEditor.openFeed({...item}).pipe(
        take(1),
        switchMap(feed => feedsService.update(feed, item.url !== feed.url)),
        switchMap(() => this.loadFeedPage(this.activePage)),
    ).subscribe({
      next: () => notificationService.pushSimpleOk('Mis à jour avec succès'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de mettre à jour le fil');
      }
    });
  }

  private itemDelete(itemId: string): void {
    const idx = this.feeds.findIndex(fv => fv.data.id === itemId);
    const message = `Supprimer l’abonnement au fil <br/> <b>${this.feeds[idx].data.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => feedsService.remove(itemId)),
        tap(() => {
          const concernedIndexes = this.feeds.findIndex(fv => fv.data.id === itemId);
          this.feeds.splice(concernedIndexes, 1);
        })
    ).subscribe({
      next: feed => notificationService.pushSimpleOk(`Feed ${feed.id.substring(0, 10)} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de mettre à jour le fil');
      }
    });
  }

  private bulkDelete(): void {
    const ids = this.feeds.filter(f => f.isSelected).map(f => f.data.id);
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
            const idx = this.feeds.findIndex(fv => fv.data.id === id);
            this.feeds.splice(idx, 1);
          })
        })
    ).subscribe({
      next: () => notificationService.pushSimpleOk(`Désinscription réalisé avec succès !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Impossible de se désinscrire des feeds sélectionnés !');
      }
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
}
</script>