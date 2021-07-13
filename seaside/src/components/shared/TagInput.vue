<template>
  <div class="flex flex-row items-center flex-wrap mt-2">
    <div v-for="tag in tags" :key="tag"
         class="badge badge-primary m-1">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
           class="inline-block w-4 h-4 mr-2 stroke-current">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
      </svg>
      {{ tag }}
    </div>
    <input type="text" placeholder="Ajouter un tag..." class="input input-ghost input-xs w-32"
           v-model.trim="tag" @keydown="onKeydown">
  </div>
</template>
<script lang="ts">
import {Component, Vue} from "vue-property-decorator";

@Component({
  components: {},
})
export default class TagInput extends Vue {
  private tags: string[] = ['test', 'info'];
  private tag = '';

  private onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case "Enter":
        if (this.tags.indexOf(this.tag) === -1) {
          this.tags.push(this.tag);
        }
        this.tag = '';
    }
  }
}
</script>