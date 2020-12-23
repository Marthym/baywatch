<template>
  <div class="flex flex-row border-b items-center justify-between pb-2">
    <span class="text-lg font-semibold capitalize dark:text-gray-300">
				Baywatch
			</span>

    <span class="relative ">
				<a class="hover:text-green-500 dark-hover:text-green-300 text-gray-600 dark:text-gray-300"
           href="inbox/">
					<svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round">
						<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
						<path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
					</svg>
				</a>
				<div class="absolute w-2 h-2 rounded-full bg-green-500 dark-hover:bg-green-300 right-0 mb-5 bottom-0"></div>
			</span>

  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component
export default class SideNavHeader extends Vue {
}
</script>