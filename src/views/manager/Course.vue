<template>
  <div class="course-page">
    <div class="course-banner">
      <div>
        <div class="banner-tag">文化课程资源库</div>
        <h1>文化课程管理，提供标准化的课程全流程维护与选课入口</h1>
        <p>
          这里支持课程查询、新增、编辑、删除、详情查看和选课操作。
        </p>
      </div>
      <div class="banner-side">
        <div class="banner-metric">
          <span>课程总数</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="banner-metric">
          <span>收藏课程</span>
          <strong>{{ favoriteIds.length }}</strong>
        </div>
        <div class="banner-metric">
          <span>主题数量</span>
          <strong>{{ topicBoards.length }}</strong>
        </div>
      </div>
    </div>

    <div class="topic-board-row" v-if="topicBoards.length">
      <div class="topic-board" v-for="topic in topicBoards" :key="topic.key">
        <div class="topic-board-head">
          <span class="topic-dot" :style="{ background: topic.color }"></span>
          <strong>{{ topic.label }}</strong>
        </div>
        <p>{{ topic.description }}</p>
        <span>{{ topic.courses.length }} 门课程</span>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="name"
            clearable
            style="width: 260px"
            placeholder="搜索课程名称"
            prefix-icon="el-icon-search"
          />
          <el-button type="primary" plain @click="load(1)">查询</el-button>
          <el-button plain @click="reset">重置</el-button>
        </div>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" @click="handleAdd">新增课程</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="course-table">
        <el-table-column label="#" width="70" align="center">
          <template slot-scope="scope">
            {{ (pageNum - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>

        <el-table-column label="封面" width="110" align="center">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.img"
              :src="scope.row.img"
              fit="cover"
              class="table-cover"
              :preview-src-list="[scope.row.img]"
            />
            <div v-else class="table-cover placeholder">{{ shortName(scope.row.name) }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="课程名称" min-width="160" />

        <el-table-column label="文化标签" min-width="190">
          <template slot-scope="scope">
            <div class="tag-wrap">
              <el-tag
                v-for="topic in courseTopics(scope.row)"
                :key="`${scope.row.id}-${topic.key}`"
                size="mini"
                :style="tagStyle(topic.color)"
              >
                {{ topic.label }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="课程简介" min-width="260">
          <template slot-scope="scope">
            {{ summarize(scope.row, 48) }}
          </template>
        </el-table-column>

        <el-table-column prop="teacher" label="授课教师" min-width="110" />
        <el-table-column prop="times" label="上课时间" min-width="120" />
        <el-table-column prop="address" label="上课地点" min-width="120" />

        <el-table-column label="操作" width="330" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="detail(scope.row)">详情</el-button>
            <el-button size="mini" plain :type="isFavorite(scope.row.id) ? 'warning' : 'info'" @click="toggleFavorite(scope.row)">
              {{ isFavorite(scope.row.id) ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button size="mini" type="success" @click="chooseCourse(scope.row)">选课</el-button>
            <el-button v-if="isAdmin" size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="isAdmin" size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      :title="form.id ? '编辑课程' : '新增课程'"
      :visible.sync="dialogFormVisible"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="100px" class="course-form">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="课程简介" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacher">
              <el-input v-model="form.teacher" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课时间" prop="times">
              <el-input v-model="form.times" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="课时安排" prop="intervals">
              <el-input v-model="form.intervals" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课地点" prop="address">
              <el-input v-model="form.address" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 暂时隐藏封面功能 -->
        <!-- <el-form-item label="封面地址" prop="img">
          <el-input v-model="form.img" placeholder="请输入图片 URL" />
        </el-form-item>

        <div v-if="form.img" class="form-preview">
          <el-image :src="form.img" fit="cover" class="preview-image" />
        </div> -->
      </el-form>

      <span slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </el-dialog>

    <el-drawer :visible.sync="drawerFormVisible" size="44%" :with-header="false">
      <div class="drawer-inner" v-if="detailCourse">
        <div class="drawer-cover" :style="coverStyle(detailCourse)">
          <img v-if="detailCourse.img" :src="detailCourse.img" :alt="detailCourse.name">
          <span v-else>{{ shortName(detailCourse.name) }}</span>
        </div>

        <div class="drawer-body">
          <div class="drawer-head">
            <div>
              <div class="drawer-title">{{ detailCourse.name }}</div>
              <div class="drawer-tags">
                <el-tag
                  v-for="topic in courseTopics(detailCourse)"
                  :key="`${detailCourse.id}-${topic.key}`"
                  size="mini"
                  :style="tagStyle(topic.color)"
                >
                  {{ topic.label }}
                </el-tag>
              </div>
            </div>
            <el-button size="mini" :type="isFavorite(detailCourse.id) ? 'warning' : 'info'" plain @click="toggleFavorite(detailCourse)">
              {{ isFavorite(detailCourse.id) ? '取消收藏' : '收藏课程' }}
            </el-button>
          </div>

          <div class="drawer-meta">
            <span><i class="el-icon-user"></i>{{ detailCourse.teacher || '待补充教师信息' }}</span>
            <span><i class="el-icon-time"></i>{{ detailCourse.times || '待补充上课时间' }}</span>
            <span><i class="el-icon-location"></i>{{ detailCourse.address || '待补充上课地点' }}</span>
          </div>

          <div class="drawer-section">
            <h3>课程简介</h3>
            <p>{{ detailCourse.content || '暂无课程简介' }}</p>
          </div>

          <div class="drawer-section">
            <h3>推荐学习路径</h3>
            <p>{{ learningRoute(detailCourse) }}</p>
          </div>

          <div class="drawer-section">
            <h3>教学补充建议</h3>
            <ul>
              <li>适合补充课程背景、文化主题和学习目标，便于答辩展示。</li>
              <li>可以进一步把这门课程和数字人讲解或 AI 出题进行绑定。</li>
              <li>如需增强主题表达，可继续扩展课程资料和案例素材。</li>
            </ul>
          </div>

          <div class="drawer-actions">
            <el-button @click="drawerFormVisible = false">关闭</el-button>
            <el-button type="primary" @click="chooseCourse(detailCourse)">立即选课</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
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
  name: 'Course',
  data() {
    return {
      tableData: [],
      allCourses: [],
      total: 0,
      pageNum: 1,
      pageSize: 8,
      name: '',
      form: {},
      detailCourse: null,
      favoriteIds: [],
      dialogFormVisible: false,
      drawerFormVisible: false,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rules: {
        name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
        content: [{ required: true, message: '请输入课程简介', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isAdmin() {
      return this.user.role === 'ADMIN'
    },
    topicBoards() {
      return groupCoursesByTopic(this.allCourses).slice(0, 4)
    }
  },
  created() {
    this.favoriteIds = loadFavoriteCourseIds(this.user)
    this.load()
    this.loadAllCourses()
  },
  methods: {
    load(pageNum) {
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get('/course/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name
        }
      }).then(res => {
        this.tableData = res.data ? res.data.records : []
        this.total = res.data ? res.data.total : 0
      })
    },
    loadAllCourses() {
      this.$request.get('/course').then(res => {
        if (res.code === '200') {
          this.allCourses = res.data || []
        }
      })
    },
    save() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) {
          return
        }
        this.$request({
          method: this.form.id ? 'PUT' : 'POST',
          url: '/course',
          data: this.form
        }).then(res => {
          if (res.code === '200') {
            this.$message.success(this.form.id ? '课程更新成功' : '课程新增成功')
            this.dialogFormVisible = false
            this.load()
            this.loadAllCourses()
          } else {
            this.$message.error(res.msg || '保存失败')
          }
        })
      })
    },
    handleAdd() {
      this.form = {}
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs.ruleForm && this.$refs.ruleForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs.ruleForm && this.$refs.ruleForm.clearValidate()
      })
    },
    detail(row) {
      this.detailCourse = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    chooseCourse(row) {
      if (!this.user.id) {
        this.$message.info('请先登录后再进行选课')
        this.$router.push('/login')
        return
      }
      this.$request.post('/choose', {
        name: row.name,
        courseid: row.id,
        studentid: this.user.id
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('选课成功')
        } else {
          this.$message.error(res.msg || '选课失败')
        }
      })
    },
    toggleFavorite(course) {
      this.favoriteIds = toggleFavoriteCourse(this.user, course.id)
      this.$message.success(this.isFavorite(course.id) ? '已加入收藏' : '已取消收藏')
    },
    isFavorite(courseId) {
      return this.favoriteIds.includes(Number(courseId))
    },
    del(id) {
      this.$confirm('确认删除这门课程吗？', '删除确认', { type: 'warning' })
        .then(() => {
          this.$request.delete(`/course/${id}`).then(res => {
            if (res.code === '200') {
              this.$message.success('删除成功')
              this.load()
              this.loadAllCourses()
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        })
        .catch(() => {})
    },
    reset() {
      this.name = ''
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    courseTopics(course) {
      return inferCourseTopics(course)
    },
    learningRoute(course) {
      return buildLearningRoute(course)
    },
    summarize(course, maxLength) {
      return summarizeCourse(course, maxLength)
    },
    shortName(name) {
      return String(name || '课程').slice(0, 2)
    },
    tagStyle(color) {
      return {
        borderColor: color,
        color
      }
    },
    coverStyle(course) {
      const colors = ['#17475f', '#2a9d8f', '#bc6c25', '#7f5539']
      const seed = course && course.id ? course.id : 0
      return {
        background: `linear-gradient(135deg, ${colors[seed % colors.length]}, ${colors[(seed + 1) % colors.length]})`
      }
    }
  }
}
</script>

<style scoped>
.course-page {
  padding: 8px 4px 24px;
}

.course-banner {
  display: grid;
  grid-template-columns: 1.7fr 0.9fr;
  gap: 20px;
  margin-bottom: 18px;
  padding: 28px;
  border-radius: 24px;
  color: #fff;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 30%),
    linear-gradient(135deg, #12344d 0%, #2a9d8f 55%, #f4a261 100%);
}

.banner-tag {
  display: inline-flex;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 13px;
}

.course-banner h1 {
  margin: 14px 0;
  font-size: 30px;
  line-height: 1.35;
}

.course-banner p {
  margin: 0;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
}

.banner-side {
  display: grid;
  gap: 14px;
}

.banner-metric {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.12);
}

.banner-metric span {
  display: block;
  color: rgba(255, 255, 255, 0.8);
}

.banner-metric strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.topic-board-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.topic-board {
  padding: 20px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(18, 52, 77, 0.08);
}

.topic-board-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topic-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.topic-board p {
  min-height: 48px;
  margin: 12px 0 14px;
  color: #667085;
  line-height: 1.7;
}

.topic-board span {
  color: #51606d;
}

.table-card {
  border-radius: 24px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.table-cover {
  width: 64px;
  height: 64px;
  border-radius: 14px;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #17475f, #2a9d8f);
}

.tag-wrap,
.drawer-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-wrap {
  margin-top: 16px;
}

.form-preview {
  display: flex;
  justify-content: center;
}

.preview-image {
  width: 180px;
  height: 110px;
  border-radius: 16px;
}

.drawer-inner {
  padding: 20px;
}

.drawer-cover {
  height: 220px;
  border-radius: 22px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 40px;
  font-weight: 700;
}

.drawer-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.drawer-body {
  padding-top: 20px;
}

.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.drawer-title {
  font-size: 28px;
  font-weight: 700;
  color: #12344d;
}

.drawer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 18px 0;
  color: #667085;
}

.drawer-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.drawer-section {
  margin-top: 18px;
}

.drawer-section h3 {
  margin: 0 0 10px;
  color: #12344d;
}

.drawer-section p,
.drawer-section ul {
  margin: 0;
  color: #51606d;
  line-height: 1.85;
}

.drawer-section ul {
  padding-left: 18px;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 1100px) {
  .course-banner,
  .topic-board-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .course-banner,
  .topic-board-row,
  .toolbar {
    grid-template-columns: 1fr;
  }

  .course-banner {
    padding: 22px;
  }

  .course-banner h1 {
    font-size: 24px;
  }

  .toolbar,
  .toolbar-left,
  .toolbar-right,
  .drawer-head,
  .drawer-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
