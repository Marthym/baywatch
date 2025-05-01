<template>
  <ul class="list bg-base-100 rounded-box shadow-md">

    <li class="p-4 pb-2 text-xs opacity-60 tracking-wide flex flex-row">
      <div class="grow">{{ t('admin.feeds.tab.feeds.list') }}</div>
      <div class="join">
        <button :disabled="activePage === 0" class="join-item btn btn-sm"
                @click.prevent.stop="loadPreviousPage">«
        </button>
        <button class="join-item btn btn-sm capitalize">{{ t('pagination.page') }} {{ activePage + 1 }}</button>
        <button :disabled="activePage === totalPages" class="join-item btn btn-sm"
                @click.prevent.stop="loadNextPage">»
        </button>
      </div>
    </li>

    <li v-for="(feed, index) in feeds" class="list-row">
      <div>
        <img :alt="feed.name?.at(0)?.toLowerCase()"
             :src="feed.icon"
             class="size-10 rounded-box font-bold bg-neutral text-center align-middle leading-10"/>
      </div>
      <div>
        <div class="text-xs italic text-base-content/60">{{ feed._id.substring(0, 10) }}</div>
        <div class="capitalize">{{ feed.name }}</div>
        <div class="text-xs font-semibold opacity-60">{{ feed.description }}</div>
        <div class="text-xs"><a :href="feed.url" class="link">{{ feed.url }}</a></div>
      </div>
      <div class="text-right">
        <div>{{ feed.lastWatch }}</div>
        <div class="text-xs">{{ feed.lastETag }}</div>
      </div>
      <button v-if="feed.error" :class="{'tooltip': feed.errorDisplay}" :data-tip="feed.error?.message"
              class="btn btn-square btn-ghost"
              @click.prevent.stop="feed.errorDisplay = !feed.errorDisplay">
        <ExclamationTriangleIcon :class="{
          'text-error-content': feed.error.level == 'SEVERE',
          'text-warning': feed.error.level == 'WARNING'
        }" class="size-6"/>
      </button>
      <div v-else class="size-10">&nbsp;</div>
      <div class="join">
        <button class="btn btn-square join-item"
                @click.stop.prevent="router.push(`/admin/feeds/${feed._id}`)">
          <PencilSquareIcon class="size-6"/>
        </button>
        <button class="btn btn-square join-item"
                @click.prevent.stop="onDeleteRawFeeds([index])">
          <TrashIcon class="size-6"/>
        </button>
      </div>
    </li>
    <li class="p-4 pb-2 text-xs opacity-60 tracking-wide flex flex-row">
      <div class="grow">{{ t('admin.feeds.tab.feeds.list') }}</div>
      <div class="join">
        <button :disabled="activePage === 0" class="join-item btn btn-sm">«</button>
        <button class="join-item btn btn-sm capitalize">{{ t('pagination.page') }} {{ activePage + 1 }}</button>
        <button :disabled="activePage === totalPages" class="join-item btn btn-sm">»</button>
      </div>
    </li>
  </ul>

  <teleport v-if="route.params.id" to="body">
    <router-view :id="route.params.id"
                 @delete="onDeleteEvent"
                 @leave="router.push('/admin/feeds')"
                 @save="onSaveEvent"/>
  </teleport>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { RawFeed } from '@/administration/model/RawFeed.type';
import {
  adminFeedFind,
  adminFeedUpdate,
  adminRawFeedCreate,
  adminRawFeedDelete,
} from '@/administration/services/FeedAdministrationService';
import { switchMap } from 'rxjs';
import { filter, map, tap } from 'rxjs/operators';
import { useI18n } from 'vue-i18n';
import { ExclamationTriangleIcon, PencilSquareIcon, TrashIcon } from '@heroicons/vue/24/outline';
import { AlertResponse, AlertType } from '@/common/components/alertdialog/AlertDialog.types';
import notificationService from '@/services/notification/NotificationService';
import { RouteLocationNormalizedLoaded, Router, useRoute, useRouter } from 'vue-router';

type RawFeedView = RawFeed & {
  errorDisplay: boolean
};

@Component({
  components: { ExclamationTriangleIcon, PencilSquareIcon, TrashIcon },
  setup() {
    const { d, t } = useI18n();
    const route = useRoute();
    const router = useRouter();
    return { d, t, route, router };
  },
})
export default class FeedsAdminTab extends Vue {
  private readonly route!: RouteLocationNormalizedLoaded;
  private readonly router!: Router;
  private readonly d!;
  private readonly t!;
  private feeds: RawFeedView[] = [];
  private activePage = -1;
  private totalPages = 0;

  public onDeleteEvent(id: number): void {
    this.onDeleteRawFeeds([id]);
  }

  public onSaveEvent(feed: RawFeed): void {
    if (feed._id) {
      adminFeedUpdate(feed._id, feed).subscribe({
        next: () => {
          notificationService.pushSimpleOk(this.t('admin.feeds.messages.feedUpdatedSuccessfully'));
          this.router.push('/admin/feeds');
          this.activePage -= 1;
          this.loadNextPage();
        },
        error: () => {
          notificationService.pushSimpleError(this.t('admin.feeds.messages.feedUpdateFailed'));
        },
      });
    } else {
      adminRawFeedCreate(feed).subscribe({
        next: () => {
          notificationService.pushSimpleOk(this.t('admin.feeds.messages.feedCreatedSuccessfully'));
          this.router.push('/admin/feeds');
          this.activePage -= 1;
          this.loadNextPage();
        },
        error: () => {
          notificationService.pushSimpleError(this.t('admin.feeds.messages.feedCreationFailed'));
        },
      });
    }
  }

  public mounted(): void {
    this.loadNextPage();
  }

  private loadPreviousPage(): void {
    this.activePage -= 2;
    this.loadNextPage();
  }

  private loadNextPage(): void {
    adminFeedFind({ _p: this.activePage + 1, _pp: 20 }).pipe(
        switchMap(rawFeeds => {
          this.totalPages = rawFeeds.totalPage;
          this.activePage = rawFeeds.currentPage;
          return rawFeeds.data;
        }),
        map((feeds: RawFeed[]): RawFeedView[] => {
          return feeds.map(feed => {
            if (feed.lastWatch) {
              feed.lastWatch = this.d(new Date(feed.lastWatch));
            }
            return { ...feed, errorDisplay: false } as RawFeedView;
          });
        }),
    ).subscribe({
      next: rawFeeds => {
        this.feeds.splice(0, this.feeds.length);
        this.feeds.push(...rawFeeds);
      },
    });
  }

  private onDeleteRawFeeds(ids: number[]): void {
    if (!ids || ids.length === 0) {
      this.$alert.fire(this.t('admin.feeds.info.deletionMustContainsOneID'), AlertType.INFO).subscribe();
      return;
    }
    const message = this.t('admin.feeds.confirm.feedsDeletion', ids.length, { named: { feed: this.feeds[ids[0]].name } });
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => adminRawFeedDelete(ids.map(index => this.feeds[index]._id))),
        tap(() => {
          this.activePage -= 1;
          return this.loadNextPage();
        }),
    ).subscribe({
      next: () =>
          notificationService.pushSimpleOk(this.t('admin.feeds.messages.feedDeletedSuccessfully', ids.length)),
      error: () =>
          notificationService.pushSimpleError(this.t('admin.feeds.messages.feedDeletionFailed')),
    });
  }
}
</script>
