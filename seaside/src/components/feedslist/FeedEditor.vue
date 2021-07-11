<template v-if="isOpened">
  <ModalWindow :is-visible="isOpened">
    <p>Prout</p>
    <template v-slot:actions>
      <button class="btn btn-primary">Enregistrer</button>
      <button class="btn" @click.stop="isOpened = false">Fermer</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {Feed} from '@/services/model/Feed';
import {Observable} from "rxjs";
import ModalWindow from "@/components/shared/ModalWindow.vue";

@Component({
  components: {
    ModalWindow,
  },
})
export default class FeedEditor extends Vue {
  private feed!: Feed;
  private isOpened = false;

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
