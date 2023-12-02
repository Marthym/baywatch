<template>
  <FeedsList/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-facing-decorator';
import FeedsList from '@/configuration/components/feedslist/FeedsList.vue';

@Component({
  name: 'FeedsConfigPage',
  components: {
    FeedsList,
  },
})
export default class FeedsConfigPage extends Vue {
}
</script>