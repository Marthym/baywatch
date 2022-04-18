<template>
  <NewsList/>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {defineAsyncComponent} from "vue";
import {NEWS_REPLACE_TAGS_MUTATION} from "@/techwatch/store/news/NewsStoreConstants";
import {setup} from "vue-class-component";
import {useRouter} from "vue-router";
import {Store, useStore} from "vuex";

const NewsList = defineAsyncComponent(() => import('@/components/newslist/NewsList.vue'));

@Options({
  name: 'HomePage',
  components: {
    NewsList,
  },
})
export default class HomePage extends Vue {
  private router = setup(() => useRouter());
  private store: Store<any> = setup(() => useStore());

  private mounted(): void {
    this.readQueryParameters();
  }

  private readQueryParameters(): void {
    const queryTag: string = this.router.currentRoute.query.tag as string;

    if (queryTag && queryTag.length > 0) {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, [queryTag]);
    } else {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, []);
    }
  }
}
</script>