package com.example.wy_black_develop;

import android.app.Activity;

public class Excute_Class {

	static private int Max_Cmd_Num = 30;
	public String Wrong_Info;
	public String Doing;
	
	private boolean Running;
	public boolean If_Wrong;
	private int Num;
	private int Running_Num;
	private Excute_Unit[] Unit_Array;
	
	Activity Ac;
	
	public Excute_Class(Activity Ac_) {
		// TODO Auto-generated constructor stub
		Ac = Ac_;
		Running =  false;
		Num = 0;
		Running_Num = 0;
		If_Wrong = false;
		Unit_Array = new Excute_Unit[Max_Cmd_Num];
	}
	public void Stop(){
		Running = false;
	}
	
	public boolean Start_Running(String x) {
		Doing = x;
		//MainActivity.Show("starting");
		if (!(Num>0)) {
			Wrong_Info = "Cmd Num <= 0";
			return true;
		}
		if (Running == true) {
			Wrong_Info = "Running now.";
			return true;
		}
		//start running
		//MainActivity.Show("start running");
		Running = true;
		Running_Num = 0;
		If_Wrong = false;
		if (Unit_Array[0].Get_Cmd().isEmpty()){
			//MainActivity.Show("empty");
			Wrong_Info = "Get a Null Cmd from 0";
			Running = false;
			return true;
		}
		//MainActivity.Show("send " + Unit_Array[0].Get_Cmd());
		Send_BLE(Unit_Array[0].Get_Cmd());
		return false;
	}
	
	public void Clear_Unit(){
		Num = 0;
	}
	
	public boolean Append_Unit(Excute_Unit x) {
		if (Num >= Max_Cmd_Num) {
			Wrong_Info = "Fulled";
			return true;
		}
		Unit_Array[Num] = x;
		Num++;
		return false;
	}
	
	public void BLE_CB (String x) {
		//MainActivity.Show("BLE_CB called :" + x);
		if (Running) {
			switch (Unit_Array[Running_Num].Manifest_Callback(x)) {
			case Excute_Unit.Wrong:
				Wrong_Info = "Manifest_Callback Wrong at Num " + Running_Num;
				If_Wrong = true;
				Running_Callback();
				return;
			case Excute_Unit.Next:
				Running_Num++;
			case Excute_Unit.Stay:
				if (Running_Num >= Num){
					If_Wrong = false;
					Running_Callback();
					return;
				}
				if (Unit_Array[Running_Num].Get_Cmd().isEmpty()){
					Wrong_Info = "Get a Null Cmd from " + Running_Num;
					If_Wrong = true;
					Running_Callback();
					return;
				}
				Send_BLE(Unit_Array[Running_Num].Get_Cmd());
				break;
			}
		}
	}
	
	private void Running_Callback (){
		Running = false;
		AmoComActivity.Running_Callback();
		//test();
	}
	
	private void Send_BLE(String x) {
		((AmoComActivity) Ac).sendM(x);
	}
	
	public void Show_All_Info(){
		AmoComActivity.Show("Running :" + Running);
		AmoComActivity.Show("Num :" + Num);
		AmoComActivity.Show("Running_Num :" + Running_Num);
		for (int i=0; i<Num; i++) {
			AmoComActivity.Show("Num" + i);
			Unit_Array[i].ShowInfo();
		}
	}
	
	public Excute_Unit Get_Unit(int i) {
		return Unit_Array[i];
	}
	
}
