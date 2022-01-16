<template>
  <div class="absolute inset-0 h-screen w-screen backdrop-filter bg-gray-700 bg-opacity-90"
       @click.prevent="closeLoginWindow" @keydown.esc="closeLoginWindow">
    <div class="card bordered flex rounded-lg shadow-lg overflow-hidden h-full
                lg:h-fit lg:mt-8 mx-auto lg:card-side lg:max-w-4xl"
         @click.stop>
      <figure class="">
        <img class="object-cover" src="/login.webp" alt="Baywatch">
      </figure>
      <div class="card-body">
        <form @submit.prevent="onLogin">
          <h2 class="card-title">Baywatch</h2>
          <div class="form-control">
            <label class="label"><span class="label-text">Email Address</span></label>
            <input ref="usrInput" v-model="username" class="input input-bordered" type="text" placeholder="Username"
                   tabindex="1"
                   :class="{'input-error': usernameError}">
          </div>
          <div class="form-control mt-4">
            <label class="label"><span class="label-text">Password</span>
              <a href="#" class="label-text-alt">Forget password ?</a></label>
            <input v-model="password" class="input input-bordered" type="password" placeholder="Password" tabindex="2"
                   :class="{'input-error': passwordError}">
            <button type="submit" class="btn btn-primary mt-4" tabindex="3">Se connecter</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import userService from "@/services/UserService";

@Options({name: 'LoginPage'})
export default class LoginPage extends Vue {
  public username = '';
  public password = '';
  private formValidation = false;

  mounted(): void {
    (this.$refs.usrInput as HTMLElement).focus();
  }

  onLogin(): void {
    if (this.username !== '' && this.password !== '') {
      userService.login(this.username, this.password)
          .subscribe(() => this.$router.back());
    } else {
      this.formValidation = true;
    }
  }

  get usernameError(): boolean {
    return this.username === '' && this.formValidation;
  }

  get passwordError(): boolean {
    return this.password === '' && this.formValidation;
  }

  public closeLoginWindow(): void {
    this.$router.back();
  }
}
</script>