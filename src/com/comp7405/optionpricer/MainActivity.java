package com.comp7405.optionpricer;



import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.ListActivity;
import android.content.Intent;

public class MainActivity extends ListActivity {
	String Pricer[] = {"European Options","Geometric Asian Option","Arithmatic Asian Option", "Geometric Baseket Option", "Arithmatic Basket Option"};
	String ActName[] = {"EuorpeanOptionActivity","GeometricAsianActivity","ArithmaticAsianActivity", "GeometricBasketActivity", "ArithmaticBasketActivity" };

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		String StartActName = ActName[position];
		
		try{
			
			Class ourClass = Class.forName("com.comp7405.optionpricer."+ StartActName );
			Intent OurIntent = new Intent(MainActivity.this, ourClass);
			startActivity(OurIntent);
			
			}catch(ClassNotFoundException e){
				e.printStackTrace();
			};
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, Pricer));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
