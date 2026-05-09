<template>
  <div class="dashboard-page">
    <section class="hero-section">
      <div class="hero-copy">
        <div class="hero-badge">智能教育与文化平台总览</div>
        <h1>AI 全链路赋能的教学辅导与自主学习一体化管理平台</h1>
        <p>
          平台以 AI 数字人答疑、智能教学视频生成为核心，配套班级管理、学情分析等功能，同时支持学生课后自主学习，实现教师教学减负、学生学习提效的双向赋能。
        </p>
        <div class="hero-actions">
          <el-button type="primary" @click="$router.push('/course')">进入课程库</el-button>
          <el-button plain @click="$router.push('/choose')">查看选课</el-button>
          <el-button v-if="canUseTeacherTools" type="success" plain @click="$router.push('/ai-ppt')">体验 AI 备课</el-button>
        </div>
      </div>

      <div class="hero-panel">
        <div class="hero-panel-item">
          <span>课程总数</span>
          <strong>{{ stats.courseCount }}</strong>
        </div>
        <div class="hero-panel-item">
          <span>公告数量</span>
          <strong>{{ stats.noticeCount }}</strong>
        </div>
        <div class="hero-panel-item">
          <span>平均成绩</span>
          <strong>{{ stats.averageScore }}</strong>
        </div>
        <div class="hero-panel-item">
          <span>及格率</span>
          <strong>{{ stats.passRate }}</strong>
        </div>
      </div>
    </section>

    <el-row :gutter="20" class="metric-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in metricCards" :key="item.label">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon" :class="item.type">
            <i :class="item.icon"></i>
          </div>
          <div class="metric-content">
            <div class="metric-label">{{ item.label }}</div>
            <div class="metric-value">{{ item.value }}</div>
            <div class="metric-desc">{{ item.desc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h2>文化主题概览</h2>
          <p>系统会根据课程内容自动识别主题标签，帮助你把课程管理提升为专题化运营。</p>
        </div>
      </div>
      <div class="topic-grid">
        <div class="topic-card" v-for="topic in topicCards" :key="topic.key">
          <div class="topic-head">
            <span class="topic-dot" :style="{ background: topic.color }"></span>
            <strong>{{ topic.label }}</strong>
          </div>
          <p>{{ topic.description }}</p>
          <div class="topic-foot">
            <span>{{ topic.courses.length }} 门相关课程</span>
            <el-button type="text" @click="$router.push('/course')">管理课程</el-button>
          </div>
        </div>
      </div>
    </section>

    <el-row :gutter="20" class="section-grid">
      <el-col :xs="24" :lg="14">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-card-header">
            <span>快捷入口</span>
            <el-button type="text" @click="$router.push('/course')">查看全部</el-button>
          </div>
          <div class="quick-grid">
            <div class="quick-card" v-for="item in quickEntries" :key="item.title" @click="$router.push(item.path)">
              <i :class="item.icon"></i>
              <div class="quick-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-card-header">
            <span>重点课程</span>
            <el-tag size="mini" type="success">主题化展示</el-tag>
          </div>
          <div v-if="highlightCourses.length" class="mini-list">
            <div class="mini-item" v-for="course in highlightCourses" :key="course.id">
              <div class="mini-dot" :style="{ background: courseTopics(course)[0].color }"></div>
              <div class="mini-text">
                <strong>{{ course.name }}</strong>
                <span>{{ learningRoute(course) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无课程数据"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-grid">
      <el-col :xs="24" :lg="12">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-card-header">
            <span>教学闭环建议</span>
          </div>
          <ul class="value-list">
            <li>先完善课程内容描述，让文化主题标签识别更准确。</li>
            <li>将重点课程和数字人讲解员绑定，形成“课程讲解”亮点。</li>
            <li>把 AI 出题和课程主题结合，展示教学闭环而不是单点功能。</li>
            <li>把学生收藏课程和选课数据沉淀成个人学习清单，增强平台感。</li>
          </ul>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-card-header">
            <span>系统技术选型</span>
          </div>
          <div class="notice-list">
            <div class="notice-item">
              <strong>操作系统</strong>
              <p>Windows10/11</p>
            </div>
            <div class="notice-item">
              <strong>系统架构</strong>
              <p>前后端分离B/S架构</p>
            </div>
            <div class="notice-item">
              <strong>后台框架</strong>
              <p>SpringBoot、Mybatis</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { buildLearningRoute, groupCoursesByTopic, inferCourseTopics } from '@/utils/culture'

export default {
  name: 'ManagerHome',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      courses: [],
      stats: {
        courseCount: 0,
        noticeCount: 0,
        totalCount: 0,
        averageScore: '0.0',
        passRate: '0.0%'
      },
      quickEntries: [
        { title: '文化课程库', desc: '维护课程基础信息、课程简介和文化学习内容。', path: '/course', icon: 'el-icon-document' },
        { title: '选课管理', desc: '查看学生选课情况，继续沿用现有选课流程。', path: '/choose', icon: 'el-icon-collection-tag' },
        { title: '成绩报告', desc: '从课程成绩中观察学习效果和教学反馈。', path: '/grade-report', icon: 'el-icon-pie-chart' },
        { title: '数字人讲解员', desc: '把文化内容和数字人讲解结合，增强展示效果。', path: '/digital-human', icon: 'el-icon-microphone' }
      ]
    }
  },
  computed: {
    canUseTeacherTools() {
      return this.user.role === 'ADMIN' || this.user.role === 'TEACHER'
    },
    metricCards() {
      return [
        {
          label: '学习记录数',
          value: this.stats.totalCount,
          desc: '成绩记录可继续用于学习分析和教学反馈。',
          icon: 'el-icon-s-data',
          type: 'primary'
        },
        {
          label: '文化主题数',
          value: this.topicCards.length,
          desc: '自动识别课程主题，更方便做专题化展示。',
          icon: 'el-icon-collection-tag',
          type: 'success'
        },
        {
          label: '重点课程数',
          value: this.highlightCourses.length,
          desc: '适合优先做数字人讲解或 AI 课件展示。',
          icon: 'el-icon-s-marketing',
          type: 'warning'
        },
        {
          label: '课程完成度',
          value: this.stats.passRate,
          desc: '保留原有成绩统计逻辑，用于教学成效判断。',
          icon: 'el-icon-data-analysis',
          type: 'danger'
        }
      ]
    },
    topicCards() {
      return groupCoursesByTopic(this.courses).slice(0, 4)
    },
    highlightCourses() {
      return this.courses.slice(0, 4)
    }
  },
  created() {
    this.loadDashboard()
  },
  methods: {
    async loadDashboard() {
      const tasks = [
        this.$request.get('/course'),
        this.$request.get('/grade/statistics')
      ]

      const results = await Promise.allSettled(tasks)

      if (results[0].status === 'fulfilled' && results[0].value.code === '200') {
        this.courses = results[0].value.data || []
        this.stats.courseCount = this.courses.length
      }

      if (results[1].status === 'fulfilled' && results[1].value.code === '200' && results[1].value.data) {
        const data = results[1].value.data
        this.stats.totalCount = data.totalCount || 0
        this.stats.averageScore = typeof data.averageScore === 'number' ? data.averageScore.toFixed(1) : '0.0'
        this.stats.passRate = typeof data.passRate === 'number' ? `${data.passRate.toFixed(1)}%` : '0.0%'
      }
      
      // 设置固定的公告数量为3（系统技术选型、系统架构、后台框架）
      this.stats.noticeCount = 3
    },
    courseTopics(course) {
      return inferCourseTopics(course)
    },
    learningRoute(course) {
      return buildLearningRoute(course)
    },

  }
}
</script>

<style scoped>
.dashboard-page {
  padding: 8px 4px 24px;
}

.hero-section {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  padding: 28px;
  margin-bottom: 20px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.26), transparent 35%),
    linear-gradient(135deg, #17475f 0%, #1f6f78 52%, #f6bd60 100%);
  color: #fff;
}

.hero-badge {
  display: inline-flex;
  padding: 7px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 13px;
}

.hero-copy h1 {
  margin: 16px 0 14px;
  font-size: 34px;
  line-height: 1.35;
}

.hero-copy p {
  margin: 0;
  max-width: 760px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.92);
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 26px;
}

