import {Observable, Subject} from "rxjs";
import {InjectionKey} from "vue";

export const alertInjectionKey: InjectionKey<IAlertDialog> = Symbol('IAlertDialog') as InjectionKey<IAlertDialog>;

export enum AlertType {INFO, CONFIRM_DELETE}

export enum AlertResponse {OK, CONFIRM, CANCEL}

export interface IAlertDialog {
    isFired: boolean;
    message: string;
    alertType: AlertType;
    response?: Subject<AlertResponse>;

    fire(message: string, alertType: AlertType): Observable<AlertResponse>;

    close(response: AlertResponse): void;
}
