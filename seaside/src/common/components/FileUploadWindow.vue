<template>
  <ModalWindow :title="t('fileupload.title')">
    <label :class="{'border-accent-focus': isDragOver, 'border-neutral-content': !isDragOver}"
           class="p-5 relative border-4 border-dotted rounded-lg flex flex-col items-center"
           @dragleave="isDragOver = false" @dragover="isDragOver = true" @drop.stop.prevent="onDropFile">
      <CloudArrowUpIcon :class="{'text-accent-focus': isDragOver, 'text-primary': !isDragOver}"
                        class="w-24 mx-auto mb-4"/>
      <input class="w-full h-full opacity-0 overflow-hidden absolute -mt-5" multiple type="file"
             @change="onChange"/>
      <span :class="{'btn-accent': isDragOver, 'btn-primary': !isDragOver}"
            class="btn btn-outline btn-wide capitalize">{{ t('fileupload.form.action.choose') }}</span>
      <span v-if="path === null" class="title text-neutral-content">{{
          t('fileupload.form.action.choose.notice')
        }}</span>
      <span v-else class="title text-neutral-content">{{ path.name }}</span>
    </label>
    <template v-slot:actions>
      <button class="btn capitalize" @click.stop="$emit('upload', undefined)">{{ t('dialog.cancel') }}</button>
      <button class="btn btn-primary capitalize" @click.stop="onUploadFile">{{
          t('fileupload.form.action.upload')
        }}
      </button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useI18n } from 'vue-i18n';
import { CloudArrowUpIcon } from '@heroicons/vue/24/outline';
import ModalWindow from '@/common/components/ModalWindow.vue';

const UPLOAD_EVENT = 'upload';

@Component({
  name: 'FileUploadWindow',
  components: {
    CloudArrowUpIcon, ModalWindow,
  },
  emits: [UPLOAD_EVENT],
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class FileUploadWindow extends Vue {
  private t;
  private isDragOver = false;
  private path: File | null = null;

  private onDropFile(event: DragEvent): void {
    this.isDragOver = false;
    if (event.dataTransfer) {
      this.path = event.dataTransfer.files[0];
    }
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
