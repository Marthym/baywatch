<template><!-- User info -->
  <div class="flex mt-5" v-if="user.id">
    <img class="h-12 w-12 mr-2 rounded-full object-cover"
         :src="avatar"
         alt="enoshima profile"/>
    <div>
      <h2 class="text-xl dark:text-gray-300 font-extrabold capitalize">
        {{ user.name }}
      </h2>
      <span class="text-sm dark:text-gray-300">
				<span class="font-semibold text-green-600 dark:text-green-300">Admin</span>
      {{ user.id }}
    </span>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import md5 from "js-md5";
import {User} from "@/services/model/User";

@Component
export default class SideNavUserInfo extends Vue {
  @Prop() private user?: User;

  get avatar(): string {
    let hash = "0";
    if (this.user?.mail !== undefined) {
      hash = md5(this.user.mail);
    }
    return `https://www.gravatar.com/avatar/${hash}?s=48`;
  }
}
</script>