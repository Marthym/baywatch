<template>
  <header class="navbar min-h-0 bg-neutral text-neutral-content">
    <button aria-label="Open Side Menu" class="btn btn-square btn-sm btn-ghost -mr-2 lg:hidden"
            @click="toggleSidenav">
      <ChevronDoubleRightIcon class="h-6 w-6"/>
    </button>
    <!-- The left side of top bar -->
    <div class="navbar-start border-b border-base-content/20 ml-8 h-full">
      <!-- The NEWS Tab-->
      <router-link :to="newsToLink"
                   active-class="border-b-2 text-accent border-accent"
                   aria-label="display news list"
                   class="py-2 font-medium capitalize focus:outline-none transition duration-500 ease-in-out">
        <NewspaperIcon class="w-8 h-8 sm:w-6 sm:h-6 inline-block -mt-2"/>
        <span class="hidden sm:inline-block capitalize">{{ t('topnav.news') }}</span>
      </router-link>
      <router-link v-if="isAuthenticated" active-class="border-b-2 text-accent border-accent"
                   aria-label="display news list"
                   class="py-2 ml-2 font-medium capitalize focus:outline-none transition duration-500 ease-in-out"
                   to="/clipped">
        <PaperClipIcon class="w-8 h-8 sm:w-6 sm:h-6 inline-block -mt-2"/>
        <span class="hidden sm:inline-block capitalize">{{ t('topnav.clipped') }}</span>
      </router-link>
      <!-- Vertical separator -->
      <div class="divider divider-horizontal hidden sm:flex"></div>
      <!-- Unread News counter -->
      <div v-if="isAuthenticated && statistics.unread_filtered > 0" class="text-sm hidden sm:inline-block">
        <EnvelopeIcon class="h-5 w-5 inline-block -mt-1"/>
        {{ statistics.unread_filtered }}
      </div>
    </div>

    <!-- The RIGHT side of top bar -->
    <div v-if="isAuthenticated" class="navbar-end w-full md:w-1/2 border-b border-base-content/20 pr-2 mr-2 h-full">
      <!-- Refresh Icon -->
      <div class="indicator mx-1">
                <span v-if="statistics.updated > 0"
                      class="indicator-item badge badge-xs badge-accent text-2xs animate-pulse"></span>
        <button class="btn btn-square btn-ghost btn-sm" @click="reload()">
          <ArrowPathIcon class="h-6 w-6"/>
        </button>
      </div>
      <!-- Clip icon -->
      <div class="text-sm inline-block mx-1 mr-3">
        <button class="btn btn-square btn-ghost btn-sm" @click="toggleClipAction">
          <SquaresPlusIcon class="w-6 h-6"/>
        </button>
      </div>
      <!-- Right Search Bar -->
      <div class="relative">
        <input v-model="searchQuery" :placeholder="t('topnav.search')"
               class="w-full pr-16 input input-sm input-ghost input-bordered placeholder:capitalize"
               type="text"
               @keydown.enter.stop="onSearchClick"
               @keydown.esc.stop="exitOverlay">
        <button class="absolute top-0 right-0 rounded-l-none btn btn-ghost btn-sm"
                @click.prevent="onSearchClick">
          <MagnifyingGlassIcon class="inline-block w-6 h-6 stroke-current"/>
        </button>
      </div>
    </div>
  </header>
  <TopNavActionOverlay v-show="actionOverlayOpen" :is-open="actionOverlayOpen" @close="actionOverlayOpen = false">
    <template v-slot:content="slotProps">
      <component :is="actionOverlayContent"
                 v-bind="{...actionOverlayProps, ...slotProps}"
                 @close="actionOverlayOpen = false">
      </component>
    </template>
  </TopNavActionOverlay>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { SidenavMutation } from '@/store/sidenav/SidenavMutation.enum';
import { StatisticsState } from '@/techwatch/store/statistics/statistics';
import { actionServiceReload } from '@/common/services/ReloadActionService';
import searchService from '@/layout/services/SearchService';
import { Store, useStore } from 'vuex';
import { RESET_UPDATED_MUTATION } from '@/techwatch/store/statistics/StatisticsConstants';
import TopNavActionOverlay from '@/layout/components/TopNavActionOverlay.vue';
import { defineAsyncComponent } from 'vue';
import {
  ArrowPathIcon,
  ChevronDoubleRightIcon,
  EnvelopeIcon,
  MagnifyingGlassIcon,
  PaperClipIcon,
  SquaresPlusIcon,
} from '@heroicons/vue/24/outline';
import { NewspaperIcon } from '@heroicons/vue/24/solid';
import { LocationQueryRaw, RouteLocationRaw } from 'vue-router';
import { useI18n } from 'vue-i18n';

const AddSingleNewsAction = defineAsyncComponent(() => import('@/layout/components/AddSingleNewsAction.vue'));
const SearchResultAction = defineAsyncComponent(() => import('@/layout/components/SearchResultAction.vue'));

type ActionOverlayComponent = 'AddSingleNewsAction' | 'SearchResultAction';

@Component({
  name: 'TopNavigationBar',
  components: {
    TopNavActionOverlay,
    AddSingleNewsAction,
    SearchResultAction,
    ChevronDoubleRightIcon,
    NewspaperIcon,
    EnvelopeIcon,
    ArrowPathIcon,
    PaperClipIcon,
    MagnifyingGlassIcon,
    SquaresPlusIcon,
  },
  setup() {
    const store = useStore();
    const { t } = useI18n();
    return {
      store: store,
      statistics: store.state.statistics,
      t: t,
    };
  },
})
export default class TopNavigationBar extends Vue {

  private statistics: StatisticsState;
  private store: Store<any>;
  private t: unknown;

  private actionOverlayOpen = false;
  private actionOverlayContent: ActionOverlayComponent = 'AddSingleNewsAction';
  private actionOverlayProps: unknown = {};
  private searchQuery = '';

  get isAuthenticated(): boolean {
    return this.store.state.user.isAuthenticated || false;
  }

  get newsToLink(): RouteLocationRaw {
    const filtersQuery: LocationQueryRaw = {};
    if (this.store.state.news.tags.length > 0) {
      filtersQuery.tag = this.store.state.news.tags;
    }
    return { name: 'HomePage', query: filtersQuery };
  }

  private toggleSidenav(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }

  private reload(): void {
    this.store.commit(RESET_UPDATED_MUTATION);
    actionServiceReload();
  }

  private toggleClipAction(): void {
    this.actionOverlayContent = 'AddSingleNewsAction';
    this.actionOverlayOpen = !this.actionOverlayOpen;
  }

  private onSearchClick(): void {
    this.actionOverlayContent = 'SearchResultAction';
    searchService.search(this.searchQuery).subscribe({
      next: r => {
        this.actionOverlayProps.entries = r;
        this.actionOverlayOpen = true;
      },
    });
  }

  private exitOverlay(): void {
    this.actionOverlayProps = {};
    this.actionOverlayOpen = false;
  }
}
</script>
