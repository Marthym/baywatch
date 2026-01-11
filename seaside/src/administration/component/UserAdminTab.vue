<template>
  <div class="overflow-x-auto">
    <div class="md:join mb-2">
      <router-link class="btn btn-sm mb-2 mr-2 join-item md:m-0 capitalize" tag="button" to="/admin/users/new">
        <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
        <span>{{ t('admin.users.add') }}</span>
      </router-link>
      <button class="btn btn-sm btn-ghost mb-2 mr-2 join-item md:m-0 capitalize" @click="">
        <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
        {{ t('admin.users.import') }}
      </button>
      <a class="btn btn-sm btn-ghost mb-2 mr-2 join-item md:m-0 capitalize">
        <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
        {{ t('admin.users.export') }}
      </a>
      <button :disabled="!checkState" class="btn btn-sm btn-error mb-2 mr-2 join-item md:m-0 capitalize"
              @click="onUserBulkDelete()">
        <TrashIcon class="w-6 h-6"/>
        {{ t('admin.users.delete') }}
      </button>
    </div>
    <table aria-describedby="User List" class="table w-full table-sm">
      <thead>
      <tr>
        <th class="w-1" scope="col">
          <input ref="globalCheck" :checked="checkState" class="checkbox"
                 type="checkbox" @change="onSelectAll()"/>
        </th>
        <th class="capitalize" scope="col">{{ t('admin.users.login') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.username') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.mail') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.role') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.createdAt') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.lastActivity') }}</th>
        <th scope="col">
          <div v-if="pagesNumber > 1" class="join justify-end">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="join-item btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="vUser in this.users" v-bind:key="vUser.data._id">
        <th class="w-1" scope="row">
          <input v-model="vUser.isSelected" class="checkbox" type="checkbox"/>
          <span class="checkbox-mark"></span>
        </th>
        <td>
          {{ vUser.data.login }}
          <div :data-tip="vUser.data._id" class="tooltip tooltip-right tooltip-secondary">
            <button class="btn btn-circle btn-xs btn-ghost -ml-2"
                    @click.prevent.stop="onCopyToClipboard(vUser.data._id)">
              <InformationCircleIcon class="w-3 h-3"/>
            </button>
          </div>
        </td>
        <td>{{ vUser.data.name }}</td>
        <td>{{ vUser.data.mail }}</td>
        <td>{{ roleFromPermission(vUser.data.roles) }}</td>
        <td>{{ vUser.data._createdAt }}</td>
        <td>
          <span :data-tip="vUser.data._loginIP"
                class="tooltip tooltip-top tooltip-secondary text-left">{{ vUser.data._loginAt }}</span>
        </td>
        <td>
          <div class="join justify-end w-full">
            <router-link :to="`/admin/users/${vUser.data._id}`"
                         class="btn btn-sm btn-square btn-ghost join-item"
                         tag="button">
              <PencilIcon class="h-6 w-6"/>
            </router-link>
            <button class="btn btn-sm btn-square btn-ghost join-item" @click.prevent="onUserDelete(vUser.data)">
              <TrashIcon class="h-6 w-6"/>
            </button>
          </div>
        </td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th class="w-1" scope="col"></th>
        <th class="capitalize" scope="col">{{ t('admin.users.login') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.username') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.mail') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.role') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.createdAt') }}</th>
        <th class="capitalize" scope="col">{{ t('admin.users.lastActivity') }}</th>
        <th scope="col">
          <div v-if="pagesNumber > 1" class="join justify-end">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="join-item btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
      </tr>
      </tfoot>
    </table>
    <teleport v-if="route.params.userId" to="body">
      <router-view/>
    </teleport>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';

