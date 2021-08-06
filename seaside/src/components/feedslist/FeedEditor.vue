<template v-if="isOpened">
  <ModalWindow :title="modalTitle" :is-visible="isOpened">
    <form class="form-control" @submit.prevent="onSaveFeed">
      <label class="label">
        <span class="label-text">Nom</span>
      </label>
      <input v-model="feed.name" type="text" placeholder="username" class="input input-bordered">
      <label class="label">
        <span class="label-text">URL</span>
      </label>
      <input v-model="feed.url" type="text" placeholder="username" class="input input-bordered">
      <TagInput v-model="feed.tags"/>
      <button class="hidden" type="submit"/>
    </form>
    <template v-slot:actions>
      <button class="btn" @click.stop="resetAndCloseModal">Annuler</button>
      <button class="btn btn-primary" @click="onSaveFeed">Enregistrer</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {Feed} from '@/services/model/Feed';
import {Observable, Subject} from "rxjs";
import ModalWindow from "@/components/shared/ModalWindow.vue";
import TagInput from "@/components/shared/TagInput.vue";

@Component({
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

  public openEmpty(): Observable<Feed> {
    return this.openFeed({} as Feed);
  }

  public openFeed(feed: Feed): Observable<Feed> {
    this.feed = feed;
    this.isOpened = true;
    this.subject = new Subject<Feed>();
    return this.subject.asObservable();
  }

  private resetAndCloseModal(): void {
    this.isOpened = false
    this.feed = {} as Feed;
    this.subject?.complete();
    this.subject = undefined;
  }

  private onSaveFeed() {
    this.subject?.next(this.feed);
    this.resetAndCloseModal();
  }
}
</script>
