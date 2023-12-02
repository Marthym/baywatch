<template>
    <div class="md:btn-group mb-2">
        <button v-if="actions.indexOf('a') >= 0" class="btn btn-sm btn-ghost mb-2 mr-2 md:m-0"
                @click.stop.prevent="$emit('add')">
            <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
            <span>Ajouter</span>
        </button>
        <button v-if="actions.indexOf('i') >= 0" class="btn btn-sm mb-2 mr-2 md:m-0" @click="$emit('import')">
            <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
            Importer
        </button>
        <a v-if="actions.indexOf('e') >= 0" class="btn btn-sm mb-2 mr-2 md:m-0">
            <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
            Exporter
        </a>
        <button v-if="actions.indexOf('d') >= 0" class="btn btn-sm btn-error mb-2 mr-2 md:m-0"
                :disabled="enabled.indexOf('d') < 0" @click="$emit('add')">
            <TrashIcon class="w-6 h-6"/>
            Supprimer
        </button>
    </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from "vue-facing-decorator";
import {ArrowDownTrayIcon, ArrowUpTrayIcon, PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";

const ADD_EVENT = 'add';
const DELETE_EVENT = 'delete';
const IMPORT_EVENT = 'import';
const EXPORT_EVENT = 'export';

@Component({
    name: 'TableActionsComponent',
    components: {ArrowDownTrayIcon, ArrowUpTrayIcon, PlusCircleIcon, TrashIcon},
    emits: [ADD_EVENT, DELETE_EVENT, IMPORT_EVENT, EXPORT_EVENT],
})
export default class TableActionsComponent extends Vue {
    @Prop({default: 'ad'})
    private actions: string;

    @Prop({default: 'ai'})
    private enabled: string;
}
</script>
>