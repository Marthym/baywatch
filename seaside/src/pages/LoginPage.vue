<template>
    <div class="absolute inset-0 z-50 h-screen w-screen backdrop-filter bg-neutral lg:bg-opacity-90"
         @click.prevent="closeLoginWindow" @keydown.esc="closeLoginWindow">
        <div class="card bordered flex rounded-none overflow-hidden h-full
                lg:rounded-lg lg:shadow-lg lg:h-fit lg:mt-8 mx-auto lg:card-side lg:max-w-4xl"
             @click.stop>
            <img class="object-cover" src="/login.webp" alt="Baywatch">
            <div class="card-body">
                <form @submit.prevent="onLogin">
                    <h2 class="card-title">Baywatch</h2>
                    <div class="form-control">
                        <label class="label"><span class="label-text">Email Address</span></label>
                        <input ref="usrInput" v-model="username" class="input input-bordered" type="text"
                               placeholder="Username"
                               tabindex="1"
                               :class="{'input-error': formValidation}">
                    </div>
                    <div class="form-control mt-4">
                        <label class="label"><span class="label-text">Password</span>
                            <a href="#" class="label-text-alt">Forget password ?</a></label>
                        <input v-model="password" class="input input-bordered" type="password"
                               placeholder="Password"
                               tabindex="2"
                               @keyup="formValidation=false"
                               :class="{'input-error': formValidation}">
                        <button type="submit" class="btn btn-primary mt-4" tabindex="3">Se connecter</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-facing-decorator';
import {useStore} from "vuex";
import {UPDATE_MUTATION} from "@/store/user/UserConstants";

import authenticationService from "@/security/services/AuthenticationService";
import notificationService from "@/services/notification/NotificationService";
import {Router, useRouter} from "vue-router";

@Component({
    name: 'LoginPage',
    setup() {
        return {
            store: useStore(),
            router: useRouter(),
        }
    }
})
export default class LoginPage extends Vue {
    private store;
    private router: Router;

    public username = '';
    public password = '';
    private formValidation = false;

    mounted(): void {
        (this.$refs.usrInput as HTMLElement).focus();
    }

    onLogin(): void {
        if (this.username !== '' && this.password !== '') {
            authenticationService.login(this.username, this.password).subscribe({
                next: user => {
                    this.store.commit(UPDATE_MUTATION, user);
                    this.router.back();
                },
                error: err => {
                    this.formValidation = true;
                    notificationService.pushSimpleError("Wrong login or password !");
                    console.debug(err);
                }
            });
        } else {
            this.formValidation = true;
        }
    }

    get usernameError(): boolean {
        return this.formValidation;
    }

    get passwordError(): boolean {
        return this.formValidation;
    }

    public closeLoginWindow(): void {
        this.router.back();
    }
}
</script>