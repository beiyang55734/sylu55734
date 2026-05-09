<template>
  <div class="landing-page">
    <section class="hero">
      <div class="hero-copy">
        <div class="hero-tag">智能教育与文化传播</div>
        <h1>让课程学习、文化理解与 AI 创作形成一条自然连贯的学习路径</h1>
        <p>
          平台保留你原有的课程学习、选课、数字人、AI PPT、AI 出题和讲解视频能力，
          同时新增文化专题推荐、课程收藏与学习清单，让学生更容易找到适合自己的学习主题。
        </p>
        <div class="hero-actions">
          <el-button type="primary" @click="scrollToCourses">浏览课程</el-button>
          <el-button plain @click="goStudyCenter">{{ user.id ? '进入学习清单' : '查看学习清单' }}</el-button>
          <el-button plain @click="openProtectedRoute('/digital-human')">体验数字人讲解</el-button>
        </div>
      </div>

      <div class="hero-panel">
        <div class="hero-stat">
          <span>文化课程数量</span>
          <strong>{{ courses.length }}</strong>
        </div>
        <div class="hero-stat">
          <span>{{ user.id ? '我的收藏课程' : '本地收藏课程' }}</span>
          <strong>{{ favoriteIds.length }}</strong>
        </div>
        <div class="hero-stat">
          <span>已选课程</span>
          <strong>{{ enrolledCourseIds.length }}</strong>
        </div>
      </div>
    </section>

    <section class="feature-strip">
      <div class="feature-pill" v-for="item in featurePills" :key="item.title">
        <i :class="item.icon"></i>
        <div>
          <strong>{{ item.title }}</strong>
          <span>{{ item.desc }}</span>
        </div>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h2>{{ user.id ? '我的学习清单' : '本地学习清单' }}</h2>
          <p>收藏课程、已选课程和推荐方向会在这里形成一个更清晰的学习视图。</p>
        </div>
        <div class="section-head-actions">
          <el-button type="text" @click="goStudyCenter">查看学习档案</el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div class="summary-card">
          <span>收藏课程</span>
          <strong>{{ favoriteIds.length }}</strong>
          <p>可以先收藏感兴趣的课程，再统一安排学习节奏。</p>
        </div>
        <div class="summary-card">
          <span>已选课程</span>
          <strong>{{ enrolledCourseIds.length }}</strong>
          <p>选课数据仍然走你原有的后端流程，不改变已有功能。</p>
        </div>
        <div class="summary-card">
          <span>推荐方向</span>
          <strong>{{ preferredTopicLabel }}</strong>
          <p>根据收藏和选课内容，自动推断更适合你的文化学习主题。</p>
        </div>
      </div>

      <div class="study-grid">
        <el-card class="study-card" shadow="never">
          <div slot="header" class="study-card-header">
            <span>收藏课程</span>
            <el-tag size="mini" type="warning">{{ favoriteCourses.length }} 门</el-tag>
          </div>
          <div v-if="favoriteCourses.length" class="mini-course-list">
            <div class="mini-course-item" v-for="course in favoriteCourses.slice(0, 3)" :key="course.id">
              <div>
                <strong>{{ course.name }}</strong>
                <p>{{ summarize(course, 42) }}</p>
              </div>
              <el-button type="text" @click="openCourse(course)">查看</el-button>
            </div>
          </div>
          <el-empty v-else description="还没有收藏课程"></el-empty>
        </el-card>

        <el-card class="study-card" shadow="never">
          <div slot="header" class="study-card-header">
            <span>已选课程</span>
            <el-tag size="mini" type="success">{{ enrolledCourses.length }} 门</el-tag>
          </div>
          <div v-if="enrolledCourses.length" class="mini-course-list">
            <div class="mini-course-item" v-for="course in enrolledCourses.slice(0, 3)" :key="course.id">
              <div>
                <strong>{{ course.name }}</strong>
                <p>{{ summarize(course, 42) }}</p>
              </div>
              <el-button type="text" @click="openCourse(course)">查看</el-button>
            </div>
          </div>
          <el-empty v-else description="暂未选课"></el-empty>
        </el-card>

        <el-card class="study-card" shadow="never">
          <div slot="header" class="study-card-header">
            <span>智能推荐</span>
            <el-tag size="mini" type="primary">{{ recommendedCourses.length }} 门</el-tag>
          </div>
          <div v-if="recommendedCourses.length" class="mini-course-list">
            <div class="mini-course-item" v-for="course in recommendedCourses.slice(0, 3)" :key="course.id">
              <div>
                <strong>{{ course.name }}</strong>
                <p>{{ learningRoute(course) }}</p>
              </div>
              <el-button type="text" @click="openCourse(course)">查看</el-button>
            </div>
          </div>
          <el-empty v-else description="课程推荐生成中"></el-empty>
        </el-card>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h2>文化专题推荐</h2>
          <p>平台会根据课程内容自动识别主题标签，帮助你从“找课程”升级为“按专题学习”。</p>
        </div>
      </div>
      <div class="topic-grid">
        <div class="topic-card" v-for="topic in topicCollections.slice(0, 4)" :key="topic.key">
          <div class="topic-card-head">
            <span class="topic-dot" :style="{ background: topic.color }"></span>
            <strong>{{ topic.label }}</strong>
          </div>
          <p>{{ topic.description }}</p>
          <div class="topic-foot">
            <span>{{ topic.courses.length }} 门相关课程</span>
            <el-button type="text" @click="activeTopic = topic.key">查看该专题</el-button>
          </div>
        </div>
      </div>
    </section>

    <section ref="courseSection" class="section-block course-section">
      <div class="section-head">
        <div>
          <h2>文化课程探索</h2>
          <p>支持课程搜索、主题筛选、收藏和选课，让原有课程功能更适合主题化展示。</p>
        </div>
        <div class="section-head-actions search-group">
          <el-input
            v-model="keyword"
            class="course-search"
            clearable
            placeholder="搜索课程、教师或简介关键词"
            prefix-icon="el-icon-search"
          />
        </div>
      </div>

      <div class="topic-filter-row">
        <el-tag
          :effect="activeTopic === '' ? 'dark' : 'plain'"
          class="filter-tag"
          @click="activeTopic = ''"
        >
          全部专题
        </el-tag>
        <el-tag
          v-for="topic in topicCollections.slice(0, 6)"
          :key="topic.key"
          :effect="activeTopic === topic.key ? 'dark' : 'plain'"
          class="filter-tag"
          :style="activeTopic === topic.key ? activeTagStyle(topic.color) : plainTagStyle(topic.color)"
          @click="activeTopic = topic.key"
        >
          {{ topic.label }}
        </el-tag>
      </div>

      <div v-if="filteredCourses.length" class="course-grid">
        <el-card v-for="course in filteredCourses" :key="course.id" class="course-card" shadow="hover">
          <div class="course-cover" :style="coverStyle(course)">
            <img v-if="course.img" :src="course.img" :alt="course.name">
            <span v-else>{{ course.name ? course.name.slice(0, 2) : '课程' }}</span>
          </div>

          <div class="course-body">
            <div class="course-top">
              <h3>{{ course.name }}</h3>
              <el-button
                circle
                size="mini"
                :type="isFavorite(course.id) ? 'warning' : ''"
                icon="el-icon-star-off"
                @click="toggleFavorite(course)"
              />
            </div>

            <div class="course-tags">
              <el-tag
                v-for="topic in courseTopics(course)"
                :key="`${course.id}-${topic.key}`"
                size="mini"
                :style="plainTagStyle(topic.color)"
              >
                {{ topic.label }}
              </el-tag>
            </div>

            <div class="course-meta">
              <span><i class="el-icon-user"></i>{{ course.teacher || '待补充教师信息' }}</span>
              <span><i class="el-icon-location"></i>{{ course.address || '待补充上课地点' }}</span>
            </div>

            <p>{{ summarize(course, 68) }}</p>

            <div class="course-actions">
              <el-button size="mini" @click="openCourse(course)">查看详情</el-button>
              <el-button 
                size="mini" 
                type="primary" 
                :disabled="isEnrolled(course.id)"
                @click="chooseCourse(course)"
              >
                {{ isEnrolled(course.id) ? '已选课' : '立即选课' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="没有找到符合条件的课程"></el-empty>
    </section>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h2>智能学习能力</h2>
          <p>保留你原有的 AI 能力入口，并把它们放入文化课程学习链路中。</p>
        </div>
      </div>
      <div class="ability-grid">
        <div class="ability-card" v-for="item in abilityCards" :key="item.title" @click="item.path && openProtectedRoute(item.path)">
          <i :class="item.icon"></i>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>

    <el-dialog title="课程详情" :visible.sync="dialogVisible" width="760px">
      <div v-if="currentCourse" class="course-dialog">
        <div class="dialog-cover" :style="coverStyle(currentCourse)">
          <img v-if="currentCourse.img" :src="currentCourse.img" :alt="currentCourse.name">
          <span v-else>{{ currentCourse.name ? currentCourse.name.slice(0, 2) : '课程' }}</span>
        </div>

        <div class="dialog-content">
          <div class="dialog-head">
            <h2>{{ currentCourse.name }}</h2>
            <el-button
              size="mini"
              :type="isFavorite(currentCourse.id) ? 'warning' : 'info'"
              plain
              @click="toggleFavorite(currentCourse)"
            >
              {{ isFavorite(currentCourse.id) ? '取消收藏' : '加入收藏' }}
            </el-button>
          </div>

          <div class="dialog-tags">
            <el-tag
              v-for="topic in courseTopics(currentCourse)"
              :key="`${currentCourse.id}-${topic.key}`"
              size="mini"
              :style="plainTagStyle(topic.color)"
            >
              {{ topic.label }}
            </el-tag>
          </div>

          <div class="dialog-meta">
            <span><i class="el-icon-user"></i>{{ currentCourse.teacher || '待补充教师信息' }}</span>
            <span><i class="el-icon-time"></i>{{ currentCourse.times || '待补充上课时间' }}</span>
            <span><i class="el-icon-location"></i>{{ currentCourse.address || '待补充上课地点' }}</span>
          </div>

          <div class="dialog-desc">{{ currentCourse.content || '暂无课程简介' }}</div>

          <div class="dialog-note">
            <strong>推荐学习路径：</strong>{{ learningRoute(currentCourse) }}
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          :disabled="currentCourse && isEnrolled(currentCourse.id)"
          @click="chooseCourse(currentCourse)"
        >
          {{ currentCourse && isEnrolled(currentCourse.id) ? '已选课' : '立即选课' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  buildLearningRoute,
  buildRecommendedCourses,
  groupCoursesByTopic,
  inferCourseTopics,
  loadFavoriteCourseIds,
  summarizeCourse,
  toggleFavoriteCourse
} from '@/utils/culture'

export default {
  name: 'FrontHome',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      courses: [],
      favoriteIds: [],
      enrolledCourseIds: [],
      keyword: '',
      activeTopic: '',
      dialogVisible: false,
      currentCourse: null,
      featurePills: [
        { icon: 'el-icon-document', title: '课程浏览保留', desc: '原有课程浏览、详情查看与选课流程继续保留。' },
        { icon: 'el-icon-star-off', title: '新增课程收藏', desc: '可以先收藏课程，再统一整理成个人学习清单。' },
        { icon: 'el-icon-collection-tag', title: '新增专题推荐', desc: '根据课程内容自动识别文化主题，更方便做主题化展示。' },
        { icon: 'el-icon-s-opportunity', title: '智能推荐联动', desc: '结合收藏和选课记录，给出更适合的学习方向。' }
      ],
      abilityCards: [
        { icon: 'el-icon-picture-outline-round', title: 'AI 备课 PPT', desc: '把课程主题快速转成结构化展示材料。', path: '/ai-ppt' },
        { icon: 'el-icon-edit-outline', title: 'AI 智能出题', desc: '围绕课程主题做课后测评和知识巩固。', path: '/ai-quiz' },
        { icon: 'el-icon-microphone', title: '数字人讲解员', desc: '适合做文化场景讲解和知识导览。', path: '/digital-human' },
        { icon: 'el-icon-video-camera', title: 'PPT 讲解视频', desc: '把课程内容转成更适合展示和汇报的视频。', path: '/ppt-video' }
      ]
    }
  },
  computed: {
    topicCollections() {
      return groupCoursesByTopic(this.courses)
    },
    filteredCourses() {
      const keyword = this.keyword.trim().toLowerCase()
      return this.courses.filter(course => {
        const text = `${course.name || ''} ${course.teacher || ''} ${course.content || ''}`.toLowerCase()
        const matchKeyword = !keyword || text.includes(keyword)
        const matchTopic = !this.activeTopic || this.courseTopics(course).some(topic => topic.key === this.activeTopic)
        return matchKeyword && matchTopic
      }).slice(0, 8)
    },
    favoriteCourses() {
      const favoriteSet = new Set(this.favoriteIds)
      return this.courses.filter(course => favoriteSet.has(course.id))
    },
    enrolledCourses() {
      const enrolledSet = new Set(this.enrolledCourseIds)
      return this.courses.filter(course => enrolledSet.has(course.id))
    },
    recommendedCourses() {
      return buildRecommendedCourses(this.courses, this.favoriteIds, this.enrolledCourseIds).slice(0, 4)
    },
    preferredTopicLabel() {
      const source = this.favoriteCourses.concat(this.enrolledCourses)
      const topTopic = groupCoursesByTopic(source)[0]
      return topTopic ? topTopic.label : '综合文化'
    }
  },
  created() {
    this.favoriteIds = loadFavoriteCourseIds(this.user)
    this.loadCourses()
    this.loadSelectedCourses()
  },
  methods: {
    async loadCourses() {
      const res = await this.$request.get('/course')
      if (res.code === '200') {
        this.courses = res.data || []
      }
    },
    async loadSelectedCourses() {
      if (!this.user.id) {
        this.enrolledCourseIds = []
        return
      }
      const res = await this.$request.get('/choose/page', {
        params: {
          pageNum: 1,
          pageSize: 100,
          name: ''
        }
      })
      if (res.code === '200') {
        this.enrolledCourseIds = (res.data.records || []).map(item => item.courseid)
      }
    },
    summarize(course, maxLength) {
      return summarizeCourse(course, maxLength)
    },
    courseTopics(course) {
      return inferCourseTopics(course)
    },
    learningRoute(course) {
      return buildLearningRoute(course)
    },
    isFavorite(courseId) {
      return this.favoriteIds.includes(Number(courseId))
    },
    isEnrolled(courseId) {
      return this.enrolledCourseIds.includes(Number(courseId))
    },
    toggleFavorite(course) {
      this.favoriteIds = toggleFavoriteCourse(this.user, course.id)
      this.$message.success(this.isFavorite(course.id) ? '已加入学习清单' : '已移出学习清单')
    },
    openCourse(course) {
      this.currentCourse = course
      this.dialogVisible = true
    },
    async chooseCourse(course) {
      if (!course) {
        return
      }
      if (!this.user.id) {
        this.$message.info('请先登录后再进行选课')
        this.$router.push('/login')
        return
      }

      const res = await this.$request.post('/choose', {
        name: course.name,
        courseid: course.id,
        studentid: this.user.id
      })

      if (res.code === '200') {
        this.$message.success('选课成功')
        this.dialogVisible = false
        this.loadSelectedCourses()
      } else {
        this.$message.error(res.msg || '选课失败')
      }
    },
    openProtectedRoute(path) {
      if (!this.user.id) {
        this.$message.info('请先登录后再体验该功能')
        this.$router.push('/login')
        return
      }
      this.$router.push(path)
    },
    goStudyCenter() {
      if (this.user.id) {
        this.$router.push('/front/person')
      } else {
        this.$router.push('/login')
      }
    },
    scrollToCourses() {
      const target = this.$refs.courseSection
      if (target && target.scrollIntoView) {
        target.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    },
    coverStyle(course) {
      const colors = ['#12344d', '#2a6f6b', '#c27b34', '#8a5a44', '#5f6c7b']
      const seed = course && course.id ? course.id : 0
      const start = colors[seed % colors.length]
      const end = colors[(seed + 1) % colors.length]
      return {
        background: `linear-gradient(135deg, ${start} 0%, ${end} 100%)`
      }
    },
    plainTagStyle(color) {
      return {
        borderColor: color,
        color
      }
    },
    activeTagStyle(color) {
      return {
        backgroundColor: color,
        borderColor: color,
        color: '#fff'
      }
    }
  }
}
</script>

<style scoped>
.landing-page {
  width: min(1180px, 94vw);
  margin: 0 auto;
  padding: 18px 0 32px;
}

.hero {
  display: grid;
  grid-template-columns: 1.6fr 0.8fr;
  gap: 20px;
  min-height: 390px;
  padding: 36px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.22), transparent 30%),
    linear-gradient(135deg, #12344d 0%, #1d5d6c 55%, #e9c46a 100%);
  color: #fff;
}

.hero-tag {
  display: inline-flex;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.18);
}

