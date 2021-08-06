<template>
  <div class="md:h-screen flex md:flex-row flex-col overflow-hidden">
    <SideNav/>
    <main class="flex-1 flex flex-col
      bg-gray-100 dark:bg-gray-700
      transition duration-500 ease-in-out
      overflow-y-auto px-10 py-2">
      <ContentTopNav/>
      <alert-dialog/>
      <router-view></router-view>
    </main>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import router from "./router";
import ContentTopNav from "@/components/topnav/ContentTopNav.vue";
import SideNav from '@/components/sidenav/SideNav.vue';

@Component({
  components: {
    ContentTopNav,
    SideNav,
  },
  router,
})
export default class App extends Vue {

}
</script>
