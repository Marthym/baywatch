<template>
  <div class="btn-group mb-2">
    <button class="btn btn-sm btn-primary" @click="$emit('clickAdd')">
      <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
           xmlns="http://www.w3.org/2000/svg">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
      </svg>
      Ajouter
    </button>
    <button class="btn btn-sm btn-primary" @click="$emit('clickImport')">
      <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
           xmlns="http://www.w3.org/2000/svg">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
      </svg>
      Importer
    </button>
    <a class="btn btn-sm btn-primary" :href="`${BASEURL}/opml/export/baywatch.opml`">
      <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
           xmlns="http://www.w3.org/2000/svg">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
      </svg>
      Exporter
    </a>
    <button class="btn btn-sm btn-error" :disabled="!deleteEnable" @click="$emit('clickDelete')">
      <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd"
              d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
              clip-rule="evenodd"></path>
      </svg>
      Suprimer
    </button>
  </div>
</template>
<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';

@Options({
  name: 'FeedActions',
  emits: ['clickAdd', 'clickImport', 'clickDelete'],
})
export default class FeedActions extends Vue {
  private readonly BASEURL = import.meta.env.VITE_API_BASE_URL;
  @Prop({default: false}) private deleteEnable!: boolean;
}
</script>