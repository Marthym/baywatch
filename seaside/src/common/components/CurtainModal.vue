<template>
  <div class="grid bg-base-200 bg-opacity-60 z-30 w-full h-full absolute top-0 left-0 overflow-hidden"
       @click="opened = false">
    <Transition
        enter-active-class="lg:duration-300 ease-in-out"
        enter-from-class="lg:transform lg:translate-x-full"
        enter-to-class="lg:translate-x-0"
        leave-active-class="lg:duration-300 ease-in-out"
        leave-from-class="lg:translate-x-0"
        leave-to-class="lg:transform lg:translate-x-full"
        @after-leave="onTransitionLeave">
      <div v-if="opened" @click.stop
           class="justify-self-end flex flex-col bg-base-100 text-base-content lg:w-3/4 w-full h-full overflow-auto p-2">
        <slot :close="close"/>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";

const LEAVE_EVENT: string = 'leave';

@Options({
  name: 'CurtainModal',
  emits: [LEAVE_EVENT],
})
export default class CurtainModal extends Vue {
  private opened: boolean = false;

  mounted(): void {
    this.$nextTick(() => this.opened = true);
  }

  private close(): void {
    this.opened = false;
  }

  private onTransitionLeave(): void {
    this.$emit(LEAVE_EVENT);
  }
}
</script>