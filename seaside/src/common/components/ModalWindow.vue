<template>
  <dialog class="modal bg-base-200 bg-opacity-50 backdrop-blur-sm text-primary-content" v-if="isOpened"
          :class="{'opacity-100 pointer-events-auto visible': isOpened}">
    <div class="modal-box flex-col space-x-0 overflow-visible">
      <h3 class="font-bold text-lg -mt-2 mb-2">{{ title }}</h3>
      <slot></slot>
      <div class="modal-action">
        <slot name="actions">
          <button class="btn" @click.stop="internalVisible = false">Fermer</button>
        </slot>
      </div>
    </div>
  </dialog>
</template>
<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';

@Component({ name: 'ModalWindow' })
export default class ModalWindow extends Vue {
  @Prop({ default: true }) isVisible!: boolean;
  @Prop({ default: 'Baywatch' }) title!: string;
  private internalVisible = true;

  get isOpened(): boolean {
    return this.isVisible ?? this.internalVisible;
  }
}
</script>