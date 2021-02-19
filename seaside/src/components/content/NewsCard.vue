<template>
  <div class="flex flex-row mt-4 bg-white dark:bg-gray-600 shadow-xl rounded-lg">
    <!-- Card -->
    <div class="flex-none"><!-- Left side -->
      <img v-if="card.image" class="w-60 object-cover rounded-l-lg "
           :src="card.image"
           alt="og:image"/>
      <img v-else class="w-60 object-cover rounded-l-lg opacity-30"
           src="/placeholder.png"
           alt="no-og-image" />
    </div>

    <div class="m-2"><!-- Middle -->
      <div class="flex flex-col text-black dark:text-gray-400">
        <a class="font-semibold text-xl" :href="card.link" v-html="card.title">{{ card.title }}</a>
        <span v-html="card.description" class="mt-2 text-base text-gray-600">{{ card.description }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';

@Component
export default class NewsCard extends Vue {
  @Prop() card?: News;

  created(): void {
    console.log(this.card);
  }
}
</script>