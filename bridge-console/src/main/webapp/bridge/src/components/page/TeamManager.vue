<template>
  <section>
    <a-row style="background-color: #fff;margin-top: 10px;">
      <!--操作栏-->
      <a-col :span="24" style="background: #fff;padding: 20px">
        <a-form layout="inline">
          <a-form-item>
            <a-input v-model="teamName" placeholder="按团队名称搜索" style="width:200px;" clearable/>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" icon="plus-circle" @click="addTeam">创建</a-button>
          </a-form-item>
        </a-form>
      </a-col>

      <a-col style="margin-top:70px;padding: 10px 20px 20px 20px;background: #fff">
        <a-alert message="在删除团队的时，请先确保团队下已无成员"
                 type="info"
                 style="font-size: 13px"
                 show-icon/>
      </a-col>

    </a-row>

    <el-row :span="24" class="el-table-my">

      <div v-for="item in teamListData" class="team-card-item">
        <div style="font-size: 16px;font-weight: bold;color: #cdcdcf">
          <div class="short-line" style="max-width: 70%;display: inline-block">
            {{ item.teamName }}
          </div>
          <div style="display: inline-block;max-width: 30%;overflow: hidden;float: right">
            <div style="margin-right: 20px;">
              <a style="color: #C0C4CC;margin-right: 5px" v-if="item.accountRole !== 0" @click="editTeam(item)">
                <a-icon type="form" style="color: #C0C4CC;font-size: 16px;"/>
              </a>
              <a style="color: #C0C4CC" v-if="item.accountRole !== 0" @click="openDeleteTeamModal(item)">
                <a-icon type="close-circle" style="color: #C0C4CC;font-size: 16px"/>
              </a>
            </div>
          </div>
        </div>
        <div style="margin-top: 10px">
          <a-icon type="crown"/>
          <div style="max-width: 80%;display: inline-block;margin-left: 5px;vertical-align: top;
                      overflow: hidden;text-overflow: ellipsis;white-space: nowrap">
            {{ item.teamDes }}
          </div>
        </div>
        <div style="margin-top: 5px">
          <a-icon type="schedule"/>
          <div style="display: inline-block;margin-left: 5px">
            {{ item.gmtCreate }}
          </div>
        </div>
      </div>

      <div style="text-align: center" v-show="listLoading">
        <a-spin tip="拼命加载中..."/>
      </div>


      <!--工具条-->
      <el-col :span="24" class="toolbar">
        <el-pagination layout="total,  prev, pager, next, jumper" @current-change="teamPageChange"
                       :page-count="totalPage" :size="size" :total="total" style="float:right;">
        </el-pagination>
      </el-col>

      <!--添加或者编辑团队-->
      <a-modal :title="addOrEditStatus.title" width="30%" :visible="teamAddOrEditFormVisible"
               @cancel="cancel">
        <el-form :model="addOrEditForm" ref="addOrEditForm" label-width="100px">
          <el-form-item label="团队名称">
            <a-input v-model="addOrEditForm.teamName" placeholder="请输入团队名称" style="width: 350px"/>
          </el-form-item>
          <el-form-item label="团队描述">
            <el-input size="small" v-model="addOrEditForm.teamDes" type="textarea" placeholder="请输入简短的团队描述"
                      maxlength="30" rows="5" show-word-limit
                      style="margin-top:10px;width: 350px"/>
          </el-form-item>
        </el-form>
        <template slot="footer">
          <a-button @click="cancel">取消</a-button>
          <a-button type="primary" @click="addOrEdit" style="margin-left: 10px">确定</a-button>
        </template>
      </a-modal>

      <!--删除的确认弹窗-->
      <a-modal v-model="deleteModalVisible" :title="'确认删除团队「' + this.DeleteTeamInfo.teamName + '」吗 ?'"
               ok-text="确认删除" okType="danger"
               cancel-text="取消"
               @cancel="cancelDeleteTeam"
               @ok="deleteTeam">
        <p style="font-size: 14px">该操作不可能逆，请谨慎操作。</p>
      </a-modal>

    </el-row>

  </section>
</template>

<script>
import eventBus from '../../common/js/eventBus.js'

let baseUrL = '/bridge';

