<template>
  <label class="text-right relative w-full" @click.stop>
    <button @click="importNewsFromLink"
            class="absolute -right-2 top-1/2 -mt-6 opacity-60 btn btn-circle btn-link text-secondary">
      <svg class="w-6 h-6 rotate-90" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
        <path
            d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112
            0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"></path>
      </svg>
    </button>
    <input ref="linkInput" v-model="link" type="text" placeholder="Paste the news link"
           class="input input-sm input-bordered w-full pr-8"
           :class="{'input-error': hasError}"
           @input="hasError = false" @keyup.esc="$emit('close')" @keyup.enter="importNewsFromLink"
    />
  </label>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import scraperService from '@/layout/services/ScraperService';
import notificationService from "@/services/notification/NotificationService";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {Severity} from "@/services/notification/Severity.enum";

@Options({
  name: 'AddSingleNewsAction',
  emits: ['close']
})
export default class AddSingleNewsAction extends Vue {
  private link: string = "";
  private hasError: boolean = false;

  mounted(): void {
    this.$refs.linkInput.focus();
  }

  private importNewsFromLink(): void {
    this.$emit('close');
    scraperService.importStandaloneNews(this.link).subscribe({
      next: () => {
        notificationService.pushNotification({
          code: NotificationCode.OK, message: 'News imported successfully !', severity: Severity.info
        });
      },
      error: () => notificationService.pushNotification({
        code: NotificationCode.ERROR, message: 'Unable to add this link !', severity: Severity.error
      }),
    });
  }
}
</script>
