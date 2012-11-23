package com.mattprecious.smsfix.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.code.microlog4android.Logger;
import com.mattprecious.smsfix.library.util.LoggerHelper;

public class LogNote extends Activity {
    private EditText logNoteText;
    private Button okButton;
    private Button cancelButton;
    
    private LoggerHelper logger;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_note);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        logger = LoggerHelper.getInstance(getApplicationContext());
        
        logNoteText = (EditText) findViewById(R.id.log_note);
        okButton = (Button) findViewById(R.id.ok);
        cancelButton = (Button) findViewById(R.id.cancel);

        setTitle(R.string.add_note);
        
        okButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String note = logNoteText.getText().toString();
                logger.userNote(note);
                
                finish();
            }
        });
        
        cancelButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