.hero-copy h1 {
  margin: 16px 0 14px;
  font-size: 40px;
  line-height: 1.3;
}

.hero-copy p {
  margin: 0;
  max-width: 700px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.9);
}

.hero-actions {
  margin-top: 26px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-panel {
  display: grid;
  gap: 14px;
}

.hero-stat {
  padding: 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.hero-stat span {
  display: block;
  color: rgba(255, 255, 255, 0.8);
}

.hero-stat strong {
  display: block;
  margin-top: 10px;
  font-size: 32px;
}

.feature-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin: 22px 0;
}

.feature-pill {
  display: flex;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(18, 52, 77, 0.08);
}

.feature-pill i {
  font-size: 22px;
  color: #1d5d6c;
}

.feature-pill strong,
.feature-pill span {
  display: block;
}

.feature-pill span {
  margin-top: 6px;
  color: #667085;
  line-height: 1.6;
}

.section-block {
  margin-top: 22px;
  padding: 26px;
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
  font-size: 26px;
  color: #12344d;
}

.section-head p {
  margin: 0;
  color: #667085;
  line-height: 1.8;
}

.section-head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  padding: 22px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f7fbfb 0%, #fef8ef 100%);
}

.summary-card span {
  display: block;
  color: #667085;
}

.summary-card strong {
  display: block;
  margin: 10px 0;
  font-size: 28px;
  color: #12344d;
}

