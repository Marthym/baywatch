<template>
    <div class="fixed right-0 h-full w-full bg-base-200 bg-opacity-60 z-20 lg:hidden"
         :class="{'hidden': !state.open}"
         @click.stop="sideNavToggle"
    ></div>
</template>

<script lang="ts">
import {Component, Vue} from "vue-facing-decorator";
import {useStore} from "vuex";
import {SidenavMutation} from "@/store/sidenav/SidenavMutation.enum";
import {SidenavState} from "@/store/sidenav/sidenav";

@Component({
    name: 'SideNavOverlay',
    setup() {
        const store = useStore();
        return {
            store: store,
            state: store.state.sidenav,
        }
    }
})
export default class SideNavOverlay extends Vue {
    private store;
    private state: SidenavState;

    public sideNavToggle(): void {
        this.store.commit(SidenavMutation.TOGGLE);
    }
}
</script>

<style scoped>

</style>