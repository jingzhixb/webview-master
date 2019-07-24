package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.iwf.photopicker.PhotoPicker;

public class MainActivity extends Activity
{

    private WebView mWebView;
    public final static String PARAM_URL = "param_url";

    public final static String PARAM_MODE = "param_mode";

    private boolean isStart=false;
    private SonicSession sonicSession;
    List<String> permission = new ArrayList<>();
    private Toast mToast;
    private LinearLayout tv;
    private BroadcastReceiver netWorkStateReceiver;
    private String mMurl="http://www.xingsongzhineng.com/index.php";

    @Override
    protected void onResume()
    {

        super.onResume();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this,"再点击一次退出",Toast.LENGTH_SHORT);
        SonicSessionClientImpl sonicSessionClient = null;

        netWorkStateReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

                    //获得ConnectivityManager对象
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    //获取WIFI连接的信息
                    @SuppressLint("MissingPermission") NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    //获取移动数据连接的信息
                    NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();

                    MainActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            noNew(true);
                        }
                    });

                    } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                noNew(true);
                            }
                        });
                    } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                noNew(true);
                            }
                        });
                    } else {
//                Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(context, "暂无网络连接", Toast.LENGTH_SHORT).show();
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                noNew(false);
                            }
                        });
                    }
                } else {
                    //这里的就不写了，前面有写，大同小异
                    System.out.println("API level 大于21");
                    //获得ConnectivityManager对象
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //获取所有网络连接的信息
                    Network[] networks = connMgr.getAllNetworks();
                    //用于存放网络连接信息
                    StringBuilder sb = new StringBuilder();
                    //通过循环将网络信息逐个取出来
                    for (int i = 0; i < networks.length; i++) {
                        //获取ConnectivityManager对象对应的NetworkInfo对象
                        NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                        sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                    }
                    if (networks.length == 0) {
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                noNew(false);
                            }
                        });
                       // Toast.makeText(context, "暂无网络连接", Toast.LENGTH_SHORT).show();
                    } else {
                        if (NetworkUtils.isNetWorkAvailable(context)&&(NetworkUtils.isMobileDataEnable(context)||NetworkUtils.isWifiDataEnable(context)))
                        {
                            MainActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    noNew(true);
                                }
                            });
                        }else {
                            MainActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    noNew(false);
                                }
                            });
                        }
                    }

                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            permission.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            permission.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permission.isEmpty())
        {
            String[] permissions = permission.toArray(new String[permission.size()]);//将集合转化成数组
            //@onRequestPermissionsResult会接受次函数传的数据
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        // step 2: Create SonicSession
      //  sonicSession = SonicEngine.getInstance().createSession(url,  new SonicSessionConfig.Builder().build());
        setContentView(R.layout.activity_main);
 tv=findViewById(R.id.tv);

        mWebView = findViewById(R.id.activity_main_webview);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (sonicSession != null) {
                    sonicSession.getSessionClient().pageFinish(url);
                }
                //mWebView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
//                tv.setVisibility(View.VISIBLE);
//                mWebView.setVisibility(View.INVISIBLE);
//                view.setVisibility(View.INVISIBLE);
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
//                tv.setVisibility(View.VISIBLE);
//                mWebView.setVisibility(View.INVISIBLE);
//                view.setVisibility(View.INVISIBLE);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (sonicSession != null) {
                    //step 6: Call sessionClient.requestResource when host allow the application
                    // to return the local data .
                    return (WebResourceResponse) sonicSession.getSessionClient().requestResource(url);
                }
                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                mMurl = request.getUrl().toString();
                if ("http://www.xingsongzhineng.com/".equals(request.getUrl().toString())||"http://www.xingsongzhineng.com".equals(request.getUrl().toString()))
                {
                    isStart=true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                mMurl = url;
                if ("http://www.xingsongzhineng.com/".equals(url)||"http://www.xingsongzhineng.com".equals(url))
                {
                    isStart=true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        // Force links and redirects to open in the WebView instead of in a browser
        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebAppInterface javaScriptInterface = new WebAppInterface(this);
        mWebView.addJavascriptInterface(javaScriptInterface, "jsa");
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        mWebView.setWebChromeClient(new WebChromeClient()
        {


            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                handler.proceed();
            }
        });
        // REMOTE RESOURCE
        mWebView.loadUrl("http://www.xingsongzhineng.com/index.php");
         //mWebView.setWebViewClient(new MyWebViewClient());

        // LOCAL RESOURCE
        // mWebView.loadUrl("file:///android_asset/index.html");
    }
    public class WebAppInterface
    {
        Context mContext;

        public WebAppInterface(Context c)
        {
            mContext = c;
        }

        @JavascriptInterface
        public void a()
        {
            permission.clear();
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                permission.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!permission.isEmpty())
            {
                String[] permissions = permission.toArray(new String[permission.size()]);//将集合转化成数组
                //@onRequestPermissionsResult会接受次函数传的数据
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
            }
            if (permission.size()==0)
            {
                getPhoto();
            }
        }








    }
    private void noNew(boolean is)
    {
       if (is)
       {
           tv.setVisibility(View.GONE);
           mWebView.loadUrl(mMurl);
           Timer timer=new Timer();
           timer.schedule(new TimerTask()
           {
               @Override
               public void run()
               {
                  MainActivity.this.runOnUiThread(new Runnable()
                  {
                      @Override
                      public void run()
                      {
                          mWebView.setVisibility(View.VISIBLE);
                      }
                  });
               }
           },1000);
       }else {
           tv.setVisibility(View.VISIBLE);
           mWebView.setVisibility(View.INVISIBLE);
       }
    }
    @Override
    protected void onDestroy()
    {
        if (mWebView!=null)
        {
            mWebView.clearHistory();
            mWebView.clearHistory();
            ((RelativeLayout)mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView=null;
            finish();
        }
        super.onDestroy();
    }

    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed()
    {
        if (isStart)
        {

        }
        if (isDoubleClick())
        {
            if (mWebView!=null)
            {
                mWebView.clearHistory();
                mWebView.clearHistory();
                ((RelativeLayout)mWebView.getParent()).removeView(mWebView);
                mWebView.destroy();
                mWebView=null;
                finish();
            }
        }
        if (mWebView.canGoBack())
        {
            mWebView.goBack();
        } else
        {
            //super.onBackPressed();
        }


    }

    private void getPhoto()
    {
        PhotoPicker.builder()
                .setPhotoCount(1)//可选择图片数量
                .setShowCamera(false)//是否显示拍照按钮
                .setShowGif(false)//是否显示动态图
                .setPreviewEnabled(false)//是否可以预览
                .start(MainActivity.this, PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                if (photos!=null)
                {
                    File file=FilesUtil.getSmallBitmap(MainActivity.this,photos.get(0));
                    mWebView.loadUrl("javascript:photo("+fileToBase64(file)+")");
                }

            }
        }
    }

    private final static int SPACE_TIME = 1500;//2次点击的间隔时间，单位ms
    private static long lastClickTime;
    public  boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick;
        if (currentTime - lastClickTime > SPACE_TIME) {
            isClick = false;
            mToast.show();
        } else {
            isClick = true;
        }

        lastClickTime = currentTime;
        return isClick;
    }
    public static String fileToBase64(File file)
    {
        String base64 = null;
        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return "'"+base64+"'";
    }
}