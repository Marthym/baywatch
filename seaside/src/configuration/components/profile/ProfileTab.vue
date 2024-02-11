<template>
  <div class="m-2">
    <!-- Avatar and name box -->
    <div class="flex">
      <div class="avatar">
        <img class="w-24 rounded-xl mr-2" :src="avatar" :alt="user.login"/>
      </div>
      <div>
        ID: <span class="italic text-sm">{{ user._id }}</span><br>
        <span class="label-text-alt italic">The Avatar was grab from gravatar depending on your mail address</span>
      </div>
    </div>
    <div class="form-control w-full max-w-lg mt-6">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input v-model="user.login"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Name</span>
      </label>
      <input v-model="user.name"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Mail adresse</span>
      </label>
      <input v-model="user.mail"
             type="text" class="input input-bordered w-full max-w-lg"/>
      <label class="label">
        <span class="label-text-alt italic mb-4">Mail address is only use for avatar and security transactions</span>
      </label>
      <div class="join w-full">
        <input v-model="user.password" placeholder="Enter your password to update your profile"
               type="password" class="input input-bordered join-item w-full"/>
        <button class="btn join-item mb-4" @click.stop="onClickChangePassword()">Change Password</button>
      </div>
      <button class="btn btn-primary" :disabled="!updateEnable" @click.stop="onClickSaveProfile()">Save Profile</button>
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

@Component({
  name: 'ProfileTab',
  components: { ChangePasswordModal },
  setup() {
    return {
      store: useStore(),
    };
  },
})
export default class ProfileTab extends Vue {
  private store: Store<UserState>;
  private user: User;
  private isChangePasswordOpen: boolean = false;

  get avatar(): string {
    let avatarHash = '0';
    const user = this.store.state.user.user;
    if (user.mail !== '') {
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
            error: () => notificationService.pushSimpleError('Unable to update user !'),
          });
    }
    this.isChangePasswordOpen = false;
  }

  private onClickSaveProfile(): void {
    if (this.user._id && this.user.password) {
      userUpdate(this.user._id, this.user, this.user.password).subscribe({
        next: updated => {
          this.store.commit(USER_UPDATE_MUTATION, updated);
          notificationService.pushSimpleOk('Profile updated successfully !');
        },
        error: () => notificationService.pushSimpleError('Unable to update user !'),
      });
      delete this.user.password;
    }
  }
}
</script>