import Vue from 'vue'
import VueRouter from 'vue-router'

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/home',
    children: [
      { path: '403', name: 'Auth', meta: { name: '无权限访问' }, component: () => import('../views/manager/Auth.vue') },
      { path: 'home', name: 'ManagerHome', meta: { name: '平台总览' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员中心', requireAdmin: true }, component: () => import('../views/manager/Admin.vue') },
      { path: 'user', name: 'User', meta: { name: '用户管理', requireAdmin: true }, component: () => import('../views/manager/User.vue') },
      { path: 'person', name: 'ManagerPerson', meta: { name: '个人中心' }, component: () => import('../views/manager/Person.vue') },
      { path: 'course', name: 'Course', meta: { name: '文化课程库' }, component: () => import('../views/manager/Course.vue') },
      { path: 'choose', name: 'Choose', meta: { name: '选课管理' }, component: () => import('../views/manager/Choose.vue') },
      { path: 'grade', name: 'Grade', meta: { name: '学习成绩' }, component: () => import('../views/manager/Grade.vue') },
      { path: 'grade-ranking', name: 'GradeRanking', meta: { name: '成绩排行' }, component: () => import('../views/manager/GradeRanking.vue') },
      { path: 'grade-report', name: 'GradeReport', meta: { name: '成绩报告' }, component: () => import('../views/manager/GradeReport.vue') },
      { path: 'ai-ppt', name: 'AiPpt', meta: { name: 'AI 备课 PPT' }, component: () => import('../views/manager/AiPpt.vue') },
      { path: 'digital-human', name: 'DigitalHuman', meta: { name: '数字人讲解员' }, component: () => import('../views/manager/DigitalHuman.vue') },
      { path: 'ppt-video', name: 'PptVideo', meta: { name: 'PPT 讲解视频' }, component: () => import('../views/manager/PptVideo.vue') }
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/front/Front.vue'),
    redirect: '/front/home',
    children: [
      { path: 'home', name: 'FrontHome', meta: { name: '文化学习首页' }, component: () => import('../views/front/Home.vue') },
      { path: 'person', name: 'FrontPerson', meta: { name: '个人中心' }, component: () => import('../views/front/Person.vue') },
      { path: 'password', name: 'Password', meta: { name: '重置密码' }, component: () => import('../views/front/Password.vue') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'page-404', meta: { name: '页面不存在' }, component: () => import('../views/404.vue') },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (to.path === '/') {
    if (user.role) {
      if (user.role === 'ADMIN' || user.role === 'TEACHER') {
        next('/home')
      } else {
        next('/front/home')
      }
    } else {
      next('/login')
    }
  } else if (to.matched.length === 0) {
    next('/404')
  } else {
    next()
  }
})

export default router
