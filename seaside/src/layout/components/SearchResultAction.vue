<template>
  <ul class="bg-base-100 p-2 lg:rounded-lg">
    <li class="text-xs font-semibold opacity-60 py-2 mb-2 border-b-2 border-b-neutral-content">
      <span>Feed</span>
    </li>
    <li v-for="e in entries" class="grid grid-cols-1">
      <FeedCard :view="{...e, icon: toIcon(e)}" :dense="true"/>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {SearchEntry} from "@/layout/model/SearchResult.type";
import FeedCard from "@/common/components/FeedCard.vue";

@Options({
  name: 'SearchResultAction',
  components: {FeedCard},
  emits: ['close'],
  props: ['entries']
})
export default class SearchResultAction extends Vue {
  @Prop() private entries!: SearchEntry[];

  // noinspection JSMethodCanBeStatic
  private toIcon(entry: SearchEntry): string {
    return new URL(entry.url).origin + '/favicon.ico';
  }
}
</script>
