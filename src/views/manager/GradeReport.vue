<template>
  <div class="report-page">
    <el-card class="report-card" shadow="never">
      <div class="filter-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="课程筛选">
            <el-select v-model="searchForm.courseId" clearable placeholder="全部课程" style="width: 220px">
              <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStatistics">刷新报告</el-button>
            <el-button plain @click="exportReport">导出概览</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-row :gutter="18" class="stat-row">
        <el-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.label">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-desc">{{ item.desc }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="18" class="chart-row">
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <div slot="header" class="chart-header">分数段分布</div>
            <div id="scoreDistributionChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <div slot="header" class="chart-header">课程平均分</div>
            <div id="courseAverageChart" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="18" class="chart-row">
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <div slot="header" class="chart-header">课程及格率</div>
            <div id="coursePassRateChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <div slot="header" class="chart-header">成绩占比</div>
            <div id="scorePercentageChart" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" class="summary-card">
        <div slot="header" class="chart-header">课程统计明细</div>
        <el-table :data="courseStatisticsData" stripe>
          <el-table-column prop="courseName" label="课程名称" min-width="160" />
          <el-table-column prop="count" label="样本数" width="90" align="center" />
          <el-table-column label="平均分" width="100" align="center">
            <template slot-scope="scope">{{ Number(scope.row.average || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="min" label="最低分" width="100" align="center" />
          <el-table-column prop="max" label="最高分" width="100" align="center" />
          <el-table-column label="及格率" width="120" align="center">
            <template slot-scope="scope">{{ Number(scope.row.passRate || 0).toFixed(2) }}%</template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'GradeReport',
  data() {
    return {
      searchForm: {
        courseId: ''
      },
      statistics: {},
      courses: [],
      courseStatisticsData: [],
      scoreDistributionChart: null,
      courseAverageChart: null,
      coursePassRateChart: null,
      scorePercentageChart: null
    }
  },
  computed: {
    statCards() {
      const totalCount = this.statistics.totalCount || 0
      const averageScore = typeof this.statistics.averageScore === 'number' ? this.statistics.averageScore.toFixed(2) : '0.00'
      const passRate = typeof this.statistics.passRate === 'number' ? `${this.statistics.passRate.toFixed(2)}%` : '0.00%'
      return [
        { label: '样本总数', value: totalCount, desc: '当前统计范围内的成绩记录数' },
        { label: '平均分', value: averageScore, desc: '课程整体学习表现' },
        { label: '及格率', value: passRate, desc: '达到合格线的比例' },
        { label: '课程数', value: this.courseStatisticsData.length, desc: '参与统计的课程数量' }
      ]
    }
  },
  created() {
    this.loadCourses()
    this.loadStatistics()
  },
  mounted() {
    this.initCharts()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.disposeCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    loadCourses() {
      this.$request.get('/course').then(res => {
        if (res.code === '200') {
          this.courses = res.data || []
        }
      })
    },
    loadStatistics() {
      this.$request.get('/grade/statistics', {
        params: {
          courseId: this.searchForm.courseId || undefined
        }
      }).then(res => {
        if (res.code === '200') {
          this.statistics = res.data || {}
          this.courseStatisticsData = Object.entries(this.statistics.courseStatistics || {}).map(([courseName, stats]) => ({
            courseName,
            ...stats
          }))
          this.updateCharts()
        }
      })
    },
    initCharts() {
      this.scoreDistributionChart = echarts.init(document.getElementById('scoreDistributionChart'))
      this.courseAverageChart = echarts.init(document.getElementById('courseAverageChart'))
      this.coursePassRateChart = echarts.init(document.getElementById('coursePassRateChart'))
      this.scorePercentageChart = echarts.init(document.getElementById('scorePercentageChart'))
      this.updateCharts()
    },
    disposeCharts() {
      ;[
        this.scoreDistributionChart,
        this.courseAverageChart,
        this.coursePassRateChart,
        this.scorePercentageChart
      ].forEach(chart => {
        if (chart) {
          chart.dispose()
        }
      })
    },
    handleResize() {
      ;[
        this.scoreDistributionChart,
        this.courseAverageChart,
        this.coursePassRateChart,
        this.scorePercentageChart
      ].forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    },
    updateCharts() {
      if (this.scoreDistributionChart) {
        const scoreData = Object.entries(this.statistics.scoreDistribution || {})
        this.scoreDistributionChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: scoreData.map(item => item[0]), axisLabel: { rotate: 24 } },
          yAxis: { type: 'value' },
          series: [{
            type: 'bar',
            data: scoreData.map(item => item[1]),
            itemStyle: {
              color: params => ['#17475f', '#2a9d8f', '#f4a261', '#e76f51', '#7f5539'][params.dataIndex % 5]
            }
          }]
        })
      }

      if (this.courseAverageChart) {
        const courseData = this.courseStatisticsData
        this.courseAverageChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: courseData.map(item => item.courseName), axisLabel: { rotate: 24 } },
          yAxis: { type: 'value', min: 0, max: 100 },
          series: [{
            type: 'bar',
            data: courseData.map(item => Number(item.average || 0)),
            itemStyle: { color: '#17475f' }
          }]
        })
      }

      if (this.coursePassRateChart) {
        const courseData = this.courseStatisticsData
        this.coursePassRateChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: courseData.map(item => item.courseName), axisLabel: { rotate: 24 } },
          yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
          series: [{
            type: 'bar',
            data: courseData.map(item => Number(item.passRate || 0)),
            itemStyle: { color: '#2a9d8f' }
          }]
        })
      }

      if (this.scorePercentageChart) {
        const scoreData = Object.entries(this.statistics.scoreDistribution || {})
        this.scorePercentageChart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left' },
          series: [{
            name: '成绩占比',
            type: 'pie',
            radius: '62%',
            center: ['58%', '50%'],
            data: scoreData.map(([name, value]) => ({ name, value })),
            emphasis: {
              itemStyle: {
                shadowBlur: 12,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.2)'
              }
            }
          }]
        })
      }
    },
    exportReport() {
      const rows = [
        ['课程名称', '样本数', '平均分', '最低分', '最高分', '及格率'],
        ...this.courseStatisticsData.map(item => [
          item.courseName,
          item.count,
          Number(item.average || 0).toFixed(2),
          item.min,
          item.max,
          `${Number(item.passRate || 0).toFixed(2)}%`
        ])
      ]
      const csv = rows.map(row => row.join(',')).join('\n')
      const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = '成绩报告概览.csv'
      link.click()
      window.URL.revokeObjectURL(url)
    }
  }
}
</script>

<style scoped>
.report-page {
  padding: 8px 4px 24px;
}

.report-card {
  border-radius: 24px;
  border: none;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.filter-bar {
  margin-bottom: 18px;
}

.stat-row,
.chart-row {
  margin-top: 18px;
}

.stat-card {
  border-radius: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #17475f;
}

.stat-label {
  margin-top: 10px;
  font-size: 16px;
  color: #1f2937;
}

.stat-desc {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}

.chart-header {
  font-weight: 700;
}

.chart-box {
  height: 320px;
}

.summary-card {
  margin-top: 18px;
  border-radius: 20px;
}
</style>
