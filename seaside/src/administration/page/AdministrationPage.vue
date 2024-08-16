<template>
  <nav class="tabs tabs-boxed place-content-start" role="navigation">
    <router-link active-class="tab-active" class="tab" role="tab" to="/admin/users">Users</router-link>
    <router-link active-class="tab-active" class="tab" role="tab" to="/admin/config">Configuration</router-link>
    <router-link active-class="tab-active" class="tab" role="tab" to="/admin/stats">Statistiques</router-link>
  </nav>
  <router-view></router-view>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';

@Component({
  name: 'AdministrationPage',
  components: {},
})
export default class AdministrationPage extends Vue {

}
</script>