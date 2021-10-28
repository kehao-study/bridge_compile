<template>
  <section>
    <a-row style="background-color: #fff;margin-top: 10px;">
      <!--操作栏-->
      <a-col :span="24" style="background: #fff;padding: 20px">
        <a-form layout="inline">
          <a-form-item>
            <a-input v-model="accountName" placeholder="按账号搜索" style="width:200px;" clearable/>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" icon="plus-circle" @click="handleAdd">创建</a-button>
          </a-form-item>
        </a-form>
      </a-col>

      <a-col style="margin-top:70px;padding: 10px 20px 20px 20px;background: #fff">
        <a-alert message="变更账号的所属团队时，要确保该账号不是某个系统的系统负责人，如果是则需要先更换该系统的系统负责人"
                 type="info"
                 style="font-size: 13px"
                 show-icon/>
      </a-col>

      <a-col :span="24" style="padding: 0 20px 0 20px;background-color: #fff;">
        <div style="position: relative">
          <div v-for="item in listData" :class='["user-card-item",item.enabledStateBoolean ? "":"alpha"]'>
            <div style="font-size: 16px;font-weight: bold">
              <div class="short-line" style="max-width: 60%;display: inline-block;color: #cdcdcf">
                {{ item.realName }}
              </div>
              <div style="display: inline-block;max-width: 40%;overflow: hidden;float: right">
                <div style="margin-right: 20px;">
                  <a v-if="item.appNameList && item.appNameList.length !== 0" style="margin-right: 4px">
                    <a-tooltip placement="top">
                      <template slot="title">
                        <span style="font-weight: bold">当前用户负责的系统为：</span>
                        <span v-for="idxData in item.appNameList"
                              style="padding: 1px 2px 1px 2px;border-radius: 5px;margin: 2px;">
                            <span style="color: #20a0ff;font-weight: bold">
                              {{ idxData }}
                            </span>
                          </span>
                      </template>
                      <a-icon type="project" style="color: #C0C4CC;font-size: 16px;"/>
                    </a-tooltip>
                  </a>
                  <a style="color: #C0C4CC;margin-right: 5px" v-if="item.accountRole !== 0" @click="handleEdit(item)">
                    <a-icon type="form" style="color: #C0C4CC;font-size: 16px;"/>
                  </a>
                  <a style="color: #C0C4CC" v-if="item.accountRole !== 0" @click="openDeleteUserModal(item)">
                    <a-icon type="close-circle" style="color: #C0C4CC;font-size: 16px"/>
                  </a>
                </div>
              </div>
            </div>
            <div style="margin-top: 10px">
              <a-icon type="tags"/>
              <span style="margin-left: 4px">
                  <a-tag color="#606266" v-if="item.accountRole === 1">
                    <span style="color: #ffffff;font-weight: bold">{{ item.accountRoleStr }}</span>
                  </a-tag>
                  <a-tag color="#606266" v-if="item.accountRole === 2">
                    <span style="color: #F3BE8C;font-weight: bold">{{ item.accountRoleStr }}</span>
                  </a-tag>
                  <a-tag color="#606266" v-if="item.accountRole === 0">
                    <span style="color: #F3BE8C;font-weight: bold">{{ item.accountRoleStr }}</span>
                  </a-tag>
                </span>
            </div>
            <div style="margin-top: 10px;">
              <a-icon type="user"/>
              <span style="margin-left: 4px;max-width: 90%" class="short-line">
                  {{ item.accountName }}
                </span>
            </div>
            <div style="margin-top: 10px;max-width: 90%" class="short-line">
              <a-icon type="mobile"/>
              <span style="margin-left: 4px">
                  {{ item.accountMobile }}
                </span>
            </div>
            <div style="margin-top: 10px;max-width: 90%" class="short-line">
              <a-icon type="team"/>
              <span style="margin-left: 4px;">
                  {{ item.teamName }}
                </span>
            </div>
            <div style="margin-top: 10px;max-width: 90%" class="short-line">
              <a-icon type="mail"/>
              <span style="margin-left: 4px">
                  <a style="color: #95959A" :href="'mailto:'+item.email">{{ item.email }}</a>
                </span>
            </div>
            <div style="margin-top: 5px">
              <a-icon type="switcher"/>
              <span style="margin-left: 4px">
                  <a-switch v-model="item.enabledStateBoolean"
                            :disabled="item.accountRole === 0" size="small"
                            @change="changeEnabledState(item)">
                    <a-icon slot="checkedChildren" type="check"/>
                    <a-icon slot="unCheckedChildren" type="close"/>
                  </a-switch>
                </span>
              <span style="margin-left: 2px;font-size: 14px;font-weight: 500">
                  <span v-if="item.enabledStateBoolean" style="color: #20a0ff">
                    启用
                  </span>
                  <span v-if="!item.enabledStateBoolean" style="color: #ff4949">
                    禁用
                  </span>
              </span>
            </div>
          </div>
          <infinite-loading @infinite="accountListLoad" distance="10" ref="infiniteLoading" style="margin-bottom: 40px">
            <div slot="spinner" style="margin-top: 10px">
              <a-spin tip="拼命加载中 ..."/>
            </div>
            <div slot="no-more" style="margin-top: 10px">
              <span style="color: #ccd0d2">—— 我是有底线的 ——</span>
            </div>
            <div slot="no-results">
              <a-empty/>
            </div>
          </infinite-loading>
        </div>
      </a-col>

      <!--新增或者编辑-->
      <a-drawer :title="addOrEditStatus.title" width="35%" :visible="drawerVisible"
                :body-style="{ paddingBottom: '80px' }"
                @close="closeDrawer">
        <a-form layout="horizontal" :form="addOrEditForm" ref="addOrEditForm" :rules="drawerRules"
                style="width: 400px;margin-left: 100px">
          <a-form-item label="账号" name="accountName">
            <a-input v-model="addOrEditForm.accountName" :disabled="accountNameEnable" placeholder="请输入账号"/>
          </a-form-item>
          <a-form-item label="密码">
            <a-input v-model="addOrEditForm.password" placeholder="请输入密码"/>
          </a-form-item>
          <a-form-item label="姓名">
            <a-input v-model="addOrEditForm.realName" placeholder="请输入你的姓名或昵称"/>
          </a-form-item>
          <a-form-item label="角色">
            <el-select size="small" v-model="addOrEditForm.accountRole" placeholder="请选择角色" clearable>
              <el-option v-for="(item,index) in optionsAccountRole"
                         :label="item.value" :value="item.key" :key="index" :disabled="item.key === 0">
              </el-option>
            </el-select>
          </a-form-item>
          <a-form-item label="团队">
            <el-select size="small" v-model="addOrEditForm.teamId" placeholder="请选择团队" clearable>
              <el-option v-for="(item,index) in optionsTeam"
                         :label="item.value" :value="item.key" :key="index">
              </el-option>
            </el-select>
          </a-form-item>
          <a-form-item label="手机号码" name="accountMobile">
            <a-input v-model="addOrEditForm.accountMobile" placeholder="请输入手机号码" clearable/>
          </a-form-item>
          <a-form-item label="邮箱" name="email">
            <a-input v-model="addOrEditForm.email" placeholder="请输入邮箱地址" clearable/>
          </a-form-item>
          <div style="position: absolute;right: 0;bottom: 0;width: 100%;border-top: 1px solid #e9e9e9;
                      padding: 10px 16px;text-align: right;z-index: 1;background: #fff">
            <a-button @click="closeDrawer">取消</a-button>
            <a-button type="primary" @click="addOrEdit" style="margin-left: 10px">提交</a-button>
          </div>
        </a-form>
      </a-drawer>

      <!--删除的确认弹窗-->
      <a-modal v-model="deleteModalVisible" title="你确认删除该账号吗 ?"
               ok-text="确认删除" okType="danger"
               cancel-text="取消"
               @cancel="cancelDeleteUser"
               @ok="deleteUser">
        <p style="font-size: 14px">删除账号操作为不可逆，请谨慎操作。</p>
      </a-modal>

    </a-row>

  </section>
