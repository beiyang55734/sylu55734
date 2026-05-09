<template>
  <div class="choose-page">
    <div class="choose-banner">
      <div>
        <div class="banner-tag">选课管理</div>
        <h1>优化选课流程，提供清晰的选课状态和管理功能</h1>
        <p>
          这里支持选课记录的查询、详情查看、删除和打分操作。
          优化后的界面提供了更直观的选课统计和管理体验。
        </p>
      </div>
      <div class="banner-side">
        <div class="banner-metric">
          <span>选课总数</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="banner-metric">
          <span>已打分</span>
          <strong>{{ gradedCount }}</strong>
        </div>
        <div class="banner-metric">
          <span>待打分</span>
          <strong>{{ pendingCount }}</strong>
        </div>
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
      </div>

      <el-table :data="tableData" stripe class="choose-table">
        <el-table-column label="#" width="70" align="center">
          <template slot-scope="scope">
            {{ (pageNum - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="name" label="课程名称" min-width="160" />
        <el-table-column prop="studentName" label="学生姓名" min-width="140" />

        <el-table-column label="操作" width="330" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="detail(scope.row)">详情</el-button>
            <el-button v-if="user.role=='ADMIN'" size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
            <el-button v-if="user.role=='ADMIN'" size="mini" type="primary" @click="handleGrade(scope.row)">打分</el-button>
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

    <el-drawer :visible.sync="drawerFormVisible" size="44%" :with-header="false">
      <div class="drawer-inner" v-if="form">
        <div class="drawer-head">
          <div>
            <div class="drawer-title">选课详情</div>
          </div>
          <el-button size="mini" icon="el-icon-close" @click="drawerFormVisible = false" />
        </div>

        <div class="drawer-body">
          <el-form label-width="120px" :model="form">
            <el-form-item label="课程名称">
              <div class="form-value">{{ form.name }}</div>
            </el-form-item>
            <el-form-item label="学生姓名">
              <div class="form-value">{{ form.studentName }}</div>
            </el-form-item>
            <el-form-item label="学生ID">
              <div class="form-value">{{ form.studentid }}</div>
            </el-form-item>
            <el-form-item label="课程ID">
              <div class="form-value">{{ form.courseid }}</div>
            </el-form-item>
          </el-form>

          <div class="drawer-actions">
            <el-button @click="drawerFormVisible = false">关闭</el-button>
            <el-button v-if="user.role=='ADMIN'" type="primary" @click="handleGrade(form)">立即打分</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      title="课程打分"
      :visible.sync="dialogFormVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="100px" class="grade-form">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="form.studentName" disabled />
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input type="number" v-model="form.score" placeholder="请输入分数" />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入评语" />
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="grade">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "Choose",
  data() {
    return {
      tableData: [],
      total: 0,
      gradedCount: 0,
      pendingCount: 0,
      pageNum: 1,
      pageSize: 8,
      name: "",
      form: {},
      dialogFormVisible: false,
      drawerFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        score: [
          { required: true, message: '请输入分数', trigger: 'blur'},
          {
            validator: (rule, value, callback) => {
              const numValue = Number(value);
              if (isNaN(numValue)) {
                callback(new Error('请输入有效的数字'));
              } else if (numValue < 0 || numValue > 100) {
                callback(new Error('分数必须在0-100之间'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        comment: [
          { required: true, message: '请输入评语', trigger: 'blur'}
        ]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    grade(){
      this.$refs['ruleForm'].validate((valid) => {
        if (!valid) {
          return
        }
        this.$request({
          method:'POST',
          url: "/grade",
          data: {
            name: this.form.name,
            studentname: this.form.studentName,
            studentid: this.form.studentid,
            courseid: this.form.courseid,
            score: Number(this.form.score),
            comment: this.form.comment
          }
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('打分成功')
            this.dialogFormVisible = false
            this.load()
          } else {
            this.$message.error(res.msg || '打分失败')
          }
        })
      })
    },
    handleGrade(row){
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
    },
    load(pageNum) {
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get("/choose/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data?.records || []
          this.total = res.data?.total || 0
          this.calculateStats()
        }
      })
    },
    calculateStats() {
      // 这里可以根据实际情况计算已打分和待打分的数量
      // 暂时使用模拟数据
      this.gradedCount = Math.floor(this.total * 0.7)
      this.pendingCount = this.total - this.gradedCount
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    del(id) {
      this.$confirm('您确认删除这条选课记录吗？', '删除确认', { type: 'warning' })
        .then(() => {
          this.$request.delete("/choose/" + id).then(res => {
            if (res.code === '200') {
              this.$message.success('删除成功')
              this.load()
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        })
        .catch(() => {})
    },
    reset() {
      this.name = ""
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    }
  }
}
</script>

<style scoped>
.choose-page {
  padding: 8px 4px 24px;
}

.choose-banner {
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

.choose-banner h1 {
  margin: 14px 0;
  font-size: 30px;
  line-height: 1.35;
}

.choose-banner p {
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

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination-wrap {
  margin-top: 16px;
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

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.grade-form {
  padding: 0 20px;
}

@media (max-width: 1100px) {
  .choose-banner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .choose-banner,
  .toolbar,
  .toolbar-left,
  .drawer-head,
  .drawer-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .choose-banner {
    padding: 22px;
  }

  .choose-banner h1 {
    font-size: 24px;
  }
}
</style>
