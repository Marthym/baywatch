<template>
  <curtain-modal @leave="close()" v-slot="curtainModal">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">Create new account</h2>
    <div class="m-4">
      <label class="label">
        <span class="label-text">Team Name</span>
      </label>
      <input type="text" class="input input-bordered w-full">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>
      <label class="label">
        <span class="label-text">Team Topic</span>
      </label>
      <input type="text" class="input input-bordered w-full"
             :class="{'input-error': errors.has('topic')}">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('topic')" class="label-text-alt">{{ errors.get('topic') }}</span>
      </label>
      <div class="text-right">
        <button class="btn btn-sm mx-1" @click.stop="curtainModal.close()">Cancel</button>
        <button class="btn btn-sm btn-primary mx-1">
          Save
        </button>
      </div>
    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal from '@/common/components/CurtainModal.vue';
import { User } from '@/security/model/User';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { CLOSE_CREATE_ACCOUNT_MUTATION } from '@/security/store/UserConstants';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal },
  setup() {
    return {
      userStore: useStore(),
    };
  },
})
export default class CreateAccountComponent extends Vue {
  private readonly userStore: Store<UserState>;
  private account: User;
  private errors: Map<string, string> = new Map<string, string>();

  private close(): void {
    this.userStore.commit(CLOSE_CREATE_ACCOUNT_MUTATION);
  }
}
</script>
