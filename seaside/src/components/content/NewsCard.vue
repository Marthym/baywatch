<template>
  <div class="h-44 flex flex-row mt-4 bg-white dark:bg-gray-600 shadow-xl rounded-lg">
    <!-- Card -->
    <div class="flex-none"><!-- Left side -->
      <img v-if="card.image" class="h-full w-60 object-cover rounded-l-lg "
           :src="card.image"
           alt="og:image"/>
      <img v-else class="h-full w-60 object-contain rounded-l-lg opacity-30"
           src="/placeholder.png"
           alt="no-og-image"/>
    </div>

    <div class="m-2 ml-4"><!-- Middle -->
      <div class="flex flex-col text-black dark:text-gray-400">
        <a class="font-semibold text-xl" :href="card.link" v-html="card.title">{{ card.title }}</a>
        <span v-html="card.description" class="mt-2 text-base text-gray-600">{{ card.description }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {News} from "@/services/model/News";

@Component
export default class NewsCard extends Vue {
  @Prop() card?: News;
  
}
</script>