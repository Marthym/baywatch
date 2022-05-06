<template>
  <span class="mt-auto"></span>
  <ul class="menu -mx-4">
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link to="/admin">
        <svg class="fill-current w-6 h-6 mr-2" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path
              d="M10.394 2.08a1 1 0 00-.788 0l-7 3a1 1 0 000 1.84L5.25 8.051a.999.999 0 01.356-.257l4-1.714a1 1 0
              11.788 1.838L7.667 9.088l1.94.831a1 1 0 00.787 0l7-3a1 1 0 000-1.838l-7-3zM3.31 9.397L5 10.12v4.102a8.969
              8.969 0 00-1.05-.174 1 1 0 01-.89-.89 11.115 11.115 0 01.25-3.762zM9.3 16.573A9.026 9.026 0 007
              14.935v-3.957l1.818.78a3 3 0 002.364 0l5.508-2.361a11.026 11.026 0 01.25 3.762 1 1 0 01-.89.89 8.968
              8.968 0 00-5.35 2.524 1 1 0 01-1.4 0zM6 18a1 1 0 001-1v-2.065a8.935 8.935 0 00-2-.712V17a1 1 0 001 1z"></path>
        </svg>
        Administration
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleManager']">
      <a>
        <svg class="fill-current w-6 h-6 mr-2" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path
              d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6
              8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75
              12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z"></path>
        </svg>
        Gestion d’équipe
      </a>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <a>
        <svg class="fill-current w-6 h-6 mr-2" fill="currentColor" viewBox="0 0 20 20"
             xmlns="http://www.w3.org/2000/svg">
          <path
              d="M5 4a1 1 0 00-2 0v7.268a2 2 0 000 3.464V16a1 1 0 102 0v-1.268a2 2 0 000-3.464V4zM11 4a1 1 0 10-2
              0v1.268a2 2 0 000 3.464V16a1 1 0 102 0V8.732a2 2 0 000-3.464V4zM16 3a1 1 0 011 1v7.268a2 2 0 010
              3.464V16a1 1 0 11-2 0v-1.268a2 2 0 010-3.464V4a1 1 0 011-1z"></path>
        </svg>
        Configuration
      </a>
    </li>
    <li class="text-primary">
      <a v-if="user.isAuthenticated" @click.stop="$emit('logout')">
        <svg class="fill-current h-5 w-5" viewBox="0 0 24 24">
          <path
              d="M16 17v-3H9v-4h7V7l5 5-5 5M14 2a2 2 0 012
						2v2h-2V4H5v16h9v-2h2v2a2 2 0 01-2 2H5a2 2 0 01-2-2V4a2 2
						0 012-2h9z"></path>
        </svg>
        <span class="ml-2 capitalize font-medium">log out</span>
      </a>
      <router-link to="/login" v-else>
        <svg class="fill-current h-5 w-5" viewBox="0 0 24 24">
          <path
              d="M16 17v-3H9v-4h7V7l5 5-5 5M14 2a2 2 0 012
						2v2h-2V4H5v16h9v-2h2v2a2 2 0 01-2 2H5a2 2 0 01-2-2V4a2 2
						0 012-2h9z"></path>
        </svg>
        <span class="ml-2 capitalize font-medium">log in</span>
      </router-link>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {UserState} from "@/store/user/user";

@Options({name: 'SideNavManagement', emits: ['logout']})
export default class SideNavManagement extends Vue {
  private store = setup(() => useStore());
  private user: UserState = setup(() => useStore().state.user);
}
</script>