import notificationService from '@/services/notification/NotificationService';
import { userDelete, userList } from '@/security/services/UserService';
import { actionServiceRegisterFunction, actionServiceUnregisterFunction } from '@/common/services/ReloadActionService';
import { Observable } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import { UserView } from '@/administration/model/UserView';
import UserEditor from '@/administration/component/usereditor/UserEditor.vue';
import { User } from '@/security/model/User';
import { AlertResponse, AlertType } from '@/common/components/alertdialog/AlertDialog.types';
import {
  ArrowDownTrayIcon,
  ArrowUpTrayIcon,
  InformationCircleIcon,
  PencilIcon,
  PlusCircleIcon,
  TrashIcon,
} from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';
import { TranslatorFunction } from '@/i18n';
import { RouteLocation, useRoute } from 'vue-router';

@Component({
  name: 'UserAdminTab',
  components: {
    ArrowDownTrayIcon,
    ArrowUpTrayIcon,
    InformationCircleIcon,
    UserEditor,
    PencilIcon,
    PlusCircleIcon,
    TrashIcon,
  },
  setup() {
    const { t } = useI18n();
    const route = useRoute();
    return { t, route };
  },
})
export default class UserAdminTab extends Vue {
  private readonly t!: TranslatorFunction;
  private readonly route!: RouteLocation;
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;

  get checkState(): boolean {
    const userView: UserView | undefined = this.users.find(f => f.isSelected);
    const isOneSelected = userView !== undefined;
    if (this.$refs?.['globalCheck'])
      (this.$refs['globalCheck'] as HTMLInputElement).indeterminate = isOneSelected && this.users.some(f => !f.isSelected);
    return isOneSelected;
  }

  mounted(): void {
    this.loadUserPage(0).subscribe({
      next: () => actionServiceRegisterFunction(context => {
        if (context === '' || context === 'users') {
          this.loadUserPage(this.activePage).subscribe();
        }
      }),
    });
  }

  loadUserPage(page: number): Observable<UserView[]> {
    const resolvedPage = Math.max(page, 0);
    return userList(resolvedPage).pipe(
        switchMap(page => {
          this.pagesNumber = page.totalPage;
          this.activePage = page.currentPage;
          return page.data;
        }),
        map(users => users.map(user => ({ isSelected: false, data: user }))),
        tap(users => this.users = users),
    );
  }

  public unmounted(): void {
    actionServiceUnregisterFunction();
  }

  private roleFromPermission(perm: string[]): string {
    const first = perm.slice(0, 1).join();
    const idx = first.indexOf(':');
    return (idx > 0) ? first.substring(0, idx) : first;
  }

  private onSelectAll(): void {
    const current = this.checkState;
    this.users.forEach(f => f.isSelected = !current);
  }

  private onCopyToClipboard(value: string): void {
    navigator.clipboard.writeText(value);
    notificationService.pushSimpleOk(this.t('admin.users.messages.userIdCopied'));
  }

  private onUserDelete(user: User): void {
    const message = this.t('admin.users.messages.configUsersDeletion', { login: user.name }, 1);
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => userDelete(user._id ? [user._id] : [])),
        tap(deletedUsers => {
          deletedUsers.forEach(user => {
            const idx = this.users.findIndex(uv => uv.data._id === user._id);
            this.users.splice(idx, 1);
          });
        }),
    ).subscribe({
      next: () => notificationService.pushSimpleOk(this.t('admin.users.messages.userDeletedSuccessfully', { login: user.login })),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError(this.t('admin.users.messages.unableDeleteUser', { login: user.login }));
      },
    });
  }

  private onUserBulkDelete(): void {
    const ids = this.users.filter(f => f.isSelected);
    if (ids.length == 0) {
      return;
    } else if (ids.length == 1) {
      return this.onUserDelete(ids[0].data);
    }
    const message = this.t('admin.users.messages.configUsersDeletion', ids.length);
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => userDelete(ids.map(uv => uv.data._id!))),
        tap(deletedUsers => {
          deletedUsers.forEach(user => {
            const idx = this.users.findIndex(uv => uv.data._id === user._id);
            this.users.splice(idx, 1);
          });
        }),
    ).subscribe({
      next: () => notificationService.pushSimpleOk(this.t('admin.users.messages.userDeletedSuccessfully', ids.length)),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError(this.t('admin.users.messages.unableDeleteUser', ids.length));
      },
    });
  }
}

</script>
