<template>
  <div class="flex flex-row bg-white dark:bg-gray-600 shadow rounded-lg"
       v-bind:class="{ 'shadow-lg h-52 my-8 border border-gray-400': card.isActive, 'h-44 m-5': !card.isActive}"
       @click="$emit('activate')">
    <!-- Card -->
    <div class="flex-none"><!-- Left side -->
      <img v-if="card.data.image" class="h-full w-60 object-cover rounded-l-lg "
           :src="card.data.image"
           v-bind:class="{ 'opacity-30': card.isRead }"
           alt="og:image"/>
      <img v-else class="h-full w-60 object-contain rounded-l-lg"
           v-bind:class="{ 'opacity-30': card.isRead }"
           src="/placeholder.png"
           alt="no-og-image"/>
    </div>

    <div v-bind:class="{ 'm-6': card.isActive, 'm-4': !card.isActive}"><!-- Middle -->
      <div class="flex flex-col dark:text-gray-400"
           v-bind:class="{ 'text-black': !card.isRead, 'text-gray-400': card.isRead}">
        <a class="font-semibold text-xl" :href="card.data.link" v-html="card.data.title"></a>
        <span v-bind:class="{ 'text-gray-600': !card.isRead, 'text-gray-300': card.isRead}"
              v-html="card.data.description" class="mt-2 text-base"></span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {NewsView} from "@/components/content/model/NewsView";

@Component
export default class NewsCard extends Vue {
  @Prop() card?: NewsView;
}
</script>