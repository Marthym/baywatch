<template>
  <div :class="{
        'shadow-lg lg:m-5 lg:h-60 my-5 border border-base-200': card.isActive,
        'lg:flex-row': displayAsMagazine,
        'lg:max-w-72 lg:h-80 lg:flex-col': displayAsCard,
        'lg:h-56 m-5': !card.isActive,
        'opacity-30': card.data.state?.read && !card.isActive,
       }"
       class="flex flex-col bg-base-100 shadow rounded-lg w-full" @click="$emit('activate')">

    <figure class="flex-none">
      <img :alt="card.data.title"
           :class="{
            'lg:rounded-none lg:rounded-l-lg lg:h-full lg:w-60': displayAsMagazine,
            'w-full': displayAsCard,
           }"
           :sizes="card.sizes"
           :src="cardImage"
           :srcset="card.srcset"
           class="h-24 object-cover rounded-t-lg
              bg-no-repeat bg-cover lg:bg-contain bg-center w-full
              bg-[url('/placeholder.svg')] text-transparent"
           loading="lazy"
           @error.stop.prevent="onImageError"
      />
    </figure>

    <div :class="{ 'm-6': card.isActive, 'm-4': !card.isActive}" class="flex-grow">
      <div class="flex flex-col h-full overflow-hidden">
        <a
            :class="{
              'text-xl': displayAsMagazine,
              'grow line-clamp-5': displayAsCard,
            }"
            :href="card.data.link" :title="card.data.link"
            class="font-semibold" target="_blank"
            @auxclick="$emit('clickTitle')"
            @click="$emit('clickTitle')">{{ card.data.title }}</a>
        <span :class="{'lg:hidden': displayAsCard}"
              class="mt-2 text-base flex-grow max-h-80 overflow-hidden"
              v-html="card.data.description"></span>

        <div class="flex flex-row flex-wrap-reverse lg:justify-end text-xs mt-2">
          <slot name="actions"></slot>
          <span class="grow"></span>
          <span class="italic self-start lg:block">{{ publication }}</span>
          <div class="text-right lg:whitespace-nowrap order-last basis-full">
            <button v-for="f in card.data.feeds"
                    class="badge badge-neutral badge-sm m-px rounded lg:whitespace-nowrap order-last basis-full"
                    @click.stop="$emit('addFilter', {type: 'feed', entity: f})">{{ f.name }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { NewsView } from '@/techwatch/components/newslist/model/NewsView';
import { ImgHTMLAttributes } from 'vue';
import { useI18n } from 'vue-i18n';

const EMPTY_IMAGE_DATA: string = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';

@Component({
  name: 'NewsCard', emits: ['activate', 'addFilter', 'clickTitle'],
  setup() {
    const { d } = useI18n();
    return { d };
  },
})
export default class NewsCard extends Vue {
  @Prop({ default: 'MAGAZINE' }) viewMode!: 'MAGAZINE' | 'CARD';
  @Prop() card: NewsView;
  private d;

  get displayAsMagazine(): boolean {
    return this.viewMode === 'MAGAZINE';
  }

  get displayAsCard(): boolean {
    return this.viewMode === 'CARD';
  }

  get cardImage() {
    return this.card?.data?.image ?? EMPTY_IMAGE_DATA;
  }

  get publication(): string | undefined {
    if (this.card) {
      return this.d(new Date(this.card.data.publication), 'long');
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