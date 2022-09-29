<template>
  <ul class="pt-8">
    <li>
      <div class="flex">
        <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd"
                d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0
                018 17v-5.586L3.293 6.707A1 1 0 013 6V3z"
                clip-rule="evenodd"></path>
        </svg>
        <span class="ml-2 capitalize font-medium">filters</span>
      </div>

      <ul class="mt-2 ml-2">
        <li>
          <label class="label cursor-pointer py-1">
            <span class="label-text">unread</span>
            <input type="checkbox" class="toggle"
                   @change="onChangeUnread" :checked="newsStore.unread">
          </label>
        </li>
        <li>
          <label class="label cursor-pointer py-1">
            <span class="label-text">popular</span>
            <input type="checkbox" class="toggle"
                   @change="onChangePopular" :checked="newsStore.popular">
          </label>
        </li>
      </ul>
    </li>
    <li class="mt-2">
      <div class="flex">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
        </svg>
        <span class="ml-2 capitalize font-medium">tags</span>
      </div>

      <ul class="flex flex-wrap list-none mt-4">
        <li v-for="tag in tags" v-bind:key="tag">
          <button class="badge m-1" :class="{'badge-accent': newsStore.tags[0] && tag === newsStore.tags[0]}"
                  @click="selectTag">
            {{ tag }}
          </button>
        </li>
        <li v-if="feedFilter" class="w-full">
          <button class="badge gap-2 m-1 badge-accent rounded whitespace-nowrap"
                  @click="resetFeedFilter()">
            <XMarkIcon class="inline-block w-4 h-4 stroke-2"/>
            {{ feedFilter.label }}
          </button>
        </li>
      </ul>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import tagsService from "@/techwatch/services/TagsService";
import reloadActionService from '@/common/services/ReloadActionService';
import {setup} from "vue-class-component";
import {useRouter} from "vue-router";
import {useStore} from "vuex";
import {
  FeedFilter,
  NEWS_REPLACE_TAGS_MUTATION,
  NEWS_RESET_FILTERS_MUTATION,
  NEWS_TOGGLE_POPULAR_MUTATION,
  NEWS_TOGGLE_UNREAD_MUTATION,
  NewsStore
} from "@/common/model/store/NewsStore.type";
import {XMarkIcon} from "@heroicons/vue/24/outline";

@Options({name: 'SideNavFilters', components: {XMarkIcon}})
export default class SideNavFilters extends Vue {
  private router = setup(() => useRouter());
  private store = setup(() => useStore());
  private newsStore: NewsStore = setup(() => useStore().state.news);
  private tags: string[] = [];

  mounted(): void {
    tagsService.list().subscribe({
      next: tags => {
        this.tags = tags;
      }
    });
  }

  get feedFilter(): FeedFilter | undefined {
    return this.newsStore.feed;
  }

  selectTag(event: MouseEvent): void {
    const currentTags = this.newsStore.tags;
    const selected = (event.target as HTMLElement).innerText;
    if (currentTags.indexOf(selected) >= 0) {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, []);
      this.router.replace({path: this.router.currentRoute.path});
      reloadActionService.reload('news');
    } else if (this.tags.indexOf(selected) >= 0) {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, [selected]);
      this.router.replace({path: this.router.currentRoute.path, query: {tag: selected}});
      reloadActionService.reload('news');
    }
  }

  private resetFeedFilter(): void {
    this.store.commit(NEWS_RESET_FILTERS_MUTATION, 'feed');
    reloadActionService.reload('news');
  }

  private onChangeUnread(): void {
    this.store.commit(NEWS_TOGGLE_UNREAD_MUTATION);
    reloadActionService.reload('news');
  }

  private onChangePopular(): void {
    this.store.commit(NEWS_TOGGLE_POPULAR_MUTATION);
    reloadActionService.reload('news');
  }
}
</script>