</template>

<script>

let baseUrL = '/bridge';
import eventBus from '../../common/js/eventBus.js'

export default {
  data() {
    return {
      accountName: '',
      listData: [],
      total: 0,
      totalPage: 0,
      size: 10,
      page: 1,
      userDeletedTmp: {},
      optionsAccountRole: [],
      optionsTeam: [],
      accountNameEnable: false,
      drawerVisible: false,
      deleteModalVisible: false,
      addOrEditForm: {},
      addOrEditStatus: {
        title: '创建账号',
        value: 0,
      },
      drawerRules: {
        email: ['email', {rules: [{type: 'email', message: '邮箱格式不合法',}, {required: true, message: '请输入你的邮箱地址'},],},],

        password: ['password', {
          rules: [{
            required: true,
            message: '请输入你的密码',
          },],
        },],

        accountMobile: ['accountMobile', {
          rules: [{
            required: true,
            message: '请输入你的手机号码',
          },],
        },],
      }
    }
  },
  methods: {

    closeDrawer() {
      this.drawerVisible = false;
      this.addOrEditForm = {};
    },

    confirmDrawer() {

    },

    // 获取用户账号列表
    getUserPage(params) {
      NProgress.start();
      let url = baseUrL + '/queryUserPageList';
      this.$http.get(url, {params: params}).then(function (res) {
        this.total = res.body.total;
        this.totalPage = res.body.totalPage;
        this.listData = res.body.result;
      }).catch(err => {
        console.log('error', err)
      });
      NProgress.done();
    },

    // 根据用户id查询列表
    queryUserById(id) {
      let url = baseUrL + '/queryUserPageList';
      let params = {};
      params.userId = id;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        if (res.data.result && res.data.result.length !== 0) {
          let userInfo = res.data.result[0];
          console.log(this.listData, userInfo)
          for (let i = 0; i < this.listData.length; i++) {
            if (userInfo.id === this.listData[i].id) {
              this.listData[i].enabledState = userInfo.enabledState;
              this.listData[i].enabledStateBoolean = userInfo.enabledStateBoolean;
              this.listData[i].accountRole = userInfo.accountRole;
              this.listData[i].realName = userInfo.realName;
              this.listData[i].teamId = userInfo.teamId;
              this.listData[i].email = userInfo.email;
              this.listData[i].id = userInfo.id;
            }
          }
        }
      }).catch(err => {
        console.log('error', err)
      });
    },

    // 加载更多，无线滚动加载
    accountListLoad($state) {
      NProgress.start();
      let url = baseUrL + '/queryUserPageList';
      let params = {};
      params.page = this.page;
      params.size = this.size;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        if (res.data.result && res.data.result.length !== 0) {
          let resultData = res.data.result;
          this.listData = this.listData.concat(resultData);
          this.total = res.data.total;
          this.page += 1;
          $state.loaded();
          if (res.data.result.size < this.size) {
            $state.complete();
          }
        } else {
          $state.complete();
        }
      }).catch(err => {
        console.log('error', err)
      });
      NProgress.done();
    },

    // 重新刷新列表
    refreshAccountList() {
      this.page = 1;
      this.listData = [];
      this.$refs['infiniteLoading'].$emit('$InfiniteLoading:reset');
    },

    // 获取角色枚举
    getAccountRoleEnum(val) {
      let url = baseUrL + '/queryEnumByTag';
      let params = {};
      params.tag = val;
      this.$http.get(url, {params: params}).then(function (res) {
        if (val === 1) {
          this.optionsAccountRole = res.data.result;
        }
        if (val === 2) {
          this.optionsTeam = res.data.result;
        }
      }).catch(err => {
        console.log('error', err);
      });
    },


    // 新增或者编辑
    addOrEdit() {
      let url;
      // 参数校验
      if (this.addOrEditForm.accountName.trim().length === 0) {
        this.$message({showClose: true, message: "账号不能为空", type: 'error'});
        return false;
      }
      // 新增
      if (this.addOrEditStatus.value === 0) {
        url = baseUrL + '/addUser';
        if (this.addOrEditForm.accountRole === '') {
          this.$message({showClose: true, message: "角色不能为空", type: 'error'});
          return false;
        }
        if (!this.addOrEditForm.password) {
          this.$message({showClose: true, message: "密码不能为空", type: 'error'});
          return false;
        }
        // 编辑更新
      } else if (this.addOrEditStatus.value === 1) {
        url = baseUrL + '/editUser';
      }
      // 发送http请求
      this.$http.post(url, this.addOrEditForm, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.drawerVisible = false;
          this.$message({showClose: true, message: "操作成功", type: 'success'});
          // 新增则重新刷新列表
          if (this.addOrEditStatus.value === 0) {
            this.refreshAccountList();
          }
          // 编辑则重新刷新列表
          if (this.addOrEditStatus.value === 1) {
            this.queryUserById(this.addOrEditForm.id);
          }
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
          this.drawerVisible = true
        }
      })
    },

    //显示编辑界面
    handleEdit(row) {
      this.addOrEditStatus = {
        title: '编辑账号',
        value: 1
      };
      this.accountNameEnable = true;
      this.drawerVisible = true;
      this.addOrEditForm = {
        realName: row.realName,
        accountName: row.accountName,
        accountRole: row.accountRole,
        teamId: row.teamId,
        password: '',
        accountMobile: row.accountMobile,
        email: row.email,
        id: row.id,
      };
    },

    //显示新增界面
    handleAdd() {
      this.drawerVisible = true;
      this.addOrEditStatus = {
        title: '创建账号',
        value: 0
      };
      this.accountNameEnable = false;
      // 显示新增页面的默认值
      this.addOrEditForm = {
        realName: '',
        accountName: '',
        password: '',
        accountMobile: '',
        teamId: '',
        email: '',
        accountRole: '',
      };
    },

    // 查询
    handleSearch() {
      let params = {};
      if (this.accountName) {
        params.accountName = this.accountName;
      }
      params.page = 1;
      params.size = 20;
      this.getUserPage(params ? params : '')
    },

    // 修改启用 禁用 状态
    changeEnabledState(item) {
      let des = (item.enabledStateBoolean ? '启用' : '禁用');
      let enabledState = (item.enabledStateBoolean ? 1 : 0);
      let url = baseUrL + '/updateUserEnable';
      this.$http.post(url, {accountId: item.id, enabledState: enabledState}, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: '已成功' + des + '「' + item.realName + '」 !', type: 'success'});
          this.queryUserById(item.id);
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch(err => {
        console.log('error', err);
        this.$message({showClose: true, message: err, type: 'error'});
      });
    },

    // 删除用户
    deleteUser() {
      let url = baseUrL + '/deleteUser';
      this.$http.post(url, {accountId: this.userDeletedTmp.id}, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "操作成功", type: 'success'});
          this.refreshAccountList();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch((err) => {
        console.log('error', err);
        this.$message({showClose: true, message: err, type: 'error'});
      })
      this.deleteModalVisible = false;
    },

    // 取消删除用户
    cancelDeleteUser() {
      this.deleteModalVisible = false;
      this.userDeletedTmp = {};
    },

    // 打开删除用户的对话框
    openDeleteUserModal(item) {
      this.deleteModalVisible = true;
      this.userDeletedTmp = item;
    },
  },

  mounted() {
    this.refreshAccountList();
    this.getAccountRoleEnum(1);
    this.getAccountRoleEnum(2);
    eventBus.$emit('firstColumn', '基础信息');
    eventBus.$emit('secondColumn', '账号管理');
    eventBus.$emit('toPage', '/account_info');
  },
  watch: {
    // 监听输入框
    accountName(val) {
      // 查询列表数据
      this.handleSearch();
    }
  }
}
</script>


<style scoped>

.user-card-item {
  width: 24%;
  height: 250px;
  border: #1a202c 1px;
  display: inline-block;
  box-shadow: 0 0 10px rgb(37, 38, 38);
  background: #2A2B36;
  border-radius: 10px;
  padding: 15px 0 15px 25px;
  box-sizing: border-box;
  margin: 5px 10px 10px 0;
  color: #95959A;
}

.alpha {
  background-color: #2A2B36;
  z-index: 99999;
  -moz-opacity: 0.1;
  opacity: .6;
}

</style>

<style>
.el-table .cell {
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  white-space: normal;
  word-break: break-all;
  line-height: 23px;
  /*color: #303133;*/
}
</style>
