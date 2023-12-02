<template>
    <div class="md:join mb-2">
        <button class="btn btn-sm join-item mb-2 mr-2 md:m-0" @click="$emit('clickAdd')">
            <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
            <span>Ajouter</span>
        </button>
        <button class="btn btn-sm btn-ghost join-item mb-2 mr-2 md:m-0" @click="$emit('clickImport')">
            <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
            Importer
        </button>
        <a class="btn btn-sm btn-ghost join-item mb-2 mr-2 md:m-0" :href="`${BASEURL}/opml/export/baywatch.opml`">
            <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
            Exporter
        </a>
        <button class="btn btn-sm btn-error join-item mb-2 mr-2 md:m-0" :disabled="!deleteEnable" @click="$emit('clickDelete')">
            <TrashIcon class="w-6 h-6"/>
            Supprimer
        </button>
    </div>
</template>
<script lang="ts">
import {Component, Prop, Vue} from 'vue-facing-decorator';
import {ArrowDownTrayIcon, ArrowUpTrayIcon, PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";

@Component({
    name: 'FeedActions',
    components: {PlusCircleIcon, ArrowDownTrayIcon, ArrowUpTrayIcon, TrashIcon},
    emits: ['clickAdd', 'clickImport', 'clickDelete'],
})
export default class FeedActions extends Vue {
    private readonly BASEURL = import.meta.env.VITE_API_BASE_URL;
    @Prop({default: false}) private deleteEnable!: boolean;
}
</script>