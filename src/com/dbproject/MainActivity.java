package com.dbproject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

public class MainActivity extends NMapActivity implements NMapView.OnMapStateChangeSimpleListener, OnCalloutOverlayListener

{
	private static final String API_KEY = "38f62692f5aba3afe453d782ade3fdf5";
	NMapView mapView;
	NMapController mapController;
	private NMapViewerResourceProvider mMapResourceProvider;
	private NMapOverlayManager mOverlayManager;
	LinearLayout lin;
	LinearLayout lin2;
	LinearLayout lin3;
	private boolean hasOverlay = false;
//	private int order = 0;
	
	ImageView mAnimTarget1;
	ImageView mAnimTarget2;
	ImageView mAnimTarget3;
	ImageView mAnimTarget4;
	AnimationSet ani1 = null;
	AnimationSet ani2 = null;
	AnimationSet ani3 = null;
	AnimationSet ani4 = null;
	
	// ���� �κ�
	ProgressDialog mProgress;
	
	int numElement;
	int numName;
	int numNode;
	String[] xmlText = new String[50];
	String[] nameText = new String[50];
	
	Node[] mainNode = new Node[100];
	String[] textElement = new String[500];
	String[] nameElement = new String[500];
	int xmlNum;
	int textNum;
	
	// DB �߰�
	WordDBHelper mHelper; // url �����ͺ��̽�
	SQLiteDatabase db;
	WordDBHelper2 mHelper2; // ��� �����ͺ��̽�
	SQLiteDatabase db2;
	WordDBHelper3 mHelper3; // �ڱ���ġ �����ͺ��̽�
	SQLiteDatabase db3;
	WordDBHelper4 mHelper4; // ��� �����ͺ��̽�
	SQLiteDatabase db4;	
	
