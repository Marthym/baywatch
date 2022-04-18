<template>
  <div class="stats shadow mt-4">

    <div class="stat">
      <div class="stat-figure text-secondary">
        <svg class="inline-block w-10 h-10" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd"
                d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z"
                clip-rule="evenodd"></path>
          <path d="M15 7h1a2 2 0 012 2v5.5a1.5 1.5 0 01-3 0V7z"></path>
        </svg>
      </div>
      <div class="stat-title">News</div>
      <div class="stat-value">{{ newsCount }}</div>
      <div class="stat-desc">Registered</div>
    </div>

    <div class="stat">
      <div class="stat-figure text-secondary">
        <svg class="inline-block w-10 h-10" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path d="M5 3a1 1 0 000 2c5.523 0 10 4.477 10 10a1 1 0 102 0C17 8.373 11.627 3 5 3z"></path>
          <path
              d="M4 9a1 1 0 011-1 7 7 0 017 7 1 1 0 11-2 0 5 5 0 00-5-5 1 1 0 01-1-1zM3 15a2 2 0 114 0 2 2 0 01-4 0z"></path>
        </svg>
      </div>
      <div class="stat-title">Feeds</div>
      <div class="stat-value">{{ feedsCount }}</div>
      <div class="stat-desc">Registered</div>
    </div>

    <div class="stat">
      <div class="stat-figure text-secondary">
        <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24"
             class="inline-block w-10 h-10">
          <path
              d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6
              8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75
              12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z"></path>
        </svg>
      </div>
      <div class="stat-title">Users</div>
      <div class="stat-value">{{ usersCount }}</div>
      <div class="stat-desc">registered</div>
    </div>

  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

import statisticsService from '@/techwatch/services/StatisticsService';

@Options({
  name: 'StatisticsAdminTab',
  components: {},
})
export default class StatisticsAdminTab extends Vue {

  private newsCount: number = 0;
  private feedsCount: number = 0;
  private usersCount: number = 0;

  public mounted(): void {
    statisticsService.get().subscribe({
      next: stats => {
        this.newsCount = stats.news;
        this.feedsCount = stats.feeds;
        this.usersCount = stats.users;
      }
    })
  }
}
</script>
