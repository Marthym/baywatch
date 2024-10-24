import { store } from './store';
import App from './App.vue';
import '@/assets/styles/index.css';
import { createApp } from 'vue';
import { plugin as alertDialogPlugin } from '@/common/components/alertdialog/plugin';
import { router } from './router';
import { i18n } from '@/i18n';

createApp(App)
    .use(alertDialogPlugin)
    .use(router)
    .use(store)
    .use(i18n)
    .mount('#app');
