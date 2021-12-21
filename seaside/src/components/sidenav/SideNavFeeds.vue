<template>
  <ul class="mt-2 text-gray-600">
    <!-- Links -->
    <li class="mt-8">
      <a href="#home" class="flex ">
        <svg
            class="fill-current h-5 w-5 dark:text-gray-300"
            viewBox="0 0 24 24">
          <path
              d="M16 20h4v-4h-4m0-2h4v-4h-4m-6-2h4V4h-4m6
							4h4V4h-4m-6 10h4v-4h-4m-6 4h4v-4H4m0 10h4v-4H4m6
							4h4v-4h-4M4 8h4V4H4v4z"></path>
        </svg>
        <span
            class="ml-2 capitalize font-medium text-black
						dark:text-gray-300">
						dashboard
					</span>
      </a>
    </li>

    <li class="mt-8">
      <a href="#home" class="flex">
        <svg
            class="fill-current h-5 w-5 dark:text-gray-300"
            viewBox="0 0 24 24">
          <path
              d="M19 19H5V8h14m-3-7v2H8V1H6v2H5c-1.11 0-2 .89-2
							2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0
							00-2-2h-1V1m-1 11h-5v5h5v-5z"></path>
        </svg>
        <span
            class="ml-2 capitalize font-medium text-black
						dark:text-gray-300">
						calendar
					</span>
      </a>
    </li>

    <li
        class="mt-8 shadow py-2 bg-white dark:bg-gray-200 rounded-lg
				-ml-4">
      <a href="#home" class="flex pl-4">
        <svg class="fill-current h-5 w-5" viewBox="0 0 24 24">
          <path
              d="M12 4a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0
							014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4
							8-4z"></path>
        </svg>
        <span class="ml-2 capitalize font-medium">users</span>
      </a>
    </li>

    <li class="mt-8">
      <a href="#home" class="flex">
        <svg
            class="fill-current h-5 w-5 dark:text-gray-300"
            viewBox="0 0 24 24">
          <path
              d="M12 13H7v5h5v2H5V10h2v1h5v2M8
							4v2H4V4h4m2-2H2v6h8V2m10 9v2h-4v-2h4m2-2h-8v6h8V9m-2
							9v2h-4v-2h4m2-2h-8v6h8v-6z"></path>
        </svg>
        <span
            class="ml-2 capitalize font-medium text-black
						dark:text-gray-300">
						tasks
					</span>
      </a>
    </li>

  </ul>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

@Options({name: 'SideNavFeeds'})
export default class SideNavFeeds extends Vue {
}
</script>