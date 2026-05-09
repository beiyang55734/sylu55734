<template>
  <div>
    <div class="header">
      <div class="front-header">
        <div class="front-header-left" @click="$router.push('/front/home')">
          <img src="@/assets/logo.svg" alt="logo">
          <div class="title">智教文脉平台</div>
        </div>

        <div class="front-header-center">
          <div
            v-for="item in visibleMenus"
            :key="item.path"
            class="menu-item"
            :class="{ 'menu-item-active': item.path === $route.path }"
            @click="goPage(item.path)"
          >
            {{ item.text }}
          </div>
        </div>

        <div class="front-header-right">
          <div v-if="!user.username" class="front-header-right-button">
            <el-button type="primary" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="success" plain @click="$router.push('/register')">注册</el-button>
          </div>
          <div v-else>
            <el-dropdown>
              <div class="front-header-dropdown">
                <img :src="user.avatar || defaultAvatar" alt="avatar">
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item disabled>{{ user.name }}</el-dropdown-item>
                <el-dropdown-item @click.native="$router.push('/front/person')">个人中心</el-dropdown-item>
                <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
    </div>

    <Footer />
  </div>
</template>

<script>
import Footer from "@/conponents/Footer.vue"

export default {
  name: 'FrontLayout',
  components: { Footer },
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      menuList: [
        { text: '文化学习首页', path: '/front/home' },
        { text: '个人中心', path: '/front/person' }
      ]
    }
  },
  computed: {
    visibleMenus() {
      if (this.user && this.user.username) {
        return this.menuList
      }
      return this.menuList.filter(item => item.path === '/front/home')
    }
  },
  methods: {
    goPage(path) {
      this.$router.push(path)
    },
    updateUser(user) {
      this.user = user || JSON.parse(localStorage.getItem('user') || '{}')
    },
    logout() {
      localStorage.removeItem('user')
      this.user = {}
      this.$router.push('/front/home')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/front.css";

.front-header-left {
  cursor: pointer;
}
</style>
