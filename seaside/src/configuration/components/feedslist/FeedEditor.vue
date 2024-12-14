<template v-if="isOpened">
  <ModalWindow :is-visible="isOpened" :title="t('config.feeds.editor.title')">
    <form class="form-control" @submit.prevent="onSaveFeed">
      <fieldset :disabled="isFormLock" class="flex flex-col">
        <legend></legend>
        <label for="feedUrl">
          <span class="label">
            <span class="label-text capitalize">{{ t('config.feeds.editor.form.location') }}</span>
          </span>
          <span class="join w-full">
            <input id="feedUrl" v-model="feed.location" :class="{'input-error': errors.location}"
                   class="input input-bordered join-item w-full" placeholder="https://..."
                   type="url"
                   @blur="onUriBlur">
            <button class="btn join-item" @click.stop="onUriBlur">
              <ArrowPathIcon class="h-6 w-6"/>
            </button>
          </span>
          <span class="label">
            <span class="label-text-alt"/>
            <span class="label-text-alt text-error-content first-letter:capitalize">{{ errors.location }}</span>
          </span>
        </label>
        <label class="label -mt-6" for="feedName">
          <span class="label-text capitalize">{{ t('config.feeds.editor.form.name') }}</span>
        </label>
        <input id="feedName" v-model="feed.name" :class="{'input-error': errors.name}"
               :placeholder="t('config.feeds.editor.form.name.placeholder')"
               class="input input-bordered placeholder:capitalize"
               type="text">

        <label class="label" for="feedDescription">
          <span class="label-text capitalize">{{ t('config.feeds.editor.form.description') }}</span>
        </label>
        <textarea id="feedDescription" v-model="feed.description" class="textarea textarea-bordered italic" rows="3"/>

        <TagInput v-model="feed.tags" :available-tags-handler="() => listAvailableTags()"/>
      </fieldset>
      <button class="hidden" type="submit"/>
    </form>
    <template v-slot:actions>
      <button class="btn capitalize" @click.stop="resetAndCloseModal">{{ t('dialog.cancel') }}</button>
      <button class="btn btn-primary capitalize"
              :disabled="Object.entries(errors).length > 0"
              @click="onSaveFeed">{{
          t('config.feeds.editor.form.action.submit')
        }}
      </button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { Feed } from '@/configuration/model/Feed.type';
import { Observable, Subject } from 'rxjs';
import ModalWindow from '@/common/components/ModalWindow.vue';
import TagInput from '@/common/components/TagInput.vue';
import { tagsListAll } from '@/techwatch/services/TagsService';
import { feedFetchInformation } from '@/configuration/services/FeedService';
import { URL_PATTERN } from '@/common/services/RegexPattern';
import { useI18n } from 'vue-i18n';
import { ArrowPathIcon } from '@heroicons/vue/24/outline';

@Component({
  name: 'FeedEditor',
  components: {
    ArrowPathIcon,
    TagInput,
    ModalWindow,
  },
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class FeedEditor extends Vue {
  private t;
  private feed: Feed = {} as Feed;
  private isOpened = false;
  private subject?: Subject<Feed>;
  private errors: {
    name?: string;
    location?: string;
  } = {};
  private isFormLock = false;

  public openEmpty(): Observable<Feed> {
    return this.openFeed({} as Feed);
  }

  public openFeed(feed: Feed): Observable<Feed> {
    this.feed = feed;
    this.isOpened = true;
    this.subject = new Subject<Feed>();
    Object.assign(this.errors, {});
    return this.subject.asObservable();
  }

  private resetAndCloseModal(): void {
    this.isOpened = false;
    this.feed = {} as Feed;
    this.subject?.complete();
    this.subject = undefined;
  }

  private onSaveFeed(): void {
    Object.assign(this.errors, {});
    if (this.feed.name === undefined || /^ *$/.exec(this.feed.name) !== null) {
      this.errors.name = this.t('config.feeds.messages.nameMandatory');
    }
    if (!URL_PATTERN.test(this.feed.location)) {
      this.errors.location = this.t('config.feeds.messages.locationMustBeURL');;
    }
    if (Object.entries(this.errors).length === 0) {
      this.subject?.next(this.feed);
      this.resetAndCloseModal();
    }
  }

  private onUriBlur(): void {
    if (!URL_PATTERN.test(this.feed.location)) {
      this.errors.location = this.t('config.feeds.messages.locationMustBeURL');;
      return;
    } else {
      delete this.errors.location;
    }
    this.isFormLock = true;
    feedFetchInformation(this.feed.location).subscribe({
      next: f => Object.assign(this.feed, { ...f, url: this.feed.location }),
      complete: () => this.isFormLock = false,
      error: err => {
        this.errors.location = this.t(err.code);
        this.isFormLock = false;
      }
    });
  }

  private listAvailableTags(): Observable<string[]> {
    return tagsListAll();
  }
}
</script>
