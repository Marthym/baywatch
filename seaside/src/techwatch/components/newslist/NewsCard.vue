<template>
  <div class="flex flex-col lg:flex-row bg-base-100 shadow rounded-lg"
       :class="{
        'shadow-lg lg:h-60 my-8 border border-base-200': card.isActive,
        'lg:h-56 m-5': !card.isActive,
        'opacity-30': card.data.state?.read && !card.isActive,
       }" @click="$emit('activate')">

    <figure class="flex-none">
      <img :src="cardImage"
           :srcset="card.srcset"
           :sizes="card.sizes"
           class="w-full h-24 lg:h-full lg:w-60 object-cover rounded-t-lg lg:rounded-none lg:rounded-l-lg
              bg-no-repeat bg-cover lg:bg-contain bg-center
              bg-[url('/placeholder.svg')] text-transparent"
           loading="lazy"
           :alt="card.data.title"
           @error.stop.prevent="onImageError"
      />
    </figure>

    <div :class="{ 'm-6': card.isActive, 'm-4': !card.isActive}" class="flex-grow">
      <div class="flex flex-col h-full overflow-hidden">
        <a class="font-semibold text-xl" target="_blank" :href="card.data.link" :title="card.data.link"
           v-html="card.data.title"></a>
        <span v-html="card.data.description" class="mt-2 text-base flex-grow max-h-80 overflow-hidden"></span>

        <div class="flex flex-row flex-wrap-reverse lg:justify-end text-xs mt-2">
          <slot name="actions"></slot>
          <span class="grow"></span>
          <span class="italic self-start lg:block">{{ publication }}</span>
          <div class="text-right lg:whitespace-nowrap order-last basis-full">
            <button class="badge badge-neutral badge-sm m-px rounded lg:whitespace-nowrap order-last basis-full" @click.stop="$emit('addFilter', {type: 'feed', entity: f})"
                    v-for="f in card.data.feeds">{{ f.name }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-facing-decorator';
import {NewsView} from "@/techwatch/components/newslist/model/NewsView";
import {ImgHTMLAttributes} from "vue";

const EMPTY_IMAGE_DATA: string = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';

@Component({name: 'NewsCard', emits: ['activate', 'addFilter']})
export default class NewsCard extends Vue {
  @Prop() card: NewsView;

  get cardImage() {
    return this.card?.data?.image ?? EMPTY_IMAGE_DATA;
  }

  get publication(): string | undefined {
    if (this.card) {
      return new Date(this.card.data.publication).toLocaleDateString(navigator.languages, {
        timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        year: 'numeric', month: 'short', day: '2-digit', hour: "2-digit", minute: "2-digit"
      });
    } else {
      return undefined;
    }
  }

  private onImageError(e: Event) {
    const img = e.target as ImgHTMLAttributes;
    if (img.srcset) {
      img.srcset = '';
      return;
    }
    img.src = EMPTY_IMAGE_DATA;
  }
}
</script>