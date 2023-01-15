<template>
  <ModalWindow :title="title">
    <form class="form-control" @submit.prevent="onSaveUser">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input v-model="modelValue.login" type="text" placeholder="login" class="input input-bordered"
             :class="{'input-error': errors.has('login')}" @change="onFieldChange('login')"
             :disabled="isEditionMode">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Username</span>
      </label>
      <input v-model="modelValue.name" type="text" placeholder="username" class="input input-bordered"
             :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Mail</span>
      </label>
      <input v-model="modelValue.mail" type="email" placeholder="mail address" class="input input-bordered"
             :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')"
             :disabled="isEditionMode">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Password</span>
      </label>
      <input v-model="modelValue.password" type="password" class="input input-bordered"
             :class="{'input-error': errors.has('password')}" @change="onFieldChange('password')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('password')" class="label-text-alt">{{ errors.get('password') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Password Confirmation</span>
      </label>
      <input v-model="modelValue.confirm" type="password" class="input input-bordered"
             :class="{'input-error': errors.has('confirm')}" @change="onFieldChange('confirm')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('confirm')" class="label-text-alt">{{ errors.get('confirm') }}</span>
      </label>

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
    </form>
    <template v-slot:actions>
      <button class="btn" @click.stop="resetAndCloseModal">Annuler</button>
      <button class="btn btn-primary" @click="onSaveUser">Enregistrer</button>
    </template>
  </ModalWindow>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import ModalWindow from "@/common/components/ModalWindow.vue";
import {User} from "@/security/model/User";

const CANCEL_EVENT = 'cancel';
// const UPDATE_EVENT = 'update:modelValue';
const SUBMIT_EVENT = 'submit';
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

  private mounted(): void {
    this.isEditionMode = '_id' in this.modelValue && this.modelValue._id !== undefined;
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
      this.$emit(SUBMIT_EVENT, this.modelValue);
    }
  }

  private resetAndCloseModal(): void {
    this.$emit(CANCEL_EVENT);
  }
}
</script>