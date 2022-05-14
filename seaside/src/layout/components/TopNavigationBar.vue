<template>
  <header class="navbar min-h-0 bg-neutral text-neutral-content">
    <button @click="toggleSidenav" class="btn btn-square btn-sm btn-ghost -mr-2 lg:hidden">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7"/>
      </svg>
    </button>
    <!-- The left side of top bar -->
    <div class="navbar-start border-b border-base-100 ml-8 h-full">
      <!-- The NEWS Tab-->
      <router-link to="/news"
                   active-class="border-b-2 text-accent border-accent"
                   class="py-2 font-medium capitalize focus:outline-none transition duration-500 ease-in-out">
        <svg class="w-8 h-8 sm:w-6 sm:h-6 inline-block -mt-2" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd"
                d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z"
                clip-rule="evenodd"></path>
          <path d="M15 7h1a2 2 0 012 2v5.5a1.5 1.5 0 01-3 0V7z"></path>
        </svg>
        <span class="hidden sm:inline-block">news</span>
      </router-link>
      <!-- Vertical separator -->
      <div class="divider divider-horizontal hidden sm:flex"></div>
      <!-- Unread News counter -->
      <div v-if="isAuthenticated && statistics.unread_filtered > 0" class="text-sm hidden sm:inline-block">
        <svg class="h-5 w-5 cursor-pointer inline-block  -mt-1" xmlns="http://www.w3.org/2000/svg" fill="none"
             viewBox="0 0 24 24"
             stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
        {{ statistics.unread_filtered }}
      </div>
    </div>

    <!-- The RIGHT side of top bar -->
    <div class="navbar-end w-full md:w-1/2 border-b border-base-100 pr-2 mr-2 h-full">
      <!-- Refresh Icon -->
      <div class="indicator mx-1">
        <div v-if="statistics.updated > 0" class="indicator-item badge badge-xs badge-accent text-2xs">
          {{ statistics.updated }}
        </div>
        <button class="btn btn-square btn-ghost btn-sm" @click="reload()">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0
                  01-15.357-2m15.357 2H15"/>
          </svg>
        </button>
      </div>
      <!-- Clip icon -->
      <div v-if="isAuthenticated" class="text-sm inline-block mx-1 mr-3">
        <button class="btn btn-square btn-ghost btn-sm" @click="toggleClipAction">
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path fill-rule="evenodd"
                  d="M8 4a3 3 0 00-3 3v4a5 5 0 0010 0V7a1 1 0 112 0v4a7 7 0 11-14 0V7a5 5 0 0110 0v4a3 3 0 11-6 0V7a1
                  1 0 012 0v4a1 1 0 102 0V7a3 3 0 00-3-3z"
                  clip-rule="evenodd"></path>
          </svg>
        </button>
      </div>
      <!-- Right Search Bar -->
      <div class="relative">
        <input type="text" placeholder="Search" class="w-full pr-16 input input-sm input-ghost input-bordered">
        <button class="absolute top-0 right-0 rounded-l-none btn btn-ghost btn-sm">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
               class="inline-block w-6 h-6 stroke-current">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
        </button>
      </div>
    </div>
  </header>
  <transition
      enter-active-class="lg:duration-200 ease-out"
      enter-from-class="lg:transform lg:translate-x-80"
      enter-to-class="lg:translate-x-0"
      leave-active-class="lg:duration-200 ease-in"
      leave-from-class="lg:translate-x-0"
      leave-to-class="lg:transform lg:translate-x-80">
    <TopNavActionOverlay v-if="actionOverlayOpen">
      <template v-slot:content>
        <component :is="actionOverlayContent" @close="actionOverlayOpen = false"></component>
      </template>
    </TopNavActionOverlay>
  </transition>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {SidenavMutation} from "@/store/sidenav/SidenavMutation.enum";
import {StatisticsState} from "@/techwatch/store/statistics/statistics";
import newsService from '@/techwatch/services/NewsService';
import {setup} from "vue-class-component";
import {Store, useStore} from "vuex";
import {RESET_UPDATED_MUTATION} from "@/techwatch/store/statistics/StatisticsConstants";
import TopNavActionOverlay from "@/layout/components/TopNavActionOverlay.vue";
import {defineAsyncComponent} from "vue";

const AddSingleNewsAction = defineAsyncComponent(() => import('@/layout/components/AddSingleNewsAction.vue'));

type ActionOverlayComponent = 'AddSingleNewsAction';

@Options({
  name: 'TopNavigationBar',
  components: {
    TopNavActionOverlay,
    AddSingleNewsAction,
  }
})
export default class TopNavigationBar extends Vue {

  private statistics: StatisticsState = setup(() => useStore().state.statistics);
  private store: Store<any> = setup(() => useStore());

  private actionOverlayOpen = false;
  private actionOverlayContent: ActionOverlayComponent = 'AddSingleNewsAction';

  get isAuthenticated(): boolean {
    return this.store.state.user.isAuthenticated || false;
  }

  private toggleSidenav(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }

  private reload(): void {
    this.store.commit(RESET_UPDATED_MUTATION);
    newsService.reload();
  }

  private toggleClipAction(): void {
    this.actionOverlayOpen = !this.actionOverlayOpen;
  }
}
</script>