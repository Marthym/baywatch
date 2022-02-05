<template>
  <aside class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-10 flex flex-col flex-shrink-0 w-64 max-h-screen overflow-hidden
  transition-all transform bg-gray-900 shadow-lg lg:z-auto lg:static lg:shadow-none"
         :class="{'-translate-x-full lg:translate-x-0': !state.open}">
    <SideNavHeader :unread="baywatchStats.unread"/>

    <SideNavUserInfo/>
    <SideNavStatistics :statistics="baywatchStats" :isLoggedIn="user.isAuthenticated"/>

    <SideNavTags v-if="user.isAuthenticated"/>
    <SideNavManagement v-if="user.isAuthenticated && store.getters['user/hasRoleManager']"/>

    <SideNavImportantActions :isLoggedIn="user.isAuthenticated" @logout="logoutUser()"/>
  </aside>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavManagement from './SideNavManagement.vue';
import SideNavStatistics from "@/components/sidenav/SideNavStatistics.vue";

import authenticationService from "@/services/AuthenticationService";
import {SidenavState} from "@/store/sidenav/sidenav";
import {StatisticsState} from "@/store/statistics/statistics";
import {RELOAD_ACTION} from "@/store/statistics/StatisticsConstants";
import {useStore} from "vuex";
import {setup} from "vue-class-component";
import {defineAsyncComponent} from "vue";
import {UserState} from "@/store/user/user";
import {LOGOUT_MUTATION} from "@/store/user/UserConstants";

const SideNavTags = defineAsyncComponent(() => import('./SideNavTags.vue').then(m => m.default))
const SideNavUserInfo = defineAsyncComponent(() => import('./SideNavUserInfo.vue').then(m => m.default));

@Options({
  name: 'SideNav',
  components: {
    SideNavStatistics,
    SideNavHeader,
    SideNavUserInfo,
    SideNavTags,
    SideNavManagement,
    SideNavImportantActions,
  },
})
export default class SideNav extends Vue {
  private store = setup(() => useStore());
  private state: SidenavState = setup(() => useStore().state.sidenav);
  private baywatchStats: StatisticsState = setup(() => useStore().state.statistics);
  private user: UserState = setup(() => useStore().state.user);

  logoutUser(): void {
    authenticationService.logout().subscribe(() => {
      this.store.commit(LOGOUT_MUTATION);
      this.store.dispatch(RELOAD_ACTION);
      this.$router.go(0);
    });
  }
}
</script>
