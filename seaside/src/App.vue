<template>
  <div class="flex h-screen overflow-y-hidden">
    <SideNav/>
    <div class="flex flex-col flex-1 h-full overflow-hidden">
      <ContentTopNav/>
      <main ref="mainElement"
          class="flex-1 flex flex-col bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-10 py-2">
        <router-view></router-view>
        <alert-dialog/>
        <notification-area/>
      </main>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import ContentTopNav from "@/components/topnav/ContentTopNav.vue";
import SideNav from '@/components/sidenav/SideNav.vue';
import NotificationArea from "@/components/shared/notificationArea/NotificationArea.vue";
import userService, {UserListener} from '@/services/UserService'
import serverEventService from '@/services/sse/ServerEventService'
import {User} from "@/services/model/User";
import {EventType} from "@/services/sse/EventType.enum";
import {StatisticsMutation} from "@/store/statistics/StatisticsMutation.enum";
import {setup} from "vue-class-component";
import {useStore} from "vuex";

@Options({
  components: {
    NotificationArea,
    ContentTopNav,
    SideNav,
  },
})
export default class App extends Vue implements UserListener {
  private readonly store = setup(() => useStore());

  mounted(): void {
    userService.registerUserListener(this);
  }

  unmounted(): void {
    userService.registerUserListener(this);
    serverEventService.unregister(EventType.NEWS, this.onServerMessage);
  }

  onUserChange(user: User): void {
    if (user) {
      serverEventService.registerListener(EventType.NEWS, this.onServerMessage);
    } else {
      serverEventService.unregister(EventType.NEWS, this.onServerMessage);
    }
  }

  private onServerMessage(evt: Event): void {
    const msg: MessageEvent = evt as MessageEvent;
    this.store.commit(StatisticsMutation.UPDATE, JSON.parse(msg.data));
  }
}
</script>

