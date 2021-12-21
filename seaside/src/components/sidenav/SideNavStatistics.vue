<template>
  <div class="mt-2 flex justify-center text-sm text-gray-600 dark:text-gray-300 border-t dark:border-gray-600">
    <span><span class="text-green-600 dark:text-green-300">{{ statistics.feeds }}</span> feeds •&nbsp;</span>
    <span v-if="isLoggedIn">
      <span class="text-green-600 dark:text-green-300">{{ statistics.unread }}</span> unread</span>
    <span v-else><span class="text-green-600 dark:text-green-300">{{ statistics.news }}</span> news</span>
  </div>
</template>
<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import {Statistics} from "@/services/model/Statistics";

@Options({name: 'SideNavStatistics'})
export default class SideNavStatistics extends Vue {
  @Prop() private statistics!: Statistics;
  @Prop() private isLoggedIn!: boolean;
}
</script>