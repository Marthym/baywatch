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
import {Component, Vue} from 'vue-facing-decorator';
import {MD5} from 'md5-js-tools';
import {useStore} from "vuex";
import {UserState} from "@/security/store/user";
import {IdentificationIcon, TrophyIcon, UsersIcon} from "@heroicons/vue/20/solid";
import {HAS_ROLE_ADMIN_GETTER, HAS_ROLE_MANAGER_GETTER} from "@/security/store/UserConstants";

@Component({
    name: 'SideNavUserInfo',
    components: {IdentificationIcon, TrophyIcon, UsersIcon},
    setup() {
        const store = useStore();
        return {
            store: store,
            userState: store.state.user,
        }
    }
})
export default class SideNavUserInfo extends Vue {
    private store;
    private userState: UserState;

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
        let avatarHash = "0";
        if (this.userState.user.mail !== '') {
            avatarHash = MD5.generate(this.userState.user.mail);
        }
        return `https://www.gravatar.com/avatar/${avatarHash}?s=48&d=retro`;
    }
}
</script>