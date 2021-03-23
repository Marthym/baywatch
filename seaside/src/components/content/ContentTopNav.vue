<template>
  <nav class="flex flex-row justify-between border-b
        dark:border-gray-600 dark:text-gray-400 transition duration-500
				ease-in-out">
    <div class="flex">
      <!-- Top NavBar -->
      <a
          href="users-dashboard/"
          class="py-2 block text-green-500 border-green-500
						dark:text-green-200 dark:border-green-200
						focus:outline-none border-b-2 font-medium capitalize
						transition duration-500 ease-in-out">
        news
      </a>
      <button
          class="ml-6 py-2 block border-b-2 border-transparent
						focus:outline-none font-medium capitalize text-center
						focus:text-green-500 focus:border-green-500
						dark-focus:text-green-200 dark-focus:border-green-200
						transition duration-500 ease-in-out">
        feeds
      </button>
      <button
          class="ml-6 py-2 block border-b-2 border-transparent
						focus:outline-none font-medium capitalize text-center
						focus:text-green-500 focus:border-green-500
						dark-focus:text-green-200 dark-focus:border-green-200
						transition duration-500 ease-in-out">
        categories
      </button>
    </div>

    <div class="flex items-center select-none">
					<span
              class="hover:text-green-500 dark-hover:text-green-300
						cursor-pointer mr-3 transition duration-500 ease-in-out">

						<svg viewBox="0 0 512 512" class="h-5 w-5 fill-current">
							<path
                  d="M505 442.7L405.3
								343c-4.5-4.5-10.6-7-17-7H372c27.6-35.3 44-79.7
								44-128C416 93.1 322.9 0 208 0S0 93.1 0 208s93.1
								208 208 208c48.3 0 92.7-16.4 128-44v16.3c0 6.4
								2.5 12.5 7 17l99.7 99.7c9.4 9.4 24.6 9.4 33.9
								0l28.3-28.3c9.4-9.4 9.4-24.6.1-34zM208 336c-70.7
								0-128-57.2-128-128 0-70.7 57.2-128 128-128 70.7 0
								128 57.2 128 128 0 70.7-57.2 128-128 128z"></path>
						</svg>
					</span>

      <label>
        <input
            class="w-12 bg-transparent focus:outline-none"
            placeholder="Search"/>
      </label>

    </div>

  </nav>

</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component
export default class ContentTopNav extends Vue {
}
</script>