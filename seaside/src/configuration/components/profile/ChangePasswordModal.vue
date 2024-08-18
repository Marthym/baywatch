<template>
  <ModalWindow :is-visible="isOpen" :title="t('config.password.title')">
    <div class="join w-full mt-4">
      <input v-model="oldPassword" :class="{'input-error': errors.old}"
             :placeholder="t('config.password.form.old.placeholder')"
             autocomplete="current-password"
             class="input input-bordered w-full insecureWarning placeholder:capitalize"
             tabindex="1"
             type="password" @blur.prevent="onBlurOldPassword()"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label mb-4">
      <span class="label-text-alt">{{ errors.old }}</span>
    </label>
    <div class="join w-full">
      <input v-model="newPassword" :class="{'input-error': errors.new, 'input-success': success.new}"
             :placeholder="t('config.password.form.new.placeholder')"
             autocomplete="new-password"
             class="input input-bordered join-item w-full placeholder:capitalize"
             tabindex="2"
             type="password" @blur.prevent="onBlurNewPassword()"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label mb-4">
      <span class="label-text-alt">{{ errors.new }}</span>
    </label>
    <div class="join w-full">
      <input v-model="confirmPassword" :class="{'input-error': errors.confirm, 'input-success': success.confirm}"
             :placeholder="t('config.password.form.confirm.placeholder')"
             autocomplete="new-password" class="input input-bordered w-full placeholder:capitalize" tabindex="3"
             type="password" @blur.prevent="onBlurConfirmPassword()"/>
      <button class="btn join-item" @click.stop.prevent="onToggleView">
        <EyeIcon class="h-6 w-6"/>
      </button>
    </div>
    <label class="label">
      <span class="label-text-alt">{{ errors.confirm }}</span>
    </label>
    <template v-slot:actions>
      <button :disabled="updateButtonDisable" class="btn btn-primary capitalize"
              @click.stop="$emit('submit', {old: oldPassword, new: confirmPassword})">
        {{ t('config.password.form.action.submit') }}
      </button>
      <button class="btn capitalize" @click.stop="$emit('cancel')">{{ t('dialog.cancel') }}</button>
    </template>
  </ModalWindow>
</template>
<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import ModalWindow from '@/common/components/ModalWindow.vue';
import { passwordCheckStrength } from '@/security/services/PasswordService';
import { EyeIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'ChangePasswordModal',
  components: { ModalWindow, EyeIcon },
  emits: ['submit', 'cancel'],
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class ChangePasswordModal extends Vue {
  private t;
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
      this.errors.old = this.t('config.password.error.old.mandatory');
    } else {
      this.success.old = true;
      delete this.errors.old;
    }
  }

  private onBlurNewPassword(): void {
    if (!this.newPassword || this.newPassword.length <= 3) {
      this.errors.new = this.t('config.password.error.new.unsecure');
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
      this.errors.confirm = this.t('config.password.error.confirm.different');
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

  private unmounted(): void {
    delete this.newPassword;
    delete this.confirmPassword;
    delete this.oldPassword;
  }
}
</script>