.hero-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.hero-panel-item {
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.14);
}

.hero-panel-item span {
  display: block;
  color: rgba(255, 255, 255, 0.8);
}

.hero-panel-item strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.metric-row {
  margin-bottom: 20px;
}

.metric-card {
  display: flex;
  align-items: center;
  min-height: 132px;
  border-radius: 22px;
}

.metric-icon {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.metric-icon.primary {
  background: linear-gradient(135deg, #2a6f97, #468faf);
}

.metric-icon.success {
  background: linear-gradient(135deg, #2a9d8f, #52b69a);
}

.metric-icon.warning {
  background: linear-gradient(135deg, #f4a261, #e9c46a);
}

.metric-icon.danger {
  background: linear-gradient(135deg, #bc6c25, #dda15e);
}

.metric-content {
  margin-top: 16px;
}

.metric-label {
  color: #667085;
}

.metric-value {
  margin: 10px 0 8px;
  font-size: 28px;
  color: #12344d;
  font-weight: 700;
}

.metric-desc {
  color: #51606d;
  line-height: 1.7;
}

.section-block {
  padding: 26px;
  margin-bottom: 20px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(18, 52, 77, 0.08);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0 0 8px;
  font-size: 25px;
  color: #12344d;
}

.section-head p {
  margin: 0;
  color: #667085;
  line-height: 1.8;
}

.topic-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.topic-card {
  padding: 22px;
  border-radius: 20px;
  border: 1px solid #eef2f6;
  background: linear-gradient(180deg, #f8fbfc 0%, #ffffff 100%);
}

.topic-head {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #12344d;
}

.topic-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.topic-card p {
  margin: 14px 0 18px;
  min-height: 48px;
  line-height: 1.75;
  color: #667085;
}

.topic-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #51606d;
}

.section-grid {
  margin-bottom: 20px;
}

.section-card {
  min-height: 100%;
  border-radius: 24px;
}

.section-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.quick-card {
  display: flex;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 28px rgba(18, 52, 77, 0.08);
}

.quick-card i {
  font-size: 24px;
  color: #1f6f78;
}

.quick-info h3 {
  margin: 0 0 6px;
  color: #12344d;
}

.quick-info p {
  margin: 0;
  color: #667085;
  line-height: 1.7;
}

.mini-list,
.notice-list {
  display: grid;
  gap: 14px;
}

.mini-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}

.mini-dot {
  width: 10px;
  border-radius: 999px;
  background: #1f6f78;
}

.mini-text strong {
  display: block;
  margin-bottom: 6px;
  color: #12344d;
}

.mini-text span {
  color: #667085;
  line-height: 1.7;
}

.value-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 12px;
  color: #51606d;
  line-height: 1.8;
}

.notice-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.notice-item strong {
  display: block;
  color: #12344d;
}

.notice-item p {
  margin: 8px 0 0;
  color: #667085;
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .hero-section,
  .topic-grid,
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-section,
  .hero-panel,
  .topic-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .section-block {
    padding: 20px;
  }
}
</style>
