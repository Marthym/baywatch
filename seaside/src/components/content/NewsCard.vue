<template>
  <div class="flex flex-row mt-4 justify-between bg-white dark:bg-gray-600 shadow-xl rounded-lg">
    <!-- Card -->
    <div class="flex-none"><!-- Left side -->
      <img class="w-60 object-cover rounded-l-lg"
           src="https://inews.gtimg.com/newsapp_match/0/8693739867/0"
           alt=""/>
    </div>

    <div class="flex-grow m-2"><!-- Middle -->
      <div class="flex flex-col text-black dark:text-gray-400">
        <span class="font-semibold text-xl">{{ card.title }}</span>
        <span class="mt-2 text-sm text-gray-600">{{ card.description }}</span>
        <div class="mt-2 w-full text-right"><a class="text-sm text-blue-600 underline" :href="card.link">ouvrir</a></div>
      </div>
    </div>

    <div class="flex"><!-- Rigth side -->
      <div class="mr-16 flex flex-col capitalize text-gray-600 dark:text-gray-400">
        <span>project</span>
        <span class="mt-2 text-black dark:text-gray-200">
							Aero treck
						</span>
        <span class="text-red-600 dark:text-red-400">
							search
						</span>
        <span>2 more...</span>
      </div>

      <div
          class="mr-16 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
        <span>role</span>
        <span class="mt-2 text-black dark:text-gray-200">
							Designer
						</span>
        <span class="text-red-600 dark:text-red-400">
							Designer
						</span>
      </div>

      <div
          class="mr-16 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
        <span>status</span>
        <span class="mt-2 text-black dark:text-gray-200">
							None
						</span>
        <span class="text-red-600 dark:text-red-400">
							in work
						</span>
      </div>

      <div
          class="mr-8 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
        <span>final date</span>
        <span class="mt-2 text-green-400 dark:text-green-200">
							20.02.2020
						</span>
        <span class="text-red-600 dark:text-red-400">
							07.02.2020 11:00
						</span>
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