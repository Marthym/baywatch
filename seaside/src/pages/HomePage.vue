<template>
  <MainContent/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

const MainContent = () => import('../components/content/MainContent.vue');

@Component({
  components: {
    MainContent,
  },
})
export default class Home extends Vue {
}
</script>