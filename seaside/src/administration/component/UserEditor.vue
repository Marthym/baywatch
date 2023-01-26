<template>
  <div class="grid bg-base-200 bg-opacity-60 z-30 w-full h-full absolute top-0 left-0"
       @click="opened = false">
    <Transition
        enter-active-class="lg:duration-300 ease-in-out"
        enter-from-class="lg:transform lg:translate-x-full"
        enter-to-class="lg:translate-x-0"
        leave-active-class="lg:duration-300 ease-in-out"
        leave-from-class="lg:translate-x-0"
        leave-to-class="lg:transform lg:translate-x-full"
        @after-leave="closeModal()">
      <form v-if="opened" class="justify-self-end flex flex-col bg-base-100 text-base-content lg:w-3/4 w-full h-full overflow-auto p-2"
            @click.stop @submit.prevent="onSaveUser">
        <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">{{ title }}</h2>
        <div class="flex flex-wrap content-start ">
          <div class="grow lg:basis-1/2 h-fit p-4">
            <label class="label">
              <span class="label-text">Login</span>
            </label>
            <input v-model="modelValue.login" type="text" placeholder="login" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('login')}" @change="onFieldChange('login')"
                   :disabled="isEditionMode">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
            </label>

            <label class="label -mt-6">
              <span class="label-text">Username</span>
            </label>
            <input v-model="modelValue.name" type="text" placeholder="username" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
            </label>

            <label class="label -mt-6">
              <span class="label-text">Mail</span>
            </label>
            <input v-model="modelValue.mail" type="email" placeholder="mail address" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')"
                   :disabled="isEditionMode">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
            </label>
          </div>
          <div class="grow lg:basis-1/2 h-fit p-4">
            <label class="label">
              <span class="label-text">Password</span>
            </label>
            <input v-model="modelValue.password" type="password" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('password')}" @change="onFieldChange('password')">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('password')" class="label-text-alt">{{ errors.get('password') }}</span>
            </label>

            <label class="label -mt-6">
              <span class="label-text">New Password</span>
            </label>
            <input v-model="modelValue.newPassword" type="password" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('password')}" @change="onFieldChange('password')">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('password')" class="label-text-alt">{{ errors.get('password') }}</span>
            </label>

            <label class="label -mt-6">
              <span class="label-text">Password Confirmation</span>
            </label>
            <input v-model="modelValue.confirm" type="password" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('confirm')}" @change="onFieldChange('confirm')">
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('confirm')" class="label-text-alt">{{ errors.get('confirm') }}</span>
            </label>
          </div>
          <div class="grow lg:basis-1/2 h-fit p-4">
            <label class="label -mt-6">
              <span class="label-text">Role</span>
            </label>
            <select v-model="modelValue.roles[0]" class="select select-bordered w-full"
                    :class="{'select-error': errors.has('role')}" @change="onFieldChange('role')">
              <option :value="undefined" disabled selected hidden>Choose the user role</option>
              <option>USER</option>
              <option>MANAGER</option>
              <option>ADMIN</option>
            </select>
            <label class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('role')" class="label-text-alt">{{ errors.get('role') }}</span>
            </label>
          </div>
        </div>
        <span class="grow"></span>
        <div>
          <button class="btn m-2" @click.stop="opened = false">Annuler</button>
          <button class="btn btn-primary m-2" @click="onSaveUser">Enregistrer</button>
        </div>
      </form>
    </Transition>

  </div>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import ModalWindow from "@/common/components/ModalWindow.vue";
import {User} from "@/security/model/User";

const CANCEL_EVENT: string = 'cancel';
const SUBMIT_EVENT: string = 'submit';
const MAIL_PATTERN = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/;

@Options({
  name: 'UserEditor',
  components: {ModalWindow},
  emits: [CANCEL_EVENT, SUBMIT_EVENT],
})
export default class UserEditor extends Vue {
  @Prop() private modelValue: User;
  private title = 'Create new user';
  private isEditionMode: boolean = false;
  private errors: Map<string, string> = new Map<string, string>();
  private opened: boolean = false;
  private closeEvent: typeof SUBMIT_EVENT | typeof CANCEL_EVENT = CANCEL_EVENT;

  private mounted(): void {
    this.isEditionMode = '_id' in this.modelValue && this.modelValue._id !== undefined;
    this.title = this.isEditionMode ? `Update user ${this.modelValue.login}` : 'Create new user';
    this.$nextTick(() => this.opened = true);
  }

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onSaveUser(): void {
    if (!this.modelValue.login) {
      this.errors.set('login', 'Login is mandatory !');
    }
    if (!this.modelValue.name) {
      this.errors.set('name', 'Name is mandatory !');
    }
    if (!this.modelValue.mail) {
      this.errors.set('mail', 'Mail address is mandatory !');
    } else if (!MAIL_PATTERN.test(this.modelValue.mail)) {
      this.errors.set('mail', 'Mail address must be syntactically correct !');
    }
    if (!this.modelValue.role) {
      this.errors.set('role', 'Role is mandatory !');
    }
    if (!this.isEditionMode) {
      if (!this.modelValue.password) {
        this.errors.set('password', 'Password is mandatory !');
      } else if (this.modelValue.password !== this.modelValue.confirm) {
        this.errors.set('confirm', "Password confirmation doesn't match !");
      }
    }

    if (this.errors.size == 0) {
      this.closeEvent = SUBMIT_EVENT;
      this.opened = false;
    }
  }

  private closeModal(): void {
    if (this.closeEvent === CANCEL_EVENT) {
      this.$emit(this.closeEvent);
    } else {
      this.$emit(this.closeEvent, this.modelValue);
    }
  }
}
</script>