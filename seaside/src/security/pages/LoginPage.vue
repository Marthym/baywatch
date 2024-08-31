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
          <label class="form-control w-full">
            <span class="label">
              <span class="label-text">{{ t('login.username') }}</span>
            </span>
            <input ref="usrInput" v-model="username" :class="{'input-error': formValidation}"
                   :placeholder="t('login.username')"
                   class="input input-bordered"
                   type="text">
          </label>
          <label class="form-control w-full">
            <span class="label">
              <span class="label-text">{{ t('login.password') }}</span>
              <span class="label-text-alt"><a class="label-text-alt" href="#" tabindex="-1">{{
                  t('login.password.forget')
                }}</a></span>
            </span>
            <input v-model="password" :class="{'input-error': formValidation}" :placeholder="t('login.password')"
                   class="input input-bordered"
                   type="password"
                   @keyup="formValidation=false">
          </label>
          <button class="btn btn-primary w-full mt-8" type="submit">{{ t('login.login') }}</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { Store, useStore } from 'vuex';
import { UPDATE_MUTATION } from '@/security/store/UserConstants';

import authenticationService from '@/security/services/AuthenticationService';
import notificationService from '@/services/notification/NotificationService';
import { Router, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { switchMap } from 'rxjs';
import { userSettingsGet } from '@/security/services/UserSettingsService';
import { map } from 'rxjs/operators';
import { UserState } from '@/security/store/user';

@Component({
  name: 'LoginPage',
  setup() {
    const { t, locale } = useI18n();
    return {
      store: useStore(),
      router: useRouter(),
      t: t,
      locale: locale,
    };
  },
})
export default class LoginPage extends Vue {
  public username = '';
  public password = '';
  private t;
  private locale;
  private formValidation = false;
  private store!: Store<UserState>;
  private router!: Router;

  mounted(): void {
    (this.$refs.usrInput as HTMLElement).focus();
  }

  onLogin(): void {
    if (this.username !== '' && this.password !== '') {
      authenticationService.login(this.username, this.password).pipe(
          switchMap(user => userSettingsGet(user._id).pipe(map(settings => ({ user, settings })))),
      ).subscribe({
        next: authent => {
          if (authent.settings?.preferredLocale) {
            this.locale = authent.settings.preferredLocale;
          }
          this.store.commit(UPDATE_MUTATION, authent.user);
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