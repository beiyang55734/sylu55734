<template>
  <div class="ranking-page">
    <div class="ranking-banner">
      <div>
        <div class="banner-tag">成绩排行榜</div>
        <h1>全面展示学生成绩排行，提供详细的数据分析</h1>
        <p>
          这里支持按课程筛选和成绩排序，直观展示学生的学习表现。
          优化后的界面提供了更清晰的排行展示和数据导出功能。
        </p>
      </div>
      <div class="banner-side">
        <div class="banner-metric">
          <span>总记录数</span>
          <strong>{{ rankingData.length }}</strong>
        </div>
        <div class="banner-metric">
          <span>参与课程</span>
          <strong>{{ courses.length }}</strong>
        </div>
        <div class="banner-metric">
          <span>平均分</span>
          <strong>{{ averageScore.toFixed(2) }}</strong>
        </div>
      </div>
    </div>

    <el-card class="ranking-card" shadow="never">
      <div class="filter-bar">
        <el-form :inline="true" :model="searchForm" class="filter-form">
          <el-form-item label="课程筛选">
            <el-select v-model="searchForm.courseId" clearable placeholder="全部课程" style="width: 220px">
              <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序方式">
            <el-select v-model="searchForm.sortBy" style="width: 180px">
              <el-option label="成绩从高到低" value="score_desc" />
              <el-option label="成绩从低到高" value="score_asc" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRanking">刷新排行</el-button>
            <el-button plain @click="exportRanking">导出排行</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="rankingData" stripe class="ranking-table">
        <el-table-column prop="rank" label="排名" width="80" align="center">
          <template slot-scope="scope">
            <div class="rank-number" :class="getRankClass(scope.row.rank)">
              {{ scope.row.rank }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="课程名称" min-width="160" />
        <el-table-column prop="studentname" label="学生姓名" min-width="140" />
        <el-table-column label="成绩" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="getScoreTagType(scope.row.score)">{{ scope.row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="教师评语" min-width="180" />
        <el-table-column prop="feedback" label="学习反馈" min-width="180" />
      </el-table>

      <div class="ranking-info" v-if="rankingData.length > 0">
        <div class="info-item">
          <span>最高分：</span>
          <strong>{{ highestScore }}</strong>
        </div>
        <div class="info-item">
          <span>最低分：</span>
          <strong>{{ lowestScore }}</strong>
        </div>
        <div class="info-item">
          <span>平均分：</span>
          <strong>{{ averageScore.toFixed(2) }}</strong>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'GradeRanking',
  data() {
    return {
      searchForm: {
        courseId: '',
        sortBy: 'score_desc'
      },
      rankingData: [],
      courses: []
    }
  },
  computed: {
    averageScore() {
      if (this.rankingData.length === 0) return 0
      const total = this.rankingData.reduce((sum, item) => sum + (Number(item.score) || 0), 0)
      return total / this.rankingData.length
    },
    highestScore() {
      if (this.rankingData.length === 0) return 0
      return Math.max(...this.rankingData.map(item => Number(item.score) || 0))
    },
    lowestScore() {
      if (this.rankingData.length === 0) return 0
      return Math.min(...this.rankingData.map(item => Number(item.score) || 0))
    }
  },
  created() {
    this.loadCourses()
    this.loadRanking()
  },
  methods: {
    loadCourses() {
      this.$request.get('/course').then(res => {
        if (res.code === '200') {
          this.courses = res.data || []
        }
      })
    },
    loadRanking() {
      this.$request.get('/grade/ranking', {
        params: {
          courseId: this.searchForm.courseId || undefined
        }
      }).then(res => {
        if (res.code === '200') {
          const rows = (res.data || []).slice()
          rows.sort((a, b) => this.searchForm.sortBy === 'score_asc'
            ? Number(a.score || 0) - Number(b.score || 0)
            : Number(b.score || 0) - Number(a.score || 0)
          )
          this.rankingData = rows.map((item, index) => ({
            ...item,
            rank: index + 1
          }))
        }
      })
    },
    exportRanking() {
      const rows = [
        ['排名', '课程名称', '学生姓名', '成绩', '教师评语', '学习反馈'],
        ...this.rankingData.map(item => [
          item.rank,
          item.name,
          item.studentname,
          item.score,
          item.comment || '',
          item.feedback || ''
        ])
      ]
      const csv = rows.map(row => row.join(',')).join('\n')
      const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = '成绩排行.csv'
      link.click()
      window.URL.revokeObjectURL(url)
    },
    getScoreTagType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      if (score >= 60) return 'info'
      return 'danger'
    },
    getRankClass(rank) {
      if (rank === 1) return 'rank-first'
      if (rank === 2) return 'rank-second'
      if (rank === 3) return 'rank-third'
      return ''
    }
  }
}
</script>

<style scoped>
.ranking-page {
  padding: 8px 4px 24px;
}

.ranking-banner {
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

.ranking-banner h1 {
  margin: 14px 0;
  font-size: 30px;
  line-height: 1.35;
}

.ranking-banner p {
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

.ranking-card {
  border-radius: 24px;
  border: none;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.filter-bar {
  margin-bottom: 18px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: 700;
  color: #667085;
  background: #f5f7fa;
}

.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #12344d;
}

.rank-second {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #12344d;
}

.rank-third {
  background: linear-gradient(135deg, #cd7f32, #e6b478);
  color: #12344d;
}

.ranking-info {
  display: flex;
  gap: 32px;
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667085;
}

.info-item strong {
  font-size: 18px;
  color: #12344d;
}

@media (max-width: 1100px) {
  .ranking-banner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .ranking-banner,
  .filter-form,
  .ranking-info {
    flex-direction: column;
    align-items: stretch;
  }

  .ranking-banner {
    padding: 22px;
  }

  .ranking-banner h1 {
    font-size: 24px;
  }

  .filter-form {
    gap: 12px;
  }

  .ranking-info {
    gap: 16px;
  }
}
</style>
