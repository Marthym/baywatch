<template>
  <label class="input w-full" @click.stop>
    <span class="label">URL</span>
    <input ref="linkInput" v-model="link" :class="{'input-error': hasError}"
           placeholder="Paste url here" type="text"
           @input="hasError = false"
           @keyup.esc="$emit('close')"
           @keyup.enter="importNewsFromLink"/>
    <button class="label btn btn-link text-secondary"
            @click="importNewsFromLink">
      <PlusCircleIcon class="h-6 w-6"/>
    </button>
  </label>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-facing-decorator';
import { scraperImportStandaloneNews } from '@/layout/services/ScraperService';
import notificationService from '@/services/notification/NotificationService';
import { NotificationCode } from '@/services/notification/NotificationCode.enum';
import { Severity } from '@/services/notification/Severity.enum';
import { PlusCircleIcon } from '@heroicons/vue/24/outline';

@Component({
  name: 'AddSingleNewsAction',
  emits: ['close'],
  props: ['isTransitioning'],
  components: { PlusCircleIcon },
})
export default class AddSingleNewsAction extends Vue {
  @Prop() private isTransitioning!: boolean;

  private link: string = '';
  private hasError: boolean = false;

  @Watch('isTransitioning')
  onAuthenticationChange(): void {
    if (!this.isTransitioning) {
      this.$refs.linkInput.focus();
    }
  }

  private importNewsFromLink(): void {
    this.$emit('close');
    scraperImportStandaloneNews(this.link).subscribe({
      next: () => console.debug('Scraping request send successfully.'),
      error: err => notificationService.pushNotification({
        code: NotificationCode.ERROR, message: `${err.message}`, severity: Severity.error,
      }),
    });
  }
}
</script>
