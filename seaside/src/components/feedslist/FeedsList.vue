<template>
  <div class="overflow-x-auto mt-5">
    <table class="table w-full">
      <thead>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox">
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <template v-for="vFeed in feeds">
        <FeedListItem :ref="vFeed.data.id" :view="vFeed" v-bind:key="vFeed.data.id"/>
      </template>
      </tbody>
      <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
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
import {map, tap} from "rxjs/operators";

@Component({
  components: {FeedListItem, FeedListHeader},
})
export default class FeedsList extends Vue {
  private feeds: FeedView[] = new Array(0);

  mounted(): void {
    feedsService.list(-1).pipe(
        map(fs => fs.map(f => ({data: f, feeds: [], isSelected: false}) as FeedView)),
        tap(fs => this.feeds.push(...fs))
    ).subscribe();
  }
}
</script>