	//������ ����
	private String mBestProvider;
	private LocationManager mLocationManager;
	//������ ��

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		mapView = new NMapView(this);
		mapView.setApiKey(API_KEY);
		mapController = mapView.getMapController();
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true, null);
		mapView.executeNaverMap();

		lin = (LinearLayout)findViewById(R.id.linear);
		lin.addView(mapView);
		mapView.setOnMapStateChangeListener(this);
		lin2 = (LinearLayout)findViewById(R.id.info);
		lin2.setVisibility(lin2.GONE);
		lin3 = (LinearLayout)findViewById(R.id.weather);
		lin3.setVisibility(lin3.GONE);
		
		mAnimTarget1 = (ImageView)findViewById(R.id.train1);
		mAnimTarget2 = (ImageView)findViewById(R.id.train2);
		mAnimTarget3 = (ImageView)findViewById(R.id.train3);
		mAnimTarget4 = (ImageView)findViewById(R.id.train4);
		
		// DB �߰�
		mHelper = new WordDBHelper(this); // url �����ͺ��̽�
		db = mHelper.getWritableDatabase();
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1135056000', '�����');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1168054500', '������');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1111065000', '���α�');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1165060000', '���ʱ�');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=2653064500', '�λ�');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=5013025300', '����');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=4613067000', '����');");
		db.execSQL("INSERT INTO dic VALUES (null, 'http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=3011062000', '����');");
		mHelper.close();
		
		mHelper2 = new WordDBHelper2(this); // ��� �����ͺ��̽�
		db2 = mHelper2.getWritableDatabase();
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060603','37.623136','����','ȫ��ġ���ǿ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.05909','37.621512','����','���������ǿ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.059539','37.62261','����','����������а��ǿ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.059125','37.621534','�౹','����౹');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.061151','37.622946','�౹','�����౹');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0594245','37.6213056','������','�����Ϸ��� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.058377','37.620237','������','�����Ϸ��� �����������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0575497','37.6190308','������','CU ���豤�����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.061093','37.62126','������','�����Ϸ��� ������Ĺ���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0572862','37.6195175','������','GS25 ��������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0606953','37.6228359','������','GS25 ���輺�Ͽ���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0575625','37.6189571','ī��','Ŀ�Ǵ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.057608','37.619292','ī��','ī�亣����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0574129','37.6194533','ī��','��������� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0582946','37.6202011','ī��','VISTA');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.061316','37.6212876','ī��','ī������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.059033','37.62132','ī��','�ͽ��غ���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060602','37.622951','ī��','ī�亣�� ���Ͽ���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0607731','37.6232102','ī��','��Ÿ���� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0605192','37.623988','ī��','�����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0608893','37.6197779','������б�','�߾ӵ�����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060936','37.6192568','������б�','������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060713','37.6202198','������б�','��õ����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.059703','37.6197084','������б�','�񸶰�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0594073','37.6204026','������б�','ȭ����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0590987','37.6188625','������б�','���ǰ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0583788','37.6194069','������б�','������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0576709','37.6198454','������б�','��ȭ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0576125','37.6195335','������б�','������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0572892','37.6200291','������б�','������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0571964','37.6202781','������б�','���̽���ũ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0570477','37.6206158','������б�','�ѿ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0575924','37.6202296','������б�','��õ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0549837','37.6203824','������б�','������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0564615','37.6221555','������б�','���ͳ׼ų��Ͽ콺');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0608','37.6239321','������б�','�̵������������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.057165','37.619612','������','�Ѽܵ��ö� ��������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0585736','37.6205767','������','������� ����������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060726','37.622938','������','�� ����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0610064','37.62365','������','ġŲŬ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0612013','37.6228753','������','������������뱹');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0600679','37.6232833','������','��ū�Ҹż��뱹');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.059631','37.622172','������','���� ����뿪��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0590239','37.6212013','������','��Ƶ��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.058572','37.620521','������','���õ�� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0585566','37.6205704','������','���� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.058285','37.620092','������','������ �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0582231','37.6200769','������','��¥��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0581788','37.6200564','������','21����н� �����̴��ж���а�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0576143','37.6190682','������','����Ұ���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.057338','37.618586','������','����������� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.06158','37.6202089','������','�̻��佺Ʈ �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0604476','37.6217758','������','ġŲ�ŴϾ� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0610669','37.6226429','������','�ƺ��������� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0609489','37.6229015','������','��κδ��ö�Ǳ��� ����뿪��');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.058511','37.62043','������','SSAM');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.058358','37.620201','������','�������ö� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0597912','37.6223681','������','ġŲ���� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0596984','37.6228172','������','���Ͽ�����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060278','37.6234558','������','���̺�붱�Ż�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0608813','37.6235076','������','����̼յκ�');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0606831','37.6212222','����','�������� �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0607361','37.6224095','����','������ �������');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.060717','37.622782','����','������ ����');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0605641','37.6228592','����','���÷���');");
		db2.execSQL("INSERT INTO dic2 VALUES (null, '127.0609256','37.622932','����','�긯��');");
		mHelper2.close();

		mHelper3 = new WordDBHelper3(this); // �ڱ���ġ �����ͺ��̽�
		mHelper4 = new WordDBHelper4(this); // ��� �����ͺ��̽�
		
		//������ ����
		MyLocation();
		//������ ��
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		NMapPathDataOverlay pathDataOverlay = null;
		NMapPathData pathData = null;
		Cursor cursor;
		Cursor cursor2;
		String s0, s1, s2;
		double d1, d2;
		int cnt;
		String Result;
		switch(item.getItemId())
		{
		case R.id.menu11:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
				mOverlayManager.clearOverlays();
			mapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
			MyLocation();
			return true;
		case R.id.menu12:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			mapController.setMapViewMode(NMapView.VIEW_MODE_HYBRID);
			return true;
		case R.id.menu13:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			mapController.setMapViewTrafficMode(!mapController.getMapViewTrafficMode());
			return true;
		case R.id.menu14:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			mapController.setMapViewBicycleMode(!mapController.getMapViewBicycleMode());
			return true;
		case R.id.menu21:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "����� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "����� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '�����'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread1 = new DownThread(Result);
			thread1.start();
			return true;
		case R.id.menu22:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "������ ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "������ ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '������'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread2 = new DownThread(Result);
			thread2.start();
			return true;
		case R.id.menu23:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "���α� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "���α� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '���α�'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread3 = new DownThread(Result);
			thread3.start();
			return true;
		case R.id.menu24:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "���ʱ� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "���ʱ� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '���ʱ�'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread4 = new DownThread(Result);
			thread4.start();
			return true;
		case R.id.menu251:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "�λ� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "�λ� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '�λ�'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread5 = new DownThread(Result);
			thread5.start();
			return true;
		case R.id.menu252:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "���� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "���� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '����'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread6 = new DownThread(Result);
			thread6.start();
			return true;
		case R.id.menu253:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "���� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "���� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '����'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread7 = new DownThread(Result);
			thread7.start();
			return true;
		case R.id.menu254:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin3.VISIBLE);
			Toast.makeText(MainActivity.this, "���� ���� �����Դϴ�", Toast.LENGTH_LONG).show();
			mProgress = ProgressDialog.show(this, "���� ���� ����", "�ε���...");
			db = mHelper.getReadableDatabase();
			cursor = db.rawQuery("SELECT url, location FROM dic WHERE location = '����'", null);
			Result = "";
			while(cursor.moveToNext())
				Result = cursor.getString(0);
			cursor.close();
			mHelper.close();
			DownThread thread8 = new DownThread(Result);
			thread8.start();
			return true;			
		case R.id.menu3:
			lin.setVisibility(lin.GONE);
			lin2.setVisibility(lin2.VISIBLE);
			lin3.setVisibility(lin3.GONE);
			startAnimation();
			return true;
		case R.id.menu41:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("������б�");
			hasOverlay = true;
			return true;
		case R.id.menu42:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("����");
			hasOverlay = true;
			return true;
		case R.id.menu43:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("����");
			hasOverlay = true;
			return true;
		case R.id.menu44:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("�౹");
			hasOverlay = true;
			return true;
		case R.id.menu45:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("������");
			hasOverlay = true;
			return true;
		case R.id.menu46:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("ī��");
			hasOverlay = true;
			return true;
		case R.id.menu47:
			lin.setVisibility(lin.VISIBLE);
			lin2.setVisibility(lin2.GONE);
			lin3.setVisibility(lin.GONE);
			if(hasOverlay)
			{
				mOverlayManager.clearOverlays();
				MyLocation();
			}
			CreateOverlay("������");
			hasOverlay = true;
			return true;
		case R.id.menu5:
			cnt = 0;
			mOverlayManager = new NMapOverlayManager(this, mapView, mMapResourceProvider);
			db4 = mHelper4.getReadableDatabase();
			
			cursor = db4.rawQuery("SELECT b, c FROM dic7 ORDER BY a", null);
			while(cursor.moveToNext()) cnt++;
			pathData = new NMapPathData(cnt + 11);
			pathData.initPathData();
			
			pathData.addPathPoint(127.0607731, 37.6232102, NMapPathLineStyle.TYPE_SOLID);
			pathData.addPathPoint(127.0596984, 37.6228172, 0);
			pathData.addPathPoint(127.059631, 37.622172, 0);
			pathData.addPathPoint(127.0604476, 37.6217758, 0);
			pathData.addPathPoint(127.0590239, 37.6212013, 0);
			pathData.addPathPoint(127.0585736, 37.6205767, 0);
			pathData.addPathPoint(127.0581788, 37.6200564, 0);
			pathData.addPathPoint(127.0583788, 37.6194069, 0);
			pathData.addPathPoint(127.0590987, 37.6188625, 0);
			pathData.addPathPoint(127.059703, 37.6197084, 0);
			pathData.addPathPoint(127.060936, 37.6192568, 0);


			cursor = db4.rawQuery("SELECT b, c FROM dic7 ORDER BY a", null);
			while(cursor.moveToNext())
			{
				s1 = cursor.getString(0);
				s2 = cursor.getString(1);
				
				d1 = Double.valueOf(s1).doubleValue();
				d2 = Double.valueOf(s2).doubleValue();

				pathData.addPathPoint(d2, d1, 0);
			} 	
			cursor.close();
			
			pathData.endPathData();
			pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
			pathDataOverlay.showAllPathData(0);
			mHelper4.close();

			return true;
		case R.id.menu6:
			db = mHelper.getWritableDatabase();
			db.execSQL("DELETE FROM dic;");
			mHelper.close();
			db2 = mHelper2.getWritableDatabase();
			db2.execSQL("DELETE FROM dic2;");
			mHelper2.close();
			db3 = mHelper3.getWritableDatabase();
			db3.execSQL("DELETE FROM dic3;");
			mHelper3.close();
			db4 = mHelper4.getWritableDatabase();
			db4.execSQL("DELETE FROM dic7;");
			mHelper4.close();
			Toast.makeText(MainActivity.this, "���ø����̼ǿ� ���� ������ �����Ǿ����ϴ�.", Toast.LENGTH_LONG).show();
			finish();
		}
		return false;
	}
	
	public void onMapInitHandler(NMapView nMapView, NMapError nMapError)
	{
		if(nMapError == null)
		{
			NGeoPoint point = new NGeoPoint(129.97362, 37.57570);
			mapController.setMapCenter(point, 20);
		}
	}
	
	private class ExampleThread extends Thread
	{
		public void run()
		{
			mHandler.sendEmptyMessage(1);
			mHandler.sendEmptyMessage(2);
			mHandler.sendEmptyMessage(3);
			mHandler.sendEmptyMessage(4);
		}
	}
	
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if(msg.what == 1)
				mAnimTarget1.startAnimation(ani1);
			if(msg.what == 2)
				mAnimTarget2.startAnimation(ani2);
			if(msg.what == 3)
				mAnimTarget3.startAnimation(ani3);
			if(msg.what == 4)
				mAnimTarget4.startAnimation(ani4);
		}
	};
	
	public void startAnimation()
	{
		int time[] = new int[20];
		int in;
		String s1[] = new String[20];
		String s2[] = new String[20];
		String s3[] = new String[20];
		boolean a1, a2, a3, a4;
		a1 = false; a2 = false; a3 = false; a4 = false;
		NGeoPoint point = null;
		NGeoPoint fromPoint;
		NGeoPoint toPoint;
		double tempLongi, tempLati;
		Cursor cursor;
		db3 = mHelper3.getReadableDatabase();
		in = 0;
		cursor = db3.rawQuery("SELECT latitude, longitude, type, name FROM dic3 ORDER BY latitude desc", null);
		while(cursor.moveToNext())
		{
			s1[in] = cursor.getString(1);
			s2[in] = cursor.getString(2);
			s3[in] = cursor.getString(3);
			
			tempLongi = Double.valueOf(s3[in]).doubleValue();
			tempLati = Double.valueOf(s2[in]).doubleValue();
			fromPoint = new NGeoPoint(tempLongi, tempLati);
			in++;
			if(in == 6)
				break;
			if(cursor.moveToNext()) {
				s1[in] = cursor.getString(1);
				s2[in] = cursor.getString(2);
				s3[in] = cursor.getString(3);
				tempLongi = Double.valueOf(s3[in]).doubleValue();
				tempLati = Double.valueOf(s2[in]).doubleValue();
				toPoint = new NGeoPoint(tempLongi, tempLati);
				
				time[in] = 100 * (int) point.getDistance(fromPoint, toPoint);

				cursor.moveToPrevious();
			}

		}
		cursor.close();
		mHelper3.close();
		TextView t1 = (TextView)findViewById(R.id.text1);
		TextView t2 = (TextView)findViewById(R.id.text2);
		TextView t3 = (TextView)findViewById(R.id.text3);
		TextView t4 = (TextView)findViewById(R.id.text4);
		TextView t5 = (TextView)findViewById(R.id.text5);
		TextView t6 = (TextView)findViewById(R.id.text6);
		TextView t7 = (TextView)findViewById(R.id.text7);
		TextView t8 = (TextView)findViewById(R.id.text8);
		TextView ta = (TextView)findViewById(R.id.texta);
		TextView tb = (TextView)findViewById(R.id.textb);
		TextView tc = (TextView)findViewById(R.id.textc);
		TextView td = (TextView)findViewById(R.id.textd);
		String b1 = String.valueOf(time[1]/100);
		String b2 = String.valueOf(time[2]/100);
		String b3 = String.valueOf(time[3]/100);
		String b4 = String.valueOf(time[4]/100);
		ta.setText("<" + b1 + "Meters Away>");
		tb.setText("<" + b2 + "Meters Away>");
		tc.setText("<" + b3 + "Meters Away>");
		td.setText("<" + b4 + "Meters Away>");

		if(in == 0)
		{
			t1.setVisibility(t1.INVISIBLE);
			t2.setVisibility(t2.INVISIBLE);
			t3.setVisibility(t3.INVISIBLE);
			t4.setVisibility(t4.INVISIBLE);
			t5.setVisibility(t5.INVISIBLE);
			t6.setVisibility(t6.INVISIBLE);
			t7.setVisibility(t7.INVISIBLE);
			t8.setVisibility(t8.INVISIBLE);
			ta.setVisibility(ta.INVISIBLE);
			tb.setVisibility(tb.INVISIBLE);
			tc.setVisibility(tc.INVISIBLE);
			td.setVisibility(td.INVISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.INVISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.INVISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.INVISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.INVISIBLE);
			a1 = false; a2 = false; a3 = false; a4 = false;
		}
		if(in == 1)
		{
			t1.setVisibility(t1.INVISIBLE);
			t2.setVisibility(t2.INVISIBLE);
			t3.setVisibility(t3.INVISIBLE);
			t4.setVisibility(t4.INVISIBLE);
			t5.setVisibility(t5.INVISIBLE);
			t6.setVisibility(t6.INVISIBLE);
			t7.setVisibility(t7.INVISIBLE);
			t8.setVisibility(t8.INVISIBLE);
			ta.setVisibility(ta.INVISIBLE);
			tb.setVisibility(tb.INVISIBLE);
			tc.setVisibility(tc.INVISIBLE);
			td.setVisibility(td.INVISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.INVISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.INVISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.INVISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.INVISIBLE);
			a1 = false; a2 = false; a3 = false; a4 = false;
		}
		if(in == 2)
		{
			t1.setVisibility(t1.VISIBLE);
			t2.setVisibility(t2.VISIBLE);
			t3.setVisibility(t3.INVISIBLE);
			t4.setVisibility(t4.INVISIBLE);
			t5.setVisibility(t5.INVISIBLE);
			t6.setVisibility(t6.INVISIBLE);
			t7.setVisibility(t7.INVISIBLE);
			t8.setVisibility(t8.INVISIBLE);
			ta.setVisibility(ta.VISIBLE);
			tb.setVisibility(tb.INVISIBLE);
			tc.setVisibility(tc.INVISIBLE);
			td.setVisibility(td.INVISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.VISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.INVISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.INVISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.INVISIBLE);
			a1 = true; a2 = false; a3 = false; a4 = false;
		}
		if(in == 3)
		{
			t1.setVisibility(t1.VISIBLE);
			t2.setVisibility(t2.VISIBLE);
			t3.setVisibility(t3.VISIBLE);
			t4.setVisibility(t4.VISIBLE);
			t5.setVisibility(t5.INVISIBLE);
			t6.setVisibility(t6.INVISIBLE);
			t7.setVisibility(t7.INVISIBLE);
			t8.setVisibility(t8.INVISIBLE);
			ta.setVisibility(ta.VISIBLE);
			tb.setVisibility(tb.VISIBLE);
			tc.setVisibility(tc.INVISIBLE);
			td.setVisibility(td.INVISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.VISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.VISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.INVISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.INVISIBLE);
			a1 = true; a2 = true; a3 = false; a4 = false;
		}
		if(in == 4)
		{
			t1.setVisibility(t1.VISIBLE);
			t2.setVisibility(t2.VISIBLE);
			t3.setVisibility(t3.VISIBLE);
			t4.setVisibility(t4.VISIBLE);
			t5.setVisibility(t5.VISIBLE);
			t6.setVisibility(t6.VISIBLE);
			t7.setVisibility(t7.INVISIBLE);
			t8.setVisibility(t8.INVISIBLE);
			ta.setVisibility(ta.VISIBLE);
			tb.setVisibility(tb.VISIBLE);
			tc.setVisibility(tc.VISIBLE);
			td.setVisibility(td.INVISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.VISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.VISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.VISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.INVISIBLE);
			a1 = true; a2 = true; a3 = true; a4 = false;
		}
		if(in >= 5)
		{
			t1.setVisibility(t1.VISIBLE);
			t2.setVisibility(t2.VISIBLE);
			t3.setVisibility(t3.VISIBLE);
			t4.setVisibility(t4.VISIBLE);
			t5.setVisibility(t5.VISIBLE);
			t6.setVisibility(t6.VISIBLE);
			t7.setVisibility(t7.VISIBLE);
			t8.setVisibility(t8.VISIBLE);
			ta.setVisibility(ta.VISIBLE);
			tb.setVisibility(tb.VISIBLE);
			tc.setVisibility(tc.VISIBLE);
			td.setVisibility(td.VISIBLE);
			mAnimTarget1.setVisibility(mAnimTarget1.VISIBLE);
			mAnimTarget2.setVisibility(mAnimTarget2.VISIBLE);
			mAnimTarget3.setVisibility(mAnimTarget3.VISIBLE);
			mAnimTarget4.setVisibility(mAnimTarget4.VISIBLE);
			a1 = true; a2 = true; a3 = true; a4 = true;
		}

		t1.setText(s1[1]);
		t2.setText(s1[0]);
		t3.setText(s1[2]);
		t4.setText(s1[1]);
		t5.setText(s1[3]);
		t6.setText(s1[2]);
		t7.setText(s1[4]);
		t8.setText(s1[3]);
		
//		String temp = in + "";
//		Toast.makeText(MainActivity.this, temp + , Toast.LENGTH_SHORT).show();

		ExampleThread exampleThread = new ExampleThread();
		
		ani1 = new AnimationSet(true);
		ani1.setInterpolator(new LinearInterpolator());
		TranslateAnimation trans1 = new TranslateAnimation(Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 650,
				Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 10);
		trans1.setDuration(time[1]);
		ani1.addAnimation(trans1);
		if(a1 == true)
			mAnimTarget1.startAnimation(ani1);
		
		ani2 = new AnimationSet(true);
		ani2.setInterpolator(new LinearInterpolator());
		TranslateAnimation trans2 = new TranslateAnimation(Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 650,
				Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 10);
		trans2.setDuration(time[2]);
		ani2.addAnimation(trans2);
		if(a2 == true)
			mAnimTarget2.startAnimation(ani2);
		
		ani3 = new AnimationSet(true);
		ani3.setInterpolator(new LinearInterpolator());
		TranslateAnimation trans3 = new TranslateAnimation(Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 650,
				Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 10);
		trans3.setDuration(time[3]);
		ani3.addAnimation(trans3);
		if(a3 == true)
			mAnimTarget3.startAnimation(ani3);
		
		ani4 = new AnimationSet(true);
		ani4.setInterpolator(new LinearInterpolator());
		TranslateAnimation trans4 = new TranslateAnimation(Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 650,
				Animation.ABSOLUTE, 10,Animation.ABSOLUTE, 10);
		trans4.setDuration(time[4]);
		ani4.addAnimation(trans4);
		if(a4 == true)
			mAnimTarget4.startAnimation(ani4);
		
		exampleThread.start();
	}
	
	@Override
	public void didChangeMapCenter(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChangeMapLevel(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChangeZoomLevelConstraint(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didFinishAnimation(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class DownThread extends Thread
	{
		String mAddr;
		
		DownThread(String addr)
		{
			mAddr = addr;
		}
		
		public void run()
		{
			String result = DownloadHtml(mAddr);
			Message message = mAfterDown.obtainMessage();
			message.obj = result;
			mAfterDown.sendMessage(message);
		}
		
		String DownloadHtml(String addr)
		{
			StringBuilder html = new StringBuilder();
			try
			{
				URL url = new URL(addr);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if(conn != null)
				{
					conn.setConnectTimeout(10000);
					conn.setUseCaches(false);
					if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
					{
						BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						for(;;)
						{
							String line = br.readLine();
							if(line == null)
								break;
							html.append(line + '\n');
						}
						br.close();
					}
					conn.disconnect();
				}
			}
			catch(NetworkOnMainThreadException e)
			{
				return "Error";
			}
			catch(Exception e)
			{
				return "Error ex";
			}
			return html.toString();
		}
	}
	
	Handler mAfterDown = new Handler()
	{
		public void handleMessage(Message msg)
		{
			mProgress.dismiss();
			TextView result = (TextView)findViewById(R.id.text9);
			String html = (String)msg.obj;

			String Result = "";
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputStream istream = new ByteArrayInputStream(html.getBytes("utf-8"));
				Document doc = builder.parse(istream);
				
				NodeList nodelist = doc.getElementsByTagName("body");
				numElement = 0;
				numName = 0;
				numNode = 0;
				Node node = nodelist.item(0);
				NodeList l1 = node.getChildNodes();
				String temp = "#text";
				for(int i=0; i < l1.getLength(); i++)
				{
					node = l1.item(i);
					if(temp.equals(node.getNodeName()) == false){
						mainNode[numNode++] = l1.item(i);
					}
				}
				
				xmlNum = 0;
				textNum = 0;
				print(mainNode[0]);

				for(int i=0; i<numElement; i++)
				{
					xmlText[i] = textElement[i];
					nameText[i] = nameElement[i];
					textNum++;
					xmlNum++;
				}
				
				TextView t1 = (TextView)findViewById(R.id.text9);
				TextView t2 = (TextView)findViewById(R.id.text10);
				TextView t3 = (TextView)findViewById(R.id.text11);
				TextView t4 = (TextView)findViewById(R.id.text12);
				TextView t5 = (TextView)findViewById(R.id.text13);
				TextView t6 = (TextView)findViewById(R.id.text14);
				TextView t7 = (TextView)findViewById(R.id.text15);
				
				t1.setText("���� �ð� : " +xmlText[1]);
				t2.setText("�µ� : " + xmlText[3]);
				t3.setText("���� : " + xmlText[8]);
				t4.setText("����Ȯ�� : " + xmlText[10]);
				t5.setText("ǳ�� : " + xmlText[13]);
				t6.setText("ǳ�� : " + xmlText[15]);
				t7.setText("���� : " + xmlText[17]);
				
				ImageView wImg= (ImageView)findViewById(R.id.weatherImg);
				if(xmlText[8].equals("�帲"))
					wImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_rainy));
				else if(xmlText[8].equals("����"))
					wImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_sunny));
				else
					wImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_etc));
			}
			catch(Exception e)
			{;}
		}
	};
	
	void print(Node node){		
		int type = node.getNodeType();
		switch(type){
		case Node.DOCUMENT_NODE:
			Document d = (Document) node;
			print(d.getDocumentElement());
			break;
			
		case Node.ELEMENT_NODE:
			NamedNodeMap attrs = node.getAttributes();
			nameElement[numName++] = node.getNodeName();
			int len = attrs.getLength();
			for(int i = 0; i < len; i++)
			{
				print(attrs.item(i));
			}
			NodeList children = node.getChildNodes();
			if(children != null){
				int n = children.getLength();
				for(int i = 0; i < n; i++)
				{
					print(children.item(i));
					}
				}
			break;
			
		case Node.CDATA_SECTION_NODE:
			String strCData = node.getNodeValue();
			strCData = strCData.trim();
			if(strCData.length() != 0)
			{
				textElement[numElement++] = node.getNodeValue();				
			}
			break;
			
		case Node.TEXT_NODE:
			String strValue = node.getNodeValue();
			strValue = strValue.trim();
			if(strValue.length() != 0)
			{
				textElement[numElement++] = strValue;
			}
			break;
			
		case Node.ATTRIBUTE_NODE:
			String strATT = node.getNodeValue();
			strATT = strATT.trim();
			if(strATT.length() != 0){
				nameElement[numName++] = node.getNodeName();
				textElement[numElement++] = strATT;
			}
			break;
		}		
	}
	//������ ����
	public void CreateOverlay(String type) {
		// �������� ���ҽ� ������ü �Ҵ�
		mMapResourceProvider = new NMapViewerResourceProvider(this);
		// �������� ������ �߰�
		mOverlayManager = new NMapOverlayManager(this, mapView, mMapResourceProvider);
		// ǥ���� ��ġ �����͸� �����Ѵ�. -- ������ ���ڰ� �������̸� �ν��ϱ� ���� id��
		int count = 0;
		Cursor cursor;
		db2 = mHelper2.getReadableDatabase();
		cursor = db2.rawQuery("SELECT latitude, longitude, type, name FROM dic2 WHERE type = '" + type + "'", null);
		while(cursor.moveToNext())
			count++;

		NMapPOIdata poiData = new NMapPOIdata(count, mMapResourceProvider);
		poiData.beginPOIdata(count);
		double longitude, latitude;
		String name;
		cursor = db2.rawQuery("SELECT latitude, longitude, type, name FROM dic2 WHERE type = '" + type + "'", null);
		while(cursor.moveToNext())
		{
			longitude = Double.parseDouble(cursor.getString(0));
			latitude = Double.parseDouble(cursor.getString(1));
			name = cursor.getString(3);
			poiData.addPOIitem(longitude, latitude, name, NMapPOIflagType.PIN, 1);
		}
		cursor.close();
		mHelper2.close();
		
		poiData.endPOIdata();
		// ��ġ �����͸� ����Ͽ� �������� ����
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		// id���� 1���� ������ ��� �������̰� ǥ�õǰ� �ִ� ��ġ�� ������ �߽ɰ� ZOOM�� �缳��
//		poiDataOverlay.showAllPOIdata(1);
		// �������� �̺�Ʈ ���
		mOverlayManager.setOnCalloutOverlayListener(this);
	}

	@Override
	public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay arg0,
			NMapOverlayItem arg1, Rect arg2) {
		//�̰��� �������� Ŭ�� �̺�Ʈ
		NGeoPoint myPoint = new NGeoPoint();
		myPoint = arg1.getPoint();
//		++order;
		
		//
		Cursor cursor;
		String t = null;
		int order = 0;
		db3 = mHelper3.getReadableDatabase();
		cursor = db3.rawQuery("SELECT latitude, longitude, type, name FROM dic3", null);
		while(cursor.moveToNext())
			t = cursor.getString(0);
		if(t != null)
			order = Integer.parseInt(t);
		order++;
		cursor.close();
		mHelper3.close();
		//
		
		String name = arg1.getTitle();
		double latitude = myPoint.latitude;
		double longitude = myPoint.longitude;
		String s1 = String.valueOf(order);
		String s2 = name;
		String s3 = String.valueOf(latitude);
		String s4 = String.valueOf(longitude);
		
		mHelper3 = new WordDBHelper3(this); // �����ͺ��̽�
		db3 = mHelper3.getWritableDatabase();
		db3.execSQL("INSERT INTO dic3 VALUES (null, '" + s1 + "', '" + s2 + "', '" + s3 + "', '" + s4 + "');");
		mHelper3.close();
		
		Location location = mLocationManager.getLastKnownLocation(mBestProvider);
		
		NGeoPoint myLocation = new NGeoPoint(location.getLongitude(), location.getLatitude());
		NGeoPoint point = null;
		int distance = (int) point.getDistance(myLocation, myPoint);
	
		Toast.makeText(this, s2 + "�� ���� ��ġ������ ����Ǿ����ϴ�\n������ �Ÿ�: " + distance + "M", Toast.LENGTH_LONG).show();
		return null;
	}
	
	public void MyLocation() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(true);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		mBestProvider = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(mBestProvider, 0, 0, mLocationListener);
		
		TimerTask myTask = new TimerTask() {
			public void run() {
				Location location = mLocationManager.getLastKnownLocation(mBestProvider);
							
				SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.KOREAN);
				Date currentTime = new Date();
				//���� �ð�
				String time = formatter.format(currentTime);
				//��ǥ
				double latitude = location.getLatitude();				
				double longitude = location.getLongitude();
				
				String t1 = String.valueOf(latitude);
				String t2 = String.valueOf(longitude);
				db4 = mHelper4.getWritableDatabase();

				db4.execSQL("INSERT INTO dic7 VALUES (null, '" + time + "', '" + t1 + "', '" + t2 + "');");
				mHelper4.close();
//				Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG).show();
			}
		};
		Timer timer = new Timer();
		timer.schedule(myTask, 1000, 60000);
	}

	private final LocationListener mLocationListener = new LocationListener () {
		@Override
		public void onLocationChanged(Location location) {
			upDateWithNewLocation(location);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		@Override
		public void onProviderEnabled(String provider) { }
		@Override
		public void onProviderDisabled(String provider) { }
	};
	
	private void upDateWithNewLocation(Location location) {
		if(location == null)
			return;
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		ShowMyLocation(longitude, latitude);
	}
	private void ShowMyLocation(double longitude, double latitude) {
		NMapViewerResourceProvider mMapViewrResourceProvider = null;
		NMapOverlayManager mOverlayManager;
		
		mMapViewrResourceProvider = new NMapViewerResourceProvider(this);
		mOverlayManager = new NMapOverlayManager(this, mapView, mMapViewrResourceProvider);
		
		NGeoPoint myPoint = new NGeoPoint(longitude, latitude);
		
		NMapPOIdata poiData = new NMapPOIdata(1, mMapViewrResourceProvider);
		poiData.beginPOIdata(1);
		poiData.addPOIitem(myPoint, "���� ��ġ", mMapViewrResourceProvider.getLocationDot()[1], 0);
		poiData.endPOIdata();
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		poiDataOverlay.showAllPOIdata(0);
		
	}
	//������ ��
}
// DB �߰�
class WordDBHelper extends SQLiteOpenHelper // url �����ͺ��̽�
{
	public WordDBHelper(Context context)
	{
		super(context, "EngWord2.db", null, 1);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE dic ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "url TEXT, location TEXT);");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS dic");
		onCreate(db);
	}
}

class WordDBHelper2 extends SQLiteOpenHelper // ��� �����ͺ��̽�
{
	public WordDBHelper2(Context context)
	{
		super(context, "EngWord3.db", null, 2);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE dic2 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "latitude TEXT, longitude TEXT, type TEXT, name TEXT);");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS dic2");
		onCreate(db);
	}
}

class WordDBHelper3 extends SQLiteOpenHelper // ��� �����ͺ��̽�
{
	public WordDBHelper3(Context context)
	{
		super(context, "EngWord4.db", null, 3);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE dic3 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "latitude TEXT, longitude TEXT, type TEXT, name TEXT);");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS dic3");
		onCreate(db);
	}
}

class WordDBHelper4 extends SQLiteOpenHelper // ��� �����ͺ��̽�
{
	public WordDBHelper4(Context context)
	{
		super(context, "EngWord7.db", null, 7);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE dic7 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "a TEXT, b TEXT, c TEXT);");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS dic7");
		onCreate(db);
	}
}