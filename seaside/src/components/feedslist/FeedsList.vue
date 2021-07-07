<template>
  <div class="overflow-x-auto mt-5 pr-5">
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
      <template v-for="vFeed in this.feeds">
        <FeedListItem :ref="vFeed.data.id" :view="vFeed" v-bind:key="vFeed.data.id"
                      :is-authenticated="isAuthenticated" />
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
  </div>
</template>
<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import FeedListHeader from "@/components/feedslist/FeedListHeader.vue";
import FeedListItem from "@/components/feedslist/FeedListItem.vue";
import {FeedView} from "@/components/feedslist/model/FeedView";
import feedsService from "@/services/FeedService";
import {map, switchMap, tap} from "rxjs/operators";
import {Observable} from "rxjs";
import {Feed} from "@/services/model/Feed";
import userService from "@/services/UserService";

@Component({
  components: {FeedListItem, FeedListHeader},
})
export default class FeedsList extends Vue {
// noinspection JSMismatchedCollectionQueryUpdate
  private feeds: FeedView[] = new Array(0);
  private pagesNumber = 0;
  private activePage = 0;

  mounted(): void {
    this.loadFeedPage(0).subscribe();
  }

  get isAuthenticated(): boolean {
    return userService.get() !== undefined;
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
    console.log(new URL(feed.url).origin + '/favicon.ico')
    return {
      icon: new URL(feed.url).origin + '/favicon.ico',
      data: feed,
      isSelected: false
    } as FeedView
  }
}
</script>