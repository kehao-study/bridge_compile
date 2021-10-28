<template>
  <section>
    <div style="margin-top: 10px;">

      <div
        style="background-color: #fff;padding:20px 30px 20px 30px;display: inline-block;position: relative;width: 100%">
        <div style="display: inline-block;vertical-align: top">
          <div style="margin-bottom: 20px">
            <p style="font-weight: 500;font-size: 14px">
              当前系统总数为
              <span style="font-size: 38px;color: #4190F7;margin-left: 10px;font-weight: bold">{{ total }}</span>
            </p>
          </div>
          <a-select :value="appName" show-search placeholder="请输入系统名称搜索"
                    style="width: 200px;margin-bottom: 15px" option-filter-prop="children"
                    @search="selectorFilterData" @change="selectorChange">
            <template v-for="(item,index) in appSelectorList">
              <a-select-option :value="item" :key="index">
                {{ item.appName }}
              </a-select-option>
            </template>
          </a-select>
          <a-menu mode="inline" theme="light" style="width: 200px;height: 580px;overflow-y: scroll"
                  :default-selected-keys="[0]">
            <template v-for="(item,appIdx) in appDataList">
              <a-menu-item :key="appIdx" @click="itemMenuShow(item,true)">
                {{ item.appName }}
              </a-menu-item>
            </template>
            <infinite-loading @infinite="appDataListLoad" distance="5" ref="infiniteLoading">
              <div slot="spinner" style="margin-top: 10px">
                <a-spin tip="拼命加载中 ..."/>
              </div>
              <div slot="no-more"/>
              <div slot="no-results">
                <a-empty style="padding-top: 20px;padding-right: 15px"
                  image="https://gw.alipayobjects.com/mdn/miniapp_social/afts/img/A*pevERLJC9v0AAAAAAAAAAABjAQAAAQ/original"
                  :image-style="{height: '60px'}">
                  <span slot="description" style="color: #abacae">暂无数据</span>
                </a-empty>
              </div>
            </infinite-loading>
          </a-menu>
        </div>

        <div style="display: inline-block;margin-left: 10px;margin-top: 43px">
          <el-form :model="appEditForm" :rules="appEditFormRules" ref="appEditForm"
                   label-width="150px" :label-position="left">
            <el-form-item label="系统名称" prop="appName">
              <el-input v-model="appEditForm.appName" auto-complete="off"
                        placeholder="请填写系统名称" style="margin-left: 20px;width:400px" size="small"/>
            </el-form-item>
            <el-form-item label="系统编码" prop="appCode">
              <div style="margin-left: 20px">
                <a-tag class="fw-500">{{ appEditForm.appCode }}</a-tag>
                <a-tooltip placement="rightTop">
                  <template slot="title">
                    <span>复制</span>
                  </template>
                  <a-button type="link" v-clipboard:copy="appEditForm.appCode" v-clipboard:success="onCopy">
                    <a-icon type="copy" theme="twoTone" style="font-size: 16px;margin-left: -15px"/>
                  </a-button>
                </a-tooltip>
              </div>
            </el-form-item>

            <el-form-item label="所属团队" prop="teamId">
              <el-select v-model="appEditForm.teamId" placeholder="请选择团队"
                         @change="doWhenTeamIdChanged" style="margin-left: 20px" size="small">
                <el-option v-for="item in teamList"
                           :label="item.value"
                           :value="item.key"
                           :key="item.value"
                           :disabled="item.key === 1">
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="系统负责人" prop="appOwner">
              <el-select v-model="appEditForm.appOwner" placeholder="请选择负责人"
                         style="margin-left: 20px" size="small">
                <el-option v-for="item in accountList"
                           :label="item.value"
                           :value="item.key"
                           :key="item.value">
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="系统描述" prop="appDes">
              <el-input v-model="appEditForm.appDes" type="textarea" placeholder="请填写系统相关描述"
                        maxlength="30" rows="5" show-word-limit
                        style="margin-left: 20px;width: 400px"/>
            </el-form-item>
          </el-form>
          <div style="text-align: center;height:80px;margin-bottom: 30px;padding-top: 20px;background-color: #fff">
            <a-button type="primary" icon="plus-circle" @click="openCreateDrawer" style="margin-left: 10px">
              创建
            </a-button>
            <a-button type="primary" icon="save" @click="saveApp" style="margin-left: 10px">
              保存
            </a-button>
            <a-button type="primary" icon="scissor" @click="openDeleteAppModal" style="margin-left: 10px">
              删除
            </a-button>
            <a-button type="primary" icon="control" @click="toConfigManage" style="margin-left: 10px">
              前往
            </a-button>
          </div>
        </div>

        <div style="display: inline-block;vertical-align: top;width: 1px;height: 600px;
                    background-color: #EBEEF5;margin-left: 40px"/>

        <div style="background-color: #fff;margin-left: 10px;;display: inline-block;vertical-align: top">
          <el-form :model="appEditForm" ref="appEditForm"
                   label-width="160px" :label-position="left" style="margin-top: 53px">
            <el-form-item label="HTTP外部订阅模式" prop="enableExternalSubscription">
              <div style="margin-left:20px">
                <a-switch un-checked-children="关" checked-children="开" @change="switchEnableExternalSubscription"
                          v-model="appEditForm.enableExternalSubscription"/>
                <a-tooltip placement="rightTop">
                  <template slot="title">
                    <div style="padding: 15px">
                      <span>
                      按以下步骤操作后，您即可在配置文件下发后收到回调通知:
                      <p style="color: #20a0ff">1. 开启HTTP外部订阅模式</p>
                      <p style="color: #20a0ff">2. 填写回调地址后</p>
                      <p style="color: #20a0ff">3. 在「配置文件」页面，找到该系统并在添加配置项后进行「保存」和「下发」</p>
                      <p>这里有以下几点需要注意:</p>
                      <p style="color: #f3be8c">1. HTTP外部订阅模式仅支持「配置文件」模式</p>
                      <p style="color: #f3be8c">2. 回调地址是区分「系统环境」的</p>
                    </span>
                    </div>
                  </template>
                  <a style="margin-left: 15px">
                    <a-icon type="question-circle" theme="twoTone" style="font-size: 16px"/>
                  </a>
                </a-tooltip>
              </div>
            </el-form-item>

            <el-form-item label="当前环境" prop="envId">
              <div style="margin-left: 20px">
                <a-tag color="blue">{{ envDes }}</a-tag>
                <a-tooltip placement="rightTop">
                  <template slot="title">
                    <span>注意这里的回调地址是区分环境的 !</span>
                  </template>
                  <a>
                    <a-icon type="question-circle" theme="twoTone" style="font-size: 16px"/>
                  </a>
                </a-tooltip>
              </div>
            </el-form-item>

            <el-form-item label="回调地址" prop="notifyUrlList">
              <div style="margin-left:20px">
                <div v-for="(item,urlIdx) in appEditForm.notifyUrlList" :key="urlIdx">
                  <div style="display: inline-block;position: relative">
                    <el-input style="width:400px" placeholder="请输入回调地址" size="small"
                              v-model="item.notifyUrl" :disabled="appEditForm.enableExternalSubscription === 0"/>
                  </div>
                  <div style="display: inline-block;margin-left: 5px"
                       v-show="appEditForm.enableExternalSubscription === 1">
                    <a-popconfirm title="确定删除该回调地址么?" placement="topLeft" ok-text="Yes" cancel-text="No"
                                  @confirm="confirmRemoveUrl(appEditForm.notifyUrlList[urlIdx])">
                      <a-icon slot="icon" type="warning" theme="twoTone" two-tone-color="#faad14"
                              style="font-size: 24px"/>
                      <a style="margin-left: 5px">
                        <a-icon type="file-excel" theme="twoTone" two-tone-color="#f5222d" style="font-size: 14px;"/>
                      </a>
                    </a-popconfirm>
                    <a-popconfirm title="确定保存该回调地址么?保存后将覆盖原来的回调地址！"
                                  placement="topLeft" ok-text="Yes" cancel-text="No"
                                  @confirm="confirmSaveUrl(appEditForm.notifyUrlList[urlIdx])">
                      <a-icon slot="icon" type="warning" theme="twoTone" two-tone-color="#faad14"
                              style="font-size: 24px"/>
                      <a style="margin-left: 5px">
                        <a-icon type="save" theme="twoTone" style="font-size: 14px"/>
                      </a>
                    </a-popconfirm>
                  </div>
                </div>
                <a-button type="dashed" style="width: 100px;font-size: 12px" size="small"
                          :disabled="appEditForm.enableExternalSubscription === 0"
                          @click="(addNewUrlVisible = true) && (newNotifyUrl === '')" block>添加
                </a-button>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>


      <!--添加回调地址-->
      <a-modal title="添加回调地址" width="30%" :visible="addNewUrlVisible"
               @cancel="(addNewUrlVisible = false) && (newNotifyUrl === '')">
        <a-input placeholder="请输入回调地址" v-model="newNotifyUrl">
          <a-select slot="addonBefore" default-value="http://" style="width: 90px" v-model="newNotifyUrlPrefix">
            <a-select-option value="http://">
              http://
            </a-select-option>
            <a-select-option value="https://">
              https://
            </a-select-option>
          </a-select>
        </a-input>

        <div style="margin-top: 20px">
          <span style="margin-right: 20px;color: red">当前环境</span>
          <a-tag color="blue">{{ envDes }}</a-tag>
          <a-tooltip placement="rightTop">
            <template slot="title">
              <span>注意这里的回调地址是区分环境的 !</span>
            </template>
            <a>
              <a-icon type="question-circle" theme="twoTone" two-tone-color="#faad14" style="font-size: 16px"/>
            </a>
          </a-tooltip>
        </div>
        <template slot="footer">
          <a-button @click="(addNewUrlVisible = false) && (newNotifyUrl === '')">取消</a-button>
          <a-button type="primary" @click="confirmAddNewUrl" style="margin-left: 10px">确定</a-button>
        </template>
      </a-modal>

      <!--删除的确认弹窗-->
      <a-modal v-model="deleteModalVisible" :title="'确认删除系统「' + appEditForm.appName + '」吗 ?'"
               ok-text="确认删除" okType="danger"
               cancel-text="取消"
               @ok="deleteApp">
        <p style="font-size: 14px">该操作不可能逆，请谨慎操作。</p>
      </a-modal>


      <a-drawer title="创建系统" width="45%" :visible="appCreateVisible" :body-style="{ paddingBottom: '80px' }"
                @close="closeCreateDrawer">
        <el-form :model="appAddForm" :rules="appAddFormRules" ref="appAddForm"
                 label-width="200px" :label-position="left" style="margin-top: 20px">

          <el-form-item label="系统名称" prop="appName">
            <el-input v-model="appAddForm.appName" auto-complete="off"
                      placeholder="请填写系统名称" style="margin-left: 20px;width:400px" size="small"/>
          </el-form-item>

          <el-form-item label="所属团队" prop="teamId">
            <el-select v-model="appAddForm.teamId" placeholder="请选择团队"
                       @change="queryTeamId4Drawer" style="margin-left: 20px" size="small">
              <el-option v-for="item in teamList"
                         :label="item.value"
                         :value="item.key"
                         :key="item.value"
                         :disabled="item.key === 1">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="系统负责人" prop="appOwner">
            <el-select v-model="appAddForm.appOwner" placeholder="请选择负责人"
                       style="margin-left: 20px" size="small">
              <el-option v-for="item in accountSelector"
                         :label="item.value"
                         :value="item.key"
                         :key="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="系统描述" prop="appDes">
            <el-input v-model="appAddForm.appDes" type="textarea" placeholder="请填写系统相关描述"
                      maxlength="20" rows="5" show-word-limit
                      style="margin-left: 20px;width: 400px"/>
          </el-form-item>

          <el-form-item label="HTTP外部订阅模式" prop="enableExternalSubscription">
            <div style="margin-left:20px">
              <a-switch un-checked-children="关" checked-children="开" @change="appAddEnableSub"
                        v-model="appAddForm.enableExternalSubscription"/>
              <a-tooltip placement="rightTop">
                <template slot="title">
                  <span>开启HTTP外部订阅模式后，并输入回调地址，您即可在配置下发后收到回调通知。另外，回调地址是区分系统环境的。</span>
                </template>
                <a style="margin-left: 10px;">
                  <a-icon type="question-circle" theme="twoTone" style="font-size: 16px"/>
                </a>
              </a-tooltip>
            </div>
          </el-form-item>

          <el-form-item label="当前环境" prop="envId">
            <div style="margin-left: 20px">
              <a-tag color="blue">{{ envDes }}</a-tag>
              <a-tooltip placement="rightTop">
                <template slot="title">
                  <span>注意这里的回调地址是区分环境的 !</span>
                </template>
                <a>
                  <a-icon type="question-circle" theme="twoTone" style="font-size: 16px"/>
                </a>
              </a-tooltip>
            </div>
          </el-form-item>

          <el-form-item label="回调地址" prop="notifyUrlList">
            <div style="margin-left:20px" v-if="appAddForm.notifyUrlList">
              <div v-for="(item,urlIdx) in appAddForm.notifyUrlList" :key="urlIdx">
                <div style="display: inline-block;position: relative;margin-top: 6px">
                  <a-input placeholder="请输入回调地址" style="width: 400px"
                           v-model="item.notifyUrl" :disabled="appAddForm.enableExternalSubscription === 0">
                    <a-select slot="addonBefore" default-value="http://" style="width: 90px"
                              :disabled="appAddForm.enableExternalSubscription === 0"
                              v-model="item.notifyUrlPrefix">
                      <a-select-option value="http://">
                        http://
                      </a-select-option>
                      <a-select-option value="https://">
                        https://
                      </a-select-option>
                    </a-select>
                  </a-input>
                </div>
                <div style="display: inline-block;margin-left: 5px">
                  <a-button type="dashed" style="width: 50px;font-size: 12px;padding-top: 4px" size="small"
                            :disabled="appAddForm.enableExternalSubscription === 0"
                            @click="appAddForm.notifyUrlList.splice(urlIdx,1)" block>移除
                  </a-button>
                </div>
              </div>
              <a-button type="dashed" style="width: 100px;font-size: 12px;padding-top: 4px" size="small"
                        :disabled="appAddForm.enableExternalSubscription === 0"
                        @click="appAddForm.notifyUrlList.push({notifyUrl:'',notifyUrlPrefix:'http://'})" block>添加
              </a-button>
            </div>
          </el-form-item>

          <div style="position: absolute;right: 0;bottom: 0;width: 100%;border-top: 1px solid #e9e9e9;
                      padding: 10px 16px;text-align: right;z-index: 1;background: #fff">
            <a-button @click="closeCreateDrawer">取消</a-button>
            <a-button type="primary" @click="createApp" style="margin-left: 10px">提交</a-button>
          </div>
        </el-form>
      </a-drawer>
    </div>

  </section>
