<template>
  <header>
    <h2 class="my-4 text-4xl font-semibold dark:text-gray-400">Latest news</h2>

    <div class="pb-2 flex justify-end
                text-gray-600 dark:text-gray-400 border-b dark:border-gray-600">
      <div>
        <span><span class="text-green-500 dark:text-green-200">{{ users }}</span>users; </span>
        <span><span class="text-green-500 dark:text-green-200">{{ feeds }}</span>feeds; </span>
        <span><span class="text-green-500 dark:text-green-200">{{ news }}</span>news</span>
      </div>
    </div>
  </header>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';

@Component
export default class ContentHeader extends Vue {
  @Prop() users?: number;
  @Prop() feeds?: number;
  @Prop() news?: number;
}
</script>