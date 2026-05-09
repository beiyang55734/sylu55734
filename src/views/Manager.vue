<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="manager-header-left clickable" @click="$router.push('/home')">
        <img src="@/assets/logo.svg" alt="logo">
        <div class="title">班级智能教育平台</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom">
          <div class="avatar">
            <img :src="user.avatar || defaultAvatar" alt="avatar">
            <div class="user-name">{{ user.name || '未登录用户' }}</div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/person')">个人中心</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <div class="manager-main">
      <div class="manager-main-left">
        <el-menu
          router
          style="border: none"
          :default-active="$route.path"
          :default-openeds="defaultOpeneds"
        >
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">平台总览</span>
          </el-menu-item>

          <el-submenu index="teaching">
            <template slot="title">
              <i class="el-icon-menu"></i>
              <span>课程与学习</span>
            </template>
            <el-menu-item index="/course">
              <i class="el-icon-document"></i>
              <span>文化课程库</span>
            </el-menu-item>
            <el-menu-item index="/choose">
              <i class="el-icon-collection-tag"></i>
              <span>选课管理</span>
            </el-menu-item>
            <el-menu-item index="/grade">
              <i class="el-icon-s-data"></i>
              <span>学习成绩</span>
            </el-menu-item>
            <el-menu-item v-if="canManageData" index="/grade-ranking">
              <i class="el-icon-s-claim"></i>
              <span>成绩排行</span>
            </el-menu-item>
            <el-menu-item v-if="canManageData" index="/grade-report">
              <i class="el-icon-pie-chart"></i>
              <span>成绩报告</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="ai">
            <template slot="title">
              <i class="el-icon-s-opportunity"></i>
              <span>智能创作</span>
            </template>
            <el-menu-item v-if="canManageData" index="/ai-ppt">
              <i class="el-icon-picture-outline-round"></i>
              <span>AI 备课 PPT</span>
            </el-menu-item>
            <!-- 暂时隐藏AI智能出题功能 -->
            <!-- <el-menu-item v-if="canManageData" index="/ai-quiz">
              <i class="el-icon-edit-outline"></i>
              <span>AI 智能出题</span>
            </el-menu-item> -->
            <el-menu-item index="/digital-human">
              <i class="el-icon-microphone"></i>
              <span>数字人讲解员</span>
            </el-menu-item>
            <el-menu-item index="/ppt-video">
              <i class="el-icon-video-camera"></i>
              <span>PPT 讲解视频</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="canManageData" index="users">
            <template slot="title">
              <i class="el-icon-s-custom"></i>
              <span>用户与权限</span>
            </template>
            <el-menu-item index="/admin">
              <i class="el-icon-user-solid"></i>
              <span>管理员中心</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="account">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>账号与设置</span>
            </template>
            <el-menu-item index="/person">
              <i class="el-icon-s-tools"></i>
              <span>个人中心</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ManagerLayout',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      defaultOpeneds: ['teaching', 'ai', 'account']
    }
  },
  computed: {
    canManageData() {
      return this.user.role === 'ADMIN' || this.user.role === 'TEACHER'
    }
  },
  mounted() {
    if (!this.user.id) {
      this.$router.push('/login')
    }
  },
  methods: {
    updateUser(user) {
      this.user = user ? JSON.parse(JSON.stringify(user)) : JSON.parse(localStorage.getItem('user') || '{}')
    },
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  }
}
</script>

<style>
@import "@/assets/css/manager.css";

.user-name {
  cursor: pointer;
  font-weight: bold;
}
</style>
