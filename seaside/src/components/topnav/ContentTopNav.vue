<template>
  <header class="navbar min-h-0 bg-neutral text-neutral-content">
    <button @click="toggleSidenav" class="btn btn-square btn-sm btn-ghost -mr-2 lg:hidden">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7"/>
      </svg>
    </button>
    <div class="navbar-start border-b border-base-100 ml-8 h-full">
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
      <router-link to="/feeds"
                   active-class="border-b-2 text-accent border-accent"
                   class="ml-6 py-2 block focus:outline-none font-medium capitalize text-center transition duration-500 ease-in-out">
        <svg class="w-8 h-8 sm:w-6 sm:h-6 inline-block -mt-2" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path d="M5 3a1 1 0 000 2c5.523 0 10 4.477 10 10a1 1 0 102 0C17 8.373 11.627 3 5 3z"></path>
          <path
              d="M4 9a1 1 0 011-1 7 7 0 017 7 1 1 0 11-2 0 5 5 0 00-5-5 1 1 0 01-1-1zM3 15a2 2 0 114 0 2 2 0 01-4 0z"></path>
        </svg>
        <span class="hidden sm:inline-block">feeds</span>
      </router-link>
      <div class="divider divider-horizontal hidden sm:flex"></div>
      <div v-if="isAuthenticated && statistics.unread_filtered > 0" class="text-sm hidden sm:inline-block">
        <svg class="h-5 w-5 cursor-pointer inline-block  -mt-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
             stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg> {{ statistics.unread_filtered }}
      </div>
    </div>
    <div class="navbar-end w-full md:w-1/2 border-b border-base-100 pr-2 mr-2 h-full">
      <div class="indicator mr-3">
        <div v-if="statistics.updated > 0" class="indicator-item badge badge-xs badge-accent text-2xs">
          {{ statistics.updated }}
        </div>
        <button class="btn btn-square btn-ghost btn-sm" @click="reload()">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
        </button>
      </div>
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

</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {SidenavMutation} from "@/store/sidenav/SidenavMutation.enum";
import {StatisticsState} from "@/store/statistics/statistics";
import newsService from '@/services/NewsService';
import {setup} from "vue-class-component";
import {Store, useStore} from "vuex";
import {RESET_UPDATED_MUTATION} from "@/store/statistics/StatisticsConstants";

@Options({name: 'ContentTopNav'})
export default class ContentTopNav extends Vue {

  private statistics: StatisticsState = setup(() => useStore().state.statistics);
  private store:Store<any> = setup(() => useStore());

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
}
</script>