.summary-card p {
  margin: 0;
  line-height: 1.7;
  color: #51606d;
}

.study-grid,
.topic-grid,
.ability-grid {
  display: grid;
  gap: 16px;
}

.study-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 18px;
}

.study-card {
  border: 1px solid #eef2f6;
}

.study-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mini-course-list {
  display: grid;
  gap: 12px;
}

.mini-course-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}

.mini-course-item strong {
  display: block;
  color: #12344d;
}

.mini-course-item p {
  margin: 6px 0 0;
  color: #667085;
  line-height: 1.6;
}

.topic-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.topic-card {
  padding: 22px;
  border-radius: 20px;
  background: linear-gradient(180deg, #f8fbfc 0%, #ffffff 100%);
  border: 1px solid #eef2f6;
}

.topic-card-head {
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
  min-height: 48px;
  margin: 12px 0 18px;
  color: #667085;
  line-height: 1.7;
}

.topic-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #51606d;
}

.search-group {
  width: min(360px, 100%);
}

.course-search {
  width: 100%;
}

.topic-filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.filter-tag {
  cursor: pointer;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.course-card {
  border-radius: 22px;
  overflow: hidden;
}

.course-cover {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32px;
  font-weight: bold;
}

.course-cover img,
.dialog-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-body {
  padding-top: 16px;
}

.course-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.course-top h3 {
  margin: 0;
  font-size: 18px;
  line-height: 1.5;
  color: #12344d;
}

.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0 10px;
}

