<template>
  <NewsList/>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {defineAsyncComponent} from "vue";

const NewsList = defineAsyncComponent(() => import('@/components/newslist/NewsList.vue'));

@Options({
  name: 'HomePage',
  components: {
    NewsList,
  },
})
export default class HomePage extends Vue {
}
</script>