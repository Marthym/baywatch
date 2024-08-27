<template>
  <div class="m-2">
    <!-- Avatar and name box -->
    <div class="flex">
      <div class="avatar">
        <img :alt="user.login" :src="avatar" class="w-24 rounded-xl mr-2"/>
      </div>
      <div>
        {{ t('config.profile.form.id') }}: <span class="italic text-sm">{{ user._id }}</span><br>
        <p class="label-text-alt italic first-letter:capitalize">{{ t('config.profile.avatar.notice') }}</p>
      </div>
    </div>
    <div class="form-control w-full max-w-lg mt-6">
      <label class="label">
        <span class="label-text capitalize">{{ t('config.profile.form.login') }}</span>
      </label>
      <input v-model="user.login"
             class="input input-bordered w-full max-w-lg mb-4" type="text"/>
      <label class="label">
        <span class="label-text capitalize">{{ t('config.profile.form.name') }}</span>
      </label>
      <input v-model="user.name"
             class="input input-bordered w-full max-w-lg mb-4" type="text"/>
      <label class="label">
        <span class="label-text capitalize">{{ t('config.profile.form.mail') }}</span>
      </label>
      <input v-model="user.mail"
             class="input input-bordered w-full max-w-lg" type="text"/>
      <label class="label">
        <span class="label-text-alt italic mb-4 first-letter:capitalize">{{ t('config.profile.mail.notice') }}</span>
      </label>
      <div class="join w-full">
        <input v-model="user.password" :placeholder="t('config.profile.form.password.placeholder')"
               class="input input-bordered join-item w-full" type="password"/>
        <button class="btn join-item mb-4 capitalize" :title="t('config.profile.form.password.tooltip')" @click.stop="onClickChangePassword()">
          {{ t('config.profile.form.password') }}
        </button>
      </div>
      <button :disabled="!updateEnable" class="btn btn-primary capitalize" @click.stop="onClickSaveProfile()">
        {{ t('config.profile.form.action.save') }}
      </button>
    </div>
    <ChangePasswordModal v-if="isChangePasswordOpen" :is-open="isChangePasswordOpen"
                         @cancel="onCancelPasswordChange()"
                         @submit="onSubmitPasswordChange"/>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { Store, useStore } from 'vuex';
import { MD5 } from 'md5-js-tools';
import { UserState } from '@/security/store/user';
import ChangePasswordModal from '@/configuration/components/profile/ChangePasswordModal.vue';
import { userUpdate } from '@/security/services/UserService';
import { UPDATE_MUTATION as USER_UPDATE_MUTATION } from '@/security/store/UserConstants';
import notificationService from '@/services/notification/NotificationService';
import { User } from '@/security/model/User';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'ProfileTab',
  components: { ChangePasswordModal },
  setup() {
    const { t } = useI18n();
    return {
      store: useStore(),
      t: t,
    };
  },
})
export default class ProfileTab extends Vue {
  private t;
  private store: Store<UserState>;
  private user: User;
  private isChangePasswordOpen: boolean = false;

  get avatar(): string {
    let avatarHash = '0';
    const user = this.store.state.user.user;
    if (user.mail && user.mail !== '') {
      avatarHash = MD5.generate(user.mail);
    }
    return `https://www.gravatar.com/avatar/${avatarHash}?s=96&d=retro`;
  }

  get updateEnable(): boolean {
    return (this.user.password !== undefined && this.user.password.length > 3);
  }

  private beforeMount(): void {
    this.user = { ...this.store.state.user.user };
  }

  private onClickChangePassword(): void {
    this.isChangePasswordOpen = true;
  }

  private onCancelPasswordChange(): void {
    this.isChangePasswordOpen = false;
  }

  private onSubmitPasswordChange(changePasswordEvent: { old: string, new: string }): void {
    if (this.user._id
        && changePasswordEvent.new
        && changePasswordEvent.new.length > 3) {
      userUpdate(this.user._id, { password: changePasswordEvent.new }, changePasswordEvent.old)
          .subscribe({
            next: updated => this.store.commit(USER_UPDATE_MUTATION, updated),
            error: () => notificationService.pushSimpleError(this.t('config.profile.messages.unableToUpdate')),
          });
    }
    this.isChangePasswordOpen = false;
  }

  private onClickSaveProfile(): void {
    if (this.user._id && this.user.password) {
      userUpdate(this.user._id, this.user, this.user.password).subscribe({
        next: updated => {
          this.store.commit(USER_UPDATE_MUTATION, updated);
          notificationService.pushSimpleOk(this.t('config.profile.messages.profileUpdateSuccessfully'));
        },
        error: () => notificationService.pushSimpleError(this.t('config.profile.messages.unableToUpdate')),
      });
      delete this.user.password;
    }
  }
}
</script>