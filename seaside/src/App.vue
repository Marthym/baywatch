<template>
  <router-view></router-view>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import router from "./router";

@Component({
  router,
})
export default class App extends Vue {
}
</script>

<style>

</style>
