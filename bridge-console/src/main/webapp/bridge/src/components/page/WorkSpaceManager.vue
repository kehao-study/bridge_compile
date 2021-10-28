<template>
  <section>
    <a-row style="background-color: #fff;margin-top: 10px;">
      <a-col :span="18" class="toolbar">
        <a-col>
          <div style="padding-top: 10px; position: relative">
            <div style="width: 72px;height: 72px;position: absolute;left :0;top:0;">
              <a-avatar style="width: 72px;height: 72px;" size="large"
                        src="http://acxnn.oss-cn-hangzhou.aliyuncs.com/bridge/operate_pic.png"/>
            </div>
            <div>
              <div class="welcome">你好，{{ realName }}，祝你开心每一天！</div>
              <div class="user_info_des">
                <Icon type="ios-briefcase"/>
                {{ userTeamName }}
                <a-divider type="vertical"/>
                <Icon type="ios-ribbon"/>
                {{ accountRoleStr }}
                <a-divider type="vertical"/>
                <Icon type="md-person"/>
                {{ realName }}
              </div>

            </div>
          </div>
        </a-col>
      </a-col>

      <a-col :span="6">
        <a-row style="background-color: #fff;margin-top: 10px;padding-top: 20px">
          <a-col :span="6">
            <a-row class="project_des">项目数</a-row>
            <a-row class="project_number">{{ projectNumber }}</a-row>
          </a-col>
          <a-col :span="2">
            <a-row>
              <a-divider class="line" type="vertical"/>
            </a-row>
          </a-col>
          <a-col :span="6">
            <a-col class="project_des">发布次数</a-col>
            <a-col class="project_number">{{ pushCount }}</a-col>
          </a-col>
        </a-row>
      </a-col>
    </a-row>

    <a-row>
      <a-card class="box-card" title="我的项目" bordered="false">
        <a href="#/system_manager" slot="extra">查看所有</a>
        <div v-if="!appDefVoList || appDefVoList.length === 0">
          <div style="color: #999;font-size: 14px;text-align: center;padding: 40px 0 40px 0">
            <a-empty>
              <span slot="description">
                暂无数据
              </span>
            </a-empty>
          </div>
        </div>

        <a-card-grid class="a-card-grid-item" v-for="(app,index) in appDefVoList" :key="index">
          <a-col>
            <a-row>
              <span class="item-icon">
                  {{ app.appName && app.appName.length > 0 ? app.appName.charAt(0).toUpperCase() : '#' }}
              </span>
              <span class="item-app-name">
                <span class="short-line" style="width: 70%;display: inline-block">{{ app.appName }}</span>
              </span>
            </a-row>
          </a-col>
          <a-col>
            <div style="padding-left: 55px;font-size: 13px;">
              <Icon type="ios-briefcase" style="color: #515a6e"/>
              <a-tag style="max-width: 80%;vertical-align: top" class="short-line">
                {{ app.teamName }}
              </a-tag>
            </div>
          </a-col>
          <a-col>
            <div class="app-info">
              <span class="short-line" style="width: 95%;display: inline-block">
                <Icon type="md-bookmarks" style="color: #515a6e"/>
                {{ app.appDes }}
              </span>
            </div>
          </a-col>
          <div style="padding-left: 55px;padding-top: 5px;overflow:hidden;margin-bottom: -15px">
            <div style="float:left">
              <Icon type="md-hammer" style="color: #515a6e"/>
              <span style="color: #515a6e;font-size: 12px;">
                {{ app.gmtCreate }}
              </span>
            </div>
            <div style="float: right;padding-bottom: 2px;margin-right: 20px">
              <a @click="toConfigManage(app)" style="font-size: 13px;color: #20a0ff;font-weight: 450">前往</a>
            </div>
          </div>
        </a-card-grid>
      </a-card>
    </a-row>


    <a-row style="margin-top: 10px;">
      <a-card style="font-size: 16px" title="最近动态" bordered="false">
        <a slot="extra">
          <a href="#/config_file_operateLog_manager" slot="extra">查看所有</a>
        </a>
        <div v-if="!configFileLogList || configFileLogList.length === 0">
          <div style="color: #999;font-size: 14px;text-align: center;padding:  40px 0 40px 0">
            <a-empty>
              <span slot="description">
                暂无数据
              </span>
            </a-empty>
          </div>
        </div>
        <div v-for="(item,index) in configFileLogList" :key="index" class="text item">
          <a-row v-if="index <= 4">
            <div style="width: 42px;height: 42px;position: absolute;left :0;top:0;">
              <a-avatar style="width: 42px;height: 42px;"
                        src="http://acxnn.oss-cn-hangzhou.aliyuncs.com/bridge/head_console.png"/>
            </div>
            <div style="padding-left: 60px">
              <a style="color: #20a0ff">{{ item.operateName }}</a>
              <a style="color: #999">操作了</a>
              <a style="color: #20a0ff">{{ item.appName }}</a>
              <a style="color: #999">的配置文件，操作类型为</a>
              <a-tag v-if="item.operateType === 2" color="red">{{ item.operateTypeStr }}</a-tag>
              <a-tag v-if="item.operateType === 3" color="orange">{{ item.operateTypeStr }}</a-tag>
              <a-tag v-if="item.operateType === 0 || item.operateType === 1 || item.operateType === 4" color="blue">
                {{ item.operateTypeStr }}
              </a-tag>
              <a style="color: #999">，配置文件的版本号为</a>
              <a-tag>{{ item.versionAfter }}</a-tag>
            </div>
            <div style="padding-left: 60px;margin-top:5px;font-size: 12px">
              <span>{{ item.gmtCreate }}</span>
            </div>
            <div v-if="index < 4">
              <Divider></Divider>
            </div>
          </a-row>
        </div>
      </a-card>
    </a-row>


  </section>

