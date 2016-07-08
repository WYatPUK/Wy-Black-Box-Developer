package com.example.wy_black_develop;

import android.app.Activity;

public class Like_BLE {
	
	private String Ask[];
	private String Re_Ask[];
	public boolean If_Use_BLE;
	
	public Like_BLE() {
		// TODO Auto-generated constructor stub
		Ask = new String[13];
		Re_Ask = new String[13];
		Ask[0] = "Ask@0"; Re_Ask[0] = "Info:111121";
		Ask[1] = "Ask@1"; Re_Ask[1] = "Info:111222";
		Ask[2] = "Ask@2"; Re_Ask[2] = "Info:111323";
		Ask[3] = "Ask@3"; Re_Ask[3] = "Info:111424";
		Ask[4] = "Ask@4"; Re_Ask[4] = "Info:111521";
		Ask[5] = "Ask@5"; Re_Ask[5] = "Info:111622";
		Ask[6] = "Ask@6"; Re_Ask[6] = "GoStart_Wrong";
		Ask[7] = "Ask@7"; Re_Ask[7] = "GoStart_Wrong";
		Ask[8] = "Ask@8"; Re_Ask[8] = "Info:111721";
		Ask[9] = "Ask@9"; Re_Ask[9] = "Port_Not_Exist";
		Ask[10] = "Ask@A"; Re_Ask[10] = "Port_Not_Exist";
		Ask[11] = "Ask@B"; Re_Ask[11] = "Info:111821";
		Ask[12] = "Ask@C"; Re_Ask[12] = "Port_Not_Exist";
		If_Use_BLE = false;
	}
	
	public void Send_String (final String x, final Activity Ac) {
		AmoComActivity.Show("SendBLE:" + x);
		if (If_Use_BLE == false) {
			new Thread(new Runnable() {
				@Override
				public synchronized void run() {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String y = "Unknown_Cmd";
					for (int i=0; i<13; i++){
						if (x.equals(Ask[i])){
							y = Re_Ask[i];
						}
					}
					if (!y.equals("Unknown_Cmd")){
						Call_Back(y);
						return;
					}
					((AmoComActivity) Ac).Get_Man(x);
					//Call_Back(y);
					
				}
			}).start();
		}
		else { //Ê¹ÓÃBLE·¢ËÍ
			DeviceScanActivity.writeChar6(x);
		}
	}
	
	public void Call_Back(String y){
		AmoComActivity.Show("ReturnBLE:" + y);
		AmoComActivity.cb(y);
	}

}
