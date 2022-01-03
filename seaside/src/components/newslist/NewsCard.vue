<template>
  <div class="flex flex-col lg:flex-row bg-white dark:bg-gray-600 shadow rounded-lg"
       :class="{
        'shadow-lg lg:h-60 my-8 border border-gray-400': card.isActive,
        'lg:h-56 m-5': !card.isActive
       }" @click="$emit('activate')">

    <figure class="flex-none">
      <img v-if="card.data.image"
           :src="card.data.image"
           class="w-full h-24 lg:h-full lg:w-60 object-cover rounded-t-lg lg:rounded-none lg:rounded-l-lg"
           :class="{ 'opacity-30': card.data.read }"
           alt="og:image"/>
      <img v-else
           class="w-full h-24 lg:w-60 lg:h-full object-contain rounded-t-lg lg:rounded-none lg:rounded-l-lg"
           :class="{ 'opacity-30': card.data.read }"
           src="/placeholder.png"
           alt="no-og-image"/>
    </figure>

    <div :class="{ 'm-6': card.isActive, 'm-4': !card.isActive}" class="flex-grow">
      <div class="flex flex-col h-full"
           :class="{
              'text-black': !card.data.read || card.isActive,
              'text-gray-400': card.data.read && !card.isActive,
              'dark:text-gray-200': !card.data.read || card.isActive,
              'dark:text-gray-500': card.data.read && !card.isActive
            }">
        <a class="font-semibold text-xl" :href="card.data.link" :title="card.data.link" v-html="card.data.title"></a>
        <span v-bind:class="{
                'text-gray-600': !card.data.read || card.isActive,
                'text-gray-300': card.data.read && !card.isActive,
                'dark:text-gray-300': !card.data.read || card.isActive,
                'dark:text-gray-500': card.data.read && !card.isActive,
              }"
              v-html="card.data.description" class="mt-2 text-base flex-grow overflow-hidden"></span>

        <div class="flex flex-row-reverse text-sm italic mt-1">
          <span>{{ card.data.publication }}</span>
        </div>
        <div class="flex flex-row-reverse text-xs italic" v-bind:class="{'-mb-2': card.isActive}">
          <span>{{ card.feeds.join(' | ') }}</span>
          <span class="flex-grow"></span>
          <slot name="actions"></slot>
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
}
</script>