<template>
  <div class="m-2">
    <!-- Avatar and name box -->
    <div class="flex">
      <div class="avatar">
        <img class="w-24 rounded-xl mr-2" :src="avatar" :alt="userState.user.login"/>
      </div>
      <div>
        ID: <span class="italic text-sm">{{ userState.user._id }}</span><br>
        <span class="label-text-alt italic">The Avatar was grab from gravatar depending on your mail address</span>
      </div>
    </div>
    <div class="form-control w-full max-w-lg mt-6">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input :value="userState.user.login"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Name</span>
      </label>
      <input :value="userState.user.name"
             type="text" class="input input-bordered w-full max-w-lg mb-4"/>
      <label class="label">
        <span class="label-text">Mail adresse</span>
      </label>
      <input :value="userState.user.mail"
             type="text" class="input input-bordered w-full max-w-lg"/>
      <label class="label">
        <span class="label-text-alt italic mb-4">Mail address is only use for avatar and security transactions</span>
      </label>
      <button class="btn mb-4" @click.stop="onClickChangePassword()">Change Password</button>
      <button class="btn btn-primary">Save Profile</button>
    </div>
    <ChangePasswordModal :is-open="isChangePasswordOpen"
                         @cancel="onCancelPasswordChange()"
                         @submit="event => onSubmitPasswordChange(event)"/>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useStore } from 'vuex';
import { MD5 } from 'md5-js-tools';
import { UserState } from '@/store/user/user';
import ChangePasswordModal from '@/configuration/components/profile/ChangePasswordModal.vue';
import { userUpdate } from '@/security/services/UserService';

@Component({
  name: 'ProfileTab',
  components: { ChangePasswordModal },
  setup() {
    return {
      userState: useStore().state.user,
    };
  },
})
export default class ProfileTab extends Vue {
  private userState: UserState;
  private isChangePasswordOpen: boolean = false;

  get avatar(): string {
    let avatarHash = '0';
    if (this.userState.user.mail !== '') {
      avatarHash = MD5.generate(this.userState.user.mail);
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
    userUpdate({})
    this.isChangePasswordOpen = false;
  }
}
</script>