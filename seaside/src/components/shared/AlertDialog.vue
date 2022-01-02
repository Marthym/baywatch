<template>
  <div v-if="isOpened" :class="{'opacity-100 pointer-events-auto visible': isOpened}" class="modal compact flex-col space-x-0">
    <div class="modal-box translate-y-0 rounded-none">
      <p v-html="message"></p>
      <div v-if="isInfoDialog" class="modal-action">
        <button class="btn" @click.stop="onClose">Fermer</button>
      </div>
      <div v-else-if="isConfirmDialog" class="modal-action">
        <button class="btn" @click.stop="onCancel">Annuler</button>
        <button class="btn btn-error" @click.stop="onConfirm">Supprimer</button>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import {Observable, Subject} from "rxjs";

export enum AlertType {INFO, CONFIRM_DELETE}

export enum AlertResponse {OK, CONFIRM, CANCEL}

@Options({name: 'AlertDialog'})
export default class AlertDialog extends Vue {

  private message!: string;
  private alertType!: AlertType;
  private isOpened = false;
  private response?: Subject<AlertResponse>;

  get isInfoDialog(): boolean {
    return this.alertType === AlertType.INFO;
  }

  get isConfirmDialog(): boolean {
    return this.alertType === AlertType.CONFIRM_DELETE;
  }

  public fire(message: string, alertType: AlertType = AlertType.INFO): Observable<AlertResponse> {
    if (this.isOpened) {
      if (this.response) {
        this.response.error(new Error('Multiple dialog at same time !'));
        this.response.complete();
      }
      this.isOpened = false;
    }

    this.message = message;
    this.alertType = alertType;
    this.isOpened = true;

    this.response = new Subject<AlertResponse>();
    return this.response.asObservable();
  }

  private close(response: AlertResponse): void {
    this.isOpened = false;
    if (this.response) {
      this.response.next(response);
      this.response.complete();
    }
  }

  private onClose(): void {
    this.close(AlertResponse.OK);
  }

  private onConfirm(): void {
    this.close(AlertResponse.CONFIRM);
  }

  private onCancel(): void {
    this.close(AlertResponse.CANCEL);
  }
}

export const plugin = {
  install(app): void {
    app.component('AlertDialog', AlertDialog);
  }
}

</script>