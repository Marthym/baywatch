<template>
  <div class="flex h-screen overflow-y-hidden">
    <SideNavOverlay/>
    <SideNav/>
    <div class="flex flex-col flex-1 h-full overflow-hidden">
      <TopNavigationBar/>
      <main ref="mainElement"
            class="flex-1 flex flex-col bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-4 py-2 lg:px-10">
        <router-view></router-view>
        <alert-dialog/>
        <notification-area/>
      </main>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import TopNavigationBar from "@/layout/components/TopNavigationBar.vue";
import SideNav from '@/layout/components/sidenav/SideNav.vue';
import SideNavOverlay from '@/layout/components/sidenav/SideNavOverlay.vue';
import NotificationArea from "@/shared/components/notificationArea/NotificationArea.vue";
import {EventType} from "@/techwatch/model/EventType.enum";
import authenticationService from '@/security/services/AuthenticationService'
import serverEventService from '@/techwatch/services/ServerEventService'
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {UPDATE_MUTATION as STATS_UPDATE_MUTATION} from "@/techwatch/store/statistics/StatisticsConstants";
import {LOGOUT_MUTATION, UPDATE_MUTATION as USER_UPDATE_MUTATION} from "@/store/user/UserConstants";

@Options({
  components: {
    NotificationArea,
    TopNavigationBar,
    SideNav,
    SideNavOverlay,
  },
})
export default class App extends Vue {
  private readonly store = setup(() => useStore());

  mounted(): void {
    authenticationService.refresh().subscribe({
      next: session => {
        this.store.commit(USER_UPDATE_MUTATION, session.user);
        serverEventService.registerListener(EventType.NEWS, this.onServerMessage);
      },
      error: () => {
        this.store.commit(LOGOUT_MUTATION);
        serverEventService.unregister(EventType.NEWS, this.onServerMessage);
      }
    });
  }

  private onServerMessage(evt: Event): void {
    const msg: MessageEvent = evt as MessageEvent;
    this.store.commit(STATS_UPDATE_MUTATION, JSON.parse(msg.data));
  }
}
</script>

