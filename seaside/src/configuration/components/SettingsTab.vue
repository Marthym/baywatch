<template>
  <div class="m-2">
    <div class="form-control w-full max-w-lg mt-6">
      <div class="label">
        <span class="label-text first-letter:capitalize">{{ t('config.settings.form.preferredLocale') }}</span>
      </div>
      <select v-model="userSettings.preferredLocale" class="select select-bordered w-full">
        <option disabled></option>
        <option value="en-US">English (en-US)</option>
        <option value="fr-FR">Français (fr-FR)</option>
      </select>
      <label class="label cursor-pointer mt-4">
        <span class="label-text capitalize">{{ t('config.settings.form.autoread.label') }}</span>
        <input v-model="userSettings.autoread" class="toggle" type="checkbox"/>
      </label>
      <div class="label label-text-alt pt-0">{{ t('config.settings.form.autoread.alt') }}</div>
      <div class="label">
        <span class="label-text first-letter:capitalize">{{ t('config.settings.form.newsView') }}</span>
      </div>
      <select v-model="userSettings.newsViewMode" class="select select-bordered w-full">
        <option disabled></option>
        <option value="MAGAZINE">Magazine</option>
        <option value="CARD">Card</option>
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
import { useStore } from 'vuex';
import notificationService from '@/services/notification/NotificationService';
import { from, switchMap } from 'rxjs';
import { store } from '@/store';
import { UPDATE_SETTINGS_MUTATION as USER_UPDATE_SETTINGS_MUTATION } from '@/security/store/UserConstants';

@Component({
  setup() {
    const { t } = useI18n();
    const { locale, mergeLocaleMessage } = useI18n({ useScope: 'global' });
    return { store: useStore(), t, locale, mergeLocaleMessage };
  },
})
export default class SettingsTab extends Vue {
  private store;
  private locale;
  private t;
  private mergeLocaleMessage;
  private userSettings: UserSettings = {
    preferredLocale: this.locale,
    autoread: true,
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
      userSettingsUpdate(this.store.state.user.user._id, this.userSettings).pipe(
          switchMap(settings => {
            Object.assign(this.userSettings, settings);
            return from(import(`../../locales/config-settings_${settings.preferredLocale}.ts`));
          }),
      ).subscribe({
        next: messages => {
          this.mergeLocaleMessage(this.userSettings.preferredLocale, messages[this.userSettings.preferredLocale.replace('-', '_')]);
          this.locale = this.userSettings.preferredLocale;
          notificationService.pushSimpleOk(this.t('config.settings.messages.settingsUpdateSuccessfully'));
          store.commit(USER_UPDATE_SETTINGS_MUTATION, this.userSettings);
        },
        error: err => notificationService.pushSimpleError(this.t('config.settings.messages.unableToUpdate')),
      });
    }
  }
}
</script>
