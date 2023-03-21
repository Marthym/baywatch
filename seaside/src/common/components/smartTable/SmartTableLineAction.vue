<template>
  <button class="btn btn-sm btn-square btn-ghost" @click="$emit('click', $event)">
    <component :is="icon" class="h-6 w-6"/>
  </button>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {AdjustmentsHorizontalIcon} from '@heroicons/vue/24/outline';
import {FunctionalComponent} from "vue";

@Options({
  name: 'stla',
  components: {AdjustmentsHorizontalIcon},
  emits: ['click'],
})
export default class SmartTableLineAction extends Vue {
  @Prop({default: AdjustmentsHorizontalIcon}) private icon: FunctionalComponent;
}
</script>
