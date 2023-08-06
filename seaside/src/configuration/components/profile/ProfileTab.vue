<template>
  <div class="m-2">
    <!-- Avatar and name box -->
    <div class="flex">
      <div class="avatar">
        <img class="w-24 rounded-xl mr-2" :src="avatar" :alt="store.state.user.user.login"/>
      </div>
      <div>
        ID: <span class="italic text-sm">{{ store.state.user.user._id }}</span><br>
        <span class="label-text-alt italic">The Avatar was grab from gravatar depending on your mail address</span>
      </div>
    </div>
    <div class="form-control w-full max-w-lg mt-6">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input :value="store.state.user.user.login"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Name</span>
      </label>
      <input :value="store.state.user.user.name"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Mail adresse</span>
      </label>
      <input :value="store.state.user.user.mail"
             type="text" class="input input-bordered w-full max-w-lg"/>
      <label class="label">
        <span class="label-text-alt italic mb-4">Mail address is only use for avatar and security transactions</span>
      </label>
      <button class="btn mb-4" @click.stop="onClickChangePassword()">Change Password</button>
      <button class="btn btn-primary">Save Profile</button>
    </div>
    <ChangePasswordModal :is-open="isChangePasswordOpen"
                         @cancel="onCancelPasswordChange()"
                         @submit="onSubmitPasswordChange"/>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { Store, useStore } from 'vuex';
import { MD5 } from 'md5-js-tools';
import { UserState } from '@/store/user/user';
import ChangePasswordModal from '@/configuration/components/profile/ChangePasswordModal.vue';
import { userUpdate } from '@/security/services/UserService';
import { UPDATE_MUTATION as USER_UPDATE_MUTATION } from '@/store/user/UserConstants';
import notificationService from '@/services/notification/NotificationService';

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
  private isChangePasswordOpen: boolean = false;

  get avatar(): string {
    let avatarHash = '0';
    const user = this.store.state.user.user;
    if (user.mail !== '') {
      avatarHash = MD5.generate(user.mail);
    }
    return `https://www.gravatar.com/avatar/${avatarHash}?s=96&d=retro`;
  }

  private onClickChangePassword(): void {
    this.isChangePasswordOpen = true;
  }

  private onCancelPasswordChange(): void {
    this.isChangePasswordOpen = false;
  }

  private onSubmitPasswordChange(changePasswordEvent: { old: string, new: string }): void {
    const user = this.store.state.user.user;
    if (user._id
        && changePasswordEvent.new
        && changePasswordEvent.new.length > 3) {
      userUpdate(user._id, changePasswordEvent.old, { password: changePasswordEvent.new })
          .subscribe({
            next: updated => this.store.commit(USER_UPDATE_MUTATION, updated),
            error: () => notificationService.pushSimpleError('Unable to update user :'),
          });
    }
    this.isChangePasswordOpen = false;
  }
}
</script>