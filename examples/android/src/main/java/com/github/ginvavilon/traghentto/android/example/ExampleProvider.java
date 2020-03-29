/**
 *
 */
package com.github.ginvavilon.traghentto.android.example;

import android.content.res.AssetManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ginvavilon.traghentto.BaseSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.android.AndroidLogHadler;
import com.github.ginvavilon.traghentto.android.AssetSource;
import com.github.ginvavilon.traghentto.android.ResourceSource;
import com.github.ginvavilon.traghentto.android.provider.SimpleSourceProvider;
import com.github.ginvavilon.traghentto.crypto.Crypto;
import com.github.ginvavilon.traghentto.crypto.CryptoConfiguration;
import com.github.ginvavilon.traghentto.crypto.EncryptoSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.params.StreamParams;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ExampleProvider extends SimpleSourceProvider {

    private static final String ENCRYPED_ROOT = "encryped-files";
    private static final String ENCRYPED_DIR = "encryped";
    private static final String ASSET_ROOT = "assets";
    private static final String FILES_ROOT = "files";
    private static final String RESOURCES_ROOT = "resources";
    public static final String PATH = "public";
    public static final String IMAGE_PNG_MIME_TYPE = "image/png";

    static {
        AndroidLogHadler.init();
    }

    @Override
    protected int getRootIcon(String root) {
        switch (root) {
            case ENCRYPED_ROOT:
                return R.drawable.ic_encrypted;
            default:
                return R.mipmap.ic_launcher_round;
        }
    }

    @Override
    protected String getRootTitle(String root) {
        switch (root) {
        case ASSET_ROOT:
            return getContext().getString(R.string.label_assets);
        case FILES_ROOT:
            return getContext().getString(R.string.label_files);
        case RESOURCES_ROOT:
            return getContext().getString(R.string.label_resources);
        case ENCRYPED_ROOT:
            return getContext().getString(R.string.label_encrypted);
        default:
            return getContext().getString(R.string.app_name);
        }
    }

    @Override
    protected String getRootSummary(String root) {
        return null;
    }

    @Override
    protected List<String> createRootNames() {
        return Arrays.asList(ENCRYPED_ROOT, FILES_ROOT, ASSET_ROOT, RESOURCES_ROOT);
    }


    @Override
    protected boolean isSupportChild(Source source) {
        return !(source instanceof SimpleSource);
    }

    @Override
    protected Source getRootSource(String name) {

        switch (name) {
        case FILES_ROOT:
            return new FileSource(getContext().getFilesDir());
        case ENCRYPED_ROOT:
            return createdCryptedSource();
        case RESOURCES_ROOT:
            SimpleSource sources = new SimpleSource();
            sources.add(new ResourceSource(getContext().getResources(), R.mipmap.ic_launcher));
            sources.add(new ResourceSource(getContext().getResources(), R.mipmap.sample));
            sources.add(new ResourceSource(getContext().getResources(), R.mipmap.ic_launcher_round));
            sources.add(new ResourceSource(getContext().getResources(), R.mipmap.ic_launcher_foreground));
            return sources;
        case ASSET_ROOT:
        default:
            AssetManager assets = getContext().getAssets();

            return new AssetSource(assets, PATH);

        }
    }

    @Override
    protected boolean isSupportChildDetection(String root, Source source) {
        if (source instanceof SimpleSource) {
            return false;
        }
        return super.isSupportChildDetection(root, source);
    }

    private Source createdCryptedSource() {
        try {
            File file = new File(getContext().getFilesDir(), ENCRYPED_DIR);
            CryptoConfiguration configuration = CryptoConfiguration
                    .builder()
                    .usePassword("testKey8Exampl56")
                    .addRandomSalt(5, 12)
                    .build();
            if (!file.exists()) {
                file.mkdirs();
            }

            return Crypto.encode(new FileSource(file), configuration);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            Logger.e(e);
        }

        return null;
    }

    @Override
    protected int getIcon(Source source, String mimeType) {
        return R.mipmap.ic_launcher;
    }

    private class SimpleSource extends BaseSource {

        private Map<String, Source> mSources = new HashMap<>();

        public void add(Source source) {
            mSources.put(source.getPath(), source);
        }

        @Override
        public List<? extends Source> getChildren() {
            return new ArrayList<>(mSources.values());
        }

        @Override
        public Source getChild(String name) {
            Logger.d(Logger.Level.MODULE, "get child %s", name);
            return mSources.get(name);
        }

        @Override
        public boolean isConteiner() {
            return true;
        }

        @Override
        protected InputStream openInputStream(StreamParams pParams)
                throws IOException, IOSourceException {
            return null;
        }

        @Override
        public StreamResource<InputStream> openResource(StreamParams pParams)
                throws IOSourceException, IOException {
            return null;
        }

        @Override
        public String getPath() {
            return "virtual";
        }

        @Override
        public String getName() {
            return "virtual";
        }

        @Override
        public String getUriString() {
            return "virtual://virtual";
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public long getLenght() {
            return 0;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public boolean isDataAvailable() {
            return true;
        }

    }

    @Override
    protected String detectRoot(String id, Source source) {
        if (source instanceof ResourceSource) {
            return RESOURCES_ROOT;
        }
        return super.detectRoot(id, source);
    }

    @Override
    protected String getTypeForSource(Source source) {
        if (source instanceof ResourceSource) {
            return IMAGE_PNG_MIME_TYPE;
        }
        return super.getTypeForSource(source);
    }
}
