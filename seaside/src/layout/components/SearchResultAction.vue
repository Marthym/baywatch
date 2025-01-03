<template>
  <ul class="bg-base-100 py-2 lg:rounded-lg">
    <li class="flex justify-between items-center px-2 text-xs font-semibold opacity-60 py-2 mb-2 border-b-2 border-b-neutral-content">
      <span class="capitalize">{{ t('topnav.feed') }}</span>
      <button class="btn btn-sm btn-square btn-ghost">
        <XMarkIcon class="h-6 w-6"/>
      </button>
    </li>
    <li v-for="e in entries" class="grid grid-cols-10 px-2 hover:bg-neutral">
      <FeedCard :dense="true" :view="{...toFeed(e), icon: toIcon(e)}"/>
      <div class="btn-group justify-self-end place-self-center col-span-3">
        <button class="btn btn-sm btn-square btn-ghost" @click.stop="displayFeed(e.id, e.name)">
          <EyeIcon class="h-6 w-6"/>
        </button>
        <button v-if="e._createdBy == undefined" class="btn btn-sm btn-square btn-ghost"
                @click.stop="subscribeFeed(e.id)">
          <PlusCircleIcon class="h-6 w-6"/>
        </button>
      </div>
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { SearchEntry } from '@/layout/model/SearchResult.type';
import FeedCard from '@/common/components/FeedCard.vue';
import { FeedCardView } from '@/common/model/FeedCardView.type';
import { EyeIcon, PlusCircleIcon, XMarkIcon } from '@heroicons/vue/24/outline';
import feedsService from '@/configuration/services/FeedService';
import notificationService from '@/services/notification/NotificationService';
import { NEWS_FILTER_FEED_MUTATION } from '@/common/model/store/NewsStore.type';
import { useStore } from 'vuex';
import { actionServiceReload } from '@/common/services/ReloadActionService';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'SearchResultAction',
  components: { FeedCard, PlusCircleIcon, EyeIcon, XMarkIcon },
  emits: ['close'],
  props: ['entries'],
  setup() {
    const { t } = useI18n();
    return {
      store: useStore(),
      t: t,
    };
  },
})
export default class SearchResultAction extends Vue {
  @Prop() private entries!: SearchEntry[];
  private store;
  private t;

  // noinspection JSUnusedLocalSymbols
  private toFeed(searchEntry: SearchEntry): FeedCardView {
    return {
      _id: searchEntry.id,
      name: searchEntry.name,
      description: searchEntry.name,
      location: searchEntry.url,
      tags: [] as string[],
    };
  }

  // noinspection JSMethodCanBeStatic
  private toIcon(entry: SearchEntry): string {
    return new URL(entry.url).origin + '/favicon.ico';
  }

  private subscribeFeed(feedId: string): void {
    feedsService.subscribe(feedId).subscribe({
      next: f => notificationService.pushSimpleOk(`Subscribe ${f.name} successfully !`),
      error: err => notificationService.pushSimpleError(`Fail to subscribe ${err}`),
    });
  }

  private displayFeed(feedId: string, label: string): void {
    this.store.commit(NEWS_FILTER_FEED_MUTATION, { id: feedId, label: label });
    actionServiceReload('news');
    this.$emit('close');
  }
}
</script>
