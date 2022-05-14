<template>
  <div class="stack absolute bottom-5 right-5 transition-opacity easy-in-out duration-500 opacity-100 hover:opacity-30">
    <div v-for="notif in notifications" :key="notif.id" class="alert" :class="{
      'bg-error': isError(notif),
      'bg-warning': isWarning(notif),
      'bg-info': isInfo(notif),
      }">
      <div class="flex-1">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="w-6 h-6 mx-2 stroke-current"
             v-if="isError(notif)">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path>
        </svg>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"
             v-else-if="isWarning(notif)">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
        </svg>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"
             v-else-if="isInfo(notif)">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <label>{{ notif.raw.message }}</label>
      </div>
      <div class="flex-none">
        <button class="btn btn-sm btn-ghost btn-square" @click="onPopNotification(notif.raw)">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
               class="inline-block w-6 h-6 stroke-current">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import {NotificationView} from "@/shared/components/notificationArea/NotificationView";
import {Notification} from "@/services/notification/Notification.type";
import NotificationListener from "@/services/notification/NotificationListener";
import notificationService from '@/services/notification/NotificationService';
import {Severity} from "@/services/notification/Severity.enum";

@Options({
  name: 'NotificationArea',
  components: {},
})
export default class NotificationArea extends Vue implements NotificationListener {
  private notifications: NotificationView[] = [];
  private keys = 0;

  mounted(): void {
    notificationService.registerNotificationListener(this);
  }

  onPushNotification(notif: Notification): void {
    this.notifications.push({
      id: ++this.keys,
      raw: notif,
    });
  }

  onPopNotification(notif: Notification): void {
    const idx = this.notifications.findIndex(e => e.raw.message === notif.message);
    if (idx >= 0) {
      this.notifications.splice(idx, 1);

    } else if (this.notifications.length > 0) {
      this.notifications.splice(0, this.notifications.length);
    }
  }

  private isError(n: NotificationView) {
    return n.raw.severity === Severity.error;
  }

  private isWarning(n: NotificationView) {
    return n.raw.severity === Severity.warning;
  }

  private isInfo(n: NotificationView) {
    return n.raw.severity === Severity.info;
  }

  destroyed(): void {
    notificationService.unregisterNotificationListener(this);
  }
}
</script>

