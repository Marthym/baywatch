<template>
  <div class="flex items-center space-x-3" :class="{'col-span-7': dense, 'col-span-3': !dense}">
    <div class="avatar placeholder">
      <div class="w-8 h-8 mask mask-squircle">
        <img class="text-center"
             :src="view.icon" :alt="view.name.substring(0,1)"
             @error.prevent.stop="iconFallback">
      </div>
    </div>
    <div>
      <div class="font-bold text-ellipsis">
        {{ view.name }}
      </div>
      <div class="text-sm opacity-50">
        {{ view._id.substring(0, 10) }}
      </div>
    </div>
  </div>
  <div v-if="!dense" :class="{'col-span-7': !dense}">
    <a class="link whitespace-normal">{{ view.location }}</a><br>
    <div v-for="tag in view.tags" class="badge mr-1 rounded">{{ tag }}</div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';

@Component({
  name: 'FeedCard',
  props: ['view', 'dense'],
})
export default class FeedCard extends Vue {
  @Prop() private view!: FeedCardView;
  @Prop({ default: false }) private dense: false;

  iconFallback(event: ErrorEvent): void {
    (event.target as HTMLImageElement).src = '/favicon.ico';
  }
}

export type FeedCardView = {
  _id: string;
  icon: string;
  name: string;
  location: string;
  tags: string[];
}
</script>