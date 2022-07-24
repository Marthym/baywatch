<template v-if="isOpened">
  <ModalWindow :title="modalTitle" :is-visible="isOpened">
    <form class="form-control" @submit.prevent="onSaveFeed">
      <fieldset :disabled="isFormLock" class="flex flex-col">
        <legend></legend>
        <label class="label" for="feedUrl">
          <span class="label-text">URL</span>
        </label>
        <input id="feedUrl" v-model="feed.url" type="url" placeholder="https://..." class="input input-bordered"
               :class="{'input-error': errors.indexOf('url') > -1}"
               @blur="onUriBlur">
        <label class="label" for="feedName">
          <span class="label-text">Nom</span>
        </label>
        <input id="feedName" v-model="feed.name" type="text" placeholder="nom" class="input input-bordered"
               :class="{'input-error': errors.indexOf('name') > -1}">

        <label class="label" for="feedDescription">
        <span class="label-text">Description</span>
        </label>
        <textarea id="feedDescription" v-model="feed.description" rows="3"
                  class="textarea italic" readonly/>

        <TagInput v-model="feed.tags" :available-tags-handler="() => listAvailableTags()"/>
      </fieldset>
      <button class="hidden" type="submit"/>
    </form>
    <template v-slot:actions>
      <button class="btn" @click.stop="resetAndCloseModal">Annuler</button>
      <button class="btn btn-primary" @click="onSaveFeed">Enregistrer</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {Feed} from '@/configuration/model/Feed.type';
import {Observable, Subject} from "rxjs";
import ModalWindow from "@/common/components/ModalWindow.vue";
import TagInput from "@/common/components/TagInput.vue";
import tagsService from '@/techwatch/services/TagsService';
import feedService, {URL_PATTERN} from "@/configuration/services/FeedService";

@Options({
  name: 'FeedEditor',
  components: {
    TagInput,
    ModalWindow,
  },
})
export default class FeedEditor extends Vue {
  private feed: Feed = {} as Feed;
  private isOpened = false;
  private modalTitle = 'Ajouter un fil';
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
    this.isOpened = false
    this.feed = {} as Feed;
    this.subject?.complete();
    this.subject = undefined;
  }

  private onSaveFeed(): void {
    this.errors.splice(0);
    if (this.feed.name === undefined || this.feed.name.match(/^ *$/) !== null) {
      this.errors.push('name');
    }
    if (!URL_PATTERN.test(this.feed.url)) {
      this.errors.push('url');
    }
    if (this.errors.length === 0) {
      this.subject?.next(this.feed);
      this.resetAndCloseModal();
    }
  }

  private onUriBlur(): void {
    if (!URL_PATTERN.test(this.feed.url)) {
      this.errors.push('url');
      return
    }
    this.isFormLock = true;
    feedService.fetchFeedInformation(this.feed.url).subscribe({
      next: f => Object.assign(this.feed, {...f, url: this.feed.url}),
      complete: () => this.isFormLock = false,
    });
  }

  private listAvailableTags(): Observable<string[]> {
    return tagsService.list();
  }
}
</script>
