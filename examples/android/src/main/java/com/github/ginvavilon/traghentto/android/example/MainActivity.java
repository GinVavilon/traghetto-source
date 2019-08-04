package com.github.ginvavilon.traghentto.android.example;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ginvavilon.traghentto.DeletableSource;
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
    public static final String IMAGE_MIME_TIPE_PREFIX = "image/";

    public static final String URL = "https://www.w3schools.com/w3css/img_lights.jpg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";

    static {
        AndroidLogHadler.init();
    }

    private TextView mInputText;
    private TextView mOutputText;

    private ProgressBar mProgress;
    private ImageView mImageView;
    private String mMimeType = IMAGE_PNG;
    private TextView mTypeText;

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

        mInputText = findViewById(R.id.txt_input);
        mTypeText = findViewById(R.id.type);
        Source initialInput = SourceFactory.createFromResource(this, R.mipmap.sample);

        mInputText.setText(initialInput.getUriString());
        show(initialInput);

        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updateInput(v);
                return false;
            }
        });

        configureInputMenu();
    }

    private void configureInputMenu() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        mInputText.setCustomInsertionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.input_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                 return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                 return onOptionsItemSelected(menuItem);
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
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
            case R.id.item_open_web_image:
                openImageInputSource(SourceFactory.createFromUri(this, URL), IMAGE_JPEG);
                return true;
            case R.id.item_open_resource_image:
                openImageInputSource(SourceFactory.createFromResource(this, R.mipmap.sample), IMAGE_PNG);
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
        } else {
            mImageView.setImageDrawable(null);
        }

    }

    void openImageInputSource(Source inputSource, String mimeType){
        mMimeType = mimeType;
        mTypeText.setText(mMimeType);
        mInputText.setText(inputSource.getUriString());
        show(inputSource);
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

                WritableSource to = (WritableSource) sources[1];
                SourceUtils.copy(sources[0], to, true, null, null, this);

            } catch (IOException | SourceAlreadyExistsException | IOSourceException e) {
                showError(mView, e);

            }
            return sources[1];
        }
    }
}
