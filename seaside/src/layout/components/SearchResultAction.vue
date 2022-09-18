<template>
  <ul class="bg-base-100 py-2 lg:rounded-lg">
    <li class="px-2 text-xs font-semibold opacity-60 py-2 mb-2 border-b-2 border-b-neutral-content">
      <span>Feed</span>
    </li>
    <li v-for="e in entries" class="grid grid-cols-10 px-2 hover:bg-neutral">
      <FeedCard :view="{...e, icon: toIcon(e)}" :dense="true"/>
      <div class="btn-group justify-self-end content-center col-span-3">
        <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-update', view.data)">
          <EyeIcon class="h-6 w-6"/>
        </button>
        <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-delete', view.data.id)">
          <PlusCircleIcon class="h-6 w-6"/>
        </button>
      </div>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {SearchEntry} from "@/layout/model/SearchResult.type";
import FeedCard from "@/common/components/FeedCard.vue";
import {EyeIcon, PlusCircleIcon} from '@heroicons/vue/24/outline'

@Options({
  name: 'SearchResultAction',
  components: {FeedCard, PlusCircleIcon, EyeIcon},
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
