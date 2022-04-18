<template>
  <div class="flex flex-col lg:flex-row bg-gray-600 shadow rounded-lg"
       :class="{
        'shadow-lg lg:h-60 my-8 border border-gray-400': card.isActive,
        'lg:h-56 m-5': !card.isActive,
        'opacity-30': card.data.read && !card.isActive,
       }" @click="$emit('activate')">

    <figure class="flex-none">
      <img v-if="card.data.image"
           :src="card.data.image"
           class="w-full h-24 lg:h-full lg:w-60 object-cover rounded-t-lg lg:rounded-none lg:rounded-l-lg"
           :alt="card.data.title"/>
      <img v-else
           class="w-full h-24 lg:w-60 lg:h-full object-contain rounded-t-lg lg:rounded-none lg:rounded-l-lg opacity-10"
           src="/placeholder.png"
           :alt="card.data.title"/>
    </figure>

    <div :class="{ 'm-6': card.isActive, 'm-4': !card.isActive}" class="flex-grow">
      <div class="flex flex-col h-full">
        <a class="font-semibold text-xl" target="_blank" :href="card.data.link" :title="card.data.link" v-html="card.data.title"></a>
        <span v-html="card.data.description" class="mt-2 text-base flex-grow overflow-hidden"></span>

        <div class="flex flex-row justify-end items-end text-xs">
          <slot name="actions"></slot>
          <span class="grow"></span>
          <div class="italic text-right">
            <div class="badge badge-xs p-2 rounded"
                 v-for="f in card.feeds">{{ f }}
            </div>
            <div class="self-end">{{ publication }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import {NewsView} from "@/components/newslist/model/NewsView";

@Options({name: 'NewsCard'})
export default class NewsCard extends Vue {
  @Prop() card?: NewsView;

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
}
</script>