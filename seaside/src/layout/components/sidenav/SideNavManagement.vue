<template>
  <span class="mt-auto"></span>
  <ul class="menu w-full">
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleAdmin']">
      <router-link active-class="active" class="capitalize" to="/admin" @click="sideNavToggle">
        <AcademicCapIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.administration') }}
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link active-class="active" class="capitalize" to="/teams" @click="sideNavToggle">
        <UserGroupIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.teams') }}
      </router-link>
    </li>
    <li v-if="user.isAuthenticated && store.getters['user/hasRoleUser']">
      <router-link active-class="active" class="capitalize" to="/config" @click="sideNavToggle">
        <AdjustmentsVerticalIcon class="fill-current w-6 h-6 mr-2"/>
        {{ t('aside.configuration') }}
      </router-link>
    </li>
    <li>
      <router-link v-if="!user.isAuthenticated" to="/register">
        <InboxArrowDownIcon class="fill-current h-6 w-6"/>
        <span class="ml-2 capitalize font-medium">{{ t('aside.register') }}</span>
      </router-link>
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
  <teleport v-if="route.params.id" to="body">
    <router-view :id="route.params.id"/>
  </teleport>
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
import { RouteLocationNormalizedLoaded, useRoute } from 'vue-router';

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
    const route = useRoute();
    const { t } = useI18n();
    return {
      store: store,
      route: route,
      user: store.state.user,
      t: t,
    };
  },
})
export default class SideNavManagement extends Vue {
  private readonly t;
  private readonly store;
  private readonly route!: RouteLocationNormalizedLoaded;
  private user: UserState;

  public sideNavToggle(): void {
    this.store.commit(SidenavMutation.TOGGLE);
  }
}
</script>