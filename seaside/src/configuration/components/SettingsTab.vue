<template>
  <div class="m-2">
    <div class="form-control w-full max-w-lg mt-6">
      <div class="label">
        <span class="label-text">{{ t('config.settings.form.preferredLocale') }}</span>
      </div>
      <select v-model="userSettings.preferredLocale" class="select select-bordered w-full max-w-xs">
        <option disabled></option>
        <option value="en-US">English (en-US)</option>
        <option value="fr-FR">Français (fr-FR)</option>
      </select>
      <button class="btn btn-primary capitalize mt-5" @click.stop="onClickSaveSettings()">
        {{ t('config.settings.form.action.save') }}
      </button>
    </div>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { UserSettings } from '@/security/model/UserSettings.type';
import { useI18n } from 'vue-i18n';
import { userSettingsGet, userSettingsUpdate } from '@/security/services/UserSettingsService';
import { Store, useStore } from 'vuex';
import { User } from '@/security/model/User';
import notificationService from '@/services/notification/NotificationService';

@Component({
  setup() {
    const { t, locale } = useI18n();

    return { store: useStore(), t, locale };
  },
})
export default class SettingsTab extends Vue {
  private store: Store<User>;
  private locale;
  private t;
  private userSettings: UserSettings = {
    preferredLocale: this.locale,
  } as UserSettings;

  private mounted(): void {
    if (this.store.state.user.user._id) {
      userSettingsGet(this.store.state.user.user._id).subscribe({
        next: us => Object.assign(this.userSettings, us),
        error: err => console.error(err),
      });
    }
  }

  private onClickSaveSettings(): void {
    if (this.store.state.user.user._id) {
      userSettingsUpdate(this.store.state.user.user._id, this.userSettings).subscribe({
        next: us => {
          Object.assign(this.userSettings, us);
          notificationService.pushSimpleOk(this.t('config.settings.messages.settingsUpdateSuccessfully'));
        },
        error: () => notificationService.pushSimpleError(this.t('config.settings.messages.unableToUpdate')),
      });
    }
  }
}
</script>
