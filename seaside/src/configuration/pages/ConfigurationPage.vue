<template>
    <nav class="tabs tabs-boxed">
        <a class="tab" :class="{'tab-active': 'FeedsList' === activeTab}"
           @click.prevent="onChangeTab('FeedsList')">Feeds</a>
        <a class="tab" :class="{'tab-active': 'PropertiesTab' === activeTab}"
           @click.prevent="onChangeTab('PropertiesTab')">Parameters</a>
    </nav>
    <component :is="activeTab"></component>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-facing-decorator';
import FeedsList from "@/configuration/components/feedslist/FeedsList.vue";

type Tab = 'FeedsList' | 'PropertiesTab';

@Component({
    name: 'ConfigurationPage',
    components: {
        FeedsList,
    },
})
export default class ConfigurationPage extends Vue {
    private activeTab: Tab = 'FeedsList';

    private onChangeTab(toBeActivate: Tab): void {
        this.activeTab = toBeActivate;
    }
}
</script>