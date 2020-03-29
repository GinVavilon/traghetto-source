/**
 * 
 */
package com.github.ginvavilon.traghentto.android.provider;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.provider.DocumentsContract.Root;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author vbaraznovsky
 *
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public abstract class SimpleSourceProvider extends SourceDocumentsProvider {

    private static final char SEPARATOR_CHAR = '/';
    private static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);
    private static final String ALL_TYPES = "*/*";
    private static final String ROOT_NAME = "ROOT";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[] { Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID
    };

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private Map<String, Source> mRoots = new LinkedHashMap<>();

    @Override
    protected String getTypeForSource(Source source) {
        return getTypeForName(source.getName());
    }

    /**
     * Get the MIME data type of a document, given its filename.
     *
     * @param name
     *            the filename of the document
     * @return the MIME data type of a document
     */
    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            if (extension != null) {
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                if (mime != null) {
                    return mime;
                }
            }
        }
        return APPLICATION_OCTET_STREAM;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {

        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));

        for (Entry<String, Source> entry : mRoots.entrySet()) {
            addRoot(result, entry.getKey(), entry.getValue());
        }

        return result;
    }

    private void addRoot(final MatrixCursor result, String root, Source source) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, root);

        row.add(Root.COLUMN_SUMMARY, getRootSummary(root));

        row.add(Root.COLUMN_FLAGS, getRootFlags(root, source));

        row.add(Root.COLUMN_TITLE, getRootTitle(root));

        row.add(Root.COLUMN_DOCUMENT_ID, getDocumentId(root, source));
        row.add(Root.COLUMN_MIME_TYPES, getChildMimeTypes(root));

        row.add(Root.COLUMN_ICON, getRootIcon(root));
    }

    protected abstract int getRootIcon(String root);

    protected abstract String getRootTitle(String root);

    protected abstract String getRootSummary(String root);

    protected int getRootFlags(String root, Source source) {
        return (canWrite(source) ? Root.FLAG_SUPPORTS_CREATE : 0)
                | (isLocalOnly(source) ? Root.FLAG_LOCAL_ONLY : 0)
                | (isSupportChild(source) ? Root.FLAG_SUPPORTS_IS_CHILD : 0)
                | getFlagSupportsIsChild(root, source);
    }

    private int getFlagSupportsIsChild(String root,Source source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return isSupportChildDetection(root,source) ? Root.FLAG_SUPPORTS_IS_CHILD : 0;
        } else {
            return 0;
        }
    }

    protected boolean isSupportChildDetection(String root, Source source) {
        return source.isConteiner();
    }

    protected boolean isSupportChild(Source source) {
        return false;
    }

    protected boolean isLocalOnly(Source source) {
        return source.isLocal();
    }

    /**
     * Gets a string of unique MIME data types a directory supports, separated
     * by newlines. This should not change.
     *
     * @param root
     *            the name of root
     * @return a string of the unique MIME data types the parent directory
     *         supports
     */
    protected String getChildMimeTypes(String root) {
        return ALL_TYPES;
    }

    @Override
    public boolean onCreate() {
        List<String> rootNames = createRootNames();
        mRoots.clear();
        for (String name : rootNames) {
            Source root = getRootSource(name);
            mRoots.put(name, root);
        }

        return true;
    }

    protected abstract Source getRootSource(String name);

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        try {
            Source parentSource = getDocumentSource(parentDocumentId);
            Source documentSource = getDocumentSource(documentId);
            return SourceUtils.isChild(parentSource,documentSource);
        } catch (FileNotFoundException e) {
            return false;
        }


    }

    @Override
    protected Source getDocumentSource(String documentId) throws FileNotFoundException {
        Logger.d(Logger.Level.MODULE, "Create source for %s", documentId);
        if (mRoots.containsKey(documentId)) {
            return mRoots.get(documentId);
        }

        final int splitIndex = documentId.indexOf(SEPARATOR_CHAR, 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root for " + documentId);
        } else {

            final String path = documentId.substring(splitIndex + 1);
            final String rootName = documentId.substring(0, splitIndex);

            Logger.d(Logger.Level.MODULE, "Extracted path: '%s', root: '%s'", path, rootName);
            Source root = mRoots.get(rootName);
            if (root == null) {
                throw new FileNotFoundException("Missing root for " + documentId);
            }
            if (TextUtils.isEmpty(path)){
                return root;
            }
            Source target = root.getChild(path);

            if (!target.exists()) {
                throw new FileNotFoundException("Missing file for " + documentId + " at " + target);
            }

            Logger.d(Logger.Level.MODULE, "Source: '%s' -> '%s'", documentId, target);
            return target;
        }

    }

    @Override
    protected String getDocumentId(String parentId, Source source) {

        Logger.d(Logger.Level.MODULE, "Generate document id '%s'", source);

        String path = source.getPath();


        String rootName = detectRoot(parentId, source);
        String rootPath = mRoots.get(rootName).getPath();

        Logger.d(Logger.Level.MODULE, "Path root: '%s'", rootPath);

        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }
        if (path.startsWith(SEPARATOR)) {
            path = path.substring(SEPARATOR.length());
        }

        Logger.d(Logger.Level.MODULE, "Document id: '%s/%s'", rootName, path);
        return rootName + SEPARATOR_CHAR + path;
    }

    protected String detectRoot(String parentId, Source source) {
        int index = parentId.indexOf(SEPARATOR_CHAR);
        if (index > 0) {
            return parentId.substring(0, index);
        }
        String path = source.getPath();

        for (Entry<String, Source> entry : mRoots.entrySet()) {
            Source root = entry.getValue();
            String rootPath = root.getPath();
            if (path.startsWith(rootPath)) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Root not found");
    }

    protected List<String> createRootNames() {
        return Arrays.asList(ROOT_NAME);
    }

}
