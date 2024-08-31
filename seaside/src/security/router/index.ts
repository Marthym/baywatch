import { NavigationGuardWithThis, RouteRecordRaw } from 'vue-router';
import { store } from '@/store';
import { i18n } from '@/i18n';
import { Session } from '@/security/model/Session';
import { firstValueFrom } from 'rxjs';
import { refresh } from '@/security/services/AuthenticationService';
import {
    LOGOUT_MUTATION as USER_LOGOUT_MUTATION,
    UPDATE_MUTATION as USER_UPDATE_MUTATION,
} from '@/security/store/UserConstants';

const LoginPage = () => import('@/security/pages/LoginPage.vue');

export const routes: RouteRecordRaw[] = [
    { path: '/login', component: LoginPage, name: 'LoginPage' },
];

export const requireAuthNavGuard: NavigationGuardWithThis<NavigationGuardWithThis<boolean>> = async to => {
    if (store.state.user.isAuthenticated === undefined) {
        try {
            const session: Session = await firstValueFrom(refresh());

            if (session.settings?.preferredLocale) {
                i18n.global.locale.value = session.settings.preferredLocale;
            }
            store.commit(USER_UPDATE_MUTATION, session.user);
        } catch (err) {
            store.commit(USER_LOGOUT_MUTATION);
        }
    }
    const isAuthenticated = store.state.user.isAuthenticated;
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            return { name: 'LoginPage', query: { redirect: to.path } };
        }
    }
};