<template>
  <div class="flex justify-center flex-wrap">
    <div v-for="counter in counters" class="stat bg-secondary-content rounded-xl shadow m-2 grow w-fit">
      <div v-if="counter.icon" class="stat-figure text-secondary">
        <component :is="iconToComponent(counter.icon)" class="inline-block w-10 h-10"/>
      </div>
      <div class="stat-title">{{ counter.name }}</div>
      <div class="stat-value">{{ counter.value }}</div>
      <div class="stat-desc">{{ counter.description }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

import {get as getStatistics} from '@/administration/services/StatisticsService';
import reloadActionService from "@/common/services/ReloadActionService";
import {Counter} from "@/administration/model/Counter.type";
import {
  ClockIcon,
  CloudArrowUpIcon,
  NewspaperIcon,
  QuestionMarkCircleIcon,
  RssIcon,
  UserGroupIcon
} from '@heroicons/vue/24/outline';
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Options({
  name: 'StatisticsAdminTab',
  components: {
    ClockIcon, CloudArrowUpIcon, UserGroupIcon, NewspaperIcon, RssIcon, QuestionMarkCircleIcon,
  },
})
export default class StatisticsAdminTab extends Vue {

  private counters: Counter[] = [];

  public mounted(): void {
    this.loadStatistics().subscribe({
      next: () => reloadActionService.registerReloadFunction(context => {
        if (context === '' || context === 'stats') {
          this.loadStatistics().subscribe();
        }
      }),
    })
  }

  private loadStatistics(): Observable<void> {
    return getStatistics().pipe(
        map(cs => {
          this.counters = [...cs];
          this.counters.forEach(c => {
            if (!isNaN(Date.parse(c.description))) {
              c.description = new Date(c.description).toLocaleDateString(navigator.languages, {
                timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                year: 'numeric', month: 'short', day: '2-digit', hour: "2-digit", minute: "2-digit"
              });
            }
          });
        }),
    );
  }

  private iconToComponent(icon: string): string {
    switch (icon) {
      case 'CLOCK_ICON':
        return 'ClockIcon';
      case 'CLOUD_ARROWUP_ICON':
        return 'CloudArrowUpIcon';
      case 'NEWSPAPER_ICON':
        return 'NewspaperIcon';
      case 'RSS_ICON':
        return 'RssIcon';
      case 'USER_GROUP_ICON':
        return 'UserGroupIcon';
      default:
        return 'QuestionMarkCircleIcon';
    }
  }

  // noinspection JSUnusedGlobalSymbols
  public unmounted(): void {
    reloadActionService.unregisterReloadFunction();
  }
}
</script>
