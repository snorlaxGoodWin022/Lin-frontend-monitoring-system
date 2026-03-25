<template>
  <div class="app">
    <el-container>
      <el-header class="header">
        <div class="logo">前端监控平台</div>
        <el-menu
          mode="horizontal"
          :default-active="$route.path"
          router
          class="menu"
        >
          <el-menu-item index="/dashboard">监控大屏</el-menu-item>
          <el-menu-item index="/performance">性能分析</el-menu-item>
          <el-menu-item index="/error">错误统计</el-menu-item>
          <el-menu-item index="/behavior">行为分析</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  margin-right: 40px;
}

.menu {
  flex: 1;
  background: transparent;
  border: none;
}

.menu :deep(.el-menu-item) {
  color: white;
  border: none;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2);
  border: none;
}

.el-main {
  padding: 20px;
}
</style>
