<template>
  <FeedsList/>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import FeedsList from '@/components/feedslist/FeedsList.vue';

@Options({
  name: 'FeedsConfigPage',
  components: {
    FeedsList,
  },
})
export default class FeedsConfigPage extends Vue {
}
</script>