<template>
  <div class="login-page">
    <div class="bow-position">
      <div class="top-bow-1"></div>
      <div class="top-bow-2"></div>
      <div class="top-bow-3"></div>

      <div class="low-bow-1"></div>
      <div class="low-bow-2"></div>
      <div class="low-bow-3"></div>

      <div class="dot-1"></div>
      <div class="dot-2"></div>
      <div class="dot-3"></div>
      <div class="dot-4"></div>
      <div class="dot-5"></div>
      <div class="dot-6"></div>
      <div class="dot-7"></div>
      <div class="dot-8"></div>
    </div>
    <div class="sport-line-wrapper" style="opacity: 1; transform: translate(0px, 0px);">
      <div class="sport-line" style="transform: translateX(-64px);"></div>
    </div>
    <div class="login-card">
      <div class="center">
        <div class="img">
          <img src="http://acxnn.oss-cn-hangzhou.aliyuncs.com/bridge/login-pic.png"/>
        </div>
        <div class="main" style="display: inline-block">
          <h2 class="title">Bridge · 管理系统</h2>
          <Form ref="loginValidate" :model="loginValidate" label-position="top"
                class="submit-form">
            <Form-item label="账号：" prop="accountName">
              <Input size="large" v-model="loginValidate.accountName" clearable/>
            </Form-item>
            <Form-item label="密码：" prop="password">
              <Input type="password" size="large" v-model="loginValidate.password" clearable
                     @keyup.enter.native="handleSubmit('loginValidate')"/>
            </Form-item>
            <div class="sub text-center">
              <Button size="large" class="login-btn" type="primary" long
                      @click="handleSubmit('loginValidate')">登&nbsp;&nbsp;录
              </Button>
            </div>
            <div class="pro">
              <div class="direct"/>
            </div>
            <div class="others text-center">
              <div class="text">Hi, 欢迎登录 配置中心·管理系统</div>
              <div class="text" style="padding-top: 2px;color: #20a0ff">
                <a href="http://doc.u2open.com/zh-cn/" target="_blank" style="font-size: 10px;" class="font-older">
                  ↪前往阅读文档
                </a>
              </div>
            </div>
          </Form>
        </div>
      </div>

    </div>
    <div class="footer">
      <a href="http://www.beian.miit.gov.cn/" target="_blank" class="pop-text">浙ICP备17049891号-1</a>
      <a href="https://gitee.com/Jay_git/bridge" target="_blank" class="pop-text">配置中心 · 管理系统</a>
      <div>
        <a href="https://gitee.com/Jay_git" target="_blank" class="pop-text">
          Copyright
          <a-icon type="copyright"/>
          2020 @不要开枪是自己人
          <a href='https://gitee.com/Jay_git/bridge/stargazers' target="_blank">
            <img src='https://gitee.com/Jay_git/bridge/badge/star.svg?theme=gvp'
                 style="height: 20px;display: inline-block" alt='star'/>
          </a>
        </a>
      </div>
    </div>
  </div>
</template>

<script type="es6">

export default {
  data() {
    return {
      loginValidate: {
        accountName: '',
        password: ''
      }
    }
  },
  created() {

  },
  methods: {

    // 登录
    handleSubmit(params) {
      const self = this;
      self.$refs[params].validate((valid) => {
        if (valid) {
          let param = {};
          if (this.loginValidate.accountName === '') {
            this.$message({showClose: true, message: '请输入账号', type: 'warning'});
            return;
          }
          if (this.loginValidate.password === '') {
            this.$message({showClose: true, message: '请输入密码', type: 'warning'});
            return;
          }
          param.accountName = this.loginValidate.accountName;
          param.password = this.loginValidate.password;
          this.postLogin(param);
        } else {
          return false;
        }
      });
    },

    // 执行登陆
    postLogin(params) {
      let url = '/bridge/login', self = this;
      self.$http.get(url, {params})
        .then(function (res) {
          if (res.body.success) {
            let userId = res.body.result.id;
            let token = res.body.result.token;
            // 缓存相关信息
            localStorage.setItem('ms_username', self.loginValidate.accountName);
            localStorage.setItem('token', token);
            localStorage.setItem('userId', userId);
            localStorage.setItem('realName', res.body.result.realName);
            localStorage.setItem('userTeamName', res.body.result.teamName);
            localStorage.setItem('accountRole', res.body.result.accountRole);
            localStorage.setItem('permissionList', res.body.result.permissionList);
            console.log(localStorage);
            self.$router.push('/welcome');
          } else {
            this.$message({showClose: true, message: res.body.message, type: 'error'});
            this.$router.push('/login').catch(err => {err});
          }
        }).catch(err => {
        // 请求数据异常的请款
        this.$message({showClose: true, message: '数据接口请求错误', type: 'error'});
        this.$router.push('/login').catch(err => {err});
      });
    },

  },
}
</script>

<style>
.bow-position {
  position: fixed;
  height: 100%;
  width: 100%;
}

