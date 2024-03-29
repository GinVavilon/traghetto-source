package com.github.ginvavilon.traghentto.android.example;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ginvavilon.traghentto.DeletableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.RetrievableSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.android.AndroidLogHandler;
import com.github.ginvavilon.traghentto.android.AndroidSourceFactory;
import com.github.ginvavilon.traghentto.android.DocumentSource;
import com.github.ginvavilon.traghentto.android.GooglePlayAssetSource;
import com.github.ginvavilon.traghentto.android.GooglePlayAssetSourceCreator;
import com.github.ginvavilon.traghentto.android.GooglePlayAssetSourceUi;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_NAME = "new";
    public static final String OPEN_TYPE = "*/*";
    public static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    public static final String URL = "https://www.w3schools.com/w3css/img_lights.jpg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";

    static {
        AndroidLogHandler.init();

    }

    private TextView mInputText;
    private TextView mOutputText;

    private ProgressBar mProgress;
    private ImageView mImageView;
    private String mMimeType = IMAGE_PNG;
    private TextView mTypeText;

    private AndroidSourceFactory mSourceFactory;
    private GooglePlayAssetSourceUi mGooglePlayAssetSourceUi = new GooglePlayAssetSourceUi(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSourceFactory = AndroidSourceFactory
                .createDefaultBuilder(this)
                .register(new GooglePlayAssetSourceCreator(this))
                .build();

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
        //Source initialInput = SourceFactory.createFromResource(this, R.mipmap.sample);
        GooglePlayAssetSource initialInput = new GooglePlayAssetSource(this,"fast_follow_pack","fast.jpeg");
        RetrievableSource.Controller controller = initialInput.getController();

        controller.registerListener(new RetrievableSource.Listener() {
            @Override
            public void onStatusUpdate(RetrievableSource source, RetrievableSource.Status status) {
                Logger.d(Logger.Level.APPLICATION, "Update status %s of %s", status, source);
                if (status == RetrievableSource.Status.READY){
                    show(initialInput);
                }
            }

            @Override
            public void onProgress(RetrievableSource source, long readyBytes, long fullBytes) {
                Logger.d(Logger.Level.APPLICATION, "Update progress %s to %s of %s", readyBytes, fullBytes, source);
            }

            @Override
            public void onError(RetrievableSource source, Throwable throwable) {
                Logger.e(Logger.Level.APPLICATION, throwable);
            }
        });
        mInputText.setText(initialInput.getUriString());
        if (initialInput.isDataAvailable()) {
            show(initialInput);
        } else {
            controller.fetch();
        }
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updateInput(v);
                return false;
            }
        });

        configureInputMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGooglePlayAssetSourceUi.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGooglePlayAssetSourceUi.start();
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
        Source source = mSourceFactory.createFromUri(text.toString());
        show(source);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateInput(mInputText);
    }

    private void startCopy(View view) {
        Source input = getInputSource();
        Source output = mSourceFactory.createFromUri(mOutputText.getText().toString());
        if (!(output instanceof WritableSource)) {
            showError(view, getString(R.string.error_output_is_not_writable));
            return;
        }
        mProgress.setMax((int) input.getLength());
        new CopyAsyncTask(view).executeOnExecutor(Executors.newCachedThreadPool(), input, output);
    }

    private void showError(View view, Throwable e) {
        Logger.e(e);
        String message = e.getMessage();
        showError(view, message);
    }

    private void showError(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
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
        int itemId = item.getItemId();
        if (itemId == R.id.item_open) {
            openInput();
            return true;
        } else if (itemId == R.id.item_open_tree) {
            openTreeInput();
            return true;
        } else if (itemId == R.id.item_replace) {
            openOutput();
            return true;
        } else if (itemId == R.id.item_create) {
            createOutput();
            return true;
        } else if (itemId == R.id.item_save_tree) {
            openTreeOutput();
            return true;
        } else if (itemId == R.id.item_delete) {
            onDelete();
            return true;
        } else if (itemId == R.id.item_open_web_image) {
            openImageInputSource(mSourceFactory.createFromUri(URL), IMAGE_JPEG);
            return true;
        } else if (itemId == R.id.item_open_resource_image) {
            openImageInputSource(mSourceFactory.createFromResource(R.mipmap.sample), IMAGE_PNG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDelete() {
        Source input = getInputSource();
        if (input instanceof DeletableSource) {
            if (((DeletableSource) input).delete()) {
                Snackbar.make(mInputText, R.string.result_deleted, Snackbar.LENGTH_SHORT).show();
                return;
            }
            Snackbar.make(mInputText, R.string.fail_delete, Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(mInputText, R.string.fail_not_deletable, Snackbar.LENGTH_SHORT).show();

    }

    private Source getInputSource() {
        return mSourceFactory.createFromUri(mInputText.getText().toString());
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
        if (mMimeType.startsWith(IMAGE_MIME_TYPE_PREFIX)) {
            show(inputSource);
        } else {
            mImageView.setImageDrawable(null);
        }

    }

    void openImageInputSource(Source inputSource, String mimeType) {
        mMimeType = mimeType;
        mTypeText.setText(mMimeType);
        mInputText.setText(inputSource.getUriString());
        show(inputSource);
    }

    private void show(Source source) {

        GlideApp.with(this)
                .load(source)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageView);
    }

    private class CopyAsyncTask extends AsyncTask<Source, Long, Source> implements SourceUtils.ICopyListener {

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
        public void onProgress(long pReadBytes) {
            publishProgress(pReadBytes);
        }

        @Override
        public void onComplete() {
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
