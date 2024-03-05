/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.BaseWritableSource;
import com.github.ginvavilon.traghentto.RenamedSource;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 */
public class DocumentSource extends BaseWritableSource {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String PATH_SEPARATOR = "/";

    private ContentResolver mContentResolver;
    private Uri mUri;
    private DocumentInfo mDocumentInfo = null;
    private Uri mParentDocument;

    public DocumentSource(ContentResolver contentResolver, Uri uri) {
        super();
        mContentResolver = contentResolver;
        mUri = uri;
    }

    private DocumentSource(ContentResolver contentResolver, Uri uri, DocumentInfo documentInfo,
                           Uri parentDocument) {
        super();
        mContentResolver = contentResolver;
        mUri = uri;
        mDocumentInfo = documentInfo;
        mParentDocument = parentDocument;
    }

    @Override
    public boolean create() throws IOException {
        if (exists()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUri = DocumentsContract.createDocument(mContentResolver,
                    toDocumentUri(mParentDocument),
                    getDocumentInfo().getMimeType(),
                    getDocumentInfo().getName());
            if (mUri != null) {
                requestInfo();
            }
        }
        return mUri != null;
    }

    @Override
    public boolean createContainer() throws IOException {
        return false;
    }

    @Override
    public WritableSource getChild(String name) {

        for (DocumentSource source : getChildren()) {
            if (name.equals(source.getName())){
                return source;
            }
        }

        return new DocumentSource(mContentResolver, null, new NameDocumentInfo(name), mUri);
    }

