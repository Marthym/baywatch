<template>
    <td class="text-ellipsis overflow-hidden">
        <slot/>
    </td>
</template>

<script lang="ts">
import {Component, Vue} from "vue-facing-decorator";

@Component({
    name: 'std',
    components: {},
    emits: [],
})
export default class SmartTableData extends Vue {
}
</script>
