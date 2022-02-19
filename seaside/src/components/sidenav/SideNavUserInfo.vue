<template>
  <div class="flex mt-5 text-primary-content" v-if="userState.isAuthenticated">
    <img class="h-12 w-12 mr-2 rounded-full object-cover"
         :src="avatar"
         :alt="userState.user.id"/>
    <div>
      <h2 class="text-xl font-extrabold capitalize">
        {{ userState.user.name }}
      </h2>
      <span class="text-sm">
				<span class="capitalize italic">{{ userState.user.role.toLowerCase()}}</span>
      {{ userState.user.id }}
    </span>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import md5 from "js-md5";
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {UserState} from "@/store/user/user";

@Options({name: 'SideNavUserInfo'})
export default class SideNavUserInfo extends Vue {
  private userState: UserState = setup(() => useStore().state.user);

  get avatar(): string {
    let hash = "0";
    if (this.userState.user.mail !== '') {
      hash = md5(this.userState.user.mail);
    }
    return `https://www.gravatar.com/avatar/${hash}?s=48&d=retro`;
  }
}
</script>