</template>

<script>
import eventBus from '../../common/js/eventBus.js';

let baseUrL = '/bridge';

export default {
  data() {
    return {
      actionType: '0',
      chartType: '0',
      realName: '',
      userTeamName: '',
      projectNumber: '',
      pushCount: '',
      appDefVoList: [],
      operateLogList: [],
      configFileLogList: [],
      listLoading: true,
      cardLoading: true,
      isPageOpen: false,
    }
  },
  computed: {
    accountRoleStr() {
      let accountRole = localStorage.getItem('accountRole');
      if (accountRole === '0') {
        return '系统管理员';
      }
      if (accountRole === '1') {
        return '普通用户';
      }
      if (accountRole === '2') {
        return '团队管理员';
      }
      return '';
    }
  },
  methods: {

    // 查询工作台数据
    queryWorkSpaceInfo() {
      NProgress.start();
      let url = baseUrL + '/getWorkSpaceInfo';
      let params = {};
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        this.projectNumber = res.body.result.projectNumber;
        this.pushCount = res.body.result.pushCount;
        this.appDefVoList = res.body.result.appDefVoList;
        this.cardLoading = false;
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询配置项操作历史记录
    queryConfigItemLog() {
      this.listLoading = true;
      NProgress.start();
      let url = baseUrL + '/queryOperateLogList';
      let params = {};
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        this.operateLogList = res.body.result;
        this.listLoading = false;
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 查询配置文件操作历史记录
    queryConfigFileLog() {
      this.listLoading = true;
      NProgress.start();
      let url = baseUrL + '/queryConfigFileLog';
      let params = {};
      params.envId = localStorage.getItem('envId');
      this.$http.get(url, {params: params}).then(function (res) {
        this.configFileLogList = res.body.result;
        this.listLoading = false;
      }).catch(err => {
        console.log('error', err);
      });
      NProgress.done();
    },

    // 跳转到发布的页面
    toConfigManage(item) {
      localStorage.setItem('teamId', item.teamId);
      localStorage.setItem('appId', item.id);
      localStorage.setItem('appName', item.appName);
      localStorage.setItem('teamName', item.teamName);
      this.$router.push('/config_file_manager').catch(err => {err});
    },
  },

  mounted() {
    this.isPageOpen = true;
    eventBus.$emit('firstColumn', 'Home');
    eventBus.$emit('secondColumn', '工作台');
    eventBus.$emit('toPage', '/welcome');
    this.realName = localStorage.getItem('realName');
    this.userTeamName = localStorage.getItem('userTeamName');
    this.queryWorkSpaceInfo();
    this.queryConfigFileLog();
    eventBus.$on('change', () => {
      if (this.isPageOpen) {
        this.queryConfigFileLog();
      }
    })
  },
  destroyed() {
    this.isPageOpen = false;
  }
}


</script>


<style scoped>

.welcome {
  font-size: 20px;
  color: #000000D9;
  font-weight: 500;
  line-height: 28px;
  margin-left: 90px;
}

.user_info_des {
  font-size: 14px;
  color: #00000073;
  line-height: 28px;
  margin-left: 90px;
  margin-top: 10px;
}

.project_des {
  text-align: center;
  font-size: 14px;
  color: #00000073;
}

.project_number {
  font-size: 30px;
  font-weight: 500;
  color: #000000D9;
  text-align: center;
}

.line {
  width: 1px;
  height: 60px;
}

.text {
  font-size: 14px;
  color: #999;
}

.clearfix:after {
  clear: both
}

.box-card {
  margin-top: 10px;
  font-size: 16px;
}

.a-card-grid-item {
  width: 25%;
  textAlign: 'center';
  padding: 25px 25px 25px 20px;
  overflow: hidden;
  position: relative;
}

.app-info {
  font-size: 13px;
  padding-top: 5px;
  padding-right: 10px;
  height: 30px;
  line-height: 25px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  padding-left: 55px;
}

.page-price-recommend {
  width: 110px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  position: absolute;
  top: 16px;
  right: -30px;
  background-color: #101117;
  /*background-color: #f60;*/
  color: #fff;
  font-size: 12px;
  transform: rotate(45deg);
  box-shadow: 0 0 15px rgb(37, 38, 38);
}

.item-icon {
  background: #303133;
  font-size: 18px;
  border: 1px solid #303133;
  border-radius: 50px;
  width: 32px;
  height: 32px;
  color: #fff;
  font-weight: 500;
  line-height: 30px;
  text-align: center;
  display: inline-block;
  margin-left: 10px;
  /*box-shadow: 0 0 5px rgb(35, 49, 41);*/

}

.item-app-name {
  font-size: 14px;
  color: #000000;
  padding-left: 5px;
  font-weight: 500;
}
</style>







