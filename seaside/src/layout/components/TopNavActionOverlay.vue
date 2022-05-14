<template>
  <div class="relative">
    <div class="w-full h-full z-10 fixed bg-base-200 bg-opacity-60 -mt-2
      lg:right-0 lg:w-80 xl:w-96 lg:h-fit lg:bg-transparent lg:mr-4">
      <div class="h-fit opacity-100 p-4 bg-base-200 lg:rounded-lg">
        <slot name="content"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";

@Options({name: 'TopNavActionOverlay'})
export default class TopNavActionOverlay extends Vue {
}
</script>
