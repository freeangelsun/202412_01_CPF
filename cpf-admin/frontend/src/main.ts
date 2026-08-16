import { createApp } from "vue";
import { createPinia } from "pinia";
import { VueQueryPlugin } from "@tanstack/vue-query";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import { admRouter } from "./app/router";
import { cpfQueryClient } from "./shared/queryClient";
import CpfStructuredData from "./components/CpfStructuredData.vue";
import "./styles/adm.css";
import "./styles/cpf-design.css";

createApp(App).component("CpfStructuredData", CpfStructuredData).use(createPinia()).use(admRouter).use(VueQueryPlugin, { queryClient: cpfQueryClient }).use(ElementPlus).mount("#app");
