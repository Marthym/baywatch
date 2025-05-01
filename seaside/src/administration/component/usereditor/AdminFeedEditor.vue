<template>
  <curtain-modal v-slot="curtainModal" @leave="close()">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">{{ t('admin.feed.editor.title') }}</h2>
    <div class="m-4">
      <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
        <legend class="fieldset-legend">Location</legend>
        <div class="join">
          <input v-model="feed.url" class="input join-item w-full" placeholder="Feed URL: https://..." type="text"/>
          <button class="btn btn-secondary join-item"
                  @click.prevent.stop="onFeedScrapping(feed.url)">
            <ArrowPathIcon class="size-6 text-secondary-content"/>
          </button>
        </div>
      </fieldset>
      <div class="flex flex-row gap-4">
        <div class="basis-1/2">
          <fieldset class="fieldset">
            <legend class="fieldset-legend">Feed Name</legend>
            <input v-model="feed.name" class="input w-full" placeholder="Enter the feed name" type="text"/>
          </fieldset>
          <fieldset class="fieldset">
            <legend class="fieldset-legend">Description</legend>
            <textarea v-model="feed.description" class="textarea h-24 w-full"
                      placeholder="Enter a short description"></textarea>
            <p class="label">Optional</p>
          </fieldset>
        </div>
        <div class="basis-1/2">
          <fieldset class="fieldset">
            <legend class="fieldset-legend">Feed icon</legend>
            <input v-model="feed.icon" class="input w-full" placeholder="https://..." type="url"/>
          </fieldset>
          <figure v-if="feed.icon" class="h-24 max-w-full mt-8">
            <img :alt="feed.name + ' icon'" :src="feed.icon" class="h-24 object-cover truncate text-transparent"/>
          </figure>
        </div>
      </div>
      <fieldset v-if="feed.error" class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
        <legend class="fieldset-legend">Errors</legend>
        <div>
          <ExclamationTriangleIcon :class="{
            'text-error-content': feed.error.level == 'SEVERE',
            'text-warning': feed.error.level == 'WARNING'
          }" class="size-6 inline"/>
          {{ feed.error.level }}
          <p>{{ feed.error.message }}</p>
          <p>Depuis: {{ feed.error.since }}</p>
          <p>Dernier: {{ feed.error.lastTime }}</p>
        </div>
      </fieldset>
      <div class="text-right mt-4">
        <button class="btn btn-sm mx-1" @click.stop="curtainModal.close()">Cancel</button>
        <button class="btn btn-sm btn-primary mx-1" @click.stop="throttledOnSave">
          Save
        </button>
      </div>
    </div>
  </curtain-modal>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import CurtainModal from '@/common/components/CurtainModal.vue';
import throttle from 'lodash/throttle';
import TeamMembersInput from '@/teams/components/TeamMembersInput.vue';
import { useI18n } from 'vue-i18n';
import { adminRawFeedGet, adminRawFeedScrap } from '@/administration/services/FeedAdministrationService';
import { RawFeed } from '@/administration/model/RawFeed.type';
import { ArrowPathIcon, ExclamationTriangleIcon } from '@heroicons/vue/24/outline';
import { map } from 'rxjs/operators';

const CLOSE_EVENT = 'close';
const SAVE_EVENT = 'save';

@Component({
  name: 'TeamEditor',
  components: { ExclamationTriangleIcon, CurtainModal, TeamMembersInput, ArrowPathIcon },
  emits: [CLOSE_EVENT, SAVE_EVENT],
  setup() {
    const { t, d } = useI18n();
    return { t, d };
  },
})
export default class AdminFeedEditor extends Vue {
  @Prop() private readonly id!: string;

  private readonly t!;
  private readonly d!;

  private feed: RawFeed = {} as RawFeed;
  private errors: Map<string, string> = new Map<string, string>();
  private payload: CloseEvent = {
    updated: false,
  };
  private throttledOnSave: () => void = this.onSave;

  private mounted(): void {
    adminRawFeedGet(this.id).pipe(
        map((feed: RawFeed) => {
          if (feed.lastWatch) {
            feed.lastWatch = this.d(new Date(feed.lastWatch));
          }
          if (feed.error?.lastTime) {
            feed.error.lastTime = this.d(new Date(feed.error.lastTime));
          }
          if (feed.error?.since) {
            feed.error.since = this.d(new Date(feed.error.since));
          }
          return { ...feed, errorDisplay: false } as RawFeed;
        }),
    ).subscribe({
      next: value => Object.assign(this.feed, value),
    });
  }

  /**
   * @see unmounted
   */
  private unmounted(): void {
    delete this.throttledOnSave;
  }

  private onSave(): void {
    this.$emit(SAVE_EVENT, this.feed);
  }

  /**
   * @see created
   */
  private created(): void {
    this.throttledOnSave = throttle(this.onSave, 1000, { 'trailing': false });
  }

  private close(): void {
    this.$emit(CLOSE_EVENT, this.payload);
  }

  private onFeedScrapping(url: string): void {
    adminRawFeedScrap(url).subscribe({
      next: value => {
        this.feed = Object.assign(this.feed, value);
      },
      error: err => {
        this.errors.set('url', err.message);
      },
    });
  }
}

export type CloseEvent = {
  updated: boolean,
}
</script>
