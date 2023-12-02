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
           class="justify-self-end flex flex-col bg-neutral text-base-content lg:w-3/4 w-full h-full overflow-auto p-2">
        <slot :close="close"/>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { KeyboardController, listener, useKeyboardController } from '@/common/services/KeyboardController';

const LEAVE_EVENT: string = 'leave';

@Component({
  name: 'CurtainModal',
  emits: [LEAVE_EVENT],
  setup() {
    return {
      keyboardController: useKeyboardController(),
    };
  },
})
export default class CurtainModal extends Vue {
  private readonly keyboardController: KeyboardController;
  private opened: boolean = false;

  mounted(): void {
    this.$nextTick(() => this.opened = true);
    this.keyboardController.register(listener('Escape', event => {
      event.preventDefault();
      this.close();
    })).start();
  }

  private close(): void {
    this.opened = false;
  }

  private onTransitionLeave(): void {
    this.$emit(LEAVE_EVENT);
  }

  unmounted(): void {
    this.keyboardController.purge();
  }
}
</script>