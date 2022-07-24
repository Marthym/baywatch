<template>
  <ul class="menu bg-base-100 p-2 lg:rounded-lg">
    <li class="menu-title">
      <span>Feed</span>
    </li>
    <li v-for="e in entries">
      <a>{{ e.name }}</a>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {SearchEntry} from "@/layout/model/SearchResult.type";

@Options({
  name: 'SearchResultAction',
  emits: ['close'],
  props: ['entries']
})
export default class SearchResultAction extends Vue {

  @Prop() private entries!: SearchEntry[];

}
</script>