</template>

<script>
import eventBus from '../../common/js/eventBus.js'

let baseUrL = '/bridge';

export default {
  data() {
    return {
      accountSelector: [],
      envDes: '',
      newNotifyUrl: '',
      newNotifyUrlPrefix: 'http://',
      deleteModalVisible: false,
      addNewUrlVisible: false,
      appCreateVisible: false,
      appName: undefined,
      appSelectorList: [],
      accountList: [],
      teamList: [],
      appDataList: [],
      total: 0,
      totalPage: 0,
      size: 5,
      page: 1,
      // 新增或编辑
      addOrEditFormVisible: false,
      appEditForm: {},
      appAddForm: {},
      appEditFormRules: {
        appName: [
          {required: true, message: '请输入客户端名称', trigger: 'blur'}
        ],
        teamId: [
          {required: true, message: '请选择团队', trigger: 'blur'}
        ],
        appOwner: [
          {required: true, message: '请选择负责人', trigger: 'blur'}
        ],
        appDes: [
          {required: true, message: '请输入系统描述', trigger: 'blur'}
        ],
      },

      appAddFormRules: {
        appName: [
          {required: true, message: '请输入客户端名称', trigger: 'blur'}
        ],
        teamId: [
          {required: true, message: '请选择团队', trigger: 'blur'}
        ],
        appOwner: [
          {required: true, message: '请选择负责人', trigger: 'blur'}
        ],
        appDes: [
          {required: true, message: '请输入系统描述', trigger: 'blur'}
        ],
      },
    }
  },
  methods: {

    // 跳转到发布的页面
    toConfigManage() {
      let item = this.appEditForm;
      localStorage.setItem('teamId', item.teamId);
      localStorage.setItem('appId', item.id);
      localStorage.setItem('appName', item.appName);
      localStorage.setItem('teamName', item.teamName);
      this.$router.push('/config_file_manager').catch(err => {err});
    },

    // 复制成功
    onCopy(val) {
      this.$message({showClose: true, message: '已复制系统编码「' + val.text + '」', type: 'success'});
    },

    // 显示编辑界面
    itemMenuShow(row, isClearAppName) {
      if (isClearAppName) {
        this.appName = undefined;
      }
      this.doRequestWhenEdit(row.teamId);
      // 显示编辑页面的默认值
      row = JSON.parse(JSON.stringify(row));
      this.appEditForm = {
        teamName: row.teamName,
        appName: row.appName,
        appCode: row.appCode,
        appOwner: row.appOwner,
        teamId: row.teamId,
        appDes: row.appDes,
        ownerRealName: row.ownerRealName,
        id: row.id,
        // configType: row.configType,
        // configTypeStr: row.configTypeStr,
        enableExternalSubscription: row.enableExternalSubscription,
        notifyUrlList: row.notifyUrlList,
      };
    },

    // 新增回调地址
    confirmAddNewUrl() {
      let url = baseUrL + '/editSubscriptionUrl';
      let param = {
        envId: localStorage.getItem('envId'),
        notifyUrl: this.newNotifyUrlPrefix + this.newNotifyUrl,
        appId: this.appEditForm.id,
        // 新增
        type: 1,
      };
      this.$http.post(url, param).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "回调地址「" + param.notifyUrl + "」已成功保存", type: 'success'});
          this.queryNotifyInfo();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
        this.addNewUrlVisible = false;
        this.newNotifyUrl = '';
      }).catch(err => {
        this.$message({showClose: true, message: err, type: 'warning'});
        return false;
      });
    },

    // 删除回调地址
    confirmRemoveUrl(item) {
      let url = baseUrL + '/editSubscriptionUrl';
      let param = {
        envId: localStorage.getItem('envId'),
        id: item.id,
        notifyUrl: item.notifyUrl,
        type: 0,
      };
      this.$http.post(url, param).then(function (res) {
        if (res.body.success) {
          this.queryNotifyInfo();
          this.$message({showClose: true, message: '回调地址「' + item.notifyUrl + '」已成功删除', type: 'success'});
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch(err => {
        this.$message({showClose: true, message: err, type: 'warning'});
        return false;
      });
    },

    // 确定保存回调url
    confirmSaveUrl(item) {
      let url = baseUrL + '/editSubscriptionUrl';
      let param = {
        envId: localStorage.getItem('envId'),
        id: item.id,
        notifyUrl: item.notifyUrl,
        // 保存
        type: 2,
      };
      this.$http.post(url, param).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "保存成功", type: 'success'});
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
        this.queryNotifyInfo();
      }).catch(err => {
        this.$message({showClose: true, message: err, type: 'warning'});
        return false;
      });
    },

    // HTTP外部订阅模式开启、关闭状态
    switchEnableExternalSubscription(checked, event) {
      this.appEditForm.enableExternalSubscription = (checked ? 1 : 0);
      let url = baseUrL + '/editExternalSubscription';
      let param = {
        envId: localStorage.getItem('envId'),
        appId: this.appEditForm.id,
        enable: this.appEditForm.enableExternalSubscription,
      };
      this.$http.post(url, param).then(function (res) {
        if (res.body.success) {
          this.queryNotifyInfo();
          if (checked) {
            this.$message({showClose: true, message: "已成功开启「HTTP外部订阅模式」", type: 'success'});
          } else {
            this.$message({showClose: true, message: "已成功关闭「HTTP外部订阅模式」", type: 'success'});
          }
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch(err => console.log(err));
    },


    // 创建app的时候
    appAddEnableSub(checked, event) {
      this.appAddForm.enableExternalSubscription = (checked ? 1 : 0);
      this.appAddForm.notifyUrlList = this.appAddForm.notifyUrlList.filter(s => {
        return s.notifyUrl !== '';
      });
    },

    // 编辑保存app
    saveApp() {
      this.$refs['appEditForm'].validate((valid) => {
        if (valid) {
          let url = baseUrL + '/editApp';
          this.appEditForm.envId = localStorage.getItem('envId');
          this.$http.post(url, JSON.stringify(this.appEditForm)).then(function (res) {
            if (res.body.success) {
              this.$message({showClose: true, message: "操作成功", type: 'success'});
              this.refreshAppForm(this.appEditForm.id)
            } else {
              this.$message({showClose: true, message: res.body.message, type: 'error'});
            }
          }).catch(err => console.log(err));
        } else {
          //表单校验失败
          this.$message({showClose: true, message: "表单校验失败", type: 'error'});
        }
      });
    },

    // 打开删除app的对话框
    openDeleteAppModal() {
      this.deleteModalVisible = true;
    },

    // 删除app
    deleteApp() {
      NProgress.start();
      let url = baseUrL + '/deleteApp';
      let appId = this.appEditForm.id;
      this.$http.post(url, {id: appId}, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.refreshAppList();
          this.$message({showClose: true, message: "操作数据成功", type: 'success'});
          NProgress.done();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
      }).catch(err => console.log('error', err));
      this.deleteModalVisible = false;
    },

    // 确定创建app
    createApp() {
      this.$refs['appAddForm'].validate((valid) => {
        if (!valid) {
          this.$message({showClose: true, message: "表单校验失败", type: 'error'});
          return;
        }
        this.appAddForm.envId = localStorage.getItem('envId');
        if (this.appAddForm.notifyUrlList.length > 0) {
          for (let i = 0; i < this.appAddForm.notifyUrlList.length; i++) {
            let data = this.appAddForm.notifyUrlList[i];
            if (data.notifyUrl !== '') {
              data.notifyUrl = data.notifyUrlPrefix + data.notifyUrl;
            }
          }
        }
        let url = baseUrL + '/addApp'
        this.$http.post(url, JSON.stringify(this.appAddForm)).then(function (res) {
          if (res.body.success) {
            this.refreshAppList();
            this.$message({showClose: true, message: "创建成功", type: 'success'});
            this.appCreateVisible = false;
          } else {
            this.$message({showClose: true, message: res.body.message, type: 'error'});
          }
        }).catch(err => {
          this.$message({showClose: true, message: err.message, type: 'warning'});
        });
      });
    },

    // 打开创建app的弹框
    openCreateDrawer() {
      this.appCreateVisible = true;
      this.appAddForm = {
        appName: '',
        appOwner: '',
        teamId: '',
        appDes: '',
        configType: 1,
        envId: '',
        enableExternalSubscription: 0,
        notifyUrlList: [{notifyUrl: '', notifyUrlPrefix: 'http://'}],
      };

    },

    // 关闭创建app的弹框
    closeCreateDrawer() {
      this.appCreateVisible = false;
      this.appAddForm = {};
      this.$refs['appAddForm'].clearValidate();
    },

    // 在drawer中，当选中的team发生改变时，重新拉取team下的人员
    queryTeamId4Drawer(value) {
      if (!value) {
        return;
      }
      let url = baseUrL + '/queryAccountByTeamId';
      let params = {};
      params.teamId = value;
      this.$http.get(url, {params: params}).then(function (res) {
        this.accountSelector = res.body.result;
        this.appAddForm.appOwner = '';
      }).catch(err => console.log('error', err));
    },


    // 当选中的team发生改变时，重新拉取team下的人员
    doWhenTeamIdChanged(val) {
      if (!val) {
        return;
      }
      let url = baseUrL + '/queryAccountByTeamId';
      let params = {};
      params.teamId = val;
      this.$http.get(url, {params: params}).then(function (res) {
        this.accountList = res.body.result;
        this.appEditForm.appOwner = '';
      }).catch(err => {
        console.log('error', err);
      });
    },

    // 当打开编辑框的时候，根据teamId查询出对应的accountList
    doRequestWhenEdit(val) {
      let url = baseUrL + '/queryAccountByTeamId';
      let params = {};
      params.teamId = val;
      this.$http.get(url, {params: params}).then(function (res) {
        this.accountList = res.body.result;
      }).catch(err => {
        console.log('error', err);
      });
    },

    // 获取账号、团队列表
    getAccountAndTeamList() {
      NProgress.start();
      let params = {};
      let url = baseUrL + '/queryTeamList';
      this.$http.get(url, {params: params}).then(function (res) {
        this.teamList = res.body.result;
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询app的外部订阅数据
    queryNotifyInfo() {
      NProgress.start();
      let url = baseUrL + '/queryNotifyInfo';
      let params = {};
      params.appId = this.appEditForm.id;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        this.appEditForm.notifyUrlList = res.data.result.notifyUrlList;
        this.appEditForm.enableExternalSubscription = res.data.result.enableExternalSubscription;
      }).catch(err => console.log('error', err));
      NProgress.done();
    },

    // 加载更多，无线滚动加载
    appDataListLoad($state) {
      let url = baseUrL + '/queryAppPageList';
      let params = {};
      params.page = this.page;
      params.size = this.size;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        if (res.data.result && res.data.result.length !== 0) {
          this.appDataList = this.appDataList.concat(res.data.result);
          if (this.page === 1) {
            this.itemMenuShow(this.appDataList[0], true);
          }
          this.total = res.data.total;
          this.page += 1;
          $state.loaded();
          if (res.data.result.size < this.size) {
            $state.complete();
          }
        } else {
          $state.complete();
        }
      }).catch(err => console.log('error', err));
    },

    // 下拉筛选框
    selectorChange(item) {
      this.appName = item.appName;
      this.itemMenuShow(item, false);
    },

    // 在输入框输入数据的时候实时搜索
    selectorFilterData(appName) {
      let url = baseUrL + '/queryAppPageList';
      let params = {};
      params.page = 1;
      params.size = 1000;
      params.appName = appName;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        this.appSelectorList = res.data.result;
      }).catch(err => console.log('error', err));
    },

    // 根据appId查询
    refreshAppForm(appId) {
      if (!appId) {
        return;
      }
      let url = baseUrL + '/queryAppPageList';
      let params = {};
      params.page = 1;
      params.size = 1000;
      params.appId = appId;
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        let dataList = res.data.result;
        if (dataList && dataList.length > 0) {
          this.itemMenuShow(dataList[0], true);
        }
      }).catch(err => console.log('error', err));
    },

    // 重新刷新列表
    refreshAppList() {
      this.page = 1;
      this.appDataList = [];
      this.$refs['infiniteLoading'].$emit('$InfiniteLoading:reset');
    },
  }
  ,

  mounted() {
    eventBus.$emit('firstColumn', '基础信息');
    eventBus.$emit('secondColumn', '系统管理');
    eventBus.$emit('toPage', '/system_manager');
    this.envDes = localStorage.getItem('env');
    this.getAccountAndTeamList();
    // 重新选择环境的时候要重新刷新
    eventBus.$on('change', () => {
      this.envDes = localStorage.getItem('env');
      this.refreshAppList();
    })
  }
  ,
}


</script>

<style>
.el-form-item__label {
  color: #000000D9 !important;
}

.ant-popover-message-title {
  padding-left: 35px;
  margin-top: 6px;
}

.ant-popover-inner-content {
  padding: 2px 10px;
  color: rgba(0, 0, 0, 0.65);
}
</style>

<style scoped>

.el-form-item__error {
  color: #F56C6C;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
  position: absolute;
  top: 100%;
  margin-left: 20px;
  left: 0;
}
</style>
