<template>
  <thead>
  <tr class="bg-gray-200 text-gray-600 uppercase text-sm leading-normal">
    <th class="py-3 px-6 text-left">Project</th>
    <th class="py-3 px-6 text-left">Client</th>
    <th class="py-3 px-6 text-center">Users</th>
    <th class="py-3 px-6 text-center">Status</th>
    <th class="py-3 px-6 text-center">Actions</th>
  </tr>
  </thead>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

@Options({name: 'FeedsListHeader'})
export default class FeedsListHeader extends Vue {
}
</script>
