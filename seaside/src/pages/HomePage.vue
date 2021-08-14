<template>
  <NewsList/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

const NewsList = () => import('@/components/newslist/NewsList.vue');

@Component({
  components: {
    NewsList,
  },
})
export default class Home extends Vue {
}
</script>