<template>
  <div class="h-screen w-full flex overflow-hidden">
    <SideNav/>
    <MainContent/>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import SideNav from "./components/sidenav/SideNav.vue";
import MainContent from "./components/content/MainContent.vue";

@Component({
  components: {
    SideNav,
    MainContent,
  },
})
export default class App extends Vue {
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
