<template>
  <nav class="tabs tabs-boxed place-content-start" role="navigation">
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/admin/users">
      {{ t('admin.tab.users') }}
    </router-link>
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/admin/config">
      {{ t('admin.tab.config') }}
    </router-link>
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/admin/stats">
      {{ t('admin.tab.stats') }}
    </router-link>
  </nav>
  <router-view></router-view>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'AdministrationPage',
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class AdministrationPage extends Vue {
  private t;
}
</script>