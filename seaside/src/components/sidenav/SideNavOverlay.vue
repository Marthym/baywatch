<template>
  <div class="fixed right-0 h-full w-full bg-base-200 bg-opacity-60 lg:hidden"
       :class="{'hidden': !state.open}"
       @click.stop="sideNavToggle"
  ></div>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {SidenavState} from "@/store/sidenav/sidenav";
import {SidenavMutation} from "@/store/sidenav/SidenavMutation.enum";

@Options({
  name: 'SideNavOverlay',
})
export default class SideNavOverlay extends Vue {
  private store = setup(() => useStore());
  private state: SidenavState = setup(() => useStore().state.sidenav);

  public sideNavToggle(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }
}
</script>

<style scoped>

</style>