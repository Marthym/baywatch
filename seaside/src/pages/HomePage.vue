<template>
  <MainContent/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

const MainContent = () => import('../components/newslist/NewsList.vue');

@Component({
  components: {
    MainContent,
  },
})
export default class Home extends Vue {
}
</script>