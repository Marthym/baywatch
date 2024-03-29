<template>
  <div
      class="stack absolute bottom-5 right-5 z-50 transition-opacity easy-in-out duration-500 opacity-100"
      @mouseenter="onMouseEnterNotification()" @mouseleave="onMouseLeaveNotification()">
    <div v-for="(notif, index) in notifications" :key="notif.id" class="alert shadow-lg" :class="{
      'bg-error': isError(notif),
      'bg-warning': isWarning(notif),
      'bg-info': isInfo(notif),
      }">
      <Component :is="notif.icon" class="w-6 h-6 stroke-current" :class="{'stroke-info': isNotice(notif)}"/>
      <div>
        <h3 v-if="notif.raw.title" class="font-bold">{{ notif.raw.title }}</h3>
        <div class="text-xs">{{ notif.raw.message }}</div>
      </div>
      <div class="join">
        <button v-if="hasAction(notif, 'C')" @click.stop="onClipAction(notif)" class="btn btn-xs btn-ghost join-item"
                :class="{'text-accent': notif.doneActions?.indexOf('C') >= 0}">
          <PaperClipIcon class="h-5 w-5 stroke-current"/>
          <span class="hidden lg:block">clip</span>
        </button>
        <button v-if="hasAction(notif, 'S')" @click.stop="onShareAction(notif)" class="btn btn-xs btn-ghost join-item"
                :class="{'text-accent': notif.doneActions?.indexOf('S') >= 0}">
          <ShareIcon class="h-5 w-5 cursor-pointer stroke-2"/>
          <span class="hidden lg:block">share</span>
        </button>
        <button class="btn btn-xs btn-ghost btn-square join-item" @click="onCloseNotification(index)">
          <XMarkIcon class="w-5 h-5 stroke-current"/>
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { NotificationView } from '@/common/components/notificationArea/NotificationView';
import { Notification } from '@/services/notification/Notification.type';
import NotificationListener from '@/services/notification/NotificationListener';
import notificationService from '@/services/notification/NotificationService';
import { Severity } from '@/services/notification/Severity.enum';
import {
  ExclamationCircleIcon,
  ExclamationTriangleIcon,
  PaperClipIcon,
  ShareIcon,
  XCircleIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';
import { newsMark, newsUnMark } from '@/techwatch/services/NewsService';
import { Mark } from '@/techwatch/model/Mark.enum';
import { actionServiceReload } from '@/common/services/ReloadActionService';
import { NotificationCode } from '@/services/notification/NotificationCode.enum';

@Component({
  name: 'NotificationArea',
  components: { ShareIcon, PaperClipIcon, ExclamationCircleIcon, ExclamationTriangleIcon, XCircleIcon, XMarkIcon },
})
export default class NotificationArea extends Vue implements NotificationListener {
  private notifications: NotificationView[] = [];
  private keys = 0;
  private popOnBlur?: number[];

  mounted(): void {
    notificationService.registerNotificationListener(this);
  }

  onPushNotification(notif: Notification): void {
    this.notifications.push({
      id: ++this.keys,
      icon: (() => {
        switch (notif.severity) {
          case 'warning':
            return 'ExclamationTriangleIcon';
          case 'error':
            return 'XCircleIcon';
          case 'info':
          case 'notice':
          default:
            return 'ExclamationCircleIcon';
        }
      })(),
      raw: notif,
    });
    if (notif.code === NotificationCode.NEWS_ADD) {
      actionServiceReload('news');
    }
  }

  /**
   * Call when user click on the close notification button on the notif
   * This does not take care about the onBlur property.
   *
   * @param nIndex The index of the notification to close
   */
  onCloseNotification(nIndex: number): void {
    if (nIndex >= 0) {
      this.notifications.splice(nIndex, 1);
    } else if (this.notifications.length > 0) {
      this.notifications.splice(0, this.notifications.length);
    }
  }

  /**
   * Call when notification rise the timeout display time.
   * @param notif The raw notification to close
   */
  onPopNotification(notif: Notification): void {
    const idx = this.notifications.findIndex(e => e.raw.message === notif.message);
    if (idx >= 0) {
      if (this.popOnBlur) {
        this.popOnBlur.push(idx);
      } else {
        this.onCloseNotification(idx);
      }
    } else if (this.notifications.length > 0) {
      this.notifications.splice(0, this.notifications.length);
    }
  }

  onClipAction(n: NotificationView) {
    if (n.raw.target) {
      const newsClipAction = (n.doneActions?.indexOf('C') >= 0) ? newsUnMark : newsMark;

      if (!n.doneActions) {
        n.doneActions = '';
      }

      newsClipAction(n.raw.target, Mark.KEEP).subscribe({
        next: status => {
          if (status.keep) {
            n.doneActions = `${n.doneActions}C`;
          } else if (n.doneActions) {
            n.doneActions = n.doneActions.replaceAll('C', '');
          }
        },
        error: () => console.error('Unable to (un)clip target ' + n.raw.target),
      });
    }
  }

  onShareAction(n: NotificationView) {
    if (n.raw.target) {
      const newsShareAction = (n.doneActions?.indexOf('S') >= 0) ? newsUnMark : newsMark;

      if (!n.doneActions) {
        n.doneActions = '';
      }
      newsShareAction(n.raw.target, Mark.SHARED).subscribe({
        next: status => {
          if (status.shared) {
            n.doneActions = `${n.doneActions}S`;
          } else if (n.doneActions) {
            n.doneActions = n.doneActions.replaceAll('S', '');
          }
        },
        error: () => console.error('Unable to (un)share target ' + n.raw.target),
      });
    }
  }

  onMouseEnterNotification(): void {
    this.popOnBlur = [];
  }

  onMouseLeaveNotification(): void {
    if (this.popOnBlur) {
      this.popOnBlur.forEach(idx => this.notifications.splice(idx, 1));
    }
    delete this.popOnBlur;
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

  private isNotice(n: NotificationView) {
    return n.raw.severity === Severity.notice;
  }

  private hasAction(n: NotificationView, action: 'V' | 'S' | 'C') {
    return n.raw.actions?.indexOf(action) >= 0;
  }

  destroyed(): void {
    notificationService.unregisterNotificationListener(this);
  }
}
</script>

