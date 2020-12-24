<template>
  <div class="h-screen w-full flex overflow-hidden">
    <SideNav/>
    <MainContent/>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import SideNav from "../components/sidenav/SideNav.vue";
import MainContent from "../components/content/MainContent.vue";

@Component({
  components: {
    SideNav,
    MainContent,
  },
})
export default class Home extends Vue {
}
</script>