.course-meta {
  display: grid;
  gap: 8px;
  color: #667085;
  font-size: 13px;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.course-body p {
  min-height: 66px;
  margin: 14px 0 18px;
  color: #51606d;
  line-height: 1.75;
}

.course-actions {
  display: flex;
  gap: 10px;
}

.ability-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.ability-card {
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(180deg, #12344d 0%, #1d5d6c 100%);
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.ability-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 36px rgba(18, 52, 77, 0.18);
}

.ability-card i {
  font-size: 28px;
}

.ability-card h3 {
  margin: 16px 0 10px;
}

.ability-card p {
  margin: 0;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.84);
}

.course-dialog {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
}

.dialog-cover {
  height: 320px;
  border-radius: 22px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 40px;
  font-weight: bold;
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.dialog-head h2 {
  margin: 0;
  color: #12344d;
}

.dialog-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0;
}

.dialog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #667085;
}

.dialog-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dialog-desc {
  margin-top: 16px;
  line-height: 1.9;
  color: #51606d;
}

.dialog-note {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  color: #12344d;
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .hero,
  .summary-grid,
  .study-grid,
  .topic-grid,
  .course-grid,
  .ability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .course-dialog {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .landing-page {
    width: 94vw;
  }

  .hero,
  .feature-strip,
  .summary-grid,
  .study-grid,
  .topic-grid,
  .course-grid,
  .ability-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: 24px;
  }

  .hero-copy h1 {
    font-size: 30px;
  }

  .section-block {
    padding: 20px;
  }

  .section-head {
    flex-direction: column;
  }
}
</style>
