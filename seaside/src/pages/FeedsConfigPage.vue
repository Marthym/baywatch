<template>
  <p>Tset</p>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

// const MainContent = () => import('../components/content/NewsList.vue');

@Component({
  components: {
    // MainContent,
  },
})
export default class FeedsConfigPage extends Vue {
}
</script>