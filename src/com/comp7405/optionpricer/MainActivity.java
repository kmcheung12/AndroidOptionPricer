package com.comp7405.optionpricer;



import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.ListActivity;
import android.content.Intent;

public class MainActivity extends ListActivity {
	private static String Pricer[] = {"European Options","Geometric Asian Option","Arithmatic Asian Option", "Geometric Baseket Option", "Arithmatic Basket Option"};
	private static String ActName[] = {"EuorpeanOptionActivity","GeometricAsianActivity","ArithmaticAsianActivity", "GeometricBasketActivity", "ArithmaticBasketActivity" };
    private PowerManager.WakeLock mWakeLock;

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		String StartActName = ActName[position];
		
		try{
			
			Class clz = Class.forName("com.comp7405.optionpricer."+ StartActName );
			Intent intent = new Intent(MainActivity.this, clz);
			startActivity(intent);
			
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        };
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, Pricer));
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }
}
