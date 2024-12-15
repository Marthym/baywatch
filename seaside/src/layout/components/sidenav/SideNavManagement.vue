<template>
  <span class="mt-auto"></span>
  <ul class="menu -mx-4">
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link active-class="active" to="/admin" @click="sideNavToggle" class="capitalize">
        <AcademicCapIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.administration') }}
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link active-class="active" to="/teams" @click="sideNavToggle" class="capitalize">
        <UserGroupIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.teams') }}
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link active-class="active" to="/config" @click="sideNavToggle" class="capitalize">
        <AdjustmentsVerticalIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.configuration') }}
      </router-link>
    </li>
    <li>
      <a v-if="!user.isAuthenticated" @click.prevent.stop="onCreateAccountClick">
        <InboxArrowDownIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">{{ t('aside.register') }}</span>
      </a>
    </li>
    <li class="text-primary">
      <a v-if="user.isAuthenticated" @click.stop="$emit('logout')">
        <ArrowRightStartOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">{{ t('aside.logout') }}</span>
      </a>
      <router-link v-else to="/login">
        <ArrowRightEndOnRectangleIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">{{ t('aside.login') }}</span>
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
import { useI18n } from 'vue-i18n';

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
    const { t } = useI18n();
    return {
      store: store,
      user: store.state.user,
      t: t,
    };
  },
})
export default class SideNavManagement extends Vue {
  private t;
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