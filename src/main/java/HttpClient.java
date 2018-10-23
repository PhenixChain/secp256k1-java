import com.sun.deploy.util.ArrayUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class HttpClient {

    private static final String CHARSET_UTF_8 = "UTF-8";

    private static final String CONTENT_FORM_TYPE = "application/x-www-form-urlencoded";

    /** 连接池最大连接数 **/
    private int maxConnTotal = 50;

    /** 每个路由最大连接数 **/
    private int maxConnPerRoute = 10;

    /** 超时时间，秒 **/
    private int timeout = 30;

    /** 重发次数 **/
    private int retryCount = 3;

    /** 连接最大空闲时间，超过连接最大空闲时间，连接将被清理；清理空闲连接的间隔默认与最大空闲时间相等 **/
    private int maxIdleTime = 30;

    /** 连接客户端 **/
    private CloseableHttpClient closeableHttpClient;

    /**
     * 构造方法
     */
    public HttpClient() {}

    // private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() throws Exception {
        // 超时时间,单位秒
        int httpReqTimeOut = timeout * 1000;

        /**
         * maxConnPerRoute为每个路由的最大连接数，如:maxConnPerRoute=2时， 请求到www.baidu.com的最大连接数只能为2，即使连接池还有1000个可用连接！
         */
        if (maxConnPerRoute == 0) {
            maxConnPerRoute = maxConnTotal / 2;
        }

        // 注册http套接字工厂和https套接字工厂
        Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE).build();
        // 连接池管理器
        PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager(r);
        pcm.setMaxTotal(maxConnTotal);// 连接池最大连接数
        pcm.setDefaultMaxPerRoute(maxConnPerRoute);// 每个路由最大连接数



        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(httpReqTimeOut)
            .setConnectTimeout(httpReqTimeOut).setSocketTimeout(httpReqTimeOut).build();
        /**
         * 构造closeableHttpClient对象
         */
        closeableHttpClient =
            HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(pcm)
                // .setRetryHandler(retryHandler)
                .evictExpiredConnections().evictIdleConnections(maxIdleTime, TimeUnit.SECONDS).build();

    }

    /**
	 * 根据约定往商户发送报文，报文属性为xmldata
	 * @param url 发送报文地址
	 * @return 请求响应消息字符串
	 * @throws IOException 
	 */
	public String post(String url, String data,String sign) throws IOException {


        Map<String,ContentBody> reqParam = new HashMap<String,ContentBody>();


        // post请求
		HttpPost httppost = new HttpPost(url);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().addBinaryBody("data",data.getBytes("utf-8")).addBinaryBody("sign",Test.hex2Bytes(sign)).setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        HttpEntity reqEntity = multipartEntityBuilder.build();

        httppost.setEntity(reqEntity);

        //发起请求
        String responseText = httpRequest(httppost, CHARSET_UTF_8);
		return responseText;
	}

    public static byte[] toBytes(String str) {
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


    
    /**
     * http 请求
     * 
     * @param request
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     */
    private String httpRequest(HttpUriRequest request, String charsetName)
        throws ClientProtocolException, ParseException, IOException {
        String responseText = null;
        CloseableHttpResponse response = null;
        HttpEntity entitys = null;
        try {
            response = closeableHttpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                entitys = response.getEntity();
                if (entitys != null) {
                    // 将返回实体转换为字符串
                    responseText = EntityUtils.toString(entitys, Charset.forName(charsetName));
                }
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            if (entitys != null) {
                try {
                    // 释放资源可用触发连接放回连接池
                    EntityUtils.consume(entitys);
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return responseText;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }



}
