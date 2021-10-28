<template>
  <section>
    <!--menu-->
    <a-row style="margin-top: 15px">
      <el-menu :default-active="activeIndex" mode="horizontal" @select="doWhenSelectChanged"
               style="width: 100%">
        <el-submenu index="0">
          <template slot="title">
            <span style="font-weight: bold;font-size: 18px;">{{ menuName }} /</span>
            <span v-if="envId === '0'" style="font-weight: bold;font-size: 26px">{{ env }}</span>
            <span v-if="envId === '1'" style="font-weight: bold;font-size: 26px">{{ env }}</span>
            <span v-if="envId === '2'" style="font-weight: bold;font-size: 26px;color: #E6A23C">{{ env }}</span>
            <span v-if="envId === '3'" style="font-weight: bold;font-size: 26px;color: #F56C6C">{{ env }}</span>
          </template>
          <div v-for="(team,index) in selectorList" :key="index">
            <el-submenu :index="team.teamId + ''" v-if="team.configAppList.length > 0" style="font-weight: 500">
              <template slot="title">
                {{ team.teamName }}
              </template>
              <el-menu-item v-if="team.configAppList.length > 0"
                            v-for="(app,appIndex) in team.configAppList"
                            :index="team.teamId + '-' + app.appId" :key="appIndex" style="font-weight: 500">
                {{ app.appName }}
              </el-menu-item>
            </el-submenu>
            <el-menu-item disabled :index="team.teamId + ''" v-else>{{ team.teamName }}</el-menu-item>
          </div>
        </el-submenu>
      </el-menu>
    </a-row>

    <a-row style="padding-top: 15px;padding-left: 20px;background-color: #fff">
      <el-col>
        <el-form :inline="true">
          <el-form-item v-show="isShowEditor">
            <a-button type="primary" icon="reload" @click="configFileSearch">刷新</a-button>
          </el-form-item>
          <el-form-item v-show="!isShowEditor">
            <a-button type="primary" icon="undo" @click="configFileSearch">取消编辑</a-button>
          </el-form-item>
          <el-form-item v-show="isShowEditor">
            <a-button type="primary" icon="edit" @click="editConfigFile">编辑</a-button>
          </el-form-item>
          <el-form-item v-show="!isShowEditor">
            <a-button loading type="dashed" shape="round">编辑中</a-button>
          </el-form-item>
          <el-form-item v-show="!isShowEditor">
            <a-button type="primary" icon="save" @click="saveConfigFile">保存</a-button>
          </el-form-item>
          <el-form-item v-show="isShowEditor">
            <a-button type="primary" icon="cloud-download" @click="pushConfigFile">下发</a-button>
          </el-form-item>
          <el-form-item>
            <a-button type="danger" icon="delete" @click="openDeleteConfigConfigFileModal">删除</a-button>
          </el-form-item>
          <el-form-item>
            <a-dropdown :trigger="['click']" placement="bottomCenter">
              <a class="ant-dropdown-link" @click="e => e.preventDefault()" style="font-weight: 450">
                主题 <a-icon type="down" />
              </a>
              <a-menu slot="overlay" @click="themeChange">
                <template v-for="item in dropDownItem">
                  <a-menu-item :command="item.commend" :key="item.commend">
                    <a-icon :type="item.icon"/>
                    <span>{{ item.name }}</span>
                  </a-menu-item>
                </template>
              </a-menu>
              <a-icon slot="icon" type="down"/>
            </a-dropdown>
          </el-form-item>
          <el-form-item>
            <a-tooltip placement="rightTop">
              <template slot="title">
                <div style="font-size: 16px;padding: 8px">
                  <div>
                    <div style="font-weight: bold;padding-bottom: 4px;font-size: 18px">提示</div>
                    <div style="padding-top: 5px;font-size: 14px;font-weight: 450">
                      <div style="margin-left: 38px;text-indent: -36px;">
                        （1）在点击『下发』按钮后配置文件将会根据您选择的「灰度/全量」的策略下发至客户端。
                      </div>
                      <div style="color: #ff4949;margin-left: 38px;text-indent: -36px;;margin-top: 6px">
                        （2）对于spring或springboot的原生注解或占位符修饰的配置项只提供托管，不支持动态变更与灰度下发。
                      </div>
                      <div style="color: #20a0ff;margin-left: 38px;text-indent: -36px;;margin-top: 6px">
                        （3）对于使用配置中心的提供的注解和占位符修饰的配置项，配置中心提供动态变更与灰度下发的功能。
                      </div>
                      <div style="margin-left: 38px;text-indent: -36px;;margin-top: 6px">
                        （4）右侧的『服务订阅者IP』表示实时订阅了配置文件的实例IP。当实例停止时，这些IP也随之消失。
                      </div>
                      <div style="margin-left: 38px;text-indent: -36px;;margin-top: 6px;margin-bottom: 10px">
                        （5）在点击『刷新』后会重新请求配置文件数据，并且不会保存你修改的数据。
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <a style="margin-left: 15px">
                <a-icon type="question-circle" theme="twoTone" style="font-size: 16px"/>
              </a>
            </a-tooltip>
          </el-form-item>
        </el-form>
      </el-col>
    </a-row>

    <el-row style="background-color: #ffffff;">


      <el-col span="18" style="margin:0 0 20px 0;padding: 0 20px 0 20px;background-color: #fff;">
        <div v-show="!configLoading" id="editor"/>
        <a-spin size="large" tip="拼命加载中 ..." v-show="configLoading" style="margin-left: 55%;margin-top: 30%"/>
      </el-col>

      <el-col span="1">
        <Divider type="vertical" style="height: 850px"/>
      </el-col>

      <el-col span="5" style="padding: 0 10px 0 20px;background-color: #fff;">
        <el-table class="my-ip-table" :data="machineHostData" fit height="350" style="width: 100%;" size="medium">
          <el-table-column prop="tag" label="服务订阅者IP" min-width="220px">
            <template slot-scope="scope">
              <el-row>
                <el-col :span="2">
                  <a-badge status="processing"/>
                </el-col>
                <el-col :span="8">
                  <el-tag size="mini" type="success">{{ scope.row.machineHost }}</el-tag>
                </el-col>
                <el-col :span="6" style="padding-left: 50px">
                  <el-tag size="mini" type="success">已订阅</el-tag>
                </el-col>
              </el-row>
            </template>
          </el-table-column>
        </el-table>

        <el-table class="my-ip-table" :data="notifyUrl" fit height="350" style="width: 100%;" size="medium">
          <el-table-column prop="tag" label="HTTP外部订阅" min-width="220px">
            <template slot-scope="scope">
              <el-row>
                <el-col :span="2">
                  <a-badge status="processing"/>
                </el-col>
                <el-col :span="8">
                  <a-tooltip>
                    <template slot="title">
                      {{ scope.row }}
                    </template>
                    <a-tag v-if="scope.row.length > 35">{{ `${scope.row.slice(0, 35)} ...` }}</a-tag>
                    <a-tag v-else>{{ scope.row }}</a-tag>
                  </a-tooltip>
                </el-col>
              </el-row>
            </template>
          </el-table-column>
        </el-table>
      </el-col>

    </el-row>

    <!--删除的确认弹窗-->
    <a-modal v-model="deleteModalVisible" title="你确认删除该系统的配置文件吗 ?"
             ok-text="确认删除" okType="danger"
             cancel-text="取消"
             @ok="deleteConfigConfigFile">
      <p style="font-size: 14px">在删除该配置文件后，可以在配置文件的操作历史中找到该版本的记录。若误删除，可以通过操作历史进行回滚。</p>
    </a-modal>

    <!--下发的弹窗-->
    <a-modal v-model="pushFormVisible" title="下发配置文件到实例" width="32%"
             ok-text="确认" cancel-text="取消" centered @ok="confirmPushConfigFile" @cancel="cancelPushConfigFile">
      <el-form label-width="100px">
        <el-form-item label="下发策略">
          <RadioGroup v-model="pushType" @on-change="onRadioChanged">
            <Radio label="1">
              <span style="font-size: 13px">全量下发</span>
            </Radio>
            <Radio label="0">
              <span style="font-size: 13px">灰度下发</span>
            </Radio>
          </RadioGroup>
        </el-form-item>

        <el-form-item label="选择实例">
          <el-select v-model="selectData" multiple placeholder="请选择实例" size="small" :disabled="showSelect">
            <el-option
              v-for="item in machineHostData"
              :key="item.machineHost"
              :label="item.machineHost"
              :value="item.machineHost">
            </el-option>
          </el-select>
        </el-form-item>
        <div style="color: #FF4949;font-size: 12px;margin-left: 100px;margin-top: -10px;margin-bottom: 20px">
          <p>* 在第一次下发的时候, 默认为下发到所有实例, 需要等待实例做出响应.</p>
        </div>
      </el-form>
    </a-modal>
  </section>