.top-bow-1 {
  height: 300px;
  width: 300px;
  position: absolute;
  top: -150px;
  left: -150px;
  border-radius: 50%;
  border-width: 2px;
  border-style: solid;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.top-bow-2 {
  height: 450px;
  width: 450px;
  position: absolute;
  top: -225px;
  left: -225px;
  border-radius: 50%;
  border-width: 2px;
  border-style: dashed;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.top-bow-3 {
  height: 600px;
  width: 600px;
  position: absolute;
  top: -300px;
  left: -300px;
  border-radius: 50%;
  border-width: 2px;
  border-style: solid;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.low-bow-1 {
  height: 300px;
  width: 300px;
  position: absolute;
  bottom: -150px;
  right: -150px;
  border-radius: 50%;
  border-width: 2px;
  border-style: solid;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.low-bow-2 {
  height: 450px;
  width: 450px;
  position: absolute;
  bottom: -225px;
  right: -225px;
  border-radius: 50%;
  border-width: 2px;
  border-style: dashed;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.low-bow-3 {
  height: 600px;
  width: 600px;
  position: absolute;
  bottom: -300px;
  right: -300px;
  border-radius: 50%;
  border-width: 2px;
  border-style: solid;
  border-color: rgb(218, 223, 226);
  border-image: initial;
}

.dot-1 {
  height: 6px;
  width: 6px;
  background-image: linear-gradient(rgb(232, 59, 69), rgb(134, 58, 227));
  display: inline-block;
  position: absolute;
  top: 10vh;
  left: 80vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 1s infinite;
}

.dot-2 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(111, 58, 255), rgb(116, 58, 255));
  display: inline-block;
  position: absolute;
  top: 11vh;
  left: 35vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 1.2s infinite;
}

.dot-3 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(250, 141, 38), rgb(206, 74, 44));
  display: inline-block;
  position: absolute;
  top: 20vh;
  left: 55vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 0.5s infinite;
}

.dot-4 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(232, 59, 69), rgb(134, 58, 227));
  display: inline-block;
  position: absolute;
  top: 65vh;
  left: 10vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 1.2s infinite;
}

.dot-5 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(59, 241, 234), rgb(58, 149, 252));
  display: inline-block;
  position: absolute;
  top: 75vh;
  left: 25vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 0.8s infinite;
}

.dot-6 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(232, 59, 69), rgb(134, 58, 227));
  display: inline-block;
  position: absolute;
  top: 80vh;
  left: 65vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 1.5s infinite;
}

.dot-7 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(250, 141, 38), rgb(206, 74, 44));
  display: inline-block;
  position: absolute;
  top: 60vh;
  left: 75vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 1s infinite;
}

.dot-8 {
  height: 16px;
  width: 16px;
  background-image: linear-gradient(rgb(250, 141, 38), rgb(206, 74, 44));
  display: inline-block;
  position: absolute;
  top: 80vh;
  left: 95vw;
  border-radius: 50%;
  animation: twinkling 2s ease-in-out 0.5s infinite;
}

@keyframes twinkling {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.sport-line {
  width: 64px;
  height: 100%;
  background: linear-gradient(90deg, rgba(24, 144, 255, 0) 0, #1890ff);
  transform: translateX(-64px);
  animation: sportLine 5s ease-in-out 0s infinite;
}

.sport-line-wrapper {
  width: 100%;
  height: 3px;
  overflow: hidden;
}

@keyframes sportLine {
  0%, 25% {
    transform: translateX(-64px);
  }
  75%, 100% {
    transform: translateX(1800px);
  }
}
</style>

<style>

.font-older {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", 微软雅黑, Arial, sans-serif;
}

.login-page {
  min-height: 100%;
  position: absolute;
  width: 100%;
  margin: 0;
  background-image: url("http://acxnn.oss-cn-hangzhou.aliyuncs.com/bridge/login-bg.svg");
  background-color: #f0f2f5;
}

.main {
  border: 0 solid #e5e5e5;
  border-radius: 0;
  background-color: #fff;
  box-shadow: 1px 1px 36px rgba(0, 0, 0, 0.2);
  -webkit-box-shadow: 1px 1px 36px rgba(0, 0, 0, .2);
  width: 450px;
  height: 564px;
  padding-left: 65px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", 微软雅黑, Arial, sans-serif;
}

.title {
  margin: 50px 0 0;
  color: #212121;
  font-size: 30px;
  font-weight: 300;
  padding-left: 35px;
}

.submit-form {
  margin-top: 45px;
  width: 315px;
}

.sub {
  margin-top: 56px;
  padding-bottom: 20px;
}

.submit-form .pro {
  text-align: center;
  font-size: 12px;
  color: #666;
  letter-spacing: 1px;
  margin-bottom: 46px;
}

.others {
  padding-top: 18px;
  border-top: 1px solid #e5e5e5;
  color: #ccc;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
}

.others .party-link {
  width: 198px;
  margin: 15px auto 0;
}

.login-btn {
  padding-bottom: 6px;
  font-size: 16px;
}

input:-webkit-autofill {
  -webkit-box-shadow: 0 0 100000px #fff inset !important;
  -webkit-text-fill-color: #333;
}

.footer {
  height: 30px;
  width: 100%;
  background-color: rgba(255, 255, 255, 0);
  position: fixed;
  bottom: 0;
  text-align: center;
  margin-bottom: 20px;
}

.pop-text {
  font-size: 12px;
  line-height: 21px;
  text-align: center;
  color: #999;
}

.center {
  width: 1190px;
  max-width: 100%;
  margin: 40px auto auto auto;
  position: relative;
}

.center .main {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  overflow: hidden;
}

.center .img {
  width: 800px;
}

.center .img img {
  width: 100%;
  transform: translateX(-20%);
}
</style>
