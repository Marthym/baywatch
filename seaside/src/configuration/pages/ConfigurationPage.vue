<template>
  <nav class="tabs tabs-boxed place-content-start" role="tablist">
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/config/feeds">{{ t('config.tab.feeds') }}</router-link>
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/config/profile">{{ t('config.tab.profile') }}</router-link>
    <router-link active-class="tab-active" class="tab capitalize" role="tab" to="/config/settings">{{ t('config.tab.settings') }}</router-link>
  </nav>
  <router-view></router-view>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'ConfigurationPage',
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class ConfigurationPage extends Vue {
  private t;
}
</script>