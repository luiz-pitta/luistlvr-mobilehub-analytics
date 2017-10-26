package br.pucrio.inf.lac.mhub;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", 
                formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text)

public class MobileHub extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init( this );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext( base );
        MultiDex.install( this );
    }
}
