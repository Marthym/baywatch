<template v-if="isOpened">
  <ModalWindow :title="modalTitle" :is-visible="isOpened">
    <form class="form-control" @submit.prevent="onSaveFeed">
      <label class="label">
        <span class="label-text">Nom</span>
      </label>
      <input v-model="feed.name" type="text" placeholder="nom" class="input input-bordered"
             :class="{'input-error': errors.indexOf('name') > -1}">
      <label class="label">
        <span class="label-text">URL</span>
      </label>
      <input v-model="feed.url" type="url" placeholder="https://..." class="input input-bordered"
             :class="{'input-error': errors.indexOf('url') > -1}">
      <TagInput v-model="feed.tags" :available-tags-handler="() => listAvailableTags()"/>
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
import {Feed} from '@/configuration/model/Feed';
import {Observable, Subject} from "rxjs";
import ModalWindow from "@/shared/components/ModalWindow.vue";
import TagInput from "@/shared/components/TagInput.vue";
import tagsService from '@/techwatch/services/TagsService';

const URL_PATTERN = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
    '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
    '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locator

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

  private listAvailableTags(): Observable<string[]> {
    return tagsService.list();
  }
}
</script>
