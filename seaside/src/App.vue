<template>
  <div class="flex h-screen overflow-y-hidden bg-white">
    <SideNav/>
    <div class="flex flex-col flex-1 h-full overflow-hidden">
      <ContentTopNav/>
      <main
          class="flex-1 flex flex-col bg-gray-100 dark:bg-gray-700 transition duration-500 ease-in-out overflow-y-auto px-10 py-2">
        <router-view></router-view>
        <alert-dialog/>
        <notification-area/>
      </main>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import router from "./router";
import ContentTopNav from "@/components/topnav/ContentTopNav.vue";
import SideNav from '@/components/sidenav/SideNav.vue';
import NotificationArea from "@/components/shared/notificationArea/NotificationArea.vue";

@Component({
  components: {
    NotificationArea,
    ContentTopNav,
    SideNav,
  },
  router,
})
export default class App extends Vue {
}
</script>
