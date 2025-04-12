<template>
  <ul class="list bg-base-100 rounded-box shadow-md">

    <li class="p-4 pb-2 text-xs opacity-60 tracking-wide flex flex-row">
      <div class="grow">Liste des feeds</div>
      <div class="join">
        <button :disabled="activePage === 0" class="join-item btn btn-sm">«</button>
        <button class="join-item btn btn-sm">Page {{ activePage + 1 }}</button>
        <button :disabled="activePage === totalPages" class="join-item btn btn-sm">»</button>
      </div>
    </li>

    <li v-for="feed in feeds" class="list-row">
      <div>
        <img :alt="feed.name.at(0).toLocaleUpperCase()"
             :src="feed.icon" class="size-10 rounded-box font-bold bg-neutral text-center align-middle leading-10"/>
      </div>
      <div>
        <div class="capitalize">{{ feed.name }}</div>
        <div class="text-xs font-semibold opacity-60">{{ feed.description }}</div>
        <div class="text-xs"><a :href="feed.url" class="link">{{ feed.url }}</a></div>
      </div>
      <button class="btn btn-square btn-ghost">
        <svg class="size-[1.2em]" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
            <path d="M6 3L20 12 6 21 6 3z"></path>
          </g>
        </svg>
      </button>
      <button class="btn btn-square btn-ghost">
        <svg class="size-[1.2em]" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
            <path
                d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
          </g>
        </svg>
      </button>
    </li>
    <li class="p-4 pb-2 text-xs opacity-60 tracking-wide flex flex-row">
      <div class="grow">Liste des feeds</div>
      <div class="join">
        <button :disabled="activePage === 0" class="join-item btn btn-sm">«</button>
        <button class="join-item btn btn-sm">Page {{ activePage + 1 }}</button>
        <button :disabled="activePage === totalPages" class="join-item btn btn-sm">»</button>
      </div>
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { RawFeed } from '@/administration/model/RawFeed.type';
import { adminFeedFind } from '@/administration/services/FeedAdministrationService';
import { switchMap } from 'rxjs';

@Component({})
export default class FeedsAdminTab extends Vue {
  private feeds: RawFeed[] = [];
  private activePage = -1;
  private totalPages = 0;

  public mounted(): void {
    adminFeedFind({ _p: this.activePage + 1, _pp: 20 }).pipe(
        switchMap(rawFeeds => {
          this.totalPages = rawFeeds.totalPage;
          this.activePage = rawFeeds.currentPage;
          return rawFeeds.data;
        }),
    ).subscribe({
      next: rawFeeds => this.feeds.push(...rawFeeds),
    });
  }
}
</script>
