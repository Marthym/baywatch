<template>
  <curtain-modal @leave="close()" v-slot="curtainModal">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">Create new account</h2>
    <div class="m-4">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input v-model="account.login" type="text" placeholder="login"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('login')}" @change="onFieldChange('login')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Username</span>
      </label>
      <input v-model="account.name" type="text" placeholder="username"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Mail</span>
      </label>
      <input v-model="account.mail" type="email" placeholder="mail address"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
      </label>

      <label class="label">
        <span class="label-text">Password</span>
      </label>
      <input v-model="account.password" type="password" class="input input-bordered w-full"
             :class="{'input-error': errors.has('password')}"
             @change="onFieldChange('password')"
             @blur="onBlurNewPassword">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('password')" class="label-text-alt">{{
            errors.get('password')
          }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Password Confirmation</span>
      </label>
      <input v-model="passwordConfirm" type="password" class="input input-bordered w-full"
             :class="{'input-error': errors.has('passwordConfirm')}"
             @change="onFieldChange('passwordConfirm')"
             @blur="onBlurConfirmPassword">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('passwordConfirm')" class="label-text-alt">{{ errors.get('passwordConfirm') }}</span>
      </label>

      <div class="text-right">
        <button class="btn btn-sm mx-1" @click.stop="curtainModal.close()">Cancel</button>
        <button class="btn btn-sm btn-primary mx-1">Register</button>
      </div>
    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal from '@/common/components/CurtainModal.vue';
import { User } from '@/security/model/User';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { CLOSE_CREATE_ACCOUNT_MUTATION } from '@/security/store/UserConstants';
import { passwordCheckStrength } from '@/security/services/PasswordService';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal },
  setup() {
    return {
      userStore: useStore(),
    };
  },
})
export default class CreateAccountComponent extends Vue {
  private readonly userStore: Store<UserState>;
  private errors: Map<string, string> = new Map<string, string>();

  private account: User = {} as User;
  private passwordConfirm: string = '';

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onBlurNewPassword(): void {
    if (!this.account.password || this.account.password.length <= 3) {
      this.errors.set('password', 'This password is not secure. An attacker will find it instant !');
      return;
    }
    passwordCheckStrength(this.account.password).subscribe({
      next: evaluation => {
        if (evaluation.isSecure) {
          this.errors.delete('password');
        } else {
          this.errors.set('password', evaluation.message);
        }
      },
      error: err => this.errors.set('password', err.message),
    });
  }

  private onBlurConfirmPassword(): void {
    if (this.passwordConfirm && this.passwordConfirm.length > 3 && this.passwordConfirm === this.account.password) {
      this.errors.delete('passwordConfirm');
    } else {
      this.errors.set('passwordConfirm', 'The new and confirmation passwords must be the same');
    }
  }

  private close(): void {
    this.userStore.commit(CLOSE_CREATE_ACCOUNT_MUTATION);
  }
}
</script>
