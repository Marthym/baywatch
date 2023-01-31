<template>
  <div class="overflow-x-auto mt-4">
    <div class="md:btn-group mb-2">
      <button class="btn btn-sm btn-ghost mb-2 mr-2 md:m-0" @click.prevent="onUserAdd()">
        <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
        <span>Ajouter</span>
      </button>
      <button class="btn btn-sm mb-2 mr-2 md:m-0" @click="">
        <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
        Importer
      </button>
      <a class="btn btn-sm mb-2 mr-2 md:m-0">
        <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
        Exporter
      </a>
      <button class="btn btn-sm btn-error mb-2 mr-2 md:m-0" :disabled="!checkState" @click="onUserBulkDelete()">
        <TrashIcon class="w-6 h-6"/>
        Supprimer
      </button>
    </div>
    <table class="table w-full table-compact" aria-describedby="User List">
      <thead>
      <tr>
        <th scope="col">
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
        <th scope="col">Created At
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
        <th scope="col">&nbsp;</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="vUser in this.users" v-bind:key="vUser.data._id">
        <th scope="row">
          <label>
            <input type="checkbox" class="checkbox" v-model="vUser.isSelected">
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <td>{{ vUser.data.login }}</td>
        <td>{{ vUser.data.name }}</td>
        <td>{{ vUser.data.mail }}</td>
        <td>{{ vUser.data.roles.join(', ') }}</td>
        <td>{{ dateToString(vUser.data._createdAt) }}</td>
        <td>
          <div class="btn-group justify-end">
            <button class="btn btn-sm btn-square btn-ghost" @click.prevent="onUserEdit(vUser.data)">
              <PencilIcon class="h-6 w-6"/>
            </button>
            <button class="btn btn-sm btn-square btn-ghost" @click.prevent="onUserDelete(vUser.data)">
              <TrashIcon class="h-6 w-6"/>
            </button>
          </div>
        </td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th scope="col"></th>
        <th scope="col">Pseudo</th>
        <th scope="col">Nom</th>
        <th scope="col">Mail</th>
        <th scope="col">Role</th>
        <th scope="col">Created At
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
        <th scope="col">&nbsp;</th>
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
import {Options, Vue} from 'vue-property-decorator';

import notificationService from "@/services/notification/NotificationService";
import userService from "@/security/services/UserService";
import reloadActionService from "@/common/services/ReloadActionService";
import {Observable} from "rxjs";
import {filter, map, switchMap, tap} from "rxjs/operators";
import {UserView} from "@/administration/model/UserView";
import UserEditor from "@/administration/component/usereditor/UserEditor.vue";
import {User} from "@/security/model/User";
import {AlertResponse, AlertType} from "@/common/components/alertdialog/AlertDialog.types";
import {ArrowDownTrayIcon, ArrowUpTrayIcon, PencilIcon, PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";

@Options({
  name: 'UserAdminTab',
  components: {ArrowDownTrayIcon, ArrowUpTrayIcon, UserEditor, PencilIcon, PlusCircleIcon, TrashIcon},
})
export default class UserAdminTab extends Vue {
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private editorOpened: boolean = false;
  private activeUser: User;

  mounted(): void {
    this.loadUserPage(0).subscribe({
      next: () => reloadActionService.registerReloadFunction(context => {
        if (context === '' || context === 'users') {
          this.loadUserPage(this.activePage).subscribe();
        }
      })
    });
  }

  get checkState(): boolean {
    const userView: UserView | undefined = this.users.find(f => f.isSelected);
    const isOneSelected = userView !== undefined;
    if (this.$refs && this.$refs['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.users.find(f => !f.isSelected) !== undefined;
    return isOneSelected;
  }

  loadUserPage(page: number): Observable<UserView[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return userService.list(resolvedPage).pipe(
        switchMap(page => {
          this.pagesNumber = page.totalPage;
          this.activePage = page.currentPage;
          return page.data;
        }),
        map(fs => fs.map(f => ({isSelected: false, data: f}))),
        tap(fs => this.users = fs)
    )
  }

  dateToString(date: string): string {
    return new Date(date).toLocaleDateString(navigator.languages, {
      timeZone: 'UTC',
      year: 'numeric', month: '2-digit', day: '2-digit', hour: "2-digit", minute: "2-digit"
    });
  }

  private onSelectAll(): void {
    const current = this.checkState;
    this.users.forEach(f => f.isSelected = !current);
  }

  private onUserSubmit(): void {
    const edit = '_id' in this.activeUser && this.activeUser._id !== undefined;
    if (edit) {
      userService.update(this.activeUser).subscribe({
        next: user => {
          notificationService.pushSimpleOk(`User ${user.login} updated successfully !`);
          this.editorOpened = false;
        },
        error: e => notificationService.pushSimpleError(e.message),
      });
    } else {
      userService.add(this.activeUser).subscribe({
        next: user => {
          this.users.push({isSelected: false, data: user});
          notificationService.pushSimpleOk(`User ${user.login} created successfully !`);
          this.editorOpened = false;
        },
        error: e => notificationService.pushSimpleError(e.message),
      });
    }
  }

  private onUserAdd(): void {
    this.activeUser = {roles:[]} as User;
    this.editorOpened = true;
  }

  private onUserEdit(user: User): void {
    this.activeUser = user;
    this.editorOpened = true;
  }

  private onUserDelete(user: User): void {
    const message = `Supprimer l’utilisateur <br/> <b>${user.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => userService.remove(user._id as string)),
        tap(() => {
          const idx = this.users.findIndex(fv => fv.data._id === user._id);
          this.users.splice(idx, 1);
        })
    ).subscribe({
      next: user => notificationService.pushSimpleOk(`User ${user.name} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError(`Unable to delete user ${user.name} !`);
      }
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
        switchMap(() => userService.bulkRemove(ids.map(uv => uv.data._id) as string[])),
        tap(() => {
          ids.forEach(id => {
            const idx = this.users.findIndex(uv => uv.data._id === id.data._id);
            this.users.splice(idx, 1);
          })
        })
    ).subscribe({
      next: () => notificationService.pushSimpleOk('All users deleted successfully !'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to delete all selected users !');
      }
    });
  }

  // noinspection JSUnusedGlobalSymbols
  public unmounted(): void {
    reloadActionService.unregisterReloadFunction();
  }
}
</script>
