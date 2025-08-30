<template>
  <div class="flex flex-row items-center flex-wrap mt-2">
    <div v-for="tag in tags" :key="tag.name"
         :class="tag.status"
         class="badge m-1 pb-1">
      <button class="p-0 m-0" type="button" @click="onRemoveTag(tag)">
        <XMarkIcon class="inline-block w-4 h-4 mr-1 stroke-current"/>
      </button>
      {{ tag.name }}
    </div>
    <div class="dropdown">
      <input v-model.trim="tag"
             :placeholder="t('taginput.placeholder')"
             class="input input-ghost input-xs w-32 grow focus:outline-hidden placeholder:capitalize"
             type="text" @keydown="onKeydown">
      <ul v-if="displayProposal && proposal.length > 0"
          class="py-3 shadow-sm menu bg-neutral border-primary-content/20 border dropdown-content w-60">
        <li v-for="tag in proposal" v-bind:key="tag"><a
            :class="{'proposal-selected': proposal[proposalIndex] === tag}"
            @click="selectProposal">{{ tag }}</a></li>
      </ul>
    </div>
  </div>
</template>
<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { Observable, of } from 'rxjs';
import { XMarkIcon } from '@heroicons/vue/20/solid';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'TagInput',
  emits: ['update:modelValue', 'submit'],
  components: { XMarkIcon },
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class TagInput extends Vue {
  private t;
  @Prop({ default: () => [] }) private modelValue!: string[];
  @Prop({ default: () => () => of([]) }) private availableTagsHandler!: () => Observable<string[]>;

  private availableTags: string[] = [];
  private proposalIndex = -1;
  private displayProposal = false;
  private tag = '';
  private tags: TagView[] = [];

  get proposal(): string[] {
    return this.availableTags
        .filter(tag => this.tags.findIndex(tv => tv.name === tag) < 0)
        .filter(t => t.startsWith(this.tag)).slice(0, 4);
  }

  mounted(): void {
    this.availableTagsHandler().subscribe({
      next: (tags) => this.availableTags = tags,
    });
    this.tags = this.modelValue.map(v => ({ name: v, status: TagStatus.PRIMARY }));
  }

  private onRemoveTag(tv: TagView) {
    const idx = this.tags.indexOf(tv);
    this.tags.splice(idx, 1);
    this.emitInputEvent();
  }

  private onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'Enter':
        if (this.onKeyEnter()) {
          break;
        }
        // falls through
      case ' ':
      case ',':
        this.onKeySpaceOrComma(event);
        break;
      case 'Escape':
        this.onKeyEscape();
        break;
      case 'Backspace':
        this.onKeyBackspace(event);
        break;
      case 'ArrowDown':
        this.onKeyArrowDown(event);
        break;
      case 'ArrowUp':
        this.onKeyArrowUp(event);
        break;
      default:
        this.displayProposal = true;
    }
  }

  private onKeySpaceOrComma(event: KeyboardEvent): void {
    event.preventDefault();
    if (this.tag !== '' && this.tags.filter(tv => tv.name === this.tag).length === 0) {
      this.tags.push({ name: this.tag, status: TagStatus.PRIMARY });
    }
    this.tag = '';
    this.emitInputEvent();
  }

  private onKeyEnter(): boolean {
    if (this.proposalIndex !== -1) {
      this.tag = this.proposal[this.proposalIndex];
      this.proposalIndex = -1;
    }
    if (this.tag === '') {
      this.emitSubmitEvent();
      return true;
    }
    return false;
  }

  private onKeyEscape(): void {
    this.displayProposal = false;
    if (this.proposalIndex !== -1) {
      this.proposalIndex = -1;
    }
  }

  private onKeyBackspace(event: KeyboardEvent): boolean {
    this.displayProposal = true;
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
        lastTag.timeout = window.setTimeout(() => lastTag.status = TagStatus.PRIMARY, 1000);
      }
    }
    return true;
  }

  private onKeyArrowDown(event: KeyboardEvent): void {
    if (this.displayProposal && this.proposal.length - 1 > this.proposalIndex) {
      event.preventDefault();
      this.proposalIndex += 1;
    } else {
      this.displayProposal = true;
    }
  }

  private onKeyArrowUp(event: KeyboardEvent): void {
    if (this.displayProposal && this.proposalIndex > 0) {
      event.preventDefault();
      this.proposalIndex -= 1;
    }
  }

  private selectProposal(event: MouseEvent): void {
    this.tag = (event.target as HTMLElement).innerText;
    if (this.tag !== '' && this.tags.filter(tv => tv.name === this.tag).length === 0) {
      this.tags.push({ name: this.tag, status: TagStatus.PRIMARY });
    }
    this.tag = '';
    this.emitInputEvent();
  }

  // noinspection JSUnusedLocalSymbols
  private beforeUnmount(): void {
    this.tags.forEach(tv => {
      if (tv.timeout) {
        window.clearTimeout(tv.timeout);
      }
    });
  }

  private emitInputEvent(): void {
    this.$emit('update:modelValue', this.tags.map(tv => tv.name));
  }

  private emitSubmitEvent(): void {
    this.$emit('submit');
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