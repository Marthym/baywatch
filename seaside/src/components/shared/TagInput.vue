<template>
  <div class="flex flex-row items-center flex-wrap mt-2">
    <div v-for="tag in tags" :key="tag.name"
         class="badge m-1"
         :class="tag.status">
      <button type="button" @click="onRemoveTag(tag)" class="p-0 m-0">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
             class="inline-block w-4 h-4 mr-2 mb-0.5 stroke-current">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
      {{ tag.name }}
    </div>
    <input type="text" placeholder="Ajouter un tag..." class="input input-ghost input-xs w-32 flex-grow"
           v-model.trim="tag" @keydown="onKeydown">
  </div>
</template>
<script lang="ts">
import {Component, Prop, Vue} from "vue-property-decorator";

@Component({
  components: {},
})
export default class TagInput extends Vue {
  @Prop({default: []}) private value!: string[];

  private tag = '';
  private tags: TagView[] = [];

  mounted(): void {
    this.tags = this.value.map(v => ({name: v, status: TagStatus.PRIMARY}));
  }

  private onRemoveTag(tv: TagView) {
    console.log('remove', tv);
    const idx = this.tags.indexOf(tv);
    this.tags.splice(idx, 1);
    this.emitInputEvent();
  }

  private onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'Enter':
        if (this.tags.filter(tv => tv.name === this.tag).length === 0) {
          this.tags.push({name: this.tag, status: TagStatus.PRIMARY});
        }
        this.tag = '';
        this.emitInputEvent();
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
            this.emitInputEvent();
          } else {
            lastTag.status = TagStatus.ALERT;
            lastTag.timeout = setTimeout(() => lastTag.status = TagStatus.PRIMARY, 1000);
          }
        }
        break;
    }
  }

  private beforeUnmount(): void {
    this.tags.forEach(tv => {
      if (tv.timeout) {
        clearTimeout(tv.timeout);
      }
    })
  }

  private emitInputEvent(): void {
    this.$emit('input', this.tags.map(tv => tv.name));
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