import {IAlertDialog} from "@/shared/components/alertdialog/AlertDialog.types";

declare module '*.vue' {
    import Vue from 'vue'
    export default Vue
}

declare module '@vue/runtime-core' {
    export interface ComponentCustomProperties {
        $alert: IAlertDialog;
    }
}