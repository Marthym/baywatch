<template v-if="isOpened">
  <ModalWindow :title="modalTitle" :is-visible="isOpened">
    <form class="form-control">
      <label class="label">
        <span class="label-text">Nom</span>
      </label>
      <input type="text" placeholder="username" class="input input-bordered">
      <label class="label">
        <span class="label-text">URL</span>
      </label>
      <input type="text" placeholder="username" class="input input-bordered">
      <TagInput/>
    </form>
    <template v-slot:actions>
      <button class="btn btn-primary">Enregistrer</button>
      <button class="btn" @click.stop="isOpened = false">Annuler</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {Feed} from '@/services/model/Feed';
import {Observable} from "rxjs";
import ModalWindow from "@/components/shared/ModalWindow.vue";
import TagInput from "@/components/shared/TagInput.vue";

@Component({
  components: {
    TagInput,
    ModalWindow,
  },
})
export default class FeedEditor extends Vue {
  private feed!: Feed;
  private isOpened = false;
  private modalTitle = 'Ajouter un fil';

  private tag = '';
  private tags: string[] = [];

  public openEmpty(): Observable<Feed> {
    this.isOpened = true;
    return new Observable<Feed>();
  }

  public openFeed(feed: Feed): Observable<Feed> {
    this.feed = feed;
    this.isOpened = true;
    return new Observable<Feed>();
  }
}
</script>
