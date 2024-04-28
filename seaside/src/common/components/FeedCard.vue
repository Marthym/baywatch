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
  <div v-if="!dense && view.error" :class="{
          'col-span-2': !dense,
          'text-error-content': view.error.level == 'ERROR',
          'text-warning-content': view.error.level == 'WARNING'
       }"
       class="place-self-end">
    <div class="tooltip tooltip-left"
         :class="{'tooltip-error': view.error.level == 'ERROR', 'tooltip-warning': view.error.level == 'WARNING'}"
         :data-tip="view.error.since.toLocaleDateString(currentLocale, formatLocaleOptions) +': '+ view.error.message">
      <ExclamationTriangleIcon class="h-8 w-8"/>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { FeedCardView } from '@/common/model/FeedCardView.type';
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline';

@Component({
  name: 'FeedCard',
  components: { ExclamationTriangleIcon },
  props: ['view', 'dense'],
})
export default class FeedCard extends Vue {
  @Prop() private view!: FeedCardView;
  @Prop({ default: false }) private dense: false;

  private readonly currentLocale = navigator.languages;
  private readonly formatLocaleOptions = {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
  };

  iconFallback(event: ErrorEvent): void {
    (event.target as HTMLImageElement).src = '/favicon.ico';
  }
}
</script>