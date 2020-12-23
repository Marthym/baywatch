<template>
  <nav class="flex flex-col bg-gray-200 dark:bg-gray-900 w-64 px-12 pt-4 pb-6">

    <SideNavHeader/>

    <SideNavUserInfo/>

    <button
        class="mt-8 flex items-center justify-between py-3 px-2 text-white
			dark:text-gray-200 bg-green-400 dark:bg-green-500 rounded-lg shadow">
      <!-- Action -->
      <span>Add user</span>
      <svg class="h-5 w-5 fill-current" viewBox="0 0 24 24">
        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
      </svg>
    </button>

    <SideNavFeeds/>

    <SideNavImportantActions/>
  </nav>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavUserInfo from "./SideNavUserInfo.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavFeeds from "./SideNavFeeds.vue";

@Component({
  components: {
    SideNavHeader,
    SideNavUserInfo,
    SideNavFeeds,
    SideNavImportantActions,
  },
})
export default class SideNav extends Vue {
}
</script>
