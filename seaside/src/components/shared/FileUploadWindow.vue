<template v-if="isOpened">
  <ModalWindow :title="modalTitle" :is-visible="isOpened">
    <form class="form-control" @submit.prevent="onSaveFeed">

      <button class="hidden" type="submit"/>
    </form>
    <template v-slot:actions>
      <button class="btn" @click.stop="resetAndCloseModal">Annuler</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {Observable, of, Subject} from "rxjs";
import ModalWindow from "@/components/shared/ModalWindow.vue";

@Component({
  components: {
    ModalWindow,
  },
})
export default class FileUploadWindow extends Vue {
  private isOpened = false;
  private modalTitle = 'Ajouter un fil';
  private subject?: Subject<string>;
  private path = "";

  public openEmpty(): Observable<string> {
    return this.openFeed("" as string);
  }

  public openFeed(path: string): Observable<string> {
    this.path = path;
    this.isOpened = true;
    this.subject = new Subject<string>();
    return this.subject.asObservable();
  }

  private resetAndCloseModal(): void {
    this.isOpened = false
    this.path = {} as string;
    this.subject?.complete();
    this.subject = undefined;
  }

  private onSaveFeed() {
    this.subject?.next(this.path);
    this.resetAndCloseModal();
  }

  public static open(parent: Element): Observable<string> {
    // const FileUploadComponent = Vue.extend(FileUploadWindow());
    const fileUpload = new FileUploadWindow();
    fileUpload.$mount();
    parent.appendChild(fileUpload.$el);
    fileUpload.openEmpty();
    console.log(fileUpload.$el);
    return of(fileUpload.path);
  }
}
</script>
