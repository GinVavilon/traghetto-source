package com.github.ginvavilon.traghentto.android.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.android.AndroidLogHadler;
import com.github.ginvavilon.traghentto.android.DocumentSource;
import com.github.ginvavilon.traghentto.android.SourceFactory;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;

import java.io.IOException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_NAME = "new";
    public static final String OPEN_TYPE = "*/*";

    public static final String URL = "https://www.w3schools.com/w3css/img_lights.jpg";
    static {
        AndroidLogHadler.init();
    }

    private TextView mInputText;
    private TextView mOutputText;

    private ProgressBar mProgress;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = findViewById(R.id.progress);
        mInputText = findViewById(R.id.txt_input);
        mOutputText = findViewById(R.id.txt_output);
        mImageView = findViewById(R.id.image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::startCopy);
        Source initSource = SourceFactory.createFromUri(this, URL);

        mInputText.setText(initSource.getUriString());
        show(initSource);
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updateInput(v);
                return false;
            }
        });
        findViewById(R.id.btn_input).setOnClickListener(this::openInput);
        findViewById(R.id.btn_output_open).setOnClickListener(this::openOutput);
        findViewById(R.id.btn_output_create).setOnClickListener(this::createOutput);
    }

    private void updateInput(TextView v) {
        CharSequence text = v.getText();
        Source source = SourceFactory.createFromUri(v.getContext(), text.toString());
        show(source);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateInput(mInputText);
    }

    private void startCopy(View view) {
        Source input = SourceFactory.createFromUri(this, mInputText.getText().toString());
        Source output = SourceFactory.createFromUri(this, mOutputText.getText().toString());
        mProgress.setMax((int) input.getLenght());
        new CopyAsyncTask(view).executeOnExecutor(Executors.newCachedThreadPool(), input, output);
    }

    private void showError(View view, Throwable e) {
        Logger.e(e);
        Snackbar snackbar = Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT);
        runOnUiThread(snackbar::show);
    }

    private void createOutput(View view) {
        createFile(DocumentSource.DEFAULT_MIME_TYPE, DEFAULT_NAME);
    }

    private void openOutput(View view) {
        openIntent(OPEN_OUTPUT_REQUEST_CODE);
    }

    private void openInput(View view) {

        openIntent(OPEN_INPUT_REQUEST_CODE);
    }

    private static final int OPEN_INPUT_REQUEST_CODE = 42;
    private static final int OPEN_OUTPUT_REQUEST_CODE = 43;

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, OPEN_OUTPUT_REQUEST_CODE);
    }


    private void openIntent(int code) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(OPEN_TYPE);
        startActivityForResult(intent, code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Uri uri = null;
        if (resultData != null) {
            uri = resultData.getData();
        }
        if (uri == null) {
            return;
        }

        switch (requestCode) {
            case OPEN_INPUT_REQUEST_CODE:
                onOpenInput(uri);
                break;
            case OPEN_OUTPUT_REQUEST_CODE:
                onOpenOutput(uri);
                break;
        }
    }


    private void onOpenOutput(Uri uri) {
        DocumentSource source = new DocumentSource(getContentResolver(), uri);
        mOutputText.setText(source.getUriString());
    }

    private void onOpenInput(Uri uri) {
        DocumentSource inputSource = new DocumentSource(getContentResolver(), uri);
        mInputText.setText(inputSource.getUriString());

        if (inputSource.getDocumentInfo().getMimeType().startsWith("image/")) {
            show(inputSource);
        } else {
            mImageView.setImageDrawable(null);
        }

    }

    private void show(Source source) {

        GlideApp.with(this)
                .load(source)
                .into(mImageView);
    }

    private class CopyAsyncTask extends AsyncTask<Source, Long, Source> implements StreamUtils.ICopyListener {

        private final View mView;

        public CopyAsyncTask(View view) {
            mView = view;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            mProgress.setProgress(values[0].intValue());
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onProgress(long pRadedByte) {
            publishProgress(pRadedByte);
        }

        @Override
        public void onCompite() {
        }

        @Override
        public void onFail(Throwable pE) {
            showError(mView, pE);
        }

        @Override
        protected Source doInBackground(Source... sources) {

            try {
                SourceUtils.copy(sources[0], (WritableSource) sources[1], true, null, null, this);

            } catch (IOException | SourceAlreadyExistsException | IOSourceException e) {
                showError(mView, e);

            }
            return sources[1];
        }
    }
}
