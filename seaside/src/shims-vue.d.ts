import {IAlertDialog} from "@/components/shared/alertdialog/AlertDialog.types";

declare module '*.vue' {
    import Vue from 'vue'
    export default Vue
}

declare module '@vue/runtime-core' {
    export interface ComponentCustomProperties {
        $alert: IAlertDialog;
    }
}