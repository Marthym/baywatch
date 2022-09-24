<template>
  <aside class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-30 flex flex-col flex-shrink-0 w-4/5 max-h-screen overflow-hidden
  transition-all transform bg-base-200 shadow-lg lg:z-auto lg:static lg:shadow-none"
         :class="{'-translate-x-full lg:translate-x-0': !state.open}">
    <SideNavHeader/>

    <SideNavUserInfo/>

    <SideNavFilters v-if="user.isAuthenticated"/>
    <SideNavManagement @logout="logoutUser()"/>
    <span class="text-xs text-neutral font-bold mx-auto -mb-3">{{ VERSION }} • {{ COMMIT }}</span>
  </aside>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavManagement from './SideNavManagement.vue';

import {LOGOUT_MUTATION} from "@/store/user/UserConstants";
import {SidenavState} from "@/store/sidenav/sidenav";
import SideNavUserInfo from "@/layout/components/sidenav/SideNavUserInfo.vue";
import SideNavFilters from "@/layout/components/sidenav/SideNavFilters.vue";
import {UserState} from "@/store/user/user";
import {useStore} from "vuex";
import {setup} from "vue-class-component";
import {switchMap} from "rxjs/operators";
import {useRouter} from "vue-router";
import authenticationService from "@/security/services/AuthenticationService";
import serverEventService from '@/techwatch/services/ServerEventService'

@Options({
  name: 'SideNav',
  components: {
    SideNavHeader,
    SideNavUserInfo,
    SideNavFilters,
    SideNavManagement,
  },
})
export default class SideNav extends Vue {
  private readonly VERSION = import.meta.env.VITE_BW_VERSION;
  private readonly COMMIT = import.meta.env.VITE_BW_COMMIT;

  private router = setup(() => useRouter());
  private store = setup(() => useStore());
  private state: SidenavState = setup(() => useStore().state.sidenav);
  private user: UserState = setup(() => useStore().state.user);

  logoutUser(): void {
    serverEventService.close().pipe(
        switchMap(() => authenticationService.logout())
    ).subscribe(() => {
      this.store.commit(LOGOUT_MUTATION);
      this.router.go(0);
    });
  }
}
</script>
