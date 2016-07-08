package com.example.wy_black_develop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Get_Man_Answer extends Activity implements View.OnClickListener {
	
	private TextView receive;
	private TextView edit;
	private TextView edit_Found;
	private String x;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		x = getIntent().getStringExtra("Need_Reply");
		setContentView(R.layout.getans);
		receive = (TextView)findViewById(R.id.receive);
		edit = (TextView)findViewById(R.id.edit);
		edit_Found = (TextView)findViewById(R.id.edit_Found);
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.unkowned_Cmd).setOnClickListener(this);
		findViewById(R.id.return_Found).setOnClickListener(this);
		findViewById(R.id.return_Link_Finished).setOnClickListener(this);
		findViewById(R.id.return_Confirmed).setOnClickListener(this);
		findViewById(R.id.return_Auto).setOnClickListener(this);
		receive.setText(x);
		edit_Found.setText("");
		if (x.length() == "Find:".length() + 6 + 3) {
			int i = Translate_hardware.Standard_To_Long(x.substring("Find:".length(), "Find:".length() + 4));
			String A = "" + i;
			edit_Found.setText(A);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.send:
			Intent intent = new Intent();
			intent.putExtra("return", edit.getText().toString());
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.unkowned_Cmd:
			Intent intent2 = new Intent();
			intent2.putExtra("return", "Unknown_Cmd");
			setResult(RESULT_OK, intent2);
			finish();
			break;
		case R.id.return_Found:
			if (x.length() == "Find:".length() + 6 + 3) {
				String Unit = x.substring("Find:".length() + 4, "Find:".length() + 6);
				//String Num = x.substring("Find:".length(), "Find:".length() + 4);
				String Num = Translate_hardware.Long_To_Standard(Integer.parseInt(edit_Found.getText().toString()));
				Intent intent3 = new Intent();
				intent3.putExtra("return", "Found:" + Num + Unit);
				setResult(RESULT_OK, intent3);
				finish();
			}
			break;
		case R.id.return_Link_Finished:
			Intent intent4 = new Intent();
			intent4.putExtra("return", "Link_Finished");
			setResult(RESULT_OK, intent4);
			finish();
			break;
		case R.id.return_Confirmed:
			Intent intent5 = new Intent();
			intent5.putExtra("return", "Confirmed");
			setResult(RESULT_OK, intent5);
			finish();
			break;
		case R.id.return_Auto:
			if (x.length() == "Find:".length() + 6 + 3 && x.startsWith("Find:")) {
				Intent intent6 = new Intent();
				intent6.putExtra("return", "Found:" + x.substring("Find:".length(), "Find:".length() + 6));
				setResult(RESULT_OK, intent6);
				finish();
			}
			if (x.equals("Confirm$")){
				Intent intent6 = new Intent();
				intent6.putExtra("return", "Confirmed");
				setResult(RESULT_OK, intent6);
				finish();
			}
			if (x.length() == "Link:".length() + 4 + 3 && x.startsWith("Link:")){
				Intent intent6 = new Intent();
				intent6.putExtra("return", "Link_Finished");
				setResult(RESULT_OK, intent6);
				finish();
			}
		}
	}
	

}
