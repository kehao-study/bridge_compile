<template>
  <section>
    <el-row class="el-operate-my">
      <!--操作栏-->
      <el-col :span="24" class="toolbar">
        <el-form :inline="true">
          <el-form-item>
            <a-input v-model="appName" spellcheck="false" placeholder="按系统名称搜索" style="width:200px;" allowClear/>
          </el-form-item>
          <el-form-item>
            <a-button type="primary" icon="search" @click="searchConfigFileLog">查询</a-button>
          </el-form-item>
          <el-form-item>
            <a-button type="primary" icon="diff" @click="showDiffWithChose">差异对比</a-button>
          </el-form-item>
        </el-form>
      </el-col>

      <el-col class="el-table-my" style="margin-top: -10px;margin-bottom: 10px">
        <el-alert style="font-size: 15px"
                  title="注意"
                  type="warning"
                  description="回滚操作会将该配置文件直接【全量下发】，请谨慎操作！点击【差异】按钮打开弹框后，
                  右侧为当前系统最新的配置文件，左侧为当前对比的配置文件">
        </el-alert>
      </el-col>

      <el-col :span="24" class="el-table-my">
        <!--列表-->
        <el-table v-show="!listLoading" :data="operateHistoryList" stripe fit style="width: 100%;" size="medium"
                  highlight-current-row @selection-change="logSelectionChange" ref="operateHistoryTable">
          <el-table-column type="selection" width="50"/>
          <el-table-column label="系统名称">
            <template slot-scope="scope">
              <span class="short-line">{{ scope.row.appName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operateName" label="操作人"/>
          <el-table-column prop="gmtCreate" label="操作时间"/>
          <el-table-column label="版本号">
            <template slot-scope="scope" v-if="scope.row.versionAfter">
              <el-tag size="mini" type="info">{{ scope.row.versionAfter }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作类型">
            <template slot-scope="scope">
              <el-tag size="mini" v-if="scope.row.operateType === 0">
                {{ scope.row.operateTypeStr }}
              </el-tag>
              <el-tag size="mini" type="info" v-if="scope.row.operateType === 1">
                {{ scope.row.operateTypeStr }}
              </el-tag>
              <el-tag size="mini" type="danger" v-if="scope.row.operateType === 2">
                {{ scope.row.operateTypeStr }}
              </el-tag>
              <el-tag size="mini" type="warning" v-if="scope.row.operateType === 3">
                {{ scope.row.operateTypeStr }}
              </el-tag>
              <el-tag size="mini" type="info" v-if="scope.row.operateType === 4">
                {{ scope.row.operateTypeStr }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button type="text" size="medium" @click="openRollBackModal(scope.row)">回滚</el-button>
              <Divider type="vertical"/>
              <el-button type="text" size="medium" @click="showDiff(scope.row)">当前差异</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="text-align: center;margin-top: 20px;margin-bottom: 10px" v-show="listLoading">
          <a-spin tip="拼命加载中..."/>
        </div>
      </el-col>
      <!--工具条-->
      <el-col class="el-page-my">
        <el-pagination layout="total, sizes, prev, pager, next, jumper"
                       @current-change="handleCurrentChange"
                       @size-change="handleSizeChange"
                       :page-sizes="[100,150,200,500,1000]"
                       :current-page.sync="page"
                       :page-count="totalPage"
                       :page-size="size"
                       :total="total"
                       style="text-align: center">
        </el-pagination>
      </el-col>
    </el-row>

    <!--差异对比-->
    <a-modal title="差异对比" width="80%" :visible="diffVisible" @cancel="(diffVisible = false)">
      <div style="display: inline-block;width: 50%">
          <span>左侧版本号为：
            <a-tag color="#F0BEC3">
            <span style="color: #1B202B;font-weight: 500">{{ oldVersion }}</span>
          </a-tag>
          </span>
        <span>右侧版本号为：
            <a-tag color="#ACEF9E">
            <span style="color: #1B202B;font-weight: 500">{{ newVersion }}</span>
          </a-tag>
          </span>
      </div>

      <code-diff :old-string="oldConfigFile" :new-string="newConfigFile"
                 outputFormat="side-by-side" :isShowNoChange="true"
                 :diffStyle="word" :matching="lines"
                 :context="100000" style="margin-bottom: 10px;margin-top: 10px"/>

      <div slot="footer">
        <a-button type="primary" @click="closeShowDiff">关 闭</a-button>
      </div>
    </a-modal>

    <!--回滚确认-->
    <a-modal v-model="rollBackVisible" title="你确认回滚该系统的配置文件吗 ?"
             ok-text="确认回滚" okType="danger"
             cancel-text="取消"
             @cancel="closeRollBackModal"
             @ok="rollBack">
      <p style="font-size: 14px">回滚操作会将该配置文件直接【全量下发】，请谨慎操作！</p>
    </a-modal>


  </section>

</template>

<script>
import eventBus from '../../common/js/eventBus.js';
import CodeDiff from 'vue-code-diff';


let baseUrL = '/bridge';

export default {
  components: {
    CodeDiff,
  },
  data() {
    return {
      diffVisible: false,
      rollBackVisible: false,
      rollBackData: {},
      appName: '',
      configKey: '',
      listData: [],
      logSelection: [],
      total: 0,
      totalPage: 0,
      size: 100,
      page: 1,
      listLoading: true,
      operateHistoryList: [],
      isPageOpen: false,
      oldConfigFile: '',
      newConfigFile: '',
      newVersion: '',
      oldVersion: '',
    }
  },
  methods: {

    // 回滚
    rollBack() {
      NProgress.start();
      let url = baseUrL + '/rollBackConfigFile';
      this.$http.post(url, {configFileLogId: this.rollBackData.id, appId: this.rollBackData.appId},
        {emulateJSON: true}).then(function (res) {
        if (res.body.success) {
          this.$message({showClose: true, message: "操作成功", type: 'success'});
          this.searchConfigFileLog();
        } else {
          this.$message({showClose: true, message: res.body.message, type: 'error'});
        }
        NProgress.done();
      }).catch((err) => {
        console.log('error', err);
        this.$message({showClose: true, message: err, type: 'error'});
      })
      this.rollBackVisible = false;
    },

    // 打开回滚对话框
    openRollBackModal(row) {
      this.rollBackData = row;
      this.rollBackVisible = true;
    },

    // 关闭回滚对话框
    closeRollBackModal() {
      this.rollBackData = {};
      this.rollBackVisible = false;
    },

    // 打开差异对比
    showDiff(row) {
      this.oldConfigFile = row.valueAfter;
      this.oldVersion = row.versionAfter;
      this.getConfigFile(row);
      this.diffVisible = true;
    },

    // 对比选择的
    showDiffWithChose() {
      if (this.logSelection.length === 2) {
        this.diffVisible = true;
        this.oldConfigFile = this.logSelection[0].valueAfter;
        this.oldVersion = this.logSelection[0].versionAfter;
        this.newConfigFile = this.logSelection[1].valueAfter;
        this.newVersion = this.logSelection[1].versionAfter;
      } else {
        this.$message({showClose: true, message: '最大支持2个配置文件同时进行差异对比！', type: 'warning'});
      }
    },

    // 关闭差异对比
    closeShowDiff() {
      this.diffVisible = false;
    },

    // 多选框
    logSelectionChange(val) {
      this.logSelection = val;
      if (this.logSelection.length > 2) {
        this.$message({showClose: true, message: '最大支持2个配置文件同时进行差异对比 ！', type: 'warning'});
        this.logSelection = [];
        this.$refs['operateHistoryTable'].clearSelection();
      }
    },

    // 查询
    searchConfigFileLog() {
      NProgress.start();
      this.listLoading = true;
      let params = {};
      if (this.appName) {
        params.appName = this.appName;
      }
      params.page = this.page;
      params.size = this.size;
      params.envId = localStorage.getItem('envId');
      let url = baseUrL + '/queryConfigFileLog';
      this.$http.get(url, {params: params}).then(function (res) {
        this.operateHistoryList = res.body.result;
        this.total = res.body.total;
        this.totalPage = res.body.totalPage;
        this.listLoading = false;
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询配置文件
    getConfigFile(row) {
      let params = {};
      params.envId = localStorage.getItem('envId');
      params.appId = row.appId;
      let url = baseUrL + '/queryConfigFileData';
      this.$http.get(url, {params: params}).then(function (res) {
        let data = res.body.result;
        if (data) {
          this.newConfigFile = data.contentProperties;
          this.newVersion = data.version;
        } else {
          this.newConfigFile = '';
          this.newVersion = '';
        }
      }).catch(err => {
        console.log('error', err);
      });
    },

    // 分页
    handleCurrentChange(val) {
      this.page = val;
      this.searchConfigFileLog();
    },

    // 分页
    handleSizeChange(val) {
      this.size = val;
      this.searchConfigFileLog();
    },

  },
  mounted() {
    this.isPageOpen = true;
    eventBus.$emit('firstColumn', '配置管理');
    eventBus.$emit('secondColumn', '操作历史');
    eventBus.$emit('toPage', '/config_file_operateLog_manager');
    this.searchConfigFileLog();
    eventBus.$on('change', () => {
      if (this.isPageOpen) {
        this.searchConfigFileLog();
      }
    })
  },
  destroyed() {
    this.isPageOpen = false;
  },
  watch: {
    appName() {
      this.searchConfigFileLog();
    },
  }
}


</script>

<style>

.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #20a0ff;
}

.el-table__body tr.current-row > td {
  background: #E2E8F0 !important;
}

</style>

