import {
    alertInjectionKey,
    AlertResponse,
    AlertType,
    IAlertDialog
} from "@/common/components/alertdialog/AlertDialog.types";
import AlertDialog from "@/common/components/alertdialog/AlertDialog.vue";
import {Observable, Subject} from "rxjs";
import {reactive} from "vue";

const $dialog: IAlertDialog = reactive({
    isFired: false,
    message: "",
    alertType: AlertType.INFO,
    confirmLabel: '',
    fire(message: string, alertType: AlertType = AlertType.INFO, confirmLabel: string = 'Supprimer'): Observable<AlertResponse> {
        if (this.isFired) {
            if (this.response) {
                this.response.error(new Error('Multiple dialog at same time !'));
                this.response.complete();
            }
            this.isFired = false;
        }

        this.message = message;
        this.alertType = alertType;
        this.isFired = true;
        this.confirmLabel = confirmLabel

        this.response = new Subject<AlertResponse>();
        return this.response.asObservable();
    },
    close(response: AlertResponse): void {
        this.isFired = false;
        if (this.response) {
            this.response.next(response);
            this.response.complete();
        }
    }
})

export const plugin = {
    install(app): void {
        app.component('AlertDialog', AlertDialog);
        app.provide(alertInjectionKey, $dialog)
        app.config.globalProperties.$alert = $dialog
    }
}