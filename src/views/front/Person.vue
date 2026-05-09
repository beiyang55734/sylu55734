<template>
  <div class="person-page">
    <section class="profile-hero">
      <div>
        <div class="hero-badge">学习档案与个人中心</div>
        <h1>在保留原有个人信息维护功能的基础上，补上一层更完整的学习视图</h1>
        <p>
          这里仍然可以修改头像、姓名、联系方式和个人简介，同时新增已选课程、收藏课程和推荐方向，
          让个人中心更像一个真正的学习档案页。
        </p>
      </div>

      <div class="hero-stats">
        <div class="hero-stat">
          <span>已选课程</span>
          <strong>{{ enrolledCourses.length }}</strong>
        </div>
        <div class="hero-stat">
          <span>收藏课程</span>
          <strong>{{ favoriteCourses.length }}</strong>
        </div>
        <div class="hero-stat">
          <span>偏好主题</span>
          <strong>{{ preferredTopicLabel }}</strong>
        </div>
      </div>
    </section>

    <div class="person-layout">
      <el-card class="profile-card" shadow="never">
        <div slot="header" class="card-header">
          <span>个人信息</span>
          <el-tag size="mini" type="success">原功能保留</el-tag>
        </div>

        <el-form :model="user" label-width="80px" class="user-form">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:9999/file/upload"
              :headers="{ token: user.token }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
            >
              <img v-if="user.avatar" :src="user.avatar" class="avatar" alt="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
          </div>

          <el-form-item label="用户名" prop="username">
            <el-input v-model="user.username" placeholder="用户名" disabled />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="user.name" placeholder="姓名" />
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="user.phone" placeholder="电话" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="user.email" placeholder="邮箱" />
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <el-input type="textarea" v-model="user.address" placeholder="地址" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio v-model="user.sex" label="男">男</el-radio>
            <el-radio v-model="user.sex" label="女">女</el-radio>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input v-model="user.age" placeholder="年龄" />
          </el-form-item>
          <el-form-item label="个人介绍" prop="infos">
            <el-input type="textarea" v-model="user.infos" :rows="4" placeholder="个人介绍" />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" @click="update">保存信息</el-button>
            <el-button type="success" plain @click="$router.push('/front/password')">修改密码</el-button>
          </div>
        </el-form>
      </el-card>

      <div class="study-column">
        <el-card class="study-card" shadow="never">
          <div slot="header" class="card-header">
            <span>收藏课程</span>
            <el-tag size="mini" type="warning">{{ favoriteCourses.length }} 门</el-tag>
          </div>

          <div v-if="favoriteCourses.length" class="course-list">
            <div class="course-item" v-for="course in favoriteCourses.slice(0, 4)" :key="course.id">
              <div>
                <strong>{{ course.name }}</strong>
                <div class="topic-tags">
                  <el-tag
                    v-for="topic in courseTopics(course)"
                    :key="`${course.id}-${topic.key}`"
                    size="mini"
                    :style="topicTagStyle(topic.color)"
                  >
                    {{ topic.label }}
                  </el-tag>
                </div>
                <p>{{ summarize(course, 48) }}</p>
              </div>
              <div class="course-item-actions">
                <el-button type="text" @click="removeFavorite(course.id)">取消收藏</el-button>
                <el-button type="primary" size="small" @click="chooseCourse(course)">立即选课</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有收藏课程"></el-empty>
        </el-card>

        <el-card class="study-card" shadow="never">
          <div slot="header" class="card-header">
            <span>已选课程</span>
            <el-tag size="mini" type="success">{{ enrolledCourses.length }} 门</el-tag>
          </div>

          <div v-if="enrolledCourses.length" class="course-list">
            <div class="course-item" v-for="course in enrolledCourses.slice(0, 4)" :key="course.id">
              <div>
                <strong>{{ course.name }}</strong>
                <div class="topic-tags">
                  <el-tag
                    v-for="topic in courseTopics(course)"
                    :key="`${course.id}-${topic.key}`"
                    size="mini"
                    :style="topicTagStyle(topic.color)"
                  >
                    {{ topic.label }}
                  </el-tag>
                </div>
                <p>{{ learningRoute(course) }}</p>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂未选课"></el-empty>
        </el-card>

        <el-card class="study-card" shadow="never">
          <div slot="header" class="card-header">
            <span>推荐学习方向</span>
            <el-tag size="mini" type="primary">{{ preferredTopicLabel }}</el-tag>
          </div>

          <div class="suggestion-box">
            <div class="preferred-topics" v-if="preferredTopics.length">
              <el-tag
                v-for="topic in preferredTopics"
                :key="topic.key"
                size="mini"
                :style="topicTagStyle(topic.color)"
              >
                {{ topic.label }}
              </el-tag>
            </div>
            <p class="suggestion-copy">
              建议围绕你当前更感兴趣的主题继续扩展学习，并把课程内容和 AI 创作工具结合起来，
              形成“学习理解 - 讲解表达 - 课后测评”的闭环。
            </p>
            <div class="recommend-actions">
              <el-button type="primary" plain @click="$router.push('/front/home')">继续选课</el-button>
              <el-button type="success" plain @click="$router.push('/digital-human')">数字人讲解</el-button>
              <el-button type="warning" plain @click="$router.push('/ai-quiz')">AI 智能出题</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import {
  buildLearningRoute,
  groupCoursesByTopic,
  inferCourseTopics,
  loadFavoriteCourseIds,
  summarizeCourse,
  toggleFavoriteCourse
} from '@/utils/culture'

