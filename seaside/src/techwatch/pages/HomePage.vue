<template>
    <NewsList/>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-facing-decorator';
import {Router, useRouter} from "vue-router";
import {Store, useStore} from "vuex";
import {NEWS_REPLACE_TAGS_MUTATION} from "@/common/model/store/NewsStore.type";
import NewsList from "@/techwatch/components/newslist/NewsList.vue";

@Component({
    name: 'HomePage',
    components: {
        NewsList,
    },
    setup() {
        return {
            router: useRouter(),
            store: useStore(),
        }
    }
})
export default class HomePage extends Vue {
    private router: Router;
    private store: Store<any>;

    /**
     * @see mounted
     */
    private mounted(): void {
        this.readQueryParameters();
    }

    private readQueryParameters(): void {
        const queryTag: string = this.router.currentRoute.value.query.tag as string;

        if (queryTag && queryTag.length > 0) {
            this.store.commit(NEWS_REPLACE_TAGS_MUTATION, [queryTag]);
        } else {
            this.store.commit(NEWS_REPLACE_TAGS_MUTATION, []);
        }
    }
}
</script>