<template>
    <aside class="fixed md:w-64 px-10 pt-4 pb-6 inset-y-0 z-30 flex flex-col flex-shrink-0 w-4/5 h-full overflow-y-auto
  transition-all transform bg-base-300 shadow-lg lg:z-auto lg:static lg:shadow-none"
           :class="{'-translate-x-full lg:translate-x-0': !state.open}"
           aria-label="Main Menu"
           :aria-expanded="state.open">
        <SideNavHeader/>

        <SideNavUserInfo/>

        <SideNavFilters v-if="user.isAuthenticated"/>
        <SideNavManagement @logout="logoutUser()"/>
        <span class="text-xs text-neutral font-bold mx-auto -mb-3">{{ VERSION }} • {{ COMMIT }}</span>
    </aside>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-facing-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavManagement from './SideNavManagement.vue';

import {LOGOUT_MUTATION} from "@/security/store/UserConstants";
import {SidenavState} from "@/store/sidenav/sidenav";
import SideNavUserInfo from "@/layout/components/sidenav/SideNavUserInfo.vue";
import SideNavFilters from "@/layout/components/sidenav/SideNavFilters.vue";
import {UserState} from "@/security/store/user";
import {useStore} from "vuex";
import {switchMap} from "rxjs/operators";
import {Router, useRouter} from "vue-router";
import {closeNotificationListeners} from "@/layout/services/ServerEventService";
import authenticationService from "@/security/services/AuthenticationService";

@Component({
    name: 'SideNav',
    components: {
        SideNavHeader,
        SideNavUserInfo,
        SideNavFilters,
        SideNavManagement,
    },
    setup() {
        const store = useStore();
        return {
            router: useRouter(),
            store: store,
            state: store.state.sidenav,
            user: store.state.user,
        }
    }
})
export default class SideNav extends Vue {
    private readonly VERSION = import.meta.env.VITE_BW_VERSION;
    private readonly COMMIT = import.meta.env.VITE_BW_COMMIT;

    private router: Router;
    private store;
    private state: SidenavState;
    private user: UserState;

    logoutUser(): void {
        closeNotificationListeners().pipe(
            switchMap(() => authenticationService.logout())
        ).subscribe(() => {
            this.store.commit(LOGOUT_MUTATION);
            this.router.go(0);
        });
    }
}
</script>
