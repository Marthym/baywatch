<template>
  <div class="flex h-screen overflow-y-hidden">
    <SideNav/>
    <div class="flex flex-col flex-1 h-full overflow-hidden">
      <ContentTopNav/>
      <main ref="mainElement"
            class="flex-1 flex flex-col bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-4  py-2 lg:px-10">
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
import userService from '@/services/UserService'
import serverEventService from '@/services/sse/ServerEventService'
import {EventType} from "@/services/sse/EventType.enum";
import {RELOAD_ACTION} from "@/store/statistics/StatisticsConstants";
import {setup} from "vue-class-component";
import {useStore} from "vuex";
import {LOGOUT_MUTATION, UPDATE_MUTATION} from "@/store/user/UserConstants";

@Options({
  components: {
    NotificationArea,
    ContentTopNav,
    SideNav,
  },
})
export default class App extends Vue {
  private readonly store = setup(() => useStore());

  mounted(): void {
    userService.get().subscribe({
      next: user => {
        this.store.commit(UPDATE_MUTATION, user);
        serverEventService.registerListener(EventType.NEWS, this.onServerMessage);
        this.store.dispatch(RELOAD_ACTION);
      },
      error: () => {
        this.store.commit(LOGOUT_MUTATION);
        serverEventService.unregister(EventType.NEWS, this.onServerMessage);
        this.store.dispatch(RELOAD_ACTION);
      }
    });
  }

  unmounted(): void {
    serverEventService.unregister(EventType.NEWS, this.onServerMessage);
  }

  private onServerMessage(evt: Event): void {
    const msg: MessageEvent = evt as MessageEvent;
    this.store.commit(UPDATE_MUTATION, JSON.parse(msg.data));
  }
}
</script>

