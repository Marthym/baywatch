<template>
  <div class="flex h-screen">
    <SideNavOverlay/>
    <SideNav/>
    <div class="flex flex-col flex-1 h-full overflow-hidden">
      <TopNavigationBar/>
      <main ref="mainElement"
            class="flex-1 flex flex-col bg-neutral transition duration-500 ease-in-out overflow-y-auto px-4 py-2 lg:px-10">
        <router-view></router-view>
        <alert-dialog/>
        <notification-area/>
      </main>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import TopNavigationBar from '@/layout/components/TopNavigationBar.vue';
import SideNav from '@/layout/components/sidenav/SideNav.vue';
import SideNavOverlay from '@/layout/components/sidenav/SideNavOverlay.vue';
import NotificationArea from '@/common/components/notificationArea/NotificationArea.vue';
import { EventType } from '@/techwatch/model/EventType.enum';
import { Notification } from '@/services/notification/Notification.type';
import { registerNotificationListener, unregisterNotificationListener } from '@/layout/services/ServerEventService';
import { refresh } from '@/security/services/AuthenticationService';
import notificationService from '@/services/notification/NotificationService';
import { useStore } from 'vuex';
import { UPDATE_MUTATION as STATS_UPDATE_MUTATION } from '@/techwatch/store/statistics/StatisticsConstants';
import {
  HAS_ROLE_USER_GETTER,
  LOGOUT_MUTATION,
  UPDATE_MUTATION as USER_UPDATE_MUTATION,
} from '@/store/user/UserConstants';

@Component({
  components: {
    NotificationArea,
    TopNavigationBar,
    SideNav,
    SideNavOverlay,
  },
  setup() {
    return {
      store: useStore(),
    };
  },
})
export default class App extends Vue {
  private readonly store;

  mounted(): void {
    refresh().subscribe({
      next: session => {
        this.store.commit(USER_UPDATE_MUTATION, session.user);
        this.registerSessionNotifications();
      },
      error: () => {
        this.store.commit(LOGOUT_MUTATION);
        unregisterNotificationListener(EventType.NEWS_UPDATE, this.onServerMessage);
        const unwatch = this.store.watch(
            (state, getters) => getters[HAS_ROLE_USER_GETTER],
            newValue => {
              unwatch();
              if (newValue) {
                this.registerSessionNotifications();
              }
            });
      },
    });
  }

  private registerSessionNotifications(): void {
    registerNotificationListener(EventType.NEWS_ADD, this.onUserMessage);
    registerNotificationListener(EventType.NEWS_UPDATE, this.onServerMessage);
    registerNotificationListener(EventType.USER_NOTIFICATION, this.onUserMessage);
  }

  private onServerMessage(): void {
    this.store.commit(STATS_UPDATE_MUTATION);
  }

  private onUserMessage(evt: Event): void {
    try {
      const userNotif: Notification = JSON.parse((evt as MessageEvent).data);
      notificationService.pushNotification(userNotif);
    } catch (err: Error) {
      console.error('Unable to parse JSON notification', err);
      console.debug('Notification message: ', evt);
    }
  }
}
</script>