export default {
  name: 'FrontPerson',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      courses: [],
      favoriteIds: [],
      enrolledCourseIds: []
    }
  },
  computed: {
    favoriteCourses() {
      const favoriteSet = new Set(this.favoriteIds)
      return this.courses.filter(course => favoriteSet.has(course.id))
    },
    enrolledCourses() {
      const enrolledSet = new Set(this.enrolledCourseIds)
      return this.courses.filter(course => enrolledSet.has(course.id))
    },
    preferredTopics() {
      return groupCoursesByTopic(this.favoriteCourses.concat(this.enrolledCourses)).slice(0, 3)
    },
    preferredTopicLabel() {
      return this.preferredTopics.length ? this.preferredTopics[0].label : '综合文化'
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
    update() {
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          this.$message.success('保存成功')
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({ title: '失败', message: res.msg, showClose: false, duration: 2000 })
        }
      })
    },
    handleAvatarSuccess(response) {
      this.user.avatar = response.data
    },
    removeFavorite(courseId) {
      this.favoriteIds = toggleFavoriteCourse(this.user, courseId)
      this.$message.success('已取消收藏')
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
        this.loadSelectedCourses()
      } else {
        this.$message.error(res.msg || '选课失败')
      }
    },
    courseTopics(course) {
      return inferCourseTopics(course)
    },
    summarize(course, maxLength) {
      return summarizeCourse(course, maxLength)
    },
    learningRoute(course) {
      return buildLearningRoute(course)
    },
    topicTagStyle(color) {
      return {
        borderColor: color,
        color
      }
    }
  }
}
</script>

<style scoped>
.person-page {
  width: min(1180px, 96vw);
  margin: 0 auto;
  padding: 10px 0 30px;
}

.profile-hero {
  display: grid;
  grid-template-columns: 1.5fr 0.9fr;
  gap: 20px;
  padding: 30px;
  border-radius: 26px;
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 28%),
    linear-gradient(135deg, #12344d 0%, #28536b 55%, #c27b34 100%);
}

.hero-badge {
  display: inline-flex;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 13px;
}

.profile-hero h1 {
  margin: 14px 0;
  font-size: 30px;
  line-height: 1.4;
}

.profile-hero p {
  margin: 0;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.9);
}

.hero-stats {
  display: grid;
  gap: 14px;
}

.hero-stat {
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.12);
}

.hero-stat span {
  display: block;
  color: rgba(255, 255, 255, 0.8);
}

.hero-stat strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.person-layout {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 20px;
  margin-top: 22px;
}

.profile-card,
.study-card {
  border-radius: 22px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-form {
  padding: 6px 4px 10px;
}

.avatar-section {
  text-align: center;
  margin: 12px 0 24px;
}

:deep(.avatar-uploader .el-upload) {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  border: 1px dashed #d0d7de;
  overflow: hidden;
  cursor: pointer;
  background: #f8fafc;
}

.avatar-uploader-icon {
  width: 92px;
  height: 92px;
  line-height: 92px;
  text-align: center;
  font-size: 26px;
  color: #8c939d;
}

.avatar {
  width: 92px;
  height: 92px;
  object-fit: cover;
  border-radius: 50%;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 26px;
}

.study-column {
  display: grid;
  gap: 18px;
}

.course-list {
  display: grid;
  gap: 12px;
}

.course-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.course-item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.course-item strong {
  display: block;
  color: #12344d;
}

.course-item p {
  margin: 10px 0 0;
  color: #667085;
  line-height: 1.7;
}

.topic-tags,
.preferred-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.suggestion-box {
  display: grid;
  gap: 14px;
}

.suggestion-copy {
  margin: 0;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbfb 0%, #fef8ef 100%);
  color: #51606d;
  line-height: 1.85;
}

.recommend-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

@media (max-width: 960px) {
  .profile-hero,
  .person-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .person-page {
    width: 94vw;
  }

  .profile-hero {
    padding: 24px;
  }

  .profile-hero h1 {
    font-size: 24px;
  }

  .course-item,
  .form-actions {
    flex-direction: column;
  }
}
</style>
