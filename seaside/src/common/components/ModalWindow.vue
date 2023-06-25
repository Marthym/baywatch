<template>
    <div class="modal compact flex-col space-x-0" v-if="isOpened"
         :class="{'opacity-100 pointer-events-auto visible': isOpened}">
        <div v-if="title !== undefined" class="modal-box scale-100 translate-y-0 rounded-none
      text-lg bg-base-200 text-base-content uppercase font-bold p-4">
            {{ title }}
        </div>
        <div class="modal-box scale-100 translate-y-0 rounded-none overflow-visible"
             :class="{'pt-2': title !== undefined}">
            <slot></slot>
            <div class="modal-action">
                <slot name="actions">
                    <button class="btn" @click.stop="internalVisible = false">Fermer</button>
                </slot>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import {Component, Prop, Vue} from "vue-facing-decorator";

@Component({name: 'ModalWindow'})
export default class ModalWindow extends Vue {
    @Prop({default: true}) isVisible!: boolean;
    @Prop({default: 'Baywatch'}) title!: string;
    private internalVisible = true;

    get isOpened(): boolean {
        return (this.isVisible === undefined) ? this.internalVisible : this.isVisible;
    }
}
</script>