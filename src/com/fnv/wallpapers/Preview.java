package com.fnv.wallpapers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Preview extends Wallpaper {
	
	String link = "";
	ProgressDialog mProgressDialog;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		ImageView p = (ImageView) findViewById(R.id.preView);
		Button d = (Button) findViewById(R.id.set_button);
		Button s = (Button) findViewById(R.id.save_button);
		
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			link = extras.getString("wp");
			UrlImageViewHelper.setUrlDrawable(p, link);
		} else {
			Intent home = new Intent(Preview.this, Wallpaper.class);
			Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
			startActivity(home);
			finish();
		}
		
        //Check if the FNV folder exists already
        Log.i(tag, "Check for FNV folder");
        File f = new File(Environment.getExternalStorageDirectory() + "/FNV/");
        if (f.isDirectory() && f.exists()) {
        	Log.i(tag, "FNV folder exists");
        } else {
        	Log.i(tag, "FNV folder does not exist. Creating...");
        	f.mkdirs();
        }
		
		d.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new DownloadFileAsync().execute(link);
			}
		});
		
		s.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new StoreFileAsync().execute(link);
			}
		});	
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    //FEATURE_NO_TITLE crashes because setContentView() has already been called
	    //I'll look into fixes later
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	setContentView(R.layout.activity_preview_land);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	ImageView v = (ImageView) findViewById(R.id.imageView1);
	    	UrlImageViewHelper.setUrlDrawable(v, link);
	    }
	    
	    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
	    	Intent restart = new Intent(Preview.this, Preview.class);
	    	restart.putExtra("wp", link);
	    	startActivity(restart);
	    	finish();
	    }
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview, menu);
        return true;
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
    			finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getResources().getString(R.string.downloading));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
        }
    }
    
    //Downloads the wallpaper in Async with a dialog
    //This one just sets the wallpaper
class DownloadFileAsync extends AsyncTask<String, String, String> {
    	
    	@SuppressWarnings("deprecation")
		@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		showDialog(DIALOG_DOWNLOAD_PROGRESS);
    	}
    	
    	@Override
    	protected String doInBackground(String... aurl) {
    		int count;
    		
    		try {
    			
        		mProgressDialog.setProgress(0);
    			
    			URL url = new URL (aurl[0]);
    			URLConnection conexion = url.openConnection();
    			conexion.connect();
    			
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    			Date now = new Date();
    			fileName = formatter.format(now) + ".jpg";

    			
    			int lengthOfFile = conexion.getContentLength();
    			Log.d("ANDRO_ASYNC", "Lenght of file: " + lengthOfFile);
    			
    			InputStream input = new BufferedInputStream(url.openStream());
    			OutputStream output = new FileOutputStream(dlDir + fileName);
    			fileDest  = dlDir + fileName;
    			
    			byte data[] = new byte[1024];
    			
    			long total = 0;
    			
    			while ((count = input.read(data)) != -1) {
    				total += count;
    				publishProgress(""+(int)((total*100)/lengthOfFile));
    				output.write(data, 0, count);
    			}
    			
    			output.flush();
    			output.close();
    			input.close();
    		} catch (Exception e) {}
			
    		return null;    		
    	}
    	
    	protected void onProgressUpdate(String... progress) {
   		 Log.d("ANDRO_ASYNC",progress[0]);
   		 mProgressDialog.setProgress(Integer.parseInt(progress[0]));
   	}
    	
    	@SuppressWarnings("deprecation")
		@Override
    	protected void onPostExecute(String unused) {
    		dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
    		mProgressDialog.setProgress(0);
    		WallpaperManager wallpaperManager = WallpaperManager.getInstance(Preview.this);
    		getResources().getDrawable(R.drawable.ic_launcher);
    		Bitmap wallpaper = BitmapFactory.decodeFile(fileDest);
    		try {
				wallpaperManager.setBitmap(wallpaper);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Toast.makeText(getApplicationContext(), R.string.set, Toast.LENGTH_LONG).show();
    		File file = new File(dlDir + fileName);
    		if (file.exists() == true) {
    			file.delete();
    			
    		}
    	}   	
	}

	//Downloads the wallpaper in Async with a dialog
	//This one saves the wallpaper
class StoreFileAsync extends AsyncTask<String, String, String> {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		showDialog(DIALOG_DOWNLOAD_PROGRESS);
	}
	
	@Override
	protected String doInBackground(String... aurl) {
		int count;
		
		try {
			
			URL url = new URL (aurl[0]);
			URLConnection conexion = url.openConnection();
			conexion.connect();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			Date now = new Date();
			fileName = formatter.format(now) + ext;

			
			int lengthOfFile = conexion.getContentLength();
			Log.d("ANDRO_ASYNC", "Lenght of file: " + lengthOfFile);
			
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(svDir + fileName);
			fileDest  = svDir + fileName;
			
			byte data[] = new byte[1024];
			
			long total = 0;
			
			while ((count = input.read(data)) != -1) {
				total += count;
				publishProgress(""+(int)((total*100)/lengthOfFile));
				output.write(data, 0, count);
			}
			
			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {}
		
		return null;    		
	}
	
	protected void onProgressUpdate(String... progress) {
		 Log.d("ANDRO_ASYNC",progress[0]);
		 mProgressDialog.setProgress(Integer.parseInt(progress[0]));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostExecute(String unused) {
		dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.saved) + " " + svDir + fileName, Toast.LENGTH_LONG).show();
	}   	
}
}
