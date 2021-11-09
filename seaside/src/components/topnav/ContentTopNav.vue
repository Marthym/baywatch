<template>
  <header class="navbar min-h-0 bg-neutral text-neutral-content">
    <button @click="$emit('toggleSidenav')" class="btn btn-square btn-sm btn-ghost -mr-2 lg:hidden">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
      </svg>
    </button>
    <div class="navbar-start border-b border-gray-600 ml-8 h-full">
      <router-link to="/news"
                   active-class="border-b-2 text-green-500 border-green-500 dark:text-green-200 dark:border-green-200"
                   class="py-2 block font-medium capitalize focus:outline-none
                          transition duration-500 ease-in-out">
        news
      </router-link>
      <router-link to="/feeds"
                   active-class="border-b-2 text-green-500 border-green-500 dark:text-green-200 dark:border-green-200"
                   class="ml-6 py-2 block focus:outline-none font-medium capitalize text-center
                          transition duration-500 ease-in-out">
        feeds
      </router-link>
      <router-link to="/categories"
                   active-class="border-b-2 text-green-500 border-green-500 dark:text-green-200 dark:border-green-200"
                   class="ml-6 py-2 block focus:outline-none font-medium capitalize text-center
                          transition duration-500 ease-in-out">
        categories
      </router-link>
    </div>
    <div class="navbar-end border-b border-gray-600 pr-2 mr-2">
      <input type="text" placeholder="Search" class="input input-ghost input-sm">
      <button class="btn btn-square btn-ghost btn-sm">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
             class="inline-block w-6 h-6 stroke-current">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
        </svg>
      </button>
    </div>
  </header>

</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component
export default class ContentTopNav extends Vue {
}
</script>