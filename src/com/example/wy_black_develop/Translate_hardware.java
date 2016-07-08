package com.example.wy_black_develop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translate_hardware {
	public final static long String_To_Long(String x) {
		if (isNumeric(x)) {
			return Integer.parseInt(x);
		}
		return -1;
	}
	
	public final static int Standard_To_Long(String x) {
		if (x.length()==4) {
			int Hex;
			Hex = (Char_To_Int(x.charAt(0))<<12) + (Char_To_Int(x.charAt(1))<<8) + (Char_To_Int(x.charAt(2))<<4)
					+ Char_To_Int(x.charAt(3));
			int Num = (Hex>>6) & 0x03ff;
			int Index = Hex & 0x3f;
			int i = Num<<Index;
			return i;
		}
		return -1;
	}
	
	public final static String Long_To_Standard(int source){
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
		a += Int_To_Char((r>>12) & 0x0f);
		a += Int_To_Char((r>>8) & 0x0f);
		a += Int_To_Char((r>>4) & 0x0f);
		a += Int_To_Char(r & 0x0f);
		return a;
	}
	
	private static int Char_To_Int(char x){
		if (x-'0' >=0 && x-'0'<10){
			return x-'0';
		}
		else if (x-'A' >= 0 && x-'A'<6){
			return x-'A'+10;
		}
		else if (x-'a' >= 0 && x-'a'<6){
			return x-'a'+10;
		}
		else return -1;
	}
	public static String Int_To_Char(int x) {
		if (x>=0 && x<10){
			return "%".replace('%', (char) ('0'+x));
		}
		else if (x>9 && x<16){
			return "%".replace('%', (char) ('A'+x - 10));
		}
		else return "0";
	}
	private static boolean isNumeric(String str){  
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false; 
		}
		return true;
	}
}
