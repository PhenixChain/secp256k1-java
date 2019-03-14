import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Test {
    public static void main(String[] args) throws Exception {

        //转出地址对应的私钥
        String privateKeyStr="lVKXDndWw1yJBuJXYNUxm0IA31dmOVQX";

        //转出地址对应的公钥
        //String pubKeyStr="04884fa0ce7d1310ab87fbd2680a21959db648ff6771248f5e2fecc45179fdbd26039b5684f6cdf5fb4f2f288e12cb982a1b3fc84b112f3cbba1b4e47ac1e04a73";

        //转出地址|转入地址|转账金额
        String dataStr="phenix2CkSyo9K5rrdw7aV4gkEAYNfAFjAi7pEgH|phenix2G9nhGgH8J5w8E2cF18DGiqxUq9Lf7p468|1.6";


        //==========sign begin====================================
        byte[] pri = privateKeyStr.getBytes();
        byte[] data = DigestUtils.md5(dataStr);

        //此处建议随机(注意是16进制)
        //byte[] rand 	=  	hex2Bytes("000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        String hex = randomHexString(42);
        byte[] rand 	=  	hex2Bytes(hex);

        BigInteger[] sig = Bouncycastle_Secp256k1.sig(data, pri, rand);
        String sign= sig[0].toString(16) + sig[1].toString(16);

        System.out.println("sign:"+sign);
        //==========sign end========================================


        //==========verify begin====================================
        //boolean verify = Bouncycastle_Secp256k1.verify(data, sig, pubKeyStr);
        //System.out.println("verify:"+verify);
        //==========verify end======================================


        //转账测试
        HttpClient httpClient = new HttpClient();
        httpClient.init();
        String res = httpClient.post("http://121.201.80.40:8888/kcoin/transign", dataStr, sign);
        System.out.println(res);


        /*================获取转账记录流程==================================*/
        /*
        *	以测试环境(http://121.201.80.40:8888)为例，其中(kcoin为开元通宝 wallet为8PHC nzc为牛樟链)
        *
        *	一、获取链的最新区块高度
        *	请求 http://121.201.80.40:8888/kcoin/getlatestblock
        *	返回 {
        *		"code": 1,
        *		"info": {
        *			"Hash": "ba9e37c5670c09f099c4a9175a5497bcd5db9b6c6593402a51cd93bc73abfcd9",
        *			"Height": "95096", //最新区块高度
        *			"PreHash": "99b2d4cc2b2f6404f042010ddb732f4cc2ae4cb58bf770f4484c3fac91e872cf"
        *		}
        *	}
        *
        *
        *	二、扫描区块(获取区块里面的转账记录)
        *   初次上线的时候应该记录当前链的最新区块高度，例如当时的区块高度为94075，这次应递增区块高度去扫描直到最新高度
        *   (备注：没必要从区块高度1开始扫描，已经扫描过的区块也没必要再次去扫描)
        *
        *	请求 http://121.201.80.40:8888/kcoin/getblocktranslist?num=94076
        *	... 94077
        *	... 94078
        *	...
        *	请求http://121.201.80.40:8888/kcoin/getblocktranslist?num=95096
        *   例如 返回
        *	{
        *		"code": 1,
        *			"info": [
        *				{
        *				"from_type": 1,
        *				"from_address": "phenix2CkSyo9K5rrdw7aV4gkEAYNfAFjAi7pEgH", //转出地址
        *				"to_address": "phenix2G9nhGgH8J5w8E2cF18DGiqxUq9Lf7p468",   //转入地址
        *				"trans_time": 1536119975, //转账时间
        *				"number": 1.6, //转账金额
        *				"tx_id": "8e16f30d9fa75abb79d21b4d2e67b8460946dc3aa64d7e246fb05fe60c8b04fa",
        *				"block": 94076,
        *				"status": 0, //交易状态（0为有效）
        *				"direct": ""
        *				}
        *			]
        *	}
        *
        *
        */
    }



    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] hex2Bytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    public static String randomHexString(int len)  {
        try {
            StringBuffer result = new StringBuffer();
            for(int i=0;i<len;i++) {
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return null;

    }
}
