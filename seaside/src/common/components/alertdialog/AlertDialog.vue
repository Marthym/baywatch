<template>
  <div v-if="instance.isFired" :class="{'opacity-100 pointer-events-auto visible': instance.isFired}"
       class="modal compact flex-col space-x-0">
    <div class="modal-box translate-y-0 rounded-none">
      <p v-html="instance.message"></p>
      <div v-if="isInfoDialog" class="modal-action">
        <button class="btn" @click.stop="onClose">Fermer</button>
      </div>
      <div v-else-if="isConfirmDialog" class="modal-action">
        <button class="btn" @click.stop="onCancel">Annuler</button>
        <button class="btn btn-error" @click.stop="onConfirm">{{ instance.confirmLabel }}</button>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import {
  alertInjectionKey,
  AlertResponse,
  AlertType,
  IAlertDialog
} from "@/common/components/alertdialog/AlertDialog.types";
import {setup} from "vue-class-component";
import {inject} from "vue";

@Options({name: 'AlertDialog'})
export default class AlertDialog extends Vue {

  instance: IAlertDialog = setup(() => inject(alertInjectionKey)) as IAlertDialog;

  get isInfoDialog(): boolean {
    return this.instance.alertType === AlertType.INFO;
  }

  get isConfirmDialog(): boolean {
    return this.instance.alertType === AlertType.CONFIRM_DELETE;
  }

  private onClose(): void {
    this.instance.close(AlertResponse.OK);
  }

  private onConfirm(): void {
    this.instance.close(AlertResponse.CONFIRM);
  }

  private onCancel(): void {
    this.instance.close(AlertResponse.CANCEL);
  }
}

</script>