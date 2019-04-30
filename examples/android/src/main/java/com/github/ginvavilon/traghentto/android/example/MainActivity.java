package com.github.ginvavilon.traghentto.android.example;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

import com.github.ginvavilon.traghentto.DeletableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.android.AndroidLogHadler;
import com.github.ginvavilon.traghentto.android.DocumentSource;
import com.github.ginvavilon.traghentto.android.SourceFactory;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_NAME = "new";
    public static final String OPEN_TYPE = "*/*";
    public static final String IMAGE_MIME_TIPE_PREFIX = "image/";

    static {
        AndroidLogHadler.init();
    }

    private TextView mInputText;
    private TextView mOutputText;

    private ProgressBar mProgress;
    private String mMimeType = "image/png";
    private TextView mTypeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgress = findViewById(R.id.progress);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::startCopy);
        mInputText = findViewById(R.id.txt_input);
        mTypeText = findViewById(R.id.type);
        Source initialInput = SourceFactory.createFromResource(this, R.mipmap.sample);
        mInputText.setText(initialInput.getUriString());
        show(initialInput);
        mOutputText = findViewById(R.id.txt_output);
    }

    private void startCopy(View view) {
        Source input = getInputSource();
        Source output = SourceFactory.createFromUri(this, mOutputText.getText().toString());
        mProgress.setMax((int) input.getLenght());
        new CopyAsyncTask(view).executeOnExecutor(Executors.newCachedThreadPool(), input, output);
    }

    private void showError(View view, Throwable e) {
        Logger.e(e);
        Snackbar snackbar = Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT);
        runOnUiThread(snackbar::show);
    }

    private void createOutput() {
        createFile(mMimeType, DEFAULT_NAME);
    }

    private void openOutput() {
        openIntent(OPEN_OUTPUT_REQUEST_CODE);
    }

    private void openInput() {
        openIntent(OPEN_INPUT_REQUEST_CODE);
    }

    private void openTreeInput() {
        openTreeIntent(OPEN_INPUT_REQUEST_CODE);
    }

    private void openTreeOutput() {
        openTreeIntent(OPEN_OUTPUT_REQUEST_CODE);
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

    private void openTreeIntent(int code) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, code);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_open:
                openInput();
                return true;
            case R.id.item_open_tree:
                openTreeInput();
                return true;
            case R.id.item_replace:
                openOutput();
                return true;
            case R.id.item_create:
                createOutput();
                return true;
            case R.id.item_save_tree:
                openTreeOutput();
                return true;
            case R.id.item_delete:
                onDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDelete() {
        Source input = getInputSource();
        if (input instanceof DeletableSource){
            if (((DeletableSource) input).delete()){
                Snackbar.make(mInputText, R.string.result_deleted,Snackbar.LENGTH_SHORT).show();
                return;
            }
            Snackbar.make(mInputText, R.string.fail_delete,Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(mInputText, R.string.failr_not_deletable,Snackbar.LENGTH_SHORT).show();

    }

    private Source getInputSource() {
        return SourceFactory.createFromUri(this, mInputText.getText().toString());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onOpenOutput(Uri uri) {
        DocumentSource source = new DocumentSource(getContentResolver(), uri);
        mOutputText.setText(source.getUriString());
    }

    private void onOpenInput(Uri uri) {

        DocumentSource inputSource = new DocumentSource(getContentResolver(), uri);
        mInputText.setText(inputSource.getUriString());

        mMimeType = inputSource.getDocumentInfo().getMimeType();
        mTypeText.setText(mMimeType);
        if (mMimeType.startsWith(IMAGE_MIME_TIPE_PREFIX)) {
            show(inputSource);
        }

    }

    private void show(Source source) {
        ImageView view = findViewById(R.id.image);
        try (StreamResource<InputStream> resource = source.openResource(null)) {

            Bitmap bitmap = BitmapFactory.decodeStream(resource.getStream());
            view.setImageBitmap(bitmap);
        } catch (IOException | IOSourceException e) {
            Logger.e(e);
        }
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

                WritableSource to = (WritableSource) sources[1];
                SourceUtils.copy(sources[0], to, true, null, null, this);

            } catch (IOException | SourceAlreadyExistsException | IOSourceException e) {
                showError(mView, e);

            }
            return sources[1];
        }
    }
}
