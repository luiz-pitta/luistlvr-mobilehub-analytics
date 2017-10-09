package br.pucrio.inf.lac.mhub.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.pucrio.inf.lac.mhub.Constants;
import br.pucrio.inf.lac.mhub.R;
import br.pucrio.inf.lac.mhub.components.AppConfig;
import br.pucrio.inf.lac.mhub.model_server.Response;
import br.pucrio.inf.lac.mhub.model_server.User;
import br.pucrio.inf.lac.mhub.network.NetworkUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView loginButton, name;

    private SharedPreferences mSharedPreferences;
    private CompositeDisposable mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initSharedPreferences();

        name = (TextView) findViewById(R.id.name);
        loginButton = (TextView) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(this);

        String name = mSharedPreferences.getString(AppConfig.NAME, "");
        if(!name.equals("")) {
            Intent intent = new Intent(LoginActivity.this, MHubSettings.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
            initSharedPreferences();

        mSubscriptions = new CompositeDisposable();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initSharedPreferences() {
        mSharedPreferences = getSharedPreferences(AppConfig.SHARED_PREF_FILE, MODE_PRIVATE);
    }

    private void loginProcess(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit().login(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(AppConfig.NAME, name.getText().toString());
        editor.apply();

        startActivity(new Intent(LoginActivity.this, MHubSettings.class));
    }

    private void handleError(Throwable error) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        if(view == loginButton) {
            User usr = new User();
            String name_text = name.getText().toString();
            if(!name_text.equals("")) {
                usr.setName(name.getText().toString());
                loginProcess(usr);
            }else
                Toast.makeText(LoginActivity.this, "Preencha o nome", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }
}

