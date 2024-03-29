<template>
  <div class="overflow-x-auto mt-4">
    <div class="md:join mb-2">
      <button class="btn btn-sm mb-2 mr-2 join-item md:m-0" @click.prevent="onUserAdd()">
        <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
        <span>Ajouter</span>
      </button>
      <button class="btn btn-sm btn-ghost mb-2 mr-2 join-item md:m-0" @click="">
        <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
        Importer
      </button>
      <a class="btn btn-sm btn-ghost mb-2 mr-2 join-item md:m-0">
        <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
        Exporter
      </a>
      <button class="btn btn-sm btn-error mb-2 mr-2 join-item md:m-0" :disabled="!checkState" @click="onUserBulkDelete()">
        <TrashIcon class="w-6 h-6"/>
        Supprimer
      </button>
    </div>
    <table class="table w-full table-sm" aria-describedby="User List">
      <thead>
      <tr>
        <th scope="col" class="w-1">
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState" @change="onSelectAll()"/>
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th scope="col">Pseudo</th>
        <th scope="col">Nom</th>
        <th scope="col">Mail</th>
        <th scope="col">Role</th>
        <th scope="col">Created At</th>
        <th scope="col">
          <div class="join justify-end" v-if="pagesNumber > 1">
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
        <th scope="row" class="w-1">
          <label>
            <input type="checkbox" class="checkbox" v-model="vUser.isSelected">
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <td>
          {{ vUser.data.login }}
          <div class="tooltip tooltip-right" :data-tip="vUser.data._id">
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
          <div class="join justify-end w-full">
            <button class="btn btn-sm btn-square btn-ghost join-item" @click.prevent="onUserEdit(vUser.data)">
              <PencilIcon class="h-6 w-6"/>
            </button>
            <button class="btn btn-sm btn-square btn-ghost join-item" @click.prevent="onUserDelete(vUser.data)">
              <TrashIcon class="h-6 w-6"/>
            </button>
          </div>
        </td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th scope="col" class="w-1"></th>
        <th scope="col">Pseudo</th>
        <th scope="col">Nom</th>
        <th scope="col">Mail</th>
        <th scope="col">Role</th>
        <th scope="col">Created At</th>
        <th scope="col">
          <div class="join justify-end" v-if="pagesNumber > 1">
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
    <UserEditor v-if="editorOpened"
                v-model="activeUser"
                @submit="onUserSubmit"
                @cancel="editorOpened = false"/>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';

import notificationService from '@/services/notification/NotificationService';
import { userCreate, userDelete, userList, userUpdate } from '@/security/services/UserService';
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
})
export default class UserAdminTab extends Vue {
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private editorOpened: boolean = false;
  private activeUser: User;
  private activeUserChange: UserChangement = { properties: false, roles: [] };

  mounted(): void {
    this.loadUserPage(0).subscribe({
      next: () => actionServiceRegisterFunction(context => {
        if (context === '' || context === 'users') {
          this.loadUserPage(this.activePage).subscribe();
        }
      }),
    });
  }

  get checkState(): boolean {
    const userView: UserView | undefined = this.users.find(f => f.isSelected);
    const isOneSelected = userView !== undefined;
    if (this.$refs?.['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.users.find(f => !f.isSelected) !== undefined;
    return isOneSelected;
  }

  loadUserPage(page: number): Observable<UserView[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return userList(resolvedPage).pipe(
        map(page => {
          this.pagesNumber = page.totalPage;
          this.activePage = page.currentPage;
          return page.data;
        }),
        map(users => users.map(user => ({ isSelected: false, data: user }))),
        tap(users => this.users = users),
    );
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
    notificationService.pushSimpleOk(`User ID copied on clipboard !`);
  }

  private onUserSubmit(): void {
    console.log('sibmit');
    const edit = '_id' in this.activeUser && this.activeUser._id !== undefined;
    if (edit) {
      this.updateActiveUser();
    } else {
      this.createActiveUser();
    }
  }

  private createActiveUser(): void {
    userCreate(this.activeUser).subscribe({
      next: user => {
        this.users.push({ isSelected: false, data: user });
        notificationService.pushSimpleOk(`User ${user.login} created successfully !`);
        this.editorOpened = false;
      },
      error: e => {
        notificationService.pushSimpleError(e.message);
        this.editorOpened = false;
      },
    });
  }

  private updateActiveUser(): void {
    const idx = this.users.findIndex(uv => uv.data._id === this.activeUser._id);
    if (!this.activeUser._id) {
      console.error('Active user has no ID !');
      return;
    }
    userUpdate(this.activeUser._id, this.activeUser).subscribe({
      next: user => {
        this.users.splice(idx, 1, { isSelected: false, data: user });
        notificationService.pushSimpleOk(`User ${user.login} updated successfully !`);
        this.editorOpened = false;
      },
      error: e => {
        notificationService.pushSimpleError(e.message);
        this.editorOpened = false;
      },
    });
  }

  private onUserAdd(): void {
    this.activeUser = { roles: [] } as User;
    this.activeUserChange = { properties: false, roles: [] };
    this.editorOpened = true;
  }

  private onUserEdit(user: User): void {
    this.activeUser = { ...user };
    this.activeUserChange = { properties: false, roles: [] };
    this.editorOpened = true;
  }

  private onUserDelete(user: User): void {
    const message = `Supprimer l’utilisateur <br/> <b>${user.name}</b>`;
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
      next: () => notificationService.pushSimpleOk(`User ${user.name} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError(`Unable to delete user ${user.name} !`);
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
    const message = `Delete all ${ids.length} selected users ?`;
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
      next: () => notificationService.pushSimpleOk('All users deleted successfully !'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to delete all selected users !');
      },
    });
  }

  // noinspection JSUnusedGlobalSymbols
  public unmounted(): void {
    actionServiceUnregisterFunction();
  }
}

type UserChangement = {
  properties: boolean,
  roles: { type: 'grant' | 'revoke', perm: string }[]
}
</script>
