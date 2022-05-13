<template>
  <div class="flex items-center space-x-3 md:col-span-3">
    <div class="avatar placeholder">
      <div class="w-8 h-8 mask mask-squircle">
        <img class="text-center"
             :src="view.icon" :alt="view.data.name.substring(0,1)"
             @error.prevent.stop="iconFallback">
      </div>
    </div>
    <div>
      <div class="font-bold">
        {{ view.data.name }}
      </div>
      <div class="text-sm opacity-50">
        {{ view.data.id.substring(0, 10) }}
      </div>
    </div>
  </div>
  <div class="md:col-span-7">
    <a class="link whitespace-normal">{{ view.data.url }}</a><br>
    <div v-for="tag in view.data.tags" class="badge mr-1 rounded">{{ tag }}</div>
  </div>
  <div class="md:col-span-2 btn-group justify-self-end">
    <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-update', view.data)">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
      </svg>
    </button>
    <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-delete', view.data.id)">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
      </svg>
    </button>
  </div>
</template>

<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import {FeedView} from "@/configuration/components/feedslist/model/FeedView";

@Options({name: 'FeedsListItem', emits: ['item-update', 'item-delete']})
export default class FeedsListItem extends Vue {
  @Prop() private view!: FeedView;

  iconFallback(event: ErrorEvent): void {
    (event.target as HTMLImageElement).src = '/favicon.ico'
  }
}
</script>
