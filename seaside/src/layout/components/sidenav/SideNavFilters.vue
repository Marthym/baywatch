<template>
    <ul class="pt-8">
        <li>
            <div class="flex">
                <FunnelIcon class="w-6 h-6"/>
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
                <TagIcon class="h-6 w-6"/>
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
import {Component, Vue} from 'vue-facing-decorator';
import tagsService from "@/techwatch/services/TagsService";
import reloadActionService from '@/common/services/ReloadActionService';
import {Router, useRouter} from "vue-router";
import {useStore} from "vuex";
import {
    FeedFilter,
    NEWS_REPLACE_TAGS_MUTATION,
    NEWS_RESET_FILTERS_MUTATION,
    NEWS_TOGGLE_POPULAR_MUTATION,
    NEWS_TOGGLE_UNREAD_MUTATION,
    NewsStore
} from "@/common/model/store/NewsStore.type";
import {FunnelIcon, TagIcon, XMarkIcon} from "@heroicons/vue/24/outline";

@Component({
    name: 'SideNavFilters',
    components: {FunnelIcon, TagIcon, XMarkIcon},
    setup() {
        const store = useStore();
        return {
            router: useRouter(),
            store: store,
            newsStore: store.state.news,
        }
    }
})
export default class SideNavFilters extends Vue {
    private router: Router;
    private store;
    private newsStore: NewsStore;
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
            this.router.replace({path: this.router.currentRoute.value.path});
            reloadActionService.reload('news');
        } else if (this.tags.indexOf(selected) >= 0) {
            this.store.commit(NEWS_REPLACE_TAGS_MUTATION, [selected]);
            this.router.replace({path: this.router.currentRoute.value.path, query: {tag: selected}});
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