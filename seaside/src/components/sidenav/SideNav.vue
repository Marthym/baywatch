<template>
  <nav class="flex flex-col bg-gray-200 dark:bg-gray-900 md:w-64 px-10 pt-4 pb-6">

    <SideNavHeader :unread="statistics.unread"/>

    <SideNavUserInfo :user="user"/>
    <SideNavStatistics :statistics="statistics"/>

    <button class="mt-8 flex items-center justify-between py-3 px-2 text-white
			dark:text-gray-200 bg-green-400 dark:bg-green-500 rounded-lg shadow">
      <!-- Action -->
      <span>Add user</span>
      <svg class="h-5 w-5 stroke-current" fill="none" stroke="currentColor" viewBox="0 0 24 24"
           xmlns="http://www.w3.org/2000/svg">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
      </svg>
    </button>

    <SideNavFeeds/>

    <SideNavImportantActions
        :isLoggedIn="isLoggedIn" @logout="logoutUser()"/>
  </nav>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import SideNavHeader from "./SideNavHeader.vue";
import SideNavUserInfo from "./SideNavUserInfo.vue";
import SideNavImportantActions from "./SideNavImportantActions.vue";
import SideNavFeeds from "./SideNavFeeds.vue";
import {Statistics} from "@/services/model/Statistics";
import UserService from "@/services/UserService";
import SideNavStatistics from "@/components/sidenav/SideNavStatistics.vue";

@Component({
  components: {
    SideNavStatistics,
    SideNavHeader,
    SideNavUserInfo,
    SideNavFeeds,
    SideNavImportantActions,
  },
})
export default class SideNav extends Vue {
  @Prop() statistics?: Statistics;

  private userService: UserService = new UserService(process.env.VUE_APP_API_BASE_URL);

  private user = this.userService.get() || {};

  get isLoggedIn(): boolean {
    return 'id' in this.user;
  }

  mounted(): void {
    if ('id' in this.user) {
      this.userService.refresh()
          .subscribe(
              user => this.$nextTick(() => this.user = user),
              () => this.$nextTick(() => delete this.user));
    }
  }

  logoutUser(): void {
    this.userService.logout()
        .subscribe(() => this.$router.go(0));
  }
}
</script>
