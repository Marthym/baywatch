<template>
  <span class="mt-auto"></span>
  <ul class="menu -mx-4">
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link to="/admin" @click="sideNavToggle">
        <AcademicCapIcon class="fill-current w-6 h-6 mr-2"/>
        Administration
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link to="/teams" @click="sideNavToggle">
        <UserGroupIcon class="fill-current w-6 h-6 mr-2"/>
        Teams
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link to="/config" @click="sideNavToggle">
        <AdjustmentsVerticalIcon class="fill-current w-6 h-6 mr-2"/>
        Configuration
      </router-link>
    </li>
    <li class="text-primary">
      <a v-if="user.isAuthenticated" @click.stop="$emit('logout')">
        <ArrowRightOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">log out</span>
      </a>
      <router-link to="/login" v-else>
        <ArrowLeftOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">log in</span>
      </router-link>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {setup} from 'vue-class-component';
import {useStore} from 'vuex';
import {UserState} from '@/store/user/user';
import {
  AcademicCapIcon,
  AdjustmentsVerticalIcon,
  ArrowLeftOnRectangleIcon,
  ArrowRightOnRectangleIcon,
  UserGroupIcon
} from '@heroicons/vue/20/solid';
import {SidenavMutation} from "@/store/sidenav/SidenavMutation.enum";

@Options({
  name: 'SideNavManagement',
  emits: ['logout'],
  components: {
    AcademicCapIcon,
    AdjustmentsVerticalIcon,
    ArrowLeftOnRectangleIcon,
    ArrowRightOnRectangleIcon,
    UserGroupIcon,
  }
})
export default class SideNavManagement extends Vue {
  private store = setup(() => useStore());
  private user: UserState = setup(() => useStore().state.user);

  public sideNavToggle(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }
}
</script>