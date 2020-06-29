package com.example.child;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

public class getPermissions extends AppCompatActivity {


    WorkManager mWorkManagerCall,mWorkManagerMessage;
    public PeriodicWorkRequest mPeriodicWorkRequestCall,mPeriodicWorkRequestMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_permissions);
        mWorkManagerCall = WorkManager.getInstance();
        mWorkManagerMessage = WorkManager.getInstance();
    }


    public void startMessageManager(View view){

        mPeriodicWorkRequestMessage = new PeriodicWorkRequest.Builder(MessageWorkManager.class,
                16, TimeUnit.MINUTES)
                .addTag("periodicWorkRequest")
                .build();


        mWorkManagerMessage.getWorkInfoByIdLiveData(mPeriodicWorkRequestMessage.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    WorkInfo.State state = workInfo.getState();
                }
            }
        });

        mWorkManagerMessage.enqueue(mPeriodicWorkRequestMessage);



    }




    public void startCallManager(View view){

        mPeriodicWorkRequestCall = new PeriodicWorkRequest.Builder(CallWorkManager.class,
                16, TimeUnit.MINUTES)
                .addTag("periodicWorkRequest")
                .build();


        mWorkManagerCall.getWorkInfoByIdLiveData(mPeriodicWorkRequestCall.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    WorkInfo.State state = workInfo.getState();
             }
            }
        });

        mWorkManagerCall.enqueue(mPeriodicWorkRequestCall);



    }


}