</template>

<script>
import eventBus from '../../common/js/eventBus.js';

let baseUrL = '/bridge';

export default {
  data() {
    return {
      dropDownItem: [
        {
          icon: 'smile',
          commend: 'ace/theme/tomorrow,Tomorrow',
          name: 'Tomorrow'
        },
        {
          icon: 'smile',
          commend: 'ace/theme/chrome,Chrome',
          name: 'Chrome'
        },
        {
          icon: 'smile',
          commend: 'ace/theme/dracula,Dracula',
          name: 'Dracula'
        },
        {
          icon: 'smile',
          commend: 'ace/theme/tomorrow_night,TomorrowNight',
          name: 'TomorrowNight'
        }
      ],
      codeTheme: 'Tomorrow',
      isShowEditor: true,
      activeIndex: '0',
      teamName: '',
      appName: '',
      appId: '',
      env: '',
      envId: '',

      pushType: '1',
      configFileId: '',
      showSelect: false,
      showRadio: false,

      selectData: [],
      menuName: '点击选择系统',
      selectorList: {},
      pushFormVisible: false,
      configLoading: true,
      consumerIpList: [],
      machineHostData: [],
      notifyUrl: [],
      isPageOpen: false,
      pushEnable: false,

      configFileData: '',
      options: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableEmmet: true,
        enableLiveAutocompletion: true,
        readOnly: true,
        maxLines: 'Infinity',
      },
      editor: '',
      deleteModalVisible: false,
    };
  },
  methods: {

    // ace编辑器初始化
    editorInit() {
      this.editor = ace.edit("editor");
      this.editor.setFontSize(14);
      let theme = localStorage.getItem("codeTheme");
      if (theme) {
        this.editor.setTheme(theme);
      } else {
        this.editor.setTheme("ace/theme/tomorrow");
      }
      if (localStorage.getItem("codeThemeName")) {
        this.codeTheme = localStorage.getItem("codeThemeName");
      }
      this.editor.session.setMode("ace/mode/properties");
      this.editor.setHighlightSelectedWord(true);
      // 设置选中的行高亮显示
      this.editor.setHighlightActiveLine(true);
      this.editor.setOptions(this.options);
    },

    // ace编辑器主题
    themeChange(val) {
      let data = val.key.split(',');
      this.editor.setTheme(data[0].trim());
      this.codeTheme = data[1].trim();
      localStorage.setItem("codeTheme", data[0].trim());
      localStorage.setItem("codeThemeName", data[1].trim());
    },

    // table数据
    doWhenSelectChanged(tab, event) {
      if (event.length === 3) {
        this.teamId = event[2].split('-')[0];
        this.appId = event[2].split('-')[1];
        localStorage.setItem('teamId', this.teamId);
        localStorage.setItem('appId', this.appId);
        // 查询标签
        this.searchTag();
        // 查询列表数据
        this.searchConfigFile();
        // 查询订阅数据
        this.searchConfigFileConsumer();
      }
    },

    // RadioButton的change事件
    onRadioChanged(val) {
      if (val === '0') {
        this.showSelect = false
      }
      if (val === '1') {
        this.showSelect = true
      }
      this.pushType = val;
    },


    // 删除配置文件
    deleteConfigConfigFile() {
      let url = baseUrL + '/deleteConfigFile';
      if (!this.configFileId) {
        this.$message({showClose: true, message: '该配置文件还未初始化', type: 'warning'});
        this.deleteModalVisible = false;
        return;
      }
      this.$http.post(url, {id: this.configFileId}, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "成功删除配置文件", type: 'success'});
          this.searchConfigFile();
          this.searchConfigFileConsumer();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'warning'});
        }
      }).catch((err) => {
        console.log('error', err);
        this.$notify({title: '提示', message: err, type: 'error', duration: 3000});
      })
      this.deleteModalVisible = false;
    },

    // 打开删除确认弹窗
    openDeleteConfigConfigFileModal() {
      this.deleteModalVisible = true;
    },

    // 打开下发弹框
    pushConfigFile() {
      this.pushFormVisible = true;
      // 显示编辑页面的默认值
      this.selectData = [];
      this.pushType = '1';
      this.showSelect = true;
    },

    // 取消下发
    cancelPushConfigFile() {
      this.pushFormVisible = false;
      this.machineHostData = [];
      this.selectData = [];
      this.pushType = '1';
      this.showSelect = true;
    },

    // 确认下发
    confirmPushConfigFile() {
      NProgress.start();
      this.pushFormVisible = false;
      if (!this.appId) {
        this.$message({showClose: true, message: '请先选择系统后再进行「下发」操作 ！', type: 'warning'});
        return;
      }
      if (!this.configFileId) {
        this.$message({showClose: true, message: '请先保存配置文件后再进行下发 ！', type: 'warning'});
        return;
      }
      let params = JSON.stringify({
        appId: this.appId,
        id: this.configFileId,
        pushType: this.pushType,
        envId: localStorage.getItem('envId'),
        machineList: this.selectData,
      });
      let url = baseUrL + '/pushConfigFile';
      this.$http.post(url, params, {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "成功下发配置文件", type: 'success'});
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
        // 查询列表数据
        this.searchConfigFile();
        // 查询订阅数据
        this.searchConfigFileConsumer();
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 编辑配置文件
    editConfigFile() {
      this.isShowEditor = false;
      this.editor.setReadOnly(false);
    },


    // 保存配置文件
    saveConfigFile() {
      NProgress.start();
      if (!this.appId || this.appId.length === 0) {
        this.$message({showClose: true, message: '请先选择系统后再进行「保存」操作 ！', type: 'warning'});
        return false;
      }
      let data = this.editor.getValue();
      if (!data) {
        this.$message({showClose: true, message: '配置文件数据不能为空，请先填写你需要的配置项 ！', type: 'warning'});
        return false;
      }
      let url = baseUrL + '/saveConfigFile';
      let configFileParam = {};
      configFileParam.appId = this.appId;
      configFileParam.envId = this.envId;
      configFileParam.content = data;
      this.$http.post(url, configFileParam).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: '已成功保存配置文件 ！', type: 'success'});
          this.searchConfigFile();
          this.editor.setReadOnly(true);
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'success'});
        }
      }).catch(err => {
        this.$message({showClose: true, message: err, type: 'warning'});
        return false;
      });
      NProgress.done();
    },

    // 查询
    configFileSearch() {
      if (this.appId) {
        this.searchTag();
        this.searchConfigFile();
        this.searchConfigFileConsumer();
      } else {
        this.$message({showClose: true, message: '请先选择系统后再进行「刷新列表」操作 ！', type: 'warning'});
      }
    },

    // 查询标签数据
    searchTag() {
      NProgress.start();
      let url = baseUrL + '/queryTeamNameAndAppNameByAppId';
      let params = {};
      params.appId = this.appId;
      this.$http.get(url, {params: params}).then(function (res) {
        this.teamName = res.body.result.teamName;
        this.appName = res.body.result.appName;
        this.menuName = this.teamName + ' / ' + this.appName;
        localStorage.setItem('teamName', this.teamName);
        localStorage.setItem('appName', this.appName);
        // localStorage.setItem('menuName', this.menuName);
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询列表数据
    searchConfigFile() {
      this.configLoading = true;
      NProgress.start();
      let params = {};
      params.envId = localStorage.getItem('envId');
      params.appId = this.appId;
      let url = baseUrL + '/queryConfigFileData';
      this.$http.get(url, {params: params}).then(function (res) {
        let data = res.body.result;
        if (data) {
          this.configFileData = data.contentProperties;
          if (res.body.result.id) {
            this.configFileId = res.body.result.id;
          } else {
            this.configFileId = '';
          }
        } else {
          this.configFileData = '';
        }
        this.editor.setValue(this.configFileData);
        this.editor.gotoLine(1);
        this.configLoading = false;
        this.isShowEditor = true;
        this.editor.setReadOnly(true);
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询外部订阅、内部订阅情况
    searchConfigFileConsumer() {
      NProgress.start();
      let params = {};
      params.envId = localStorage.getItem('envId');
      params.appId = localStorage.getItem('appId');
      let consumerUrl = baseUrL + '/queryConfigFileConsumerHost';
      this.$http.get(consumerUrl, {params: params}).then(function (res) {
        if (res.body.result) {
          this.machineHostData = res.body.result
        }else {
          this.machineHostData = [];
        }
      }).catch(err => {
        console.log('error', err);
      });
      let queryNotifyUrl = baseUrL + '/queryNotifyUrl';
      this.$http.get(queryNotifyUrl, {params: params}).then(function (res) {
        if (res.body.result) {
          this.notifyUrl = res.body.result
        }else {
          this.notifyUrl = [];
        }
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },


    // 查询下拉框筛选列表
    getSelectorData() {
      let params = {};
      let url = baseUrL + '/getSelectorData';
      this.$http.get(url, {params: params}).then(function (res) {
        this.selectorList = res.body.result;
      }).catch(err => {
        console.log('error', err);
      });
    }
  },


  mounted() {
    this.editorInit();
    this.isPageOpen = true;
    eventBus.$emit('firstColumn', '配置管理');
    eventBus.$emit('secondColumn', '配置文件');
    eventBus.$emit('toPage', '/config_file_manager');
    this.getSelectorData();
    this.teamId = localStorage.getItem('teamId');
    this.appId = localStorage.getItem('appId');
    this.teamName = localStorage.getItem('teamName') ? localStorage.getItem('teamName') : '';
    this.appName = localStorage.getItem('appName') ? localStorage.getItem('appName') : '';
    this.env = localStorage.getItem('env');
    this.envId = localStorage.getItem('envId');
    this.activeIndex = this.teamId + '-' + this.appId;
    if (this.appName && this.teamName) {
      this.menuName = this.teamName + ' / ' + this.appName;
    } else {
      this.menuName = '点击选择系统';
    }
    if (this.appId) {
      this.configFileSearch();
    } else {
      this.configLoading = false;
    }
    eventBus.$on('change', () => {
      this.env = localStorage.getItem('env');
      this.envId = localStorage.getItem('envId');
      if (this.appId && this.isPageOpen) {
        this.searchConfigFile();
        this.searchConfigFileConsumer();
      } else {
        this.configLoading = false;
      }
    })
  },
  destroyed() {
    this.isPageOpen = false;
  },
}
</script>

<style>
.my-ip-table.el-table td {
  border: none;
}

.my-ip-table.el-table th.is-leaf {
  border: none;
}

.el-col.el-col-1 {
  width: 1%;
}

.el-table .position-relative .cell {
  position: relative;
}

@font-face {
  font-family: 'JetBrainsMono';
  src: url('../../assets/font/JetBrainsMono-Medium.woff'),
  url('../../assets/font/JetBrainsMono-Medium.woff2'),
  url('../../assets/font/JetBrainsMono-Medium.ttf'),
  url('../../assets/font/JetBrainsMono-Medium.eot');
}

#editor {
  width: 100%;
  min-height: 1038px;
  font-size: 15px;
  font-family: JetBrainsMono
}

.el-loading-spinner {
  left: 50%;
}

</style>