    @Override
    public boolean delete() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return DocumentsContract.deleteDocument(mContentResolver, mUri);
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<? extends DocumentSource> getChildren() {
        List<DocumentSource> children = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return children;
        }
        Uri uri = toDocumentUri(mUri);
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                DocumentsContract.getDocumentId(uri));

        try (Cursor cursor = mContentResolver.query(childrenUri, null, null, null, null)) {
            if (cursor != null) {
                CursorColumns columns = new CursorColumns(cursor);
                while (cursor.moveToNext()) {
                    CursorInfo info = new CursorInfo(cursor, columns);
                    DocumentSource child = createChild(info);
                    children.add(child);
                }
            }
        }

        return children;
    }


    @TargetApi(Build.VERSION_CODES.N)
    private static Uri toDocumentUri(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (DocumentsContract.isTreeUri(uri)) {
                return DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri));
            }
        }
        return uri;
    }

    private DocumentSource createChild(CursorInfo info) {
        Uri uri = createChildUri(info.getId());
        return new DocumentSource(mContentResolver, uri, info, mUri);
    }

    private Uri createChildUri(String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return DocumentsContract.buildDocumentUriUsingTree(mUri, id);
        }
        return mUri.buildUpon().appendPath(id).build();
    }

    @Override
    public boolean isContainer() {
        if (mUri == null) {
            return false;
        }
        String type = getDocumentInfo().getMimeType();
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(type);
    }

    @Override
    public String getPath() {
        return mUri.getPath();
    }

    @Override
    public String getName() {
        return getDocumentInfo().getName();
    }

    @Override
    public String getUriString() {

        return String.valueOf(mUri);
    }

    @Override
    public boolean exists() {
        return (mUri != null) && (getDocumentInfo() != null);
    }

    @Override
    public long getLength() {
        return getDocumentInfo().getSize();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDataAvailable() {
        return exists();
    }

    @Override
    public RenamedSource createRenamedSource(String name) {
        return new DocumentSource(mContentResolver, null, new NameDocumentInfo(name), mParentDocument);
    }

    @Override
    public boolean canBeRenamed(RenamedSource source) {
        if (source instanceof DocumentSource) {

            DocumentSource documentSource = (DocumentSource) source;
            return (documentSource.mUri == null)
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    && (documentSource.mParentDocument != null)
                    && (mParentDocument.equals(getParentUri())
                    || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N));
        }
        return false;
    }

    private Uri getParentUri() {
        if (mParentDocument != null) {
            return mParentDocument;
        }
        if (mUri == null) {
            return null;
        }
        List<String> pathSegments = mUri.getPathSegments();

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < pathSegments.size() - 1; i++) {
            path.append(pathSegments.get(i));
            path.append(PATH_SEPARATOR);
        }
        return mUri.buildUpon().path(path.toString()).build();
    }

    @Override
    public void rename(RenamedSource source) throws RenameException {
        if (!(source instanceof DocumentSource)) {
            throw new RenameException("Not support source for rename");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new RenameException("Not support rename parent on android less then 24");
        }

        try {

            DocumentSource documentSource = (DocumentSource) source;
            String oldName = getName();
            String newName = source.getName();
            Uri newUri = null;
            Uri parentUri = getParentUri();
            if (parentUri == null) {
                throw new RenameException("Unknown parent");
            }
            Uri newParentUri = documentSource.getParentUri();
            if (newParentUri == null) {
                throw new RenameException("Unknown new parent");
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                newUri = DocumentsContract.moveDocument(mContentResolver, mUri, parentUri,
                        newParentUri);
            } else {
                if (!parentUri.equals(newParentUri)) {
                    throw new RenameException("Not support rename to different parent on android less then 24");
                }
            }

            if (!oldName.equals(newName)) {
                DocumentsContract.renameDocument(mContentResolver, newUri, newName);
            }

        } catch (FileNotFoundException e) {
            throw new RenameException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void rename(String name) throws RenameException {
        try {
            mUri = DocumentsContract.renameDocument(mContentResolver, mUri, name);
            mDocumentInfo = null;
        } catch (FileNotFoundException e) {
            throw new RenameException(e);
        }
    }

    @Override
    protected OutputStream openOutputStream(StreamParams pParams) throws IOException {
        return mContentResolver.openOutputStream(mUri);
    }

    @Override
    protected InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException {
        return mContentResolver.openInputStream(mUri);
    }

    public DocumentInfo getDocumentInfo() {
        if (mDocumentInfo == null) {
            requestInfo();
        }
        return mDocumentInfo;
    }

    private void requestInfo() {
        try (Cursor cursor = mContentResolver.query(toDocumentUri(mUri), null, null, null, null)) {
            if ((cursor != null) && (cursor.moveToNext())) {
                mDocumentInfo = createInfo(cursor);
            }
        }
    }

    private CursorInfo createInfo(Cursor cursor) {
        return new CursorInfo(cursor);
    }

    public interface DocumentInfo {
        String getName();

        String getMimeType();

        long getSize();

        boolean isSupportDelete();

        boolean isSupportWrite();
    }

    private final class NameDocumentInfo implements DocumentInfo {
        private static final char DOT = '.';
        private final String mName;

        private NameDocumentInfo(String name) {
            mName = name;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getMimeType() {
            int last = mName.lastIndexOf(DOT);
            if (last > 1) {
                String extension = mName.substring(last);
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            return DocumentSource.DEFAULT_MIME_TYPE;
        }

        @Override
        public long getSize() {
            return UNKNOWN_LENGTH;
        }

        @Override
        public boolean isSupportDelete() {
            return false;
        }

        @Override
        public boolean isSupportWrite() {
            return false;
        }
    }

    private static class CursorInfo implements DocumentInfo {

        private String mName;
        private long mSize;
        private String mMimeType;
        private String mId;
        private int mFlags;

        public CursorInfo(Cursor cursor) {
            this(cursor, new CursorColumns(cursor));
        }

        public String getId() {
            return mId;
        }

        public CursorInfo(Cursor cursor, CursorColumns columns) {
            mName = columns.getName(cursor);
            mSize = columns.getSize(cursor);
            mMimeType = columns.getMimeType(cursor);
            mId = columns.getId(cursor);
            mFlags = columns.getFlags(cursor);
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public long getSize() {
            return mSize;
        }

        @Override
        public String getMimeType() {
            return mMimeType;
        }

        @Override
        public boolean isSupportDelete() {
            return checkFlag(Document.FLAG_SUPPORTS_DELETE);
        }

        private boolean checkFlag(int flags) {
            return (mFlags & flags) == flags;
        }

        @Override
        public boolean isSupportWrite() {
            return checkFlag(Document.FLAG_SUPPORTS_WRITE)
                    || checkFlag(Document.FLAG_DIR_SUPPORTS_CREATE);
        }

    }

    private static class CursorColumns {

        private int mName;
        private int mId;
        private int mSize;
        private int mMimeType;
        private int mFlags;

        public CursorColumns(Cursor cursor) {
            mName = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            mId = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            mSize = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);
            mMimeType = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            mFlags = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS);
        }

        public String getName(Cursor cursor) {
            return getString(cursor, mName, "");
        }

        public String getId(Cursor cursor) {
            return getString(cursor, mId, "");
        }

        public String getMimeType(Cursor cursor) {
            return getString(cursor, mMimeType, "");
        }

        public long getSize(Cursor cursor) {
            return getLong(cursor, mSize, UNKNOWN_LENGTH);
        }

        public int getFlags(Cursor cursor) {
            return getInt(cursor, mFlags, 0);
        }

        private String getString(Cursor cursor, int column, String def) {
            if (column < 0) {
                return def;
            }
            return cursor.getString(column);
        }

        private long getLong(Cursor cursor, int column, long def) {
            if (column < 0) {
                return def;
            }
            return cursor.getLong(column);
        }

        private int getInt(Cursor cursor, int column, int def) {
            if (column < 0) {
                return def;
            }
            return cursor.getInt(column);
        }
    }


    @Override
    public boolean isWritable() {
        return mDocumentInfo.isSupportWrite();
    }

    @Override
    public boolean canBeDeleted() {
        return mDocumentInfo.isSupportDelete();
    }

    public static final AndroidSourceCreator<DocumentSource> ANDROID_CREATOR = new AndroidSourceCreator<DocumentSource>() {

        @Override
        public DocumentSource create(Context context, Uri uri) {
            return new DocumentSource(context.getContentResolver(), uri);
        }
    };
}
