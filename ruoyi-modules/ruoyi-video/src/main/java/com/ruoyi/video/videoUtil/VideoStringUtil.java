package com.ruoyi.video.videoUtil;



import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
@Service
public class VideoStringUtil {

    /**
     * random 基数
     */
    private final static int random_base = 10;
    /**
     * 产生指定长度的数字值随机数
     *
     * @param length
     * 需要产生的长度
     * @return
     */

    private final  static int random_length = 25;

    public static String getrandomMd5() {
        String s = String.valueOf(System.currentTimeMillis() + new Random().nextInt());
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            byte[] digest = messageDigest.digest(s.getBytes());


            return Base64.getEncoder().encodeToString(digest);//不使用Base64的话会出现乱码。因为new String默认编码可能不能完全包含上面这个字节数组
            //base64将每三个字节转成4个字节，这样高位就使用00补齐，这样最大也就是63，最小为0。一共只有64种情况，就不会出现乱码了。

            /**
             * Java8之前的做法
             */
//            BASE64Encoder encoder = new BASE64Encoder();
//            return encoder.encode(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }


    }

    //1.將JSONObject對象轉換為HashMap<String,String>
    public HashMap<String, String> JsonObjectToHashMap(JSONObject jsonObj){
        HashMap<String, String> data = new HashMap<String, String>();
        Iterator it = jsonObj.keySet().iterator();
        while(it.hasNext()){
            String key = String.valueOf(it.next().toString());
            String value = (String)jsonObj.get(key).toString();
            data.put(key, value);
        }
        System.out.println(data);
        return data;
    }

    public String getJSONObjectVal(String key,JSONObject obj)
    {
        String result = "";
        if(obj.containsKey(key))
        {
            if(!StringUtils.isEmpty(obj.getString(key)))
            {
                result = obj.getString(key);
            }
        }
        return result;
    }



    //可以指定字符串的某个位置是什么范围的值
    public  String getRandomString(){
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<random_length;i++){
            int number=random.nextInt(3);
            long result=0;
            switch(number){
                case 0:
                    result=Math.round(Math.random()*25+65);
                    sb.append(String.valueOf((char)result));
                    break;
                case 1:
                    result=Math.round(Math.random()*25+97);
                    sb.append(String.valueOf((char)result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
            }
        }
        return sb.toString();
    }
}
