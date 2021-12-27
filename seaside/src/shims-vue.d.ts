import AlertDialog from '@/components/shared/AlertDialog.vue';

declare module '*.vue' {
    import Vue from 'vue'
    export default Vue
}

declare module '@vue/runtime-core' {
    export interface ComponentCustomProperties {
        $alert: AlertDialog;
    }
}