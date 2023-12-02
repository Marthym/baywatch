<template>
  <NewsList/>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import { Store, useStore } from 'vuex';
import {
  NEWS_REPLACE_TAGS_MUTATION,
  NEWS_RESET_FILTERS_MUTATION,
  NEWS_TOGGLE_KEEP_MUTATION,
  NEWS_TOGGLE_UNREAD_MUTATION,
} from '@/common/model/store/NewsStore.type';
import NewsList from '@/techwatch/components/newslist/NewsList.vue';
import { actionServiceReload } from '@/common/services/ReloadActionService';

@Component({
  name: 'HomePage',
  components: {
    NewsList,
  },
  beforeRouteEnter(to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) {
    next((vm: HomePage) => {
      vm.onPageEnter(to);
      return true;
    });
  },
  beforeRouteLeave(to, from) {
    this.onPageLeave(from);
  },
  setup() {
    return {
      store: useStore(),
    };
  },
})
export default class HomePage extends Vue {
  private store: Store<any>;

  public onPageEnter(to: RouteLocationNormalized) {
    const queryTag: string = to.query.tag as string;

    if (queryTag && queryTag.length > 0) {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, [queryTag]);
    } else {
      this.store.commit(NEWS_REPLACE_TAGS_MUTATION, []);
    }

    if (to.path === '/clipped') {
      this.store.commit(NEWS_RESET_FILTERS_MUTATION);
      this.store.commit(NEWS_TOGGLE_KEEP_MUTATION);
      this.store.commit(NEWS_TOGGLE_UNREAD_MUTATION);
    }
    actionServiceReload('news');
  }

  public onPageLeave(from: RouteLocationNormalized) {
    if (from.path === '/clipped') {
      this.store.commit(NEWS_RESET_FILTERS_MUTATION);
    }
  }
}
</script>