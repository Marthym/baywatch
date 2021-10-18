<template>
  <div class="stack absolute bottom-5 right-5">
    <div class="alert bg-error" v-for="notif in notifications" :key="notif.id">
      <div class="flex-1">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="w-6 h-6 mx-2 stroke-current">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path>
        </svg>
        <label>{{ notif.message }}</label>
      </div>
      <div class="flex-none">
        <button class="btn btn-sm btn-ghost btn-square">
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
import {Component, Vue} from "vue-property-decorator";
import {NotificationView} from "@/components/shared/notificationArea/NotificationView";
import {Notification} from "@/services/notification/Notification";
import NotificationListener from "@/services/notification/NotificationListener";
import notificationService from '@/services/notification/NotificationService';

@Component({
  components: {},
})
export default class NotificationArea extends Vue implements NotificationListener {
  private notifications: NotificationView[] = [];
  private keys = 0;

  mounted(): void {
    notificationService.registerNotificationListener(this);
  }

  onPushNotification(notif: Notification): void {
    console.info('recieve: ', notif);
    this.notifications.push({
      id: ++this.keys,
      severity: notif.severity,
      message: notif.message
    });
  }

  onPopNotification(): void {
    this.notifications.shift();
  }
}
</script>

