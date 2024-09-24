<template v-if="isOpened">
  <ModalWindow :is-visible="isOpened" :title="t('config.feeds.editor.title')">
    <form class="form-control" @submit.prevent="onSaveFeed">
      <fieldset :disabled="isFormLock" class="flex flex-col">
        <legend></legend>
        <label class="label" for="feedUrl">
          <span class="label-text capitalize">{{ t('config.feeds.editor.form.location') }}</span>
        </label>
        <input id="feedUrl" v-model="feed.location" :class="{'input-error': errors.indexOf('location') > -1}"
               class="input input-bordered" placeholder="https://..."
               type="url"
               @blur="onUriBlur">
        <label class="label" for="feedName">
          <span class="label-text capitalize">{{ t('config.feeds.editor.form.name') }}</span>
        </label>
        <input id="feedName" v-model="feed.name" :class="{'input-error': errors.indexOf('name') > -1}"
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
      <button class="btn btn-primary capitalize" @click="onSaveFeed">{{
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
import feedService from '@/configuration/services/FeedService';
import { URL_PATTERN } from '@/common/services/RegexPattern';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'FeedEditor',
  components: {
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
  private errors: string[] = [];
  private isFormLock = false;

  public openEmpty(): Observable<Feed> {
    return this.openFeed({} as Feed);
  }

  public openFeed(feed: Feed): Observable<Feed> {
    this.feed = feed;
    this.isOpened = true;
    this.subject = new Subject<Feed>();
    this.errors.splice(0);
    return this.subject.asObservable();
  }

  private resetAndCloseModal(): void {
    this.isOpened = false;
    this.feed = {} as Feed;
    this.subject?.complete();
    this.subject = undefined;
  }

  private onSaveFeed(): void {
    this.errors.splice(0);
    if (this.feed.name === undefined || /^ *$/.exec(this.feed.name) !== null) {
      this.errors.push('name');
    }
    if (!URL_PATTERN.test(this.feed.location)) {
      this.errors.push('location');
    }
    if (this.errors.length === 0) {
      this.subject?.next(this.feed);
      this.resetAndCloseModal();
    }
  }

  private onUriBlur(): void {
    if (!URL_PATTERN.test(this.feed.location)) {
      this.errors.push('location');
      return;
    }
    this.isFormLock = true;
    feedService.fetchFeedInformation(this.feed.location).subscribe({
      next: f => Object.assign(this.feed, { ...f, url: this.feed.location }),
      complete: () => this.isFormLock = false,
      error: () => this.isFormLock = false,
    });
  }

  private listAvailableTags(): Observable<string[]> {
    return tagsListAll();
  }
}
</script>