export default {
  data() {
    return {
      teamName: '',
      myTeamId: '',
      teamListData: [],
      total: 0,
      totalPage: 0,
      size: 10,
      page: 1,
      listLoading: true,
      deleteModalVisible: false,
      DeleteTeamInfo: {},
      teamAddOrEditFormVisible: false,
      addOrEditForm: {},
      addOrEditStatus: {
        title: '创建团队',
        value: 0,
      },
    }
  },
  computed: {},
  methods: {

    // 获取用户账号列表
    getTeamPage(params) {
      NProgress.start();
      let url = baseUrL + '/queryTeamDefVOList';
      this.$http.get(url, {params: params}).then(function (res) {
        this.total = res.body.total;
        this.totalPage = res.body.totalPage;
        this.teamListData = res.body.result;
        this.listLoading = false;
        NProgress.done();
      }).catch(err => {
        console.log('error', err)
      });
    },

    // 取消
    cancel() {
      this.teamAddOrEditFormVisible = false;
      this.$refs['addOrEditForm'].clearValidate();
    },

    // 新增或者编辑
    addOrEdit() {
      if (!this.addOrEditForm) {
        this.$message({showClose: true, message: "请输入团队名称", type: 'warning'});
        return;
      }
      if (!this.addOrEditForm.teamName) {
        this.$message({showClose: true, message: "请输入团队名称", type: 'warning'});
        return;
      }
      if (!this.addOrEditForm.teamDes) {
        this.$message({showClose: true, message: "请输入团队描述", type: 'warning'});
        return;
      }
      let url;
      // 新增
      if (this.addOrEditStatus.value === 0) {
        url = baseUrL + '/addTeam';
        // 编辑更新
      } else if (this.addOrEditStatus.value === 1) {
        url = baseUrL + '/editTeam';
      }
      // 发送http请求
      this.$http.post(url, this.addOrEditForm, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.teamAddOrEditFormVisible = false;
          this.$message({showClose: true, message: "操作成功", type: 'success'});
          this.teamSearch();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
          this.teamAddOrEditFormVisible = true
        }
      })
    },

    // 显示编辑界面
    editTeam: function (row) {
      this.addOrEditStatus = {
        title: '编辑团队信息',
        value: 1
      };
      this.teamAddOrEditFormVisible = true;
      this.addOrEditForm = {
        teamName: row.teamName,
        teamDes: row.teamDes,
        id: row.id,
      };
    },

    // 显示新增界面
    addTeam: function () {
      this.addOrEditStatus = {
        title: '创建团队',
        value: 0
      };
      this.teamAddOrEditFormVisible = true;
      this.addOrEditForm = {
        teamName: '',
        teamDes: '',
        id: '',
      };
    },

    // 分页
    teamPageChange(val) {
      this.page = val;
      this.teamSearch();
    },

    // 查询
    teamSearch() {
      this.listLoading = true;
      let params = {};
      if (this.teamName) {
        params.teamName = this.teamName;
      }
      params.page = this.page;
      params.size = this.size;
      this.getTeamPage(params ? params : '')
    },

    // 打开删除团队的对话框
    openDeleteTeamModal(item) {
      this.deleteModalVisible = true;
      this.DeleteTeamInfo = item;
    },

    // 取消删除团队
    cancelDeleteTeam() {
      this.deleteModalVisible = false;
      this.DeleteTeamInfo = {};
    },

    // 删除用户
    deleteTeam() {
      let url = baseUrL + '/deleteTeam';
      this.$http.post(url, {teamId: this.DeleteTeamInfo.id}, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.teamAddOrEditFormVisible = false;
          this.$message({showClose: true, message: "操作成功", type: 'success'});
          this.teamSearch();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch((err) => {
        console.log('error', err);
        this.$message({showClose: true, message: err, type: 'error'});
      })
      this.deleteModalVisible = false;
    },
  },

  mounted() {
    eventBus.$emit('firstColumn', '基础信息');
    eventBus.$emit('secondColumn', '团队管理');
    eventBus.$emit('toPage', '/team_manager');
    this.teamSearch();
  },
  watch: {
    // 监听输入框
    teamName(val) {
      // 查询列表数据
      this.teamSearch();
    }
  }
}
</script>

<style scoped>
.team-card-item {
  width: 24%;
  height: auto;
  border: #252626 1px;
  display: inline-block;
  box-shadow: 0 0 10px #252626;
  background: #252626;
  border-radius: 10px;
  padding: 15px 0 15px 25px;
  box-sizing: border-box;
  margin: 5px 10px 10px 0;
  color: #95959A;
  position: relative;
  vertical-align: top;
}
</style>
