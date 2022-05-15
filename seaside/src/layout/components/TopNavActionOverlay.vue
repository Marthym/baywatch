<template>
  <transition
      enter-active-class="lg:duration-200 ease-out"
      enter-from-class="lg:transform lg:translate-x-80"
      enter-to-class="lg:translate-x-0"
      leave-active-class="lg:duration-200 ease-in"
      leave-from-class="lg:translate-x-0"
      leave-to-class="lg:transform lg:translate-x-80">
    <div class="relative" v-if="isOpen">
      <div class="w-full h-full z-10 fixed bg-base-200 bg-opacity-60 -mt-2
      lg:right-0 lg:w-80 xl:w-96 lg:h-fit lg:bg-transparent lg:mr-4" @click.stop="$emit('close')">
        <div class="h-fit opacity-100 p-4 bg-base-200 lg:rounded-lg">
          <slot name="content"/>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";

@Options({
  name: 'TopNavActionOverlay',
  emits: ['close']
})
export default class TopNavActionOverlay extends Vue {
  @Prop({default: false}) isOpen!: boolean;
}
</script>
