<template>
  <FeedsList/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';

const FeedsList = () => import('@/components/feedslist/FeedsList.vue');

@Component({
  components: {
    FeedsList,
  },
})
export default class FeedsConfigPage extends Vue {
}
</script>