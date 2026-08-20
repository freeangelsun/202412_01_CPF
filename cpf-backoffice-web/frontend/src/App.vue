<template>
  <div class="layout">
    <aside>
      <h1>CPF Backoffice</h1>
      <p>Reference Backoffice</p>
      <nav>
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/employees">직원 관리</RouterLink>
        <RouterLink to="/approvals">결재</RouterLink>
        <RouterLink to="/authorization">권한</RouterLink>
      </nav>
    </aside>
    <main><RouterView /></main>
  </div>
</template>
