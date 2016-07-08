package com.example.wy_black_develop;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AmoComActivity extends Activity implements View.OnClickListener,OnCheckedChangeListener {
	private final static String TAG = "BleToolWy2";// DeviceScanActivity.class.getSimpleName();
	private final String ACTION_NAME_RSSI = "AMOMCU_RSSI"; // 其他文件广播的定义必须一致
	private final String ACTION_CONNECT = "AMOMCU_CONNECT"; // 其他文件广播的定义必须一致
	public static final int SCAN_CODE = 1;

	static TextView Text_Recv;
	static String Str_Recv;
	
	static private String Scaned_Code = "";

	static String ReciveStr;
	static ScrollView scrollView;
	static Handler mHandler = new Handler();
	static TextView textview_recive_send_info;
	static int Totol_Send_bytes = 0;
	static int Totol_recv_bytes = 0;
	static int Totol_recv_bytes_temp = 0;
	static String SendString = "WyBlack:Find(24,100,100,100)Link(0081)&Find(22,150,235,300)Link(0028)&Find(21,150,200,300)Link(0038)";
	
	static boolean flag_send_find_between = false;
	
	static String SendString_edit_text_low_wy = "100";
	static String SendString_edit_text_high_wy = "300";
	static String SendString_edit_text_wy = "200";
	static String SendString_edit_text_A = "3";
	static String SendString_edit_text_B = "8";
	static String SendString_edit_text_password = "0000";

	ToggleButton toggleHexStr;
	ToggleButton toggleTime;
	
	//新增变量
	private static String[] Port_Array;
	private static Excute_Class Excute;
	private static int Num_of_Received;
	private static Like_BLE BLE;
	private ToggleButton mToggleButton;  
	//结束新增

	// 根据rssi 值计算距离， 只是参考作用， 不准确---amomcu
	static final int rssibufferSize = 10;
	int[] rssibuffer = new int[rssibufferSize];
	int rssibufferIndex = 0;
	boolean rssiUsedFalg = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.other);
		getActionBar().setTitle("BleTool_Wy");
		registerBoradcastReceiver();
		
		findViewById(R.id.button_Save).setOnClickListener(this);
		findViewById(R.id.button_Com_excute).setOnClickListener(this);
		findViewById(R.id.button_Com_scan).setOnClickListener(this);
		findViewById(R.id.button_send).setOnClickListener(this);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String mac_addr = bundle.getString("mac_addr");
		String char_uuid = bundle.getString("char_uuid");
		TextView tv_mac_addr = (TextView) this
				.findViewById(R.id.textview_mac_addr);
		TextView tv_char_uuid = (TextView) this
				.findViewById(R.id.textview_char_uuid);

		tv_mac_addr.setText("设备地址:" + mac_addr);
		tv_char_uuid.setText("特征值UUID:" + char_uuid);

		textview_recive_send_info = (TextView) this
				.findViewById(R.id.textview_recive_send_info);

		Text_Recv = (TextView) findViewById(R.id.device_address);
		Text_Recv.setGravity(Gravity.CLIP_VERTICAL | Gravity.CLIP_HORIZONTAL);
		ReciveStr = "";
		Text_Recv.setMovementMethod(ScrollingMovementMethod.getInstance());
		scrollView = (ScrollView) findViewById(R.id.scroll);

		TextView text2 = (TextView) this.findViewById(R.id.edit_text);
		text2.setText(SendString);

		Totol_Send_bytes = 0;
		Totol_recv_bytes = 0;
		Totol_recv_bytes_temp = 0;
		update_display_send_recv_info(Totol_Send_bytes, Totol_recv_bytes);

		//新增
		Excute = new Excute_Class(AmoComActivity.this);
		Port_Array = new String[13];
		Num_of_Received = 0;
		BLE = new Like_BLE();
		mToggleButton = (ToggleButton) findViewById(R.id.toggleButton1); //获取到控件  
		mToggleButton.setOnCheckedChangeListener(this);//添加监听事件  
		//结束新增
	}

	// 接收 rssi 的广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(ACTION_NAME_RSSI)) {
				int rssi = intent.getIntExtra("RSSI", 0);

				// 以下这些参数我 amomcu 自己设置的， 不太具有参考意义，
				// 实际上我的本意就是根据rssi的信号前度计算以下距离，
				// 以便达到定位目的， 但这个方法并不准 ---amomcu---------20150411

				int rssi_avg = 0;
				int distance_cm_min = 10; // 距离cm -30dbm
				int distance_cm_max_near = 1500; // 距离cm -90dbm
				int distance_cm_max_middle = 5000; // 距离cm -90dbm
				int distance_cm_max_far = 10000; // 距离cm -90dbm
				int near = -72;
				int middle = -80;
				int far = -88;
				double distance = 0.0f;

				if (true) {
					rssibuffer[rssibufferIndex] = rssi;
					rssibufferIndex++;

					if (rssibufferIndex == rssibufferSize)
						rssiUsedFalg = true;

					rssibufferIndex = rssibufferIndex % rssibufferSize;

					if (rssiUsedFalg == true) {
						int rssi_sum = 0;
						for (int i = 0; i < rssibufferSize; i++) {
							rssi_sum += rssibuffer[i];
						}

						rssi_avg = rssi_sum / rssibufferSize;

						if (-rssi_avg < 35)
							rssi_avg = -35;

						if (-rssi_avg < -near) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-near - 35))
									* distance_cm_max_near;
						} else if (-rssi_avg < -middle) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-middle - 35))
									* distance_cm_max_middle;
						} else {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-far - 35))
									* distance_cm_max_far;
						}
					}
				}

				getActionBar().setTitle(
						"RSSI:" + rssi_avg + "dbm" + "," + "距离:"
								+ (int) distance + "cm");
			} else if (action.equals(ACTION_CONNECT)) {
				int status = intent.getIntExtra("CONNECT_STATUC", 0);
				if (status == 0) {
					getActionBar().setTitle("已断开连接");
					finish();
				} else {
					getActionBar().setTitle("已连接");
				}
			}
		}
	};

	// 注册广播
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME_RSSI);
		myIntentFilter.addAction(ACTION_CONNECT);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}
	
	public static int Wy_char_to_int(char x){
		if (x-'0' >=0 && x-'0'<10){
			return x-'0';
		}
		else if (x-'a' >= 0 && x-'a'<6){
			return x-'a'+10;
		}
		else return 0;
	}
	
	public static long Wy_string_to_long (String source){
		long source_long = 0;
		for (int i=0; i<source.length() && source.charAt(i)!='\0'; i++){
			source_long *= 10;
			source_long += Wy_char_to_int(source.charAt(i));
		}
		return source_long;
	}
	
	public static char Wy_int_to_hex (int x){
		if (x>=0 && x<10){
			return (char) ('0' + x);
		}
		else if (x>9 && x<16){
			return (char) ('a' + x - 10);
		}
		else return (char) 0;
	}
	
	public static String Wy_normal_to_special_basic_4_char(long source){
		int r = 0, j;
		for (j=0; source>1000; ++j)
		{
			source /= 2;
		}
		for (int i=9; i>-1; --i)
		{
			r = r << 1;
			r += ((source>>i)&1);
		}
		for (int i=5; i>-1; --i)
		{
			r = r << 1;
			r += ((j>>i)&1);
		}
		String a = "";
		String b = "%";
		a += b.replace('%', Wy_int_to_hex((r>>12) & 0x0f));
		a += b.replace('%', Wy_int_to_hex((r>>8) & 0x0f));
		a += b.replace('%', Wy_int_to_hex((r>>4) & 0x0f));
		a += b.replace('%', Wy_int_to_hex(r & 0x0f));
		return a;
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.button_Save:
			TextView text3 = (TextView) this.findViewById(R.id.edit_text);
			Scaned_Code = text3.getText().toString();
			Wy_print("Scaned_Code = " + Scaned_Code);
			break;
		case R.id.button_send:
			TextView text2 = (TextView) this.findViewById(R.id.edit_text);
			if (text2.length() > 0) {
				String s1 = text2.getText().toString();
				DeviceScanActivity.writeChar6(s1);

				Totol_Send_bytes += s1.length();
				update_display_send_recv_info(Totol_Send_bytes,
						Totol_recv_bytes);

				SendString = text2.getText().toString();

			}
			break;
		case R.id.button_Com_scan:
			Wy_print("scan_Called");
			Intent intent = new Intent(AmoComActivity.this, CaptureActivity.class);
			startActivityForResult(intent, SCAN_CODE);
			break;
		case R.id.button_Com_excute:
			Excute.Stop();
			Excute.Clear_Unit();
			Ask_All_Generate();
			Excute.Start_Running("Initialize");
			break;
		}
			
	}
	
	private static synchronized void Wy_print(String str){
		char6_display(str, str.getBytes(), DeviceScanActivity.UUID_CHAR6);
	}
	public static synchronized void Wy_print_public(String str) {
		char6_display(str, str.getBytes(), DeviceScanActivity.UUID_CHAR6);
	}

	public static synchronized void char6_display(String str, byte[] data,
			String uuid) {
		Log.i(TAG, "char6_display str = " + str);
		//这里是把数据交给Like_BLE的地方
		if (BLE.If_Use_BLE){
			BLE.Call_Back(str);
		}
		//
		if (uuid.equals(DeviceScanActivity.UUID_HERATRATE)) {
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String TimeStr = formatter.format(curDate);
			byte[] ht = new byte[str.length()];
			// System.arraycopy(ht, Totol_Send_bytes,
			// Utils.hexStringToBytes(str), 0, str.length());

			String DisplayStr = "[" + TimeStr + "] " + "HeartRate : " + data[0]
					+ "=" + data[1];
			// Text_Recv.append(DisplayStr + "\r\n");
			Str_Recv = DisplayStr + "\r\n";
		} else if (uuid.equals(DeviceScanActivity.UUID_TEMPERATURE)) // 温度测量
		{
			byte[] midbytes = str.getBytes();
			String HexStr = Utils.bytesToHexString(midbytes);
			// Text_Recv.append(HexStr);
			Str_Recv = HexStr;
		} else if (uuid.equals(DeviceScanActivity.UUID_CHAR6)) // amomcu 的串口透传
		{
			
			SimpleDateFormat formatter = new SimpleDateFormat(
					"HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String TimeStr = formatter.format(curDate);
			String DisplayStr = "[" + TimeStr + "] " + str;
			// Text_Recv.append(DisplayStr + "\r\n");
			Str_Recv = DisplayStr + "\r\n";	

		} else // 默认显示 hex
		{
			byte[] midbytes = str.getBytes();
			String HexStr = Utils.bytesToHexString(midbytes);
			// Text_Recv.append(HexStr);
			Str_Recv = HexStr;
		}

		Totol_recv_bytes += str.length();
		Totol_recv_bytes_temp += str.length();

		mHandler.post(new Runnable() {
			@Override
			public synchronized void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);// 滚动到底
				Text_Recv.append(Str_Recv);

				if (Totol_recv_bytes_temp > 10000) // 数据太多时清空数据
				{
					Totol_recv_bytes_temp = 0;
					Text_Recv.setText("");
				}

				update_display_send_recv_info(Totol_Send_bytes,
						Totol_recv_bytes);
			}
		});
	}

	public synchronized static String GetLastData() {
		String string = Str_Recv;
		return string;
	}

	public synchronized static void update_display_send_recv_info(int send,
			int recv) {
		String info1 = String.format("发送%4d,接收%4d [字节]", send, recv);
		textview_recive_send_info.setText(info1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SCAN_CODE:
			//TextView scanResult = (TextView) findViewById(R.id.scan_result);
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("scan_result");
				Wy_print("Scaned_Code = " + result);
				Scaned_Code = result;
			} else if (resultCode == RESULT_CANCELED) {
				Wy_print("扫描未结束返回");
			}
			break;
		case 0:
			if (requestCode == 0 && resultCode == RESULT_OK) {
				BLE.Call_Back(data.getStringExtra("return"));
				//Show(intent.getStringExtra("return"));
			}
		default:
			break;
		}
	}
	
	
	private void Ask_All_Generate(){

		Excute_Unit A = new Excute_Unit(Excute_Unit.Type_Ask);
		A.Set_Ask_Unit("0");
		Excute.Append_Unit(A);
		Excute_Unit B = new Excute_Unit(Excute_Unit.Type_Ask);
		B.Set_Ask_Unit("1");
		Excute.Append_Unit(B);
		Excute_Unit C = new Excute_Unit(Excute_Unit.Type_Ask);
		C.Set_Ask_Unit("2");
		Excute.Append_Unit(C);
		Excute_Unit D = new Excute_Unit(Excute_Unit.Type_Ask);
		D.Set_Ask_Unit("3");
		Excute.Append_Unit(D);
		Excute_Unit E = new Excute_Unit(Excute_Unit.Type_Ask);
		E.Set_Ask_Unit("4");
		Excute.Append_Unit(E);
		Excute_Unit F = new Excute_Unit(Excute_Unit.Type_Ask);
		F.Set_Ask_Unit("5");
		Excute.Append_Unit(F);
		Excute_Unit G = new Excute_Unit(Excute_Unit.Type_Ask);
		G.Set_Ask_Unit("6");
		Excute.Append_Unit(G);
		Excute_Unit H = new Excute_Unit(Excute_Unit.Type_Ask);
		H.Set_Ask_Unit("7");
		Excute.Append_Unit(H);
		Excute_Unit I = new Excute_Unit(Excute_Unit.Type_Ask);
		I.Set_Ask_Unit("8");
		Excute.Append_Unit(I);
		Excute_Unit J = new Excute_Unit(Excute_Unit.Type_Ask);
		J.Set_Ask_Unit("9");
		Excute.Append_Unit(J);
		Excute_Unit K = new Excute_Unit(Excute_Unit.Type_Ask);
		K.Set_Ask_Unit("A");
		Excute.Append_Unit(K);
		Excute_Unit L = new Excute_Unit(Excute_Unit.Type_Ask);
		L.Set_Ask_Unit("B");
		Excute.Append_Unit(L);
		Excute_Unit M = new Excute_Unit(Excute_Unit.Type_Ask);
		M.Set_Ask_Unit("C");
		Excute.Append_Unit(M);
	}
	
	private static String Return_Interprete_Wrong = "Recognize_Wrong";
	private static String Return_Interprete_Finished = "Finished";
	private static String Recognize_Head = "WyBlack:";
	private static String Interprete() {
		//Scaned_Code = Do_Text.getText().toString();
		if (Scaned_Code.length() >= Recognize_Head.length() && Scaned_Code.startsWith(Recognize_Head)) {
			int Num_Of_Cmd;
			String Code1 = Scaned_Code.substring(Recognize_Head.length());
			Num_Of_Cmd = Code1.split("&").length;
			String[] Cmd_Array = new String[Num_Of_Cmd];
			Cmd_Array = Code1.split("&");
			for (int i=0; i<Num_Of_Cmd; i++) {
				Show("---> " + Cmd_Array[i]);
			} // 应该都是6位的String，比如电阻xxxx21，如果发现有空字符串，则报错
			for (int i=0; i<13; i++) {
				if (Port_Array[i].length() != 6) {
					return "Port_Wrong";
				}
				//Show(Port_Array[i]);
			}
			boolean[] If_Free = new boolean[13];
			for (int i=0; i<13; i++) {
				If_Free[i] = true;
			}
			String[] Need_Port = new String[Num_Of_Cmd];
			Need_Port = Find_Need_Port(Cmd_Array); 
			String[] Got_Port = new String[Num_Of_Cmd];
			for (int i=0; i<Num_Of_Cmd; i++){Got_Port[i] = "";}
			for (int i=0; i<Num_Of_Cmd; i++) {
				int j=0;
				for (j=0; j<13; j++) {
					if (If_Free[j] && Need_Port[i].equals(Port_Array[j].substring(4))){
						If_Free[j] = false;
						Got_Port[i] = Translate_hardware.Int_To_Char(j);
						break;
					}
				}
				if (j == 13){
					Show("Can't Afford");
					return "few port";
				}
			}
			//找到所有的端口了
			for (int i=0; i<Num_Of_Cmd; i++) {
				if (Got_Port[i].isEmpty()) return "Can't Afford";//只要有没找到端口的命令都会返回
				//Show(Got_Port[i]);
			}
			Excute.Clear_Unit();
			for (int i=0; i<Num_Of_Cmd; i++) {
				if (Is_Find_And_Link(Cmd_Array[i])){
					Excute_Unit A = Get_Unit_Find_And_Link1(Cmd_Array[i]);
					Excute_Unit B = Get_Unit_Find_And_Link2(Cmd_Array[i]);
					A.Set_Port(Got_Port[i]);
					B.Set_Port(Got_Port[i]);
					A.Generate_Find_Unit();
					B.Generate_Link_Unit();
					Excute.Append_Unit(A);
					Excute.Append_Unit(B);
				}
			}
			//Excute.Show_All_Info();
			Excute.Start_Running("Excuting");
			return Return_Interprete_Finished;
		}
		else {
			return Return_Interprete_Wrong;
		}
	}
	private static String[] Find_Need_Port(String[] A) {	
		int Num_Of_Cmd = A.length;
		String[] Need_Port = new String[Num_Of_Cmd];
		for (int i=0; i<Num_Of_Cmd; i++) {
			Need_Port[i] = "";
		}
		for (int i=0; i<Num_Of_Cmd; i++) { //先解决指定类型的连接
			if (Is_Find_And_Link(A[i])) {
				Need_Port[i] = Get_Unit_Find_And_Link1(A[i]).Find_Unit;
				//Show(Need_Port[i]);

			}
		}
		return Need_Port;
	}
	private static boolean Is_Find_And_Link(String x) {
		if (! x.startsWith("Find(")) return false;
		String x1 = x.substring("Find(".length());
		//接受第一个参数
		if (x1.indexOf(',') == -1) return false;
		String Find_P1_Str = x1.substring(0,x1.indexOf(','));
		if (Find_P1_Str.length()!=2) return false; //必须是两位十六进制的单位
		//Show(Find_P1_Str);
		
		String x2 = x1.substring(x1.indexOf(',')+1);
		//接受第二个参数
		if (x2.indexOf(',') == -1) return false;
		
		String Find_P2_Str = x2.substring(0,x2.indexOf(','));
		if (!isNumeric(Find_P2_Str)) return false;
		int Find_P2 = Integer.parseInt(Find_P2_Str);
		//Show(Find_P2_Str);
		
		String x3 = x2.substring(x2.indexOf(',')+1);
		//接受第三个参数
		if (x3.indexOf(',') == -1) return false;
		String Find_P3_Str = x3.substring(0,x3.indexOf(','));
		if (!isNumeric(Find_P3_Str)) return false;
		int Find_P3 = Integer.parseInt(Find_P3_Str);
		//Show(Find_P3_Str);
		
		String x4 = x3.substring(x3.indexOf(',')+1);
		//接受第四个参数
		if (x4.indexOf(')') == -1) return false;
		String Find_P4_Str = x4.substring(0,x4.indexOf(')'));
		if (!isNumeric(Find_P4_Str)) return false;
		int Find_P4 = Integer.parseInt(Find_P4_Str);
		//Show(Find_P4_Str);
		
		//Wy_print("x1is:" + Find_P1_Str + "  x2is:" + Find_P2_Str + "  x3is:"+ Find_P3_Str);
		
		String x5 = x4.substring(x4.indexOf(')')+1);
		//Wy_print(x5);
		
		if (! x5.startsWith("Link(")) return false;
		String x6 = x5.substring("Link(".length());
		//接受唯一的参数
		if (x6.indexOf(')') == -1) return false;
		String Link_Str = x6.substring(0, x6.indexOf(')'));
		if (Link_Str.length() != 4) return false; //必须是四位连接方式
		//Show(Link_Str);
		
		if (x6.indexOf(')') != x6.length()-1) return false;
		//Show("Is Find_And_Link");
		return true;
	}
	
	private static Excute_Unit Get_Unit_Find_And_Link1(String x){
		String x1 = x.substring("Find(".length());
		String Find_P1_Str = x1.substring(0,x1.indexOf(','));
		String x2 = x1.substring(x1.indexOf(',')+1);
		String Find_P2_Str = x2.substring(0,x2.indexOf(','));
		int Find_P2 = Integer.parseInt(Find_P2_Str);
		String x3 = x2.substring(x2.indexOf(',')+1);
		String Find_P3_Str = x3.substring(0,x3.indexOf(','));
		int Find_P3 = Integer.parseInt(Find_P3_Str);
		String x4 = x3.substring(x3.indexOf(',')+1);
		String Find_P4_Str = x4.substring(0,x4.indexOf(')'));
		int Find_P4 = Integer.parseInt(Find_P4_Str);
		String x5 = x4.substring(x4.indexOf(')')+1);
		String x6 = x5.substring("Link(".length());
		String Link_Str = x6.substring(0, x6.indexOf(')'));
		Excute_Unit A = new Excute_Unit(Excute_Unit.Type_Find);
		if (A.Set_Find_Unit(Find_P1_Str, Find_P2, Find_P3, Find_P4)){
			Show("Get_Unit_Find_And_Link1 Wrong");
		}
		return A;
	}
	
	private static Excute_Unit Get_Unit_Find_And_Link2(String x){
		String x1 = x.substring("Find(".length());
		String Find_P1_Str = x1.substring(0,x1.indexOf(','));
		String x2 = x1.substring(x1.indexOf(',')+1);
		String Find_P2_Str = x2.substring(0,x2.indexOf(','));
		int Find_P2 = Integer.parseInt(Find_P2_Str);
		String x3 = x2.substring(x2.indexOf(',')+1);
		String Find_P3_Str = x3.substring(0,x3.indexOf(','));
		int Find_P3 = Integer.parseInt(Find_P3_Str);
		String x4 = x3.substring(x3.indexOf(',')+1);
		String Find_P4_Str = x4.substring(0,x4.indexOf(')'));
		int Find_P4 = Integer.parseInt(Find_P4_Str);
		String x5 = x4.substring(x4.indexOf(')')+1);
		String x6 = x5.substring("Link(".length());
		String Link_Str = x6.substring(0, x6.indexOf(')'));
		Excute_Unit A = new Excute_Unit(Excute_Unit.Type_Link);
		if (A.Set_Link_Unit(Link_Str)){
			Show("Get_Unit_Find_And_Link2 Wrong");
		}
		return A;
	}
	
	public static boolean isNumeric(String str){  
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false; 
		}
		return true;
	}
	public static void cb(final String x) {
		Num_of_Received++;
		Excute.BLE_CB(x);
		//Show(x);
	}
	public static void Show(final String x) {
		mHandler.post(new Runnable() {
			@Override
			public synchronized void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);// 滚动到底
				Text_Recv.append(x + "\n");
			}
		});
		//Wy_print(x);
	}
	
	public static void Running_Callback() {
		//Excute.Show_All_Info();
		if (Excute.If_Wrong) {
			Show("Interrupt: " + Excute.Wrong_Info);
		}
		//Show("Doing:" + Excute.Doing);
		if (Excute.Doing.equals("Ask_All")){
			//收集所有的端口信息
			for (int i=0; i<13; i++) {
				Port_Array[i] = Excute.Get_Unit(i).Re_Ask_Sub;
				//Show(Port_Array[i]);
			}
		}
		else if (Excute.Doing.equals("Initialize")){
			//第一步是收集端口信息
			for (int i=0; i<13; i++) {
				Port_Array[i] = Excute.Get_Unit(i).Re_Ask_Sub;
			}
			String mBack = Interprete();
			if (mBack != "Finished") {
				Show("Interprete: " + mBack);
				return ;
			}
			//返回finished了，下一步开始工作
		}
		else if (Excute.Doing.equals("Excuting")){
			//Excute.Show_All_Info();
			if (! (Excute.If_Wrong)){
				Show("---> Excute Finished Successfully");
			}
		}
	}
	
	public void Get_Man(String y){
		Intent intent = new Intent();
		intent.setClass(AmoComActivity.this, Get_Man_Answer.class);
		intent.putExtra("Need_Reply", y);
		startActivityForResult(intent, 0);
	}
	
	public void sendM(String x) {
		BLE.Send_String(x, AmoComActivity.this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(isChecked){  
			Show("开启真实BLE传输模式,并终止之前的进程");
			Excute.Stop();
			BLE.If_Use_BLE = true;
		}else{  
			Show("开启本地实验模式,并终止之前的进程"); 
			Excute.Stop();
			BLE.If_Use_BLE = false;
		}  
	}

}

/*********
 * WyBlack:Need5(21~0,21~1,22~2,23~3,25~4)Cmd10(Find@0(80,100,120),Link@0(0038),Find@1(70,80,90),Link@1(0024),
 * ShortCut@3(0034),Find@4(40,60,70),Link@4(1234))
 * 这样二维码太大了，必须使用压缩的语言
 * WyBlack:Find(21,80,100,120)Link(0038)&Find(21,70,80,90)Link(0024)&ShortCut(0034)&Find(25,40,60,70)Link(1234)
 * WyBlack:Find(21,80,100,120)Link(0038)&Find(21,70,80,90)Link(0024)&ShortCut(0034)&Find(25,40,60,70)Link(1234)
 * WyBlack:Find(21,80,100,120)Link(0038)&ShortCut(0034)
 ********/