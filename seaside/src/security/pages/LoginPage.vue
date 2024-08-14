<template>
  <div class="absolute inset-0 z-50 h-screen w-screen backdrop-filter bg-neutral lg:bg-opacity-90"
       @click.prevent="closeLoginWindow" @keydown.esc="closeLoginWindow">
    <div class="card bordered flex rounded-none overflow-hidden h-full
                lg:rounded-lg lg:shadow-lg lg:h-fit lg:mt-8 mx-auto lg:card-side lg:max-w-4xl"
         @click.stop>
      <img alt="Baywatch" class="object-cover" src="/login.webp">
      <div class="card-body">
        <form @submit.prevent="onLogin">
          <h2 class="card-title">{{ t('main.application') }}</h2>
          <div class="form-control">
            <label class="label"><span class="label-text">{{ t('login.username') }}</span></label>
            <input ref="usrInput" v-model="username" :class="{'input-error': formValidation}" class="input input-bordered"
                   :placeholder="t('login.username')"
                   tabindex="1"
                   type="text">
          </div>
          <div class="form-control mt-4">
            <label class="label"><span class="label-text">{{ t('login.password') }}</span>
              <a class="label-text-alt" href="#">{{ t('login.password.forget') }}</a></label>
            <input v-model="password" :class="{'input-error': formValidation}" class="input input-bordered"
                   :placeholder="t('login.password')"
                   tabindex="2"
                   type="password"
                   @keyup="formValidation=false">
            <button class="btn btn-primary mt-4" tabindex="3" type="submit">{{ t('login.login') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useStore } from 'vuex';
import { UPDATE_MUTATION } from '@/security/store/UserConstants';

import authenticationService from '@/security/services/AuthenticationService';
import notificationService from '@/services/notification/NotificationService';
import { Router, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'LoginPage',
  setup() {
    const { t } = useI18n();
    return {
      store: useStore(),
      router: useRouter(),
      t: t,
    };
  },
})
export default class LoginPage extends Vue {
  public username = '';
  public password = '';
  private store;
  private t;
  private router: Router;
  private formValidation = false;

  mounted(): void {
    (this.$refs.usrInput as HTMLElement).focus();
  }

  onLogin(): void {
    if (this.username !== '' && this.password !== '') {
      authenticationService.login(this.username, this.password).subscribe({
        next: user => {
          this.store.commit(UPDATE_MUTATION, user);
          if (this.router.currentRoute.value.query.redirect) {
            for (let route of this.router.getRoutes()) {
              if (route.path === this.router.currentRoute.value.query.redirect) {
                this.router.push(route);
                return;
              }
            }
          }
          this.router.back();
        },
        error: err => {
          this.formValidation = true;
          notificationService.pushSimpleError('Wrong login or password !');
          console.debug(err);
        },
      });
    } else {
      this.formValidation = true;
    }
  }

  public closeLoginWindow(): void {
    this.router.back();
  }
}
</script>