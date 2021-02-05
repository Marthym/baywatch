import Vue from 'vue';
import App from './App.vue';
import Rx from 'rxjs/Rx';
import VueRx from "vue-rx";
import './assets/styles/index.css';

Vue.use(VueRx, Rx)
Vue.config.productionTip = false

new Vue({
    render: h => h(App),
}).$mount('#app')
