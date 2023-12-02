import { IAlertDialog } from '@/common/components/alertdialog/AlertDialog.types';
import 'vue-router';

declare module '*.vue' {
    import Vue from 'vue';
    export default Vue;
}

declare module '@vue/runtime-core' {
    export interface ComponentCustomProperties {
        $alert: IAlertDialog;
    }
}

declare module 'vue-router' {
    interface RouteMeta {
        requiresAuth?: boolean;
    }
}
