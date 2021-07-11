<template>
  <div class="modal compact flex-col space-x-0" :class="{'modal-active': isOpened}" v-if="isOpened">
    <div v-if="title !== undefined" class="modal-box translate-y-0 rounded-none
      text-lg bg-base-200 text-base-content uppercase font-bold p-4">
      {{ title }}
    </div>
    <div class="modal-box translate-y-0 rounded-none" :class="{'pt-2': title !== undefined}">
      <slot></slot>
      <div class="modal-action">
        <slot name="actions">
          <button class="btn" @click.stop="internalVisible = false">Fermer</button>
        </slot>
      </div>
    </div>
  </div>
</template>
<style>
@layer components {
  .modal-active {
    opacity: 1;
    pointer-events: auto;
    visibility: visible;
  }
}
</style>
<script lang="ts">
import {Component, Prop, Vue} from "vue-property-decorator";

@Component({
  components: {},
})
export default class ModalWindow extends Vue {
  @Prop() isVisible?: boolean;
  @Prop() title?: string;
  private internalVisible = true;

  get isOpened(): boolean {
    console.log(this.isVisible, this.internalVisible);
    return (this.isVisible == undefined) ? this.internalVisible : this.isVisible;
  }
}
</script>