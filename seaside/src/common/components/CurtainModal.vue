<template>
  <div ref="root"
       class="grid bg-base-200/60 z-30 w-full h-full absolute top-0 right-0 overflow-hidden" @click="opened = false">
    <Transition
        enter-active-class="lg:duration-300 ease-in-out"
        enter-from-class="lg:transform lg:translate-x-full"
        enter-to-class="lg:translate-x-0"
        leave-active-class="lg:duration-300 ease-in-out"
        leave-from-class="lg:translate-x-0"
        leave-to-class="lg:transform lg:translate-x-full"
        @after-leave="onTransitionLeave">
      <div v-if="opened"
           class="justify-self-end flex flex-col bg-neutral text-base-content lg:w-3/4 w-full h-full overflow-auto p-2"
           @click.stop>
        <slot :close="close"/>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { KeyboardController, listener, useKeyboardController } from '@/common/services/KeyboardController';
import { ref } from 'vue';
import { undefined } from 'valibot';

const LEAVE_EVENT: string = 'leave';

@Component({
  name: 'CurtainModal',
  emits: [LEAVE_EVENT],
  setup() {
    const root = ref(HTMLElement.prototype);
    return {
      root,
      keyboardController: useKeyboardController(root),
    };
  },
})
export default class CurtainModal extends Vue {
  @Prop({ default: undefined }) public readonly closeOnKey: string | undefined;
  private readonly keyboardController!: KeyboardController;
  private opened: boolean = false;

  mounted(): void {
    this.$nextTick(() => this.opened = true);
    if (this.closeOnKey) {
      this.keyboardController.register(listener(this.closeOnKey, event => {
        event.preventDefault();
        this.close();
      })).start();
    }
  }

  beforeUnmount(): void {
    this.keyboardController.purge();
  }

  private close(): void {
    this.opened = false;
  }

  private onTransitionLeave(): void {
    this.$emit(LEAVE_EVENT);
  }
}

export interface CurtainModalSlot {
  close(): void;
}
</script>