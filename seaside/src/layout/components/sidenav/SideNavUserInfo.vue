<template>
  <div class="flex mt-5 text-primary-content" v-if="userState.isAuthenticated">
    <img class="h-12 w-12 mr-2 rounded-full object-cover"
         :src="avatar"
         :alt="userState.user.login"/>
    <div>
      <h2 class="text-xl font-extrabold capitalize">
        {{ userState.user.name }}
      </h2>
      <span class="text-sm whitespace-nowrap">
				<span class="italic">{{ userState.user.login }}
          <component class="inline opacity-30 fill-current h-4 w-4"
                     :is="roleIcon" v-if="userState.user.roles.length > 0"/>
        </span>
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
import {IdentificationIcon, TrophyIcon, UsersIcon} from "@heroicons/vue/20/solid";
import {HAS_ROLE_ADMIN_GETTER, HAS_ROLE_MANAGER_GETTER} from "@/store/user/UserConstants";

@Options({name: 'SideNavUserInfo', components: {IdentificationIcon, TrophyIcon, UsersIcon}})
export default class SideNavUserInfo extends Vue {
  private store = setup(() => useStore());
  private userState: UserState = setup(() => useStore().state.user);

  get roleIcon(): string {
    if (this.store.getters) {
      if (this.store.getters[HAS_ROLE_ADMIN_GETTER]) {
        return "TrophyIcon";
      } else if (this.store.getters[HAS_ROLE_MANAGER_GETTER]) {
        return "UsersIcon";
      }
    }
    return "IdentificationIcon";
  }

  get avatar(): string {
    let hash = "0";
    if (this.userState.user.mail !== '') {
      hash = md5(this.userState.user.mail);
    }
    return `https://www.gravatar.com/avatar/${hash}?s=48&d=retro`;
  }
}
</script>