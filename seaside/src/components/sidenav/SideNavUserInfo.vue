<template>
  <div class="mt-8">
    <!-- User info -->
    <img
        class="h-12 w-12 rounded-full object-cover"
        src="https://appzzang.me/data/editor/1608/f9c387cb6bd7a0b004f975cd92cbe2d9_1471626325_6802.png"
        alt="enoshima profile"/>
    <h2
        class="mt-4 text-xl dark:text-gray-300 font-extrabold capitalize">
      Hello Enoshima
    </h2>
    <span class="text-sm dark:text-gray-300">
				<span class="font-semibold text-green-600 dark:text-green-300">
					Admin
				</span>
				id789038
			</span>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

@Component
export default class SideNavUserInfo extends Vue {
}
</script>