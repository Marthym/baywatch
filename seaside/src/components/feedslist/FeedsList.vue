<template>
  <div class="overflow-x-auto mt-5 pr-5">
    <div class="btn-group mb-2" v-if="isAuthenticated">
      <button class="btn btn-primary" @click="addNewFeed">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        Ajouter
      </button>
      <button class="btn btn-primary" @click="this.isFileUploadVisible = true">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
        </svg>
        Importer
      </button>
      <a class="btn btn-primary" :href="`${BASEURL}/opml/export/baywatch.opml`">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
        </svg>
        Exporter
      </a>
    </div>
    <table class="table w-full">
      <thead>
      <tr>
        <th v-if="isAuthenticated">
          <label>
            <input type="checkbox" class="checkbox">
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th>Name</th>
        <th>Link / Categories</th>
        <th colspan="2" v-if="isAuthenticated">
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
        <FeedListItem :ref="vFeed.data.id" :view="vFeed"
                      :is-authenticated="isAuthenticated"
                      @item-update="itemUpdate" @item-delete="itemDelete"/>
      </template>
      </tbody>
      <tfoot>
      <tr>
        <th v-if="isAuthenticated"></th>
        <th>Name</th>
        <th>Link / Categories</th>
        <th colspan="2" v-if="isAuthenticated">
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

const FileUploadWindow = defineAsyncComponent(() => import('@/components/shared/FileUploadWindow.vue'));

@Options({
  name: 'FeedsList',
  components: {FeedEditor, FeedListItem, FeedListHeader, FileUploadWindow},
})
export default class FeedsList extends Vue {
  private readonly BASEURL = import.meta.env.VITE_API_BASE_URL;
  private feedEditor!: FeedEditor;
// noinspection JSMismatchedCollectionQueryUpdate
  private feeds: FeedView[] = new Array(0);
  private pagesNumber = 0;
  private activePage = 0;
  private isAuthenticated = false;
  private isFileUploadVisible = false;

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