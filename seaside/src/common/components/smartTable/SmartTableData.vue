<template>
  <td>
    <slot/>
  </td>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";

@Options({
  name: 'std',
  components: {},
  emits: [],
})
export default class SmartTableData extends Vue {
}
</script>
