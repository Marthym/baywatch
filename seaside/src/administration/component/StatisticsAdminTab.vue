<template>
  <div class="flex justify-center flex-wrap">
    <div v-for="counter in counters" class="stat bg-secondary-content rounded-xl shadow m-2 grow w-fit">
      <div v-if="counter.icon" class="stat-figure text-secondary">
        <component :is="counter.icon" class="inline-block w-10 h-10" />
      </div>
      <div class="stat-title">{{ counter.name }}</div>
      <div class="stat-value">{{ counter.value }}</div>
      <div class="stat-desc">{{ counter.description }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

import statisticsService from '@/administration/services/StatisticsService';
import {Counter} from "@/administration/model/Counter.type";
import {ClockIcon, CloudUploadIcon, UserGroupIcon, NewspaperIcon, RssIcon} from '@heroicons/vue/solid';

@Options({
  name: 'StatisticsAdminTab',
  components: {
    ClockIcon, CloudUploadIcon, UserGroupIcon, NewspaperIcon, RssIcon
  },
})
export default class StatisticsAdminTab extends Vue {

  private counters: Counter[] = [];

  public mounted(): void {
    statisticsService.get().subscribe({
      next: cs => {
        this.counters = [...cs];
        this.counters.forEach(c => {
          if (!isNaN(Date.parse(c.description))) {
            c.description = new Date(c.description).toLocaleDateString(navigator.languages, {
              timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
              year: 'numeric', month: 'short', day: '2-digit', hour: "2-digit", minute: "2-digit"
            });
          }
        })
      }
    })
  }
}
</script>
