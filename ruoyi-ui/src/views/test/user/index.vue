<template>
  <div>

    <div id="dplayer"></div>
   
  
</div>
</template>
 

<script>


</script> 


<script >
  import DPlayer from 'dplayer';
  //import "DPlayer.min.css"; //css文件
  export default {
    name:'PlayComponent',
    mounted(){
      const dp = new DPlayer({
    container: document.getElementById('dplayer'),
    video: {
      type:'',
      url:'https://video-storage-test-616.oss-cn-beijing.aliyuncs.com/1614949650_oDrVrtV52mf1Q3X1r_Source.mp4'
        //url: require('C:/Users/Retasu/Desktop/824147683-1-208.mp4'),
    },
})
    }
  }
 

</script>
 
<style >
.el-upload__tip {
  line-height: 1.2;
}
</style>
