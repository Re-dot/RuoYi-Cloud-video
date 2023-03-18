<template xmlns="http://www.w3.org/1999/html">
    <div id="VideoPlayer">

      <d-player :options="options"
                @play="play"
                
                ref="player">
      </d-player>
    
    </div>
  </template>
  
  <script>
    import VueDPlayer from "vue-dplayer"
    import "vue-dplayer/dist/vue-dplayer.css"
    import {getFileUrl} from "@/api/uploadFile";
    
    
    export default {
      
      name : "VideoPlayer",
      components : {
        'd-player' : VueDPlayer,
      },
      
      data ()   { 
          const data= this.$route.query.id;
         
      //  let result =  getUrl(json);
      //   console.log("接收到的url:"+result)
      
       // console.log("返回的数据"+getUrl());
        debugger;
        return {
          
          options : {
            
            container: document.getElementById('VideoPlayer'),
            
            video : {
              
              url: data,
              type: 'auto',
              defaultQuality : 0,
            },
            
            theme : '#FADFA3',
            loop : true,
            lang : 'zh-cn',
            preload : 'auto',
            volume : 0.7,
            autoplay : false,
            player : null,
          },
          video_path : null,
          video_list : [],
          btn_status : true,
        }
      },
      mounted () {
        console.log(this.$refs.player)
        this.player = this.$refs.player.dp;
        this.get_video_list();
      },
      methods:{
        // 切换视频按钮操作状态
        show_btn(){
          if (this.video_path == null | this.video_path == ''){
            this.btn_status = true;
          } else {
            this.btn_status = false;  
          }
          console.log('btn 状态：', this.btn_status)
          console.log('video_path ：', this.video_path)
        },
        // 视频列表, 调用后端接口 video/download，获取本地视频列表
        get_video_list () {
          this.video_list = [];
          let url = this.$store.state.host + 'video/download'
          this.$axios.get(url)
            .then((res) => {
              if (res.data.code == 0) {
                console.log(res.data)
                let videos = res.data.data;
                if (videos.length > 0) {
                  videos.forEach((video) => {
                       // 获取视频名称
                    let video_name = video.split('/').pop()
                    // 将组合为 el-select 列表数据，label视频名，value视频路径
                    this.video_list.push({label : video_name, value : video})
                  });
                } else {
                  this.$message.error('文件下载失败. ' + res.data.msg);
                }
                console.log(this.video_list)
              }
            }).catch((err) => {
            this.$message.error(networkErr);
            console.log(err);
          })
        },
        
        // 播放视频
        play () {
          console.log('play callback')
          console.log(this.player)
        },

        
        
        // 切换视频
        switchHandle () {
          if (this.video_path != null) {
            console.debug('切换视频为： ' + this.video_path)
            this.player.switchVideo({
             // 改变当前视频路径为 vue 的静态资源存放路径.
             // vue 2.x ：video/xx.mp4;
             // vue 1.x : static/video/xx.mp4
              url : this.video_path.substr(this.video_path.indexOf('static')),
              type : 'atuo'
            })
            this.options.video.url = this.video_path
          }
        },
      }
    }
  </script>
  
  <style scoped>
    .dplayer {
      /*width: 800px;*/
      margin: 10px auto;
    }
  </style>
