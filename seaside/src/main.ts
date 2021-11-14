import Vue from 'vue';
import store from './store'
import router from "./router";
import App from './App.vue';
import VueRx from 'vue-rx';
import './assets/styles/index.css';
import AlertDialog from "@/components/shared/AlertDialog.vue";

Vue.config.productionTip = false
Vue.use(VueRx);
Vue.use(AlertDialog);

new Vue({
    render: h => h(App),
    router: router,
    store: store,
}).$mount('#app')
