<template>
  <div class="flex flex-row items-center flex-wrap mt-2">
    <div v-for="tag in tags" :key="tag.name"
         class="badge m-1"
         :class="tag.status">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
           class="inline-block w-4 h-4 mr-2 stroke-current">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
      </svg>
      {{ tag.name }}
    </div>
    <input type="text" placeholder="Ajouter un tag..." class="input input-ghost input-xs w-32 flex-grow"
           v-model.trim="tag" @keydown="onKeydown">
  </div>
</template>
<script lang="ts">
import {Component, Vue} from "vue-property-decorator";

@Component({
  components: {},
})
export default class TagInput extends Vue {
  private tags: TagView[] = [
    {name: 'test', status: TagStatus.PRIMARY},
    {name: 'info', status: TagStatus.PRIMARY},
  ];
  private tag = '';

  private onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'Enter':
        if (this.tags.filter(tv => tv.name === this.tag).length === 0) {
          this.tags.push({name: this.tag, status: TagStatus.PRIMARY});
        }
        this.tag = '';
        break;
      case 'Backspace':
        if (this.tag === '' && this.tags.length > 0) {
          event.preventDefault();
          const lastTag = this.tags[this.tags.length - 1];
          if (lastTag.status === TagStatus.ALERT) {
            this.tags.pop();
            if (lastTag.timeout) {
              clearTimeout(lastTag.timeout);
            }
          } else {
            lastTag.status = TagStatus.ALERT;
            lastTag.timeout = setTimeout(() => lastTag.status = TagStatus.PRIMARY, 1000);
          }
        }
    }
  }
}

enum TagStatus {
  PRIMARY = 'badge-primary', ALERT = 'badge-error'
}

type TagView = {
  name: string;
  status: TagStatus;
  timeout?: number;
}
</script>