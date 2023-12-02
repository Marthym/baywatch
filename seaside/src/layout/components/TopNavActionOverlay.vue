<template>
  <transition @before-enter="onTransitionStart"
              @after-enter="onTransitionEnd"
              enter-active-class="lg:duration-200 ease-out"
              enter-from-class="lg:transform lg:translate-x-80"
              enter-to-class="lg:translate-x-0"
              leave-active-class="lg:duration-200 ease-in"
              leave-from-class="lg:translate-x-0"
              leave-to-class="lg:transform lg:translate-x-80">
    <div ref="tnOverlay" class="relative" v-if="isOpen">
      <div class="w-full h-full z-10 fixed bg-base-200 bg-opacity-60 -mt-2
      lg:right-0 lg:w-80 xl:w-96 lg:h-fit lg:bg-transparent lg:mr-4" @click.stop="$emit('close')">
        <div class="h-fit opacity-100 p-4 bg-base-200 lg:rounded-lg">
          <slot name="content" :isTransitioning="isTransitioning"/>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts">
import {Component, Prop, Vue} from "vue-facing-decorator";

@Component({
  name: 'TopNavActionOverlay',
  emits: ['close'],
  props: ['isOpen'],
})
export default class TopNavActionOverlay extends Vue {
  @Prop({default: false}) isOpen!: boolean;
  private isTransitioning = false;

  onTransitionStart(): void {
    this.isTransitioning = true;
  }

  onTransitionEnd(): void {
    this.isTransitioning = false;
  }
}
</script>
