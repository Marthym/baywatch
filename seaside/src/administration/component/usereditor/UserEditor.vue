<template>
  <curtain-modal v-slot="curtainModal" @leave="onCancel()">
    <form v-if="opened"
          class="justify-self-end flex flex-col text-base-content lg:w-3/4 w-full h-full overflow-auto p-6"
          @click.stop @submit.prevent="onSaveUser(curtainModal)">
      <h2 class="card-title text-2xl pb-2 w-full first-letter:capitalize">{{ title }}</h2>
      <p class="text-sm text-base-content/70 mb-4">
        {{ isEditionMode ? t('admin.users.editor.subtitle.update') : t('admin.users.editor.subtitle.create') }}
      </p>

      <div class="space-y-6 [&_input]:text-base-content [&_label]:first-letter:uppercase">

        <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
          <legend class="fieldset-legend">{{ t('admin.users.editor.section.general') }}</legend>

          <label class="label block">
            {{ t('admin.users.login') }}
            <input v-model="modelValue.login"
                   :class="{'input-error': errors.has('login')}"
                   :disabled="isEditionMode"
                   class="input input-bordered w-full block"
                   type="text"
                   @input="onFieldChange('login')">
            <span v-if="errors.has('login')" class="label text-error text-xs">{{ errors.get('login') }}</span>
          </label>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="label block">
              {{ t('admin.users.username') }}
              <input v-model="modelValue.name"
                     :class="{'input-error': errors.has('name')}"
                     class="input input-bordered w-full block"
                     type="text"
                     @input="onFieldChange('name')">
              <span v-if="errors.has('name')" class="label text-error text-xs">{{ errors.get('name') }}</span>
            </label>

            <label class="label block">
              {{ t('admin.users.mail') }}
              <input v-model="modelValue.mail"
                     :class="{'input-error': errors.has('mail')}"
                     class="input input-bordered w-full block"
                     type="email"
                     @input="onFieldChange('mail')">
              <span v-if="errors.has('mail')" class="label text-error text-xs">{{ errors.get('mail') }}</span>
            </label>
          </div>
        </fieldset>

        <!-- Sécurité / Mot de passe -->
        <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
          <legend class="fieldset-legend">{{ t('admin.users.editor.section.security') }}</legend>

          <span class="label block first-letter:uppercase">{{ t('admin.users.password') }}</span>
          <div class="join w-full">
            <input v-model="modelValue.password"
                   :class="{'input-error': errors.has('password')}"
                   :type="visible.password?'text':'password'"
                   class="input input-bordered join-item w-full"
                   @input="onFieldChange('password')"
                   @blur.stop="onBlurNewPassword">
            <button class="btn btn-neutral input input-bordered border-x-0 join-item max-w-fit focus:outline-hidden"
                    type="button"
                    @click.prevent.stop="visible.password = !visible.password">
              <EyeIcon v-if="!visible.password" class="h-5 w-5 opacity-50"/>
              <EyeSlashIcon v-else class="h-5 w-5 opacity-50"/>
            </button>
            <button class="btn btn-soft join-item" type="button" @click.prevent.stop="onPasswordGenerate">
              {{ t('admin.users.editor.button.generate') }}
            </button>
          </div>
          <p v-if="errors.has('password')" class="label text-error text-xs">{{ errors.get('password') }}</p>

          <label class="label block mt-2">
            {{ t('admin.users.confirmation') }}
            <input v-model="modelValue.passwordConfirm"
                   :class="{'input-error': errors.has('confirm')}"
                   :type="visible.password?'text':'password'"
                   class="input input-bordered w-full block _js_input-confirm"
                   @blur="onBlurConfirmPassword"
                   @input="onFieldChange('confirm')">

            <span v-if="errors.has('confirm')" class="label text-error text-xs">{{ errors.get('confirm') }}</span>
          </label>
        </fieldset>

        <!-- Rôles -->
        <fieldset :class="{'border-error': errors.has('roles')}"
                  class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
          <legend class="fieldset-legend">{{ t('admin.users.roles.title') }}</legend>
          <UserRoleInput :model-value="modelValue.roles" @update:modelValue="onRoleUpdate"/>
          <span v-if="errors.has('roles')" class="label text-error text-xs">{{ errors.get('roles') }}</span>
        </fieldset>
      </div>

      <div class="grow"></div>

      <div class="card-actions justify-end pt-6">
        <button class="btn first-letter:uppercase" @click.prevent.stop="onCancel">
          {{ t('dialog.cancel') }}
        </button>
        <button :disabled="!hasValidRoles" class="btn btn-primary first-letter:uppercase _js_btn-save"
                @click.prevent.stop="onSaveUser(curtainModal)">
          {{ t('dialog.save') }}
        </button>
      </div>
    </form>
  </curtain-modal>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { UserCreated } from '@/security/model/User';
