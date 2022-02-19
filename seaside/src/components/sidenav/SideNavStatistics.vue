<template>
  <div class="h-24 text-center">
    <div class="mt-2 flex justify-center text-sm text-primary-content">
      <span><span class="text-accent">{{ statistics.feeds }}</span> feeds •&nbsp;</span>
      <span v-if="isLoggedIn">
      <span class="text-accent">{{ statistics.unread }}</span> unread<br>
    </span>
      <span v-else><span class="text-accent">{{ statistics.news }}</span> news</span>
    </div>
    <div v-if="isLoggedIn && statistics.unread !== statistics.unread_filtered"
         class="text-sm text-primary-content mx-auto">
      <span class="text-accent">{{ statistics.unread_filtered }}</span> in view
    </div>
  </div>
</template>
<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import {StatisticsState} from "@/store/statistics/statistics";

@Options({name: 'SideNavStatistics'})
export default class SideNavStatistics extends Vue {
  @Prop() private statistics!: StatisticsState;
  @Prop() private isLoggedIn!: boolean;
}
</script>