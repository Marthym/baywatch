<template>
  <div class="mt-2 flex justify-center text-sm text-primary-content">
    <span><span class="text-accent">{{ statistics.feeds }}</span> feeds •&nbsp;</span>
    <span v-if="isLoggedIn">
      <span class="text-accent">{{ statistics.unread }}</span> unread</span>
    <span v-else><span class="text-accent">{{ statistics.news }}</span> news</span>
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