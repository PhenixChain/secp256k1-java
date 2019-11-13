import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws Exception {

        //转出地址|转入地址|转账金额
        String dataStr = "phenix2CkSyo9K5rrdw7aV4gkEAYNfAFjAi7pEgH|phenix2G9nhGgH8J5w8E2cF18DGiqxUq9Lf7p468|1.6";
        //转出地址的私钥
        String privateKeyStr = "lVKXDndWw1yJBuJXYNUxm0IA31dmOVQX";

        //==========sign begin====================================
        byte[] pri = privateKeyStr.getBytes(StandardCharsets.UTF_8);
        byte[] data = DigestUtils.md5(dataStr);

        String hex = randomHexString(42);
        byte[] rand = hex2Bytes(hex);
        BigInteger[] sig = Bouncycastle_Secp256k1.sig(data, pri, rand);
        String sign = sig[0].toString(16)+ sig[1].toString(16);

        //如果长度不够,则前面补0
        if (sign.length() != 128) {
                sign = StringUtils.leftPad(sig[0].toString(16),64,'0') + StringUtils.leftPad(sig[1].toString(16),64,'0');
        }
        System.out.println("sign:"+sign);
        //==========sign end========================================


        //==========verify begin====================================
        //转出地址的公钥
        String pubKeyStr="04884fa0ce7d1310ab87fbd2680a21959db648ff6771248f5e2fecc45179fdbd26039b5684f6cdf5fb4f2f288e12cb982a1b3fc84b112f3cbba1b4e47ac1e04a73";
        boolean verify = Bouncycastle_Secp256k1.verify(data, sig, pubKeyStr);
        if(!verify) {
            System.out.println("verify:" + verify);
        }
        //==========verify end======================================


        //转账测试
        HttpClient httpClient = new HttpClient();
        httpClient.init();
        String res = httpClient.post("http://121.201.80.40:8888/kcoin/transign", dataStr, sign);
        System.out.println(res);
    }


    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] hex2Bytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    public static String randomHexString(int len) {
        try {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
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
