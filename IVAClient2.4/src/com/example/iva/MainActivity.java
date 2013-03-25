package com.example.iva;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity  {
  WebView wv;
  //static DefaultHttpClient client1 = new DefaultHttpClient();
  
  
  MyArrayList Listitems;
  String bot_responce;
  EditText et;
  JsonIVA jiva;
    
  String userPath;
  String botPath;
  String clientName;
  
  LauncherProcessor l = new LauncherProcessor(this,true);
  
  //TextToSpeech tts ;
  static String PREFS_NAME="listData"; 
  
  ProgressDialog myPd_ring;
  
/** Called when the activity is first created. */

  @SuppressWarnings("deprecation")
@Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	setContentView(R.layout.activity_main);
	setProgressBarIndeterminateVisibility(false);
    	
	//client = new DefaultHttpClient();
	GlobalObjects.mainActivity = this;
	
		
	Button b = (Button)findViewById(R.id.button1);
	b.setBackgroundResource(R.drawable.button_effect);
   
	Button b2 = (Button)findViewById(R.id.button2);
	b2.setBackgroundResource(R.drawable.button_effect2);
   
	
	
	b.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			
		startVoiceRecognitionActivity();	
			
		}
	});
	
	b2.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			
			showDialog();
		}
	});
	
	//init webview
    wv = (WebView) findViewById(R.id.webView01);
    wv.setBackgroundColor(0);
    wv.setBackgroundResource(color.transparent);
    wv.setWebViewClient(new WebViewClient());
    wv.getSettings().setJavaScriptEnabled(true);
    wv.loadUrl("file:///android_asset/www/index.html");
    
    
    wv.setWebViewClient(new WebViewClient() {

    	   public void onPageFinished(WebView view, String url) {
    	        
    		SharedPreferences settings_restore = getSharedPreferences("SETTINGS_DATA", 0);
   		    
   		    userPath = settings_restore.getString("userpath", "images/photo.jpg");
   		    botPath = settings_restore.getString("botpath", "images/photo.jpg");
   		    clientName = settings_restore.getString("clientname", "clientName");
   		    //Log.i("SETING_DATA", userPath+"  "+botPath+"  "+clientName);
   		    wv.loadUrl("javascript:setUserImage(\""+ userPath +"\")");
   		    wv.loadUrl("javascript:setBotImage(\""+ botPath +"\")");
   		    wv.loadUrl("javascript:setUserName(\""+ clientName +"\")");
    		   
   		 	// Restore preferences
   	         restoreList(null);
    	    }
    	});
			
			
			
    //getKey
    
    if(chechConnection()){
    	new KeyCheck(this).getKey();	
    }else{
    	
    	new AlertDialog.Builder(this)
		.setMessage("CHECK YOUR INTERNET CONNECTION")
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	           finish();             
	        }
	    }).show();
    	
    }   
   
    Listitems = new MyArrayList();
    
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    
    if (activities.size() == 0) 
    {    	  	  	
    	AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("warning");
    	alertDialog.setMessage("Recognizer not present");
    	   
    	alertDialog.show();
    } 
  
    
   
    
   		
		
       
    //tts
	//tts = new TextToSpeech(this, this);
    
   
   
  }
  
  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
   
	  	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_main, menu);
	    return true;
	}
  
  @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
      
		case R.id.menu_save:
         
		  Bundle b = new Bundle();
		 
			
          Intent intent = new Intent(this, PrefsActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivityForResult(intent,123);
          return true;
      
      default:
          return super.onOptionsItemSelected(item);
  
		}

	}
  
  private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
       
	  
	  @Override
	protected void onPreExecute() {
	   super.onPreExecute();
	   setProgressBarIndeterminateVisibility(true);
	}
	
	@Override
    protected String doInBackground(String... urls) {
    			
 	    String response = "";
        
        HttpGet httpGet;
		try {
			httpGet = new HttpGet("http://ivaserver.intelligent-voice-agent.cloudbees.net/MainBotChat?input="+URLEncoder.encode(urls[0], "UTF-8")+"&userId="+GlobalObjects.userId);
		    	
			HttpResponse execute = GlobalObjects.client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while ((s = buffer.readLine()) != null) {
                            response += s;
                       }

             } catch (Exception e) {
                         e.printStackTrace();
                         }
		
          return response;
    }

    @Override
    protected void onPostExecute(String result) {
    	
      GlobalObjects.bot_responce = result;
      bot_responce = result;
      sendToProcess(result);
      setProgressBarIndeterminateVisibility(false); 
    }
  }

  
  public void readWebpage(View view,String reqData) {
	  
	
	 //EditText et = (EditText)findViewById(R.id.editText1);  
	
     DownloadWebPageTask task = new DownloadWebPageTask();
     
     //String request = et.getText().toString();
     String request = reqData;
     
     Listitems.add("client:"+request);
     
     wv.loadUrl("javascript:addTextC(\""+ request +"\")");   
    
    
     task.execute(new String[] { request });
    
    
   
  }
  
  public void sendToProcess(String responce)
  {
	  responce = responce.replaceAll("\\\"", "'");
	  
	  //Log.i("Responce",responce);
	  
	  if(responce.contains("<oob>"))
	  {
		  //LauncherProcessor l = new LauncherProcessor(this,true);
		  l.process(responce);
		  
	  }
		  
	  
	  else{
		  Listitems.add("server:"+responce); 
     	  wv.loadUrl("javascript:addTextS(\""+ responce +"\")");
     	  
     	  if(responce.contains("<a href"))
     	  {
     		  String speak = responce.substring(0, responce.indexOf("<a href"));
     		// tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
     	  }else {
     		   
     		// tts.speak(responce, TextToSpeech.QUEUE_FLUSH, null);
     	  }
	      
	  }
	  
	  
  }
  
  
  
  public void showDialog()
	{
   final EditText input = new EditText(this);
		
		new AlertDialog.Builder(this)
		.setMessage("TYPE PROMPT")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String value = input.getText().toString();
	            GlobalObjects.user_request = value;
	            readWebpage(new View(getBaseContext()), value);
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
		
	}
  
  public void restoreList(MyArrayList lData)
  {
	  SharedPreferences restore = getSharedPreferences(PREFS_NAME, 0);
      String jList = restore.getString("listData", "[]");
      //Log.i("RESTORE STRING", jList);
      
      jiva = new JsonIVA();
      
      if(lData == null)
      lData = jiva.toList(jList);
      
     try{ 
	  Iterator<String> i = lData.iterator();
	  while(i.hasNext())
	  {		  
		  String print_data = i.next();
		  Listitems.add(print_data);
		  //Log.i("RESTORE LISTITEM", print_data); 
		  if(print_data.startsWith("client:"))
		  {
			  print_data = print_data.substring(7);	
			  wv.loadUrl("javascript:addTextC(\""+ print_data +"\")");
		  }
		  else {
			  print_data = print_data.substring(7);	
			  wv.loadUrl("javascript:addTextS(\""+ print_data +"\")");
		  }
	  }
     }catch(Exception e)
     {
    	 System.out.print(e);    	 
     }
	 
     
  }
  
  private void startVoiceRecognitionActivity() {
	  	
	  try
	  {
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
  	      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
  	      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Recognition");
  	      startActivityForResult(intent, 1234);
  	  }catch(Exception e){
  		Toast t = Toast.makeText(getBaseContext(), "RecognizerIntent not present", Toast.LENGTH_LONG);
  		t.show();
  	  }
  	}
  
  @SuppressLint("CommitPrefEdits")
@Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == 1234 && resultCode == RESULT_OK) {
  // Fill the list view with the strings the recognizer thought it could have heard
  ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
  GlobalObjects.user_request = matches.get(0);
  readWebpage(new View(getBaseContext()), matches.get(0));
  }
  
  if(requestCode == 123 && resultCode == RESULT_OK)
  {
	  userPath = data.getStringExtra("userpath");
      botPath = data.getStringExtra("botpath");
      clientName = data.getStringExtra("clientname");
     
      wv.loadUrl("javascript:setUserImage(\""+ userPath +"\")");
      wv.loadUrl("javascript:setBotImage(\""+ botPath +"\")");
      wv.loadUrl("javascript:setUserName(\""+ clientName +"\")");
      
      SharedPreferences settings = getSharedPreferences("SETTINGS_DATA", 0);
	  SharedPreferences.Editor editor = settings.edit();
	  editor.putString("userpath", userPath);
	  editor.putString("botpath", botPath);
	  editor.putString("clientname", clientName);
	  
	  editor.commit();
  }

  super.onActivityResult(requestCode, resultCode, data);
  
  }
  
   @Override
	protected void onStop() {
		
		 super.onStop();
		 
		 jiva = new JsonIVA();
		 String jList = jiva.toJson(Listitems);
		 //Log.i("STORING DATA", jList);		
		
		  SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString("listData", jList);		

	    
	      editor.commit();
	      
	      
	      
	         
	      
	}
  @Override
	public void onConfigurationChanged(Configuration newConfig) {
	  
	    wv.loadUrl("file:///android_asset/www/index.html");
	    //Log.i("CONFIG CHANG", Listitems.get(0));
		super.onConfigurationChanged(newConfig);
		restoreList(Listitems);
		
	}
  
  public boolean chechConnection()
  {
	  ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	  if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED 
	      ||  conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING  ) {


	      //notify user you are online
		  return true;

	  }
	  else if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED 
	      ||  conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
	      //notify user you are not online
		  return false;
	  }
	  
	  return false;
  }   
   
} 