import { createRouter, createWebHistory } from 'vue-router'
import ApprovalInboxPage from '../features/approvals/pages/ApprovalInboxPage.vue'
import AuthorizationPage from '../features/authorization/pages/AuthorizationPage.vue'
import DashboardPage from '../features/dashboard/pages/DashboardPage.vue'
import EmployeesPage from '../features/employees/pages/EmployeesPage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: DashboardPage, meta: { title: 'Dashboard' } },
    { path: '/employees', component: EmployeesPage, meta: { title: '직원 관리' } },
    { path: '/approvals', component: ApprovalInboxPage, meta: { title: '결재' } },
    { path: '/authorization', component: AuthorizationPage, meta: { title: '권한' } },
  ],
})
