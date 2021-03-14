<template>
  <div class="mt-8" v-if="user">
    <!-- User info -->
    <img class="h-12 w-12 rounded-full object-cover"
         :src="avatar"
         alt="enoshima profile"/>
    <h2 class="mt-4 text-xl dark:text-gray-300 font-extrabold capitalize">
      {{ user.name }}
    </h2>
    <span class="text-sm dark:text-gray-300">
				<span class="font-semibold text-green-600 dark:text-green-300">Admin</span>
      {{ user.id }}
    </span>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import UserService from "@/services/UserService";
import md5 from "js-md5";

@Component
export default class SideNavUserInfo extends Vue {
  private userService: UserService = new UserService(process.env.VUE_APP_API_BASE_URL);

  private user = this.userService.get();

  mounted(): void {
    this.userService.refresh()
        .subscribe(user => this.user = user);
  }

  get avatar(): string {
    let hash = "0";
    if (this.user?.mail !== undefined) {
      hash = md5(this.user.mail);
    }
    return `https://www.gravatar.com/avatar/${hash}?s=48`;
  }
}
</script>