package com.dbproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Loading extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		Handler h = new Handler();
		h.postDelayed(new splashhandler(), 2000);
	}
	
	class splashhandler implements Runnable
	{
		public void run()
		{
			startActivity(new Intent(getApplication(), MainActivity.class));
			Loading.this.finish();
		}
	}
}