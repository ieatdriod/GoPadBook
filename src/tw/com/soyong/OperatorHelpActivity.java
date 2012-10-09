package tw.com.soyong;

import com.taiwanmobile.myBook_PAD.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 操作說明頁面
 * @author Victor
 *
 */
public class OperatorHelpActivity extends Activity {
	 public void onCreate(Bundle savedInstanceState){
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.operator_help);
	        
	        
	        final  View view = (View)this.findViewById(R.id.help_layout);
	        view.setOnClickListener( new OnClickListener(){

				@Override
				public void onClick(View v) {
					finish();
				}
	        });
	 }
}
