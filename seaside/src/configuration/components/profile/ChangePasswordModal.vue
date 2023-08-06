<template>
  <ModalWindow title="Change password" :is-visible="isOpen">
    <div class="join w-full mt-4">
      <input v-model="oldPassword" @blur.prevent="onBlurOldPassword()"
             :class="{'input-error': errors.old}"
             type="password" placeholder="Old Password" class="input input-bordered w-full insecureWarning"
             autocomplete="current-password" tabindex="1"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label mb-4">
      <span class="label-text-alt">{{ errors.old }}</span>
    </label>
    <div class="join w-full">
      <input v-model="newPassword" @blur.prevent="onBlurNewPassword()"
             :class="{'input-error': errors.new, 'input-success': success.new}"
             type="password" placeholder="New Password" class="input input-bordered join-item w-full"
             autocomplete="new-password" tabindex="2"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label mb-4">
      <span class="label-text-alt">{{ errors.new }}</span>
    </label>
    <div class="join w-full">
      <input v-model="confirmPassword" @blur.prevent="onBlurConfirmPassword()"
             :class="{'input-error': errors.confirm, 'input-success': success.confirm}"
             type="password" placeholder="Confirm Password" class="input input-bordered w-full"
             autocomplete="new-password" tabindex="3"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label">
      <span class="label-text-alt">{{ errors.confirm }}</span>
    </label>
    <template v-slot:actions>
      <button class="btn btn-primary" :disabled="updateButtonDisable"
              @click.stop="$emit('submit', {old: oldPassword, new: confirmPassword})">
        Update
      </button>
      <button class="btn" @click.stop="$emit('cancel')">Cancel</button>
    </template>
  </ModalWindow>
</template>
<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import ModalWindow from '@/common/components/ModalWindow.vue';
import { passwordCheckStrength } from '@/security/services/PasswordService';
import { EyeIcon } from '@heroicons/vue/24/outline';

@Component({
  name: 'ChangePasswordModal',
  components: { ModalWindow, EyeIcon },
  emits: ['submit', 'cancel'],
})
export default class ChangePasswordModal extends Vue {
  @Prop({ default: false }) private isOpen: boolean = false;
  private oldPassword?: string;
  private newPassword?: string;
  private confirmPassword?: string;
  private success: {
    old: boolean,
    new: boolean,
    confirm: boolean,
  } = { old: false, new: false, confirm: false };
  private errors: {
    old?: string,
    new?: string,
    confirm?: string,
  } = {};

  get updateButtonDisable(): boolean {
    return !(this.success.old && this.success.new
        && this.success.confirm && this.success.new === this.success.confirm);
  }

  private onBlurOldPassword(): void {
    if (!this.oldPassword || this.oldPassword.length <= 0) {
      this.errors.old = 'You must enter old password';
    } else {
      this.success.old = true;
      delete this.errors.old;
    }
  }

  private onBlurNewPassword(): void {
    if (!this.newPassword || this.newPassword.length <= 3) {
      this.errors.new = `This password is not secure. An attacker will find it instant}`;
      return;
    }
    passwordCheckStrength(this.newPassword).subscribe({
      next: evaluation => {
        if (evaluation.isSecure) {
          delete this.errors.new;
          this.success.new = true;
        } else {
          this.success.new = false;
          this.errors.new = evaluation.message;
        }
      },
      error: err => this.errors.new = err.message,
    });
  }

  private onBlurConfirmPassword(): void {
    if (this.confirmPassword && this.confirmPassword.length > 3 && this.confirmPassword === this.newPassword) {
      this.success.confirm = true;
      delete this.errors.confirm;
    } else {
      this.success.confirm = false;
      this.errors.confirm = 'The new and confirmation passwords must be the same';
    }
  }

  private onToggleView(event: InputEvent): void {
    const t: HTMLInputElement = event.currentTarget as HTMLInputElement;
    const s: HTMLInputElement = t.previousElementSibling as HTMLInputElement;
    if (s.type === 'text') {
      s.type = 'password';
    } else {
      s.type = 'text';
    }
  }
}
</script>
