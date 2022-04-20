<template>
  <aside class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-20 flex flex-col flex-shrink-0 w-4/5 max-h-screen overflow-hidden
  transition-all transform bg-base-200 shadow-lg lg:z-auto lg:static lg:shadow-none"
         :class="{'-translate-x-full lg:translate-x-0': !state.open}">
    <SideNavHeader/>

    <SideNavUserInfo/>

    <SideNavTags v-if="user.isAuthenticated"/>
    <SideNavManagement @logout="logoutUser()"/>
  </aside>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavManagement from './SideNavManagement.vue';

import authenticationService from "@/services/AuthenticationService";
import serverEventService from '@/techwatch/services/ServerEventService'
import {SidenavState} from "@/store/sidenav/sidenav";
import {useStore} from "vuex";
import {setup} from "vue-class-component";
import {defineAsyncComponent} from "vue";
import {UserState} from "@/store/user/user";
import {LOGOUT_MUTATION} from "@/store/user/UserConstants";
import {switchMap} from "rxjs/operators";
import {useRouter} from "vue-router";

const SideNavTags = defineAsyncComponent(() => import('./SideNavTags.vue').then(m => m.default))
const SideNavUserInfo = defineAsyncComponent(() => import('./SideNavUserInfo.vue').then(m => m.default));

@Options({
  name: 'SideNav',
  components: {
    SideNavHeader,
    SideNavUserInfo,
    SideNavTags,
    SideNavManagement,
  },
})
export default class SideNav extends Vue {
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
