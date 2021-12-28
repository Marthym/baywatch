<template>
  <ModalWindow :title="title" :is-visible="true">
    <label class="p-5 relative border-4 border-dotted rounded-lg flex flex-col items-center"
           :class="{'border-accent-focus': isDragOver, 'border-neutral-content': !isDragOver}"
           @dragover="isDragOver = true" @dragleave="isDragOver = false" @drop.stop.prevent="onDropFile">
      <svg class="w-24 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none"
           :class="{'text-accent-focus': isDragOver, 'text-primary': !isDragOver}"
           viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
      </svg>
      <input class="w-full h-full opacity-0 overflow-hidden absolute -mt-5" type="file" multiple
             @change="onChange"/>
      <span class="btn btn-outline btn-wide"
            :class="{'btn-accent': isDragOver, 'btn-primary': !isDragOver}">Sélectionner</span>
      <span v-if="path === null" class="title text-neutral-content">ou déplacer un fichier ici</span>
      <span v-else class="title text-neutral-content">{{ path.name }}</span>
    </label>
    <template v-slot:actions>
      <button class="btn" @click.stop="$emit('upload', undefined)">Annuler</button>
      <button class="btn btn-primary" @click.stop="onUploadFile">Téléverser</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import ModalWindow from "@/components/shared/ModalWindow.vue";

const UPLOAD_EVENT = 'upload';

@Options({
  name: 'FileUploadWindow',
  components: {
    ModalWindow,
  },
  emits: [UPLOAD_EVENT],
})
export default class FileUploadWindow extends Vue {
  @Prop({default: 'Upload file'}) private title!: string;
  private isDragOver = false;
  private path: File | null = null;

  private onDropFile(event: DragEvent): void {
    this.isDragOver = false;
    if (event.dataTransfer) {
      this.path = event.dataTransfer.files[0];
    }
    return;
  }

  private onChange(event: InputEvent): void {
    const target = event.target as HTMLInputElement;
    if (target?.files?.item(0)) {
      this.path = target.files.item(0);
    }
  }

  private onUploadFile(): void {
    this.$emit(UPLOAD_EVENT, this.path);
    this.path = null;
  }
}
</script>
