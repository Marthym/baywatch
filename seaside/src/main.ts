import store from './store'
import router from "./router";
import App from './App.vue';
import './assets/styles/index.css';
import {createApp} from "vue";
import {plugin as alertDialogPlugin} from "@/components/shared/AlertDialog.vue";

// Vue.config.productionTip = false
// Vue.use(VueRx);
// Vue.use(AlertDialog);

createApp(App)
    .use(router)
    .use(store)
    .use(alertDialogPlugin)
    .mount('#app');