import UserRoleInput from '@/administration/component/usereditor/UserRoleInput.vue';
import { ULID_PATTERN } from '@/common/services/RegexPattern';
import { EyeIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';
import { EyeSlashIcon } from '@heroicons/vue/24/solid';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { TranslatorFunction } from '@/i18n';
import { parse, pick, ValiError } from 'valibot';
import CurtainModal, { CurtainModalSlot } from '@/common/components/CurtainModal.vue';
import { userCreate, userGet, userUpdate } from '@/security/services/UserService';
import { of, switchMap } from 'rxjs';
import { Router, useRoute, useRouter } from 'vue-router';
import { UserAccountForm, UserAccountFormSchema } from '@/administration/model/User';
import notificationService from '@/services/notification/NotificationService';

const CANCEL_EVENT: string = 'cancel';
const SUBMIT_EVENT: string = 'submit';
const CHANGE_EVENT: string = 'change';
const FIELD_PASSWORD: string = 'password';
const FIELD_CONFIRM: string = 'confirm';

@Component({
  name: 'UserEditor',
  components: { CurtainModal, EyeSlashIcon, EyeIcon, UserRoleInput },
  emits: [CANCEL_EVENT, SUBMIT_EVENT, CHANGE_EVENT],
  setup() {
    const { t } = useI18n();
    const id = useRoute().params.userId as string;
    const router = useRouter();
    return { t, id, router };
  },
})
export default class UserEditor extends Vue {
  private readonly id!: string;
  private readonly t!: TranslatorFunction;
  private readonly router!: Router;
  private modelValue: UserAccountForm = {
    login: '',
    password: '',
    passwordConfirm: '',
    mail: '',
    name: '',
    roles: [],
  };
  private title: string = '';
  private isEditionMode: boolean = false;
  private errors: Map<string, string> = new Map<string, string>();
  private visible = {
    password: false,
    confirm: false,
  };
  private opened: boolean = false;
  private closeEvent: typeof SUBMIT_EVENT | typeof CANCEL_EVENT = CANCEL_EVENT;

  get hasValidRoles(): boolean {
    return this.modelValue !== undefined && !this.modelValue.roles.some(r => !ULID_PATTERN.test(r));
  }

  mounted(): void {
    this.isEditionMode = this.id !== 'new';
    of(this.isEditionMode).pipe(
        switchMap(idEdit => {
          if (idEdit) {
            return userGet(this.id);
          }
          return of({
            login: '',
            mail: '',
            name: '',
            roles: [],
          });
        }),
    ).subscribe({
      next: user => {
        Object.assign(this.modelValue, user);
        delete this.modelValue.password;
        delete this.modelValue.passwordConfirm;
        this.title = this.isEditionMode
            ? this.t('admin.users.editor.title.update', { login: this.modelValue?.login || 'unknown' })
            : this.t('admin.users.editor.title.create');
        this.$nextTick(() => this.opened = true);
      },
      error: err => console.error('Failed to load user', err),
    });
  }

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onPasswordGenerate(): void {
    passwordGenerate(20).subscribe({
      next: passwords => {
        this.errors.delete(FIELD_PASSWORD);
        let randomValue = new Uint32Array(1);
        crypto.getRandomValues(randomValue);
        this.modelValue.password = passwords[randomValue[0] % 19];
        this.modelValue.passwordConfirm = this.modelValue.password;
      },
      error: err => this.errors.set(FIELD_PASSWORD, err.message),
    });
  }

  private onBlurNewPassword(): void {
    try {
      parse(pick(UserAccountFormSchema.pipe[0], ['login', 'password']), {
        password: this.modelValue.password,
        login: this.modelValue.login,
      });

      const user: UserCreated = {
        login: this.modelValue.login,
        name: this.modelValue.name,
        mail: this.modelValue.mail,
        password: this.modelValue.password,
        roles: [],
      };
      passwordAnonymousCheckStrength(user).subscribe({
        next: evaluation => {
          if (evaluation.isSecure) {
            this.errors.delete(FIELD_PASSWORD);
          } else {
            this.errors.set(FIELD_PASSWORD, evaluation.message);
          }
        },
        error: err => this.errors.set(FIELD_PASSWORD, err.message),
      });
    } catch (e) {
      this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
    }
  }

  private onBlurConfirmPassword(): void {
    if (this.modelValue.passwordConfirm && this.modelValue.passwordConfirm.length > 3 && this.modelValue.passwordConfirm === this.modelValue.password) {
      this.errors.delete(FIELD_CONFIRM);
    } else {
      this.errors.set(FIELD_CONFIRM, this.t('admin.users.editor.message.confirm.different.password'));
    }
  }

  private onCancel(): void {
    this.router.push({ name: 'admin-users' });
    console.log('Navigating back to admin-users');
  }

  private onSaveUser(curtainModal: CurtainModalSlot): void {
    try {
      parse(UserAccountFormSchema, this.modelValue);
      if (!this.isEditionMode && !this.modelValue.password) {
        this.errors.set('password', this.t('admin.users.editor.message.password.too.short'));
        return;
      }

      if (!this.hasValidRoles) {
        this.errors.set('roles', this.t('admin.users.editor.message.role_incorrect', { pattern: ULID_PATTERN }));
        return;
      }

      const user: UserCreated = {
        login: this.modelValue.login,
        name: this.modelValue.name,
        mail: this.modelValue.mail,
        password: this.modelValue.password,
        roles: this.modelValue.roles,
      };
      const persistFunction = (this.isEditionMode) ? () => userUpdate(this.id, user) : () => userCreate(user);
      persistFunction().subscribe({
        next: created => {
          notificationService.pushSimpleOk(this.t('admin.users.editor.message.save.successfully', { login: created.login }));
          curtainModal.close();
        },
        error: () => notificationService.pushSimpleError(this.t('admin.users.editor.message.save.error')),
      });
    } catch (e) {
      this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
    }
  }

  private onRoleUpdate(event: Event): void {
    this.errors.delete('roles');
    this.modelValue.roles.splice(0, this.modelValue.roles.length, ...event);
  }

  private handleValiError(error: ValiError<typeof UserAccountFormSchema>): void {
    this.errors.clear();
    error.issues.forEach((value) => {
      if (value.path) {
        this.errors.set(value.path.map((p: { key: any }) => p.key).join('.'), this.t(value.message));
      } else {
        console.debug('No path for validation error: ', value);
      }
    });
  }
}
</script>