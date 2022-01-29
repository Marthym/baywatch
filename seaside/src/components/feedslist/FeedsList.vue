<template>
  <div class="overflow-x-auto mt-5">
    <FeedActions v-if="isAuthenticated" :delete-enable="checkState"
                 @clickAdd="addNewFeed" @clickImport="this.isFileUploadVisible = true" @clickDelete="bulkDelete()"/>
    <table class="table w-full">
      <thead>
      <tr>
        <th v-if="isAuthenticated">
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState" @change="onSelectAll()"/>
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th class="grid grid-cols-1 md:grid-cols-12">
          <div class="col-span-4">Name</div>
          <div class="col-span-7">Link / Categories</div>
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadFeedPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
      </tr>
      </thead>
      <tbody>
      <template v-for="vFeed in this.feeds" v-bind:key="vFeed.data.id">
        <tr>
          <th v-if="isAuthenticated">
            <label>
              <input type="checkbox" class="checkbox" v-model="vFeed.isSelected">
              <span class="checkbox-mark"></span>
            </label>
          </th>
          <td class="grid grid-cols-1 md:grid-cols-12 auto-cols-auto">
            <FeedListItem :ref="vFeed.data.id" :view="vFeed"
                          :is-authenticated="isAuthenticated"
                          @item-update="itemUpdate" @item-delete="itemDelete"/>
          </td>
        </tr>
      </template>
      </tbody>
      <tfoot>
      <tr>
        <th v-if="isAuthenticated"></th>
        <th>
          <div>Name</div>
          <div>Link / Categories</div>
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadFeedPage(i-1).subscribe()">
              {{ i }}
            </button>
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
import FeedListHeader from "@/components/feedslist/FeedListHeader.vue";
import FeedListItem from "@/components/feedslist/FeedListItem.vue";
import {FeedView} from "@/components/feedslist/model/FeedView";
import {filter, map, switchMap, take, tap} from "rxjs/operators";
import {Observable} from "rxjs";
import {Feed} from "@/services/model/Feed";
import FeedEditor from "@/components/feedslist/FeedEditor.vue";
import feedsService from "@/services/FeedService";
import userService from "@/services/UserService";
import opmlService from "@/services/opml/OpmlService";
import notificationService from "@/services/notification/NotificationService";
import {defineAsyncComponent} from "vue";
import {AlertResponse, AlertType} from "@/components/shared/alertdialog/AlertDialog.types";
import FeedActions from "@/components/feedslist/FeedActions.vue";

const FileUploadWindow = defineAsyncComponent(() => import('@/components/shared/FileUploadWindow.vue'));

@Options({
  name: 'FeedsList',
  components: {FeedActions, FeedEditor, FeedListItem, FeedListHeader, FileUploadWindow},
})
export default class FeedsList extends Vue {
  private feedEditor!: FeedEditor;
// noinspection JSMismatchedCollectionQueryUpdate
  private feeds: FeedView[] = new Array(0);
  private pagesNumber = 0;
  private activePage = 0;
  private isAuthenticated = false;
  private isFileUploadVisible = false;

  get checkState(): boolean {
    const isOneSelected = this.feeds.find(f => f.isSelected) !== undefined;
    if (this.$refs['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.feeds.find(f => !f.isSelected) !== undefined;
    return isOneSelected;
  }

  mounted(): void {
    userService.get().subscribe({
      next: () => this.isAuthenticated = true,
      error: () => this.isAuthenticated = false,
    })
    this.loadFeedPage(0).subscribe();
    this.feedEditor = this.$refs.feedEditor as FeedEditor;
  }

  loadFeedPage(page: number): Observable<FeedView[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return feedsService.list(resolvedPage).pipe(
        switchMap(page => {
          this.pagesNumber = page.totalPage;
          this.activePage = page.currentPage;
          return page.data;
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
        console.error(e);
        notificationService.pushSimpleError('Impossible d’ajouter le fil !');
      }
    });
  }

  private itemUpdate(item: Feed): void {
    this.feedEditor.openFeed(item).pipe(
        take(1),
        switchMap(feed => feedsService.update(feed)),
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
          const idx = this.feeds.findIndex(fv => fv.data.id === itemId);
          this.feeds.splice(idx, 1);
        })
    ).subscribe({
      next: feed => notificationService.pushSimpleOk(`Feed ${feed.id.substr(0, 10)} deleted successfully !`),
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
        console.debug('feeds: ', ids);
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