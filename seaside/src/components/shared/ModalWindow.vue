<template>
  <div class="modal"
       :class="{'modal-active': isOpened}" v-if="isOpened">
    <div class="modal-box rounded-none">
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
  private internalVisible = true;

  get isOpened(): boolean {
    console.log(this.isVisible, this.internalVisible);
    return (this.isVisible == undefined) ? this.internalVisible : this.isVisible;
  }
}
</script>