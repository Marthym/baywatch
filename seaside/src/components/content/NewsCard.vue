<template>
  <div class="mt-2 flex px-4 py-4 justify-between bg-white
				dark:bg-gray-600 shadow-xl rounded-lg cursor-pointer ">
    <!-- Card -->

    <div class="flex justify-between">
      <!-- Left side -->

      <img
          class="h-12 w-12 rounded-full object-cover"
          src="https://inews.gtimg.com/newsapp_match/0/8693739867/0"
          alt=""/>

      <div
          class="ml-4 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
        <span>name</span>
        <span class="mt-2 text-black dark:text-gray-200">
							{{ card.title }}
						</span>
      </div>

      <div
          class="ml-12 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
        <span>login</span>
        <span class="mt-2 text-black dark:text-gray-200">
							carmen.bel
						</span>

      </div>

    </div>

    <div class="flex">
      <!-- Rigt side -->

      <div
          class="mr-16 flex flex-col capitalize text-gray-600
						dark:text-gray-400">
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