<template>
  <div class="grade-page">
    <div class="grade-banner">
      <div>
        <div class="banner-tag">学习成绩管理</div>
        <h1>全面管理学生成绩，提供详细的数据分析和可视化</h1>
        <p>
          这里支持成绩的查询、新增、编辑、删除、导入导出和详情查看。
          优化后的界面提供了更直观的成绩统计和数据分析功能。
        </p>
      </div>
      <div class="banner-side">
        <div class="banner-metric">
          <span>总记录数</span>
          <strong>{{ statistics.totalCount || 0 }}</strong>
        </div>
        <div class="banner-metric">
          <span>平均分</span>
          <strong>{{ statistics.averageScore ? statistics.averageScore.toFixed(2) : '0.00' }}</strong>
        </div>
        <div class="banner-metric">
          <span>及格率</span>
          <strong>{{ statistics.passRate ? statistics.passRate.toFixed(2) : '0.00' }}%</strong>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-header">
          <span>分数分布</span>
        </div>
        <div id="scoreDistributionChart" style="height: 300px;"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span>课程平均分</span>
        </div>
        <div id="courseAverageChart" style="height: 300px;"></div>
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
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAdd">新增</el-button>
          <el-button type="warning" plain @click="handleImport">导入</el-button>
          <el-button type="info" plain @click="handleExport">导出</el-button>
          <el-button type="danger" plain @click="batchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="grade-table" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="#" width="70" align="center">
          <template slot-scope="scope">
            {{ (pageNum - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="课程名称" min-width="160" />
        <el-table-column prop="studentname" label="学生姓名" min-width="140" />
        <el-table-column label="分数" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="getScoreTagType(scope.row.score)">{{ scope.row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="180" />
        <el-table-column prop="feedback" label="学生反馈" min-width="180" />

        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="detail(scope.row)">详情</el-button>
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <div class="selected-info">
          <span>已选择 {{ selectedIds.length }} 项</span>
        </div>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="form.id ? '编辑成绩' : '新增成绩'"
      :visible.sync="dialogFormVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="100px" class="grade-form">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学生姓名" prop="studentname">
          <el-input v-model="form.studentname" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="学生ID" prop="studentid">
          <el-input type="number" v-model="form.studentid" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseid">
          <el-input type="number" v-model="form.courseid" placeholder="请输入课程ID" />
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input type="number" v-model="form.score" placeholder="请输入分数" />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入评语" />
        </el-form-item>
        <el-form-item label="学生反馈" prop="feedback">
          <el-input v-model="form.feedback" type="textarea" :rows="3" placeholder="请输入学生反馈" />
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      title="导入成绩"
      :visible.sync="importDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px" class="import-form">
        <el-form-item label="上传文件">
          <el-upload
            class="upload-demo"
            action="/grade/import"
            :on-success="handleImportSuccess"
            :on-error="handleImportError"
            :auto-upload="false"
            :file-list="fileList"
            ref="upload"
          >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <el-button style="margin-left: 10px" size="small" type="success" @click="submitUpload">上传</el-button>
            <div slot="tip" class="el-upload__tip">只能上传xlsx文件，且大小不超过5MB</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="importDialogVisible = false">取消</el-button>
      </span>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer :visible.sync="drawerFormVisible" size="44%" :with-header="false">
      <div class="drawer-inner" v-if="form">
        <div class="drawer-head">
          <div>
            <div class="drawer-title">成绩详情</div>
          </div>
          <el-button size="mini" icon="el-icon-close" @click="drawerFormVisible = false" />
        </div>

        <div class="drawer-body">
          <el-form label-width="120px" :model="form">
            <el-form-item label="课程名称">
              <div class="form-value">{{ form.name }}</div>
            </el-form-item>
            <el-form-item label="学生姓名">
              <div class="form-value">{{ form.studentname }}</div>
            </el-form-item>
            <el-form-item label="学生ID">
              <div class="form-value">{{ form.studentid }}</div>
            </el-form-item>
            <el-form-item label="课程ID">
              <div class="form-value">{{ form.courseid }}</div>
            </el-form-item>
            <el-form-item label="分数">
              <div class="form-value score-value">
                <el-tag :type="getScoreTagType(form.score)">{{ form.score }}</el-tag>
              </div>
            </el-form-item>
            <el-form-item label="评语">
              <div class="form-value">{{ form.comment || '暂无评语' }}</div>
            </el-form-item>
            <el-form-item label="学生反馈">
              <div class="form-value">{{ form.feedback || '暂无反馈' }}</div>
            </el-form-item>
          </el-form>

          <div class="drawer-actions">
            <el-button @click="drawerFormVisible = false">关闭</el-button>
            <el-button type="primary" @click="handleEdit(form)">编辑</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "Grade",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 8,
      name: "",
      form: {},
      dialogFormVisible: false,
      importDialogVisible: false,
      drawerFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        name: [
          { required: true, message: '请输入课程名称', trigger: 'blur'}
        ],
        studentname: [
          { required: true, message: '请输入学生姓名', trigger: 'blur'}
        ],
        studentid: [
          { required: true, message: '请输入学生ID', trigger: 'blur'}
        ],
        courseid: [
          { required: true, message: '请输入课程ID', trigger: 'blur'}
        ],
        score: [
          { required: true, message: '请输入分数', trigger: 'blur'},
          { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur'}
        ]
      },
      selectedIds: [],
      fileList: [],
      statistics: {},
      courseCount: 0,
      scoreDistributionChart: null,
      courseAverageChart: null
    }
  },
  created() {
    this.load()
    this.loadStatistics()
  },
  mounted() {
    this.initCharts()
  },
  beforeDestroy() {
    if (this.scoreDistributionChart) {
      this.scoreDistributionChart.dispose()
    }
    if (this.courseAverageChart) {
      this.courseAverageChart.dispose()
    }
  },
  methods: {
    load(pageNum) {
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get("/grade/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data?.records || []
          this.total = res.data?.total || 0
        }
      })
    },
    loadStatistics() {
      this.$request.get("/grade/statistics").then(res => {
        if (res.code === '200') {
          this.statistics = res.data
          this.courseCount = Object.keys(res.data.courseStatistics || {}).length
          this.updateCharts()
        }
      })
    },
    initCharts() {
      this.scoreDistributionChart = echarts.init(document.getElementById('scoreDistributionChart'))
      this.courseAverageChart = echarts.init(document.getElementById('courseAverageChart'))
      this.updateCharts()
    },
    updateCharts() {
      // 分数分布图表
      if (this.scoreDistributionChart && this.statistics.scoreDistribution) {
        const scoreData = Object.entries(this.statistics.scoreDistribution)
        const categories = scoreData.map(item => item[0])
        const values = scoreData.map(item => item[1])
        
        this.scoreDistributionChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: categories,
            axisLabel: {
              rotate: 45
            }
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: values,
            type: 'bar',
            itemStyle: {
              color: function(params) {
                const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
                return colors[params.dataIndex]
              }
            }
          }]
        })
      }
      
      // 课程平均分图表
      if (this.courseAverageChart && this.statistics.courseStatistics) {
        const courseData = Object.entries(this.statistics.courseStatistics)
        const courses = courseData.map(item => item[0])
        const averages = courseData.map(item => item[1].average)
        
        this.courseAverageChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: courses,
            axisLabel: {
              rotate: 45
            }
          },
          yAxis: {
            type: 'value',
            min: 0,
            max: 100
          },
          series: [{
            data: averages,
            type: 'bar',
            itemStyle: {
              color: '#5470c6'
            }
          }]
        })
      }
    },
    save() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          this.$request({
             method: this.form.id ? 'PUT' : 'POST',
             url: "/grade",
             data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('操作成功')
              this.dialogFormVisible = false
              this.load()
              this.loadStatistics()
            } else {
              this.$message.error(res.msg || '操作失败')
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
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
      this.form = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    del(id) {
      this.$confirm('您确认删除这条数据吗？', '删除确认', { type: 'warning' })
        .then(() => {
          this.$request.delete("/grade/" + id).then(res => {
            if (res.code === '200') {
              this.$message.success('删除成功')
              this.load()
              this.loadStatistics()
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        })
        .catch(() => {})
    },
    batchDelete() {
      this.$confirm('您确认删除选中的' + this.selectedIds.length + '条数据吗？', '删除确认', { type: 'warning' })
        .then(() => {
          this.$request.delete("/grade/batch", {
            data: this.selectedIds
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('删除成功')
              this.load()
              this.loadStatistics()
              this.selectedIds = []
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        })
        .catch(() => {})
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    handleImport() {
      this.importDialogVisible = true
      this.fileList = []
    },
    handleExport() {
      window.open("/grade/export")
    },
    submitUpload() {
      this.$refs.upload.submit()
    },
    handleImportSuccess(res) {
      if (res.code === '200') {
        this.$message.success(res.msg || '导入成功')
        this.importDialogVisible = false
        this.load()
        this.loadStatistics()
      } else {
        this.$message.error(res.msg || '导入失败')
      }
    },
    handleImportError() {
      this.$message.error('导入失败，请检查文件格式')
    },
    reset() {
      this.name = ""
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    getScoreTagType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      if (score >= 60) return 'info'
      return 'danger'
    }
  }
}
</script>

<style scoped>
.grade-page {
  padding: 8px 4px 24px;
}

.grade-banner {
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

.grade-banner h1 {
  margin: 14px 0;
  font-size: 30px;
  line-height: 1.35;
}

.grade-banner p {
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

.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 18px;
}

.chart-card {
  padding: 20px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(18, 52, 77, 0.08);
}

.chart-header {
  margin-bottom: 16px;
  font-weight: 700;
  color: #12344d;
  font-size: 16px;
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

.pagination-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.selected-info {
  color: #667085;
}

.drawer-inner {
  padding: 20px;
}

.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.drawer-title {
  font-size: 24px;
  font-weight: 700;
  color: #12344d;
}

.form-value {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #333;
}

.score-value {
  display: inline-block;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.grade-form,
.import-form {
  padding: 0 20px;
}

@media (max-width: 1100px) {
  .grade-banner,
  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .grade-banner,
  .toolbar,
  .toolbar-left,
  .toolbar-right,
  .pagination-wrap,
  .drawer-head,
  .drawer-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .grade-banner {
    padding: 22px;
  }

  .grade-banner h1 {
    font-size: 24px;
  }
}
</style>
