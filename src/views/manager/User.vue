<template>
  <div>
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px" placeholder="查询用户名" v-model="username"></el-input>
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询姓名" v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
        <el-button type="primary" plain @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="序号" width="70" align="center">
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column prop="sex" label="性别"></el-table-column>
        <el-table-column prop="age" label="年龄"></el-table-column>
        <el-table-column prop="infos" label="个人介绍"></el-table-column>
        <el-table-column label="头像">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 50px; height: 50px; border-radius: 50%" v-if="scope.row.avatar"
                        :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">详情</el-button>
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增 | 编辑弹框 -->
    <el-dialog title="信息" :visible.sync="fromVisible" width="30%">
      <el-form :model="form" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="姓名"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" v-model="form.address" placeholder="地址"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/file/upload'"
              :headers="{ token: user.token }"
              :file-list="form.avatar? [form.avatar] : []"
              list-type="picture"
              :on-success="handleAvatarSuccess">
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio v-model="form.sex" label="男">男</el-radio>
          <el-radio v-model="form.sex" label="女">女</el-radio>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="form.age" placeholder="年龄"></el-input>
        </el-form-item>
        <el-form-item label="个人介绍" prop="infos">
          <el-input type="textarea" v-model="form.infos" placeholder="个人介绍"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 详情内容 -->
    <el-drawer :visible.sync="formDetailVisible" title="详情" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">详情</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <!-- 抽屉内容 -->
      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item label="用户名" prop="username">
            <div class="form_title">{{form.username}}</div>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <div class="form_title">{{form.name}}</div>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <div class="form_title">{{form.phone}}</div>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <div class="form_title">{{form.email}}</div>
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <div class="form_title">{{form.address}}</div>
          </el-form-item>
          <el-form-item label="头像" prop="avatar">
            <div class="form_title">
              <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            </div>
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <div class="form_title">{{form.sex}}</div>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <div class="form_title">{{form.age}}</div>
          </el-form-item>
          <el-form-item label="个人介绍" prop="infos">
            <div class="form_title">{{form.infos}}</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 抽屉底部 -->
      <div class="drawer-footer">
        <el-button @click="formDetailVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "User",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      username: '',
      name: '',
      total: 0,
      fromVisible: false,
      formDetailVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rules: {
        username: [
          {required: true, message: '请输入账号', trigger: 'blur'},
        ]
      },
      isFullscreen: false,
      drawerWidth: '50%', // 默认抽屉宽度
      drawerPosition: 'right' // 默认抽屉位置
    }
  },
  created() {
    this.load()
  },
  methods: {
    del(id) {
      this.$confirm('您确认删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/user/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load(1)
          } else {
            this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    detail(row) {   // 编辑数据
      this.form = JSON.parse(JSON.stringify(row))  // 给form对象赋值  注意要深拷贝数据
      this.formDetailVisible = true   // 打开弹窗
    },
    handleEdit(row) {   // 编辑数据
      this.form = JSON.parse(JSON.stringify(row))  // 给form对象赋值  注意要深拷贝数据
      this.fromVisible = true   // 打开弹窗
    },
    handleAdd() {   // 新增数据
      this.form = {role: 'USER'}  // 新增数据的时候清空数据
      this.fromVisible = true   // 打开弹窗
    },
    save() {   // 保存按钮触发的逻辑  它会触发新增或者更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/user/update' : '/user/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // 表示成功保存
              this.$notify.success({title: '成功', message: '保存成功', showClose: false, duration: 2000});
              this.load(1)
              this.fromVisible = false
            } else {
              this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});// 弹出错误的信息
            }
          })
        }
      })
    },
    reset() {
      this.name = ''
      this.username = ''
      this.load()
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/user/selectByPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
          name: this.name,
          role: 'USER'
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleAvatarSuccess(response, file, fileList) {
      // 把user的头像属性换成上传的图片的链接
      this.form.avatar = response.data
    },
    handleFullscreenChange() {
      // 如果用户通过ESC键退出全屏，同步更新状态
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        // 保存当前抽屉状态以便退出全屏时恢复
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;

        // 进入全屏模式
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';

        // 延迟执行全屏请求，确保DOM已更新
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        // 退出全屏模式
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;

        this.exitFullscreen();
      }
    },
    // 全屏API相关方法
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  // 监听全屏状态变化
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>
/* 已上传头像样式 */
.avatar {
  width: 78px;                             /* 固定宽度 */
  height: 78px;                            /* 固定高度 */
  display: block;                           /* 块级元素 */
  border-radius: 50%;                       /* 圆形 */
  object-fit: cover;                        /* 裁剪填充 */
  transition: transform 0.3s ease;          /* 缩放过渡 */
}
</style>
