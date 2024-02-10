import { NavigationGuardWithThis, RouteRecordRaw } from 'vue-router';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { refresh } from '@/security/services/AuthenticationService';
import { LOGOUT_MUTATION, UPDATE_MUTATION as USER_UPDATE_MUTATION } from '@/security/store/UserConstants';

const LoginPage = () => import('@/security/pages/LoginPage.vue');

export const routes: RouteRecordRaw[] = [
    { path: '/login', component: LoginPage, name: 'LoginPage' },
];

function refreshSession(store: Store<UserState>): Promise<boolean> {
    const isAuthenticated = useStore<UserState>().state.user.isAuthenticated;
    if (isAuthenticated === undefined) {
        return new Promise(resolve => {
            refresh().subscribe({
                next: session => {
                    store.commit(USER_UPDATE_MUTATION, session.user);
                    resolve(true);
                },
                error: () => {
                    store.commit(LOGOUT_MUTATION);
                    resolve(false);
                },
            });
        });
    } else {
        return Promise.resolve(true);
    }
}

export const requireAuthNavGuard: NavigationGuardWithThis<NavigationGuardWithThis<boolean>> = async to => {
    const isAuthenticated = await refreshSession(useStore<UserState>());
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            return { name: 'LoginPage', query: { redirect: to.path } };
        }
    }
};