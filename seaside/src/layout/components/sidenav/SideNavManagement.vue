<template>
  <span class="mt-auto"></span>
  <ul class="menu -mx-4">
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link to="/admin" active-class="active" @click="sideNavToggle">
        <AcademicCapIcon class="fill-current w-6 h-6 mr-2"/>
        Administration
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link to="/teams" active-class="active" @click="sideNavToggle">
        <UserGroupIcon class="fill-current w-6 h-6 mr-2"/>
        Teams
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link to="/config" active-class="active" @click="sideNavToggle">
        <AdjustmentsVerticalIcon class="fill-current w-6 h-6 mr-2"/>
        Configuration
      </router-link>
    </li>
    <li>
      <a v-if="!user.isAuthenticated" @click.prevent.stop="onCreateAccountClick">
        <InboxArrowDownIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">Create Account</span>
      </a>
    </li>
    <li class="text-primary">
      <a v-if="user.isAuthenticated" @click.stop="$emit('logout')">
        <ArrowRightStartOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">log out</span>
      </a>
      <router-link to="/login" v-else>
        <ArrowRightEndOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">log in</span>
      </router-link>
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import {
  AcademicCapIcon,
  AdjustmentsVerticalIcon,
  ArrowRightEndOnRectangleIcon,
  ArrowRightStartOnRectangleIcon,
  InboxArrowDownIcon,
  UserGroupIcon,
} from '@heroicons/vue/20/solid';
import { SidenavMutation } from '@/store/sidenav/SidenavMutation.enum';

@Component({
  name: 'SideNavManagement',
  emits: ['logout'],
  components: {
    AcademicCapIcon,
    AdjustmentsVerticalIcon,
    InboxArrowDownIcon,
    ArrowRightStartOnRectangleIcon,
    ArrowRightEndOnRectangleIcon,
    UserGroupIcon,
  },
  setup() {
    const store = useStore();
    return {
      store: store,
      user: store.state.user,
    };
  },
})
export default class SideNavManagement extends Vue {
  private store;
  private user: UserState;

  public sideNavToggle(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }

  public onCreateAccountClick(): void {
    this.store.commit(SidenavMutation.OPEN_CREATE_ACCOUNT_MUTATION);
  }
}
</script>