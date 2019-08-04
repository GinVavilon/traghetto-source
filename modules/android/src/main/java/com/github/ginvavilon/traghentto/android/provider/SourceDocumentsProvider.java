package com.github.ginvavilon.traghentto.android.provider;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsProvider;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.DeletableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.RenamedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.android.AssetFileDescriptorSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.SourceStreamParams;

/**
 * @author vbaraznovsky
 *
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public abstract class SourceDocumentsProvider extends DocumentsProvider {

    private static final String THUMBNAIL_MODE = "r";
    private static final String IMAGE_TYPE_PREFIX = "image/";
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = {
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
    };

    /**
     * Returns source by document id
     * 
     * @param documentId
     *            the id of document
     * @see Document#COLUMN_DOCUMENT_ID
     * @see #getDocumentId(Source)
     */
    protected abstract Source getDocumentSource(String documentId) throws FileNotFoundException;

    /**
     * Generate document id of {@link Source}
     * 
     * @param parentId
     *            the id of parent document
     * @param source
     *            the source
     * @see Document#COLUMN_DOCUMENT_ID
     * @see #getDocumentSource(String)
     */
    protected abstract String getDocumentId(String parentId, Source source);

    /**
     * Returns id of drawable resource
     * 
     * @param source
     *            the target resource
     * @param mimeType
     *            the MIME type
     */
    @DrawableRes
    protected abstract int getIcon(Source source, String mimeType);

    /**
     * Returns mime type of source
     * 
     * @param source
     *            the source
     */
    @NonNull
    protected abstract String getTypeForSource(Source source);

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection)
            throws FileNotFoundException {
        Logger.d(Logger.Level.MODULE, "Query source %s", documentId);
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        includeSource(result, documentId, null, documentId);
        return result;
    }


    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        try {
            Source parentSource = getDocumentSource(parentDocumentId);
            Source source = getDocumentSource(documentId);
            return SourceUtils.isChild(parentSource, source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection,
            String sortOrder) throws FileNotFoundException {
        Logger.d(Logger.Level.SOURCE, "Query children %s", parentDocumentId);
        Source source = getDocumentSource(parentDocumentId);

        List<? extends Source> children = source.getChildren();

        return extractCursor(projection, children, parentDocumentId);
    }

    private Cursor extractCursor(String[] projection, List<? extends Source> sources,
            String parentId)
            throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        for (Source child : sources) {
            includeSource(result, null, child, parentId);
        }
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode,
            CancellationSignal signal) throws FileNotFoundException {
        Source source = getDocumentSource(documentId);
        try {
            return open(source, mode, signal);
        } catch (IOSourceException | IOException e) {
            throw new FileNotFoundException("Source" + documentId + " not found");
        }

    }

    private ParcelFileDescriptor open(Source source, String mode, CancellationSignal signal)
            throws IOException, IOSourceException {

        int accessMode = ParcelFileDescriptor.parseMode(mode);

        if (source instanceof FileSource) {
            FileSource fileSource = (FileSource) source;
            return ParcelFileDescriptor.open(fileSource.getFile(), accessMode);
        }

        if (checkFlags(accessMode, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return openForRead(source, accessMode, signal);
        }

        if (checkFlags(accessMode, ParcelFileDescriptor.MODE_WRITE_ONLY)
                && (source instanceof WritableSource)) {
            return openForWrite((WritableSource) source, accessMode, signal);
        }

        throw new IOException("Mode " + mode + " is not supported for source " + source);

    }

    private ParcelFileDescriptor openForRead(Source source, int accessMode,
            CancellationSignal signal)
            throws IOException, IOSourceException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide);
        SourceStreamParams params = createStreamParams(accessMode);
        StreamResource<InputStream> resource = source.openResource(params);

        ReadResourceThread thread = new ReadResourceThread(resource, outputStream);
        if (signal != null) {
            signal.setOnCancelListener(thread::interrupt);
        }
        thread.start();

        return readSide;
    }

    private ParcelFileDescriptor openForWrite(WritableSource source, int accessMode,
            CancellationSignal signal)
            throws IOException, IOSourceException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(readSide);

        SourceStreamParams params = createStreamParams(accessMode);

        StreamResource<OutputStream> resource = source.openOutputResource(params);

        WriteResourceThread thread = new WriteResourceThread(resource, inputStream);
        if (signal != null) {
            signal.setOnCancelListener(thread::interrupt);
        }
        thread.start();

        return writeSide;
    }

    public static SourceStreamParams createStreamParams(int accessMode) {
        SourceStreamParams params = new SourceStreamParams();
        params.set(ParamNames.APPEND,
                checkFlags(accessMode, ParcelFileDescriptor.MODE_APPEND));

        params.set(ParamNames.CREATE,
                checkFlags(accessMode, ParcelFileDescriptor.MODE_CREATE));
        return params;
    }

    private static boolean checkFlags(int accessMode, int mode) {
        return (accessMode & mode) == mode;
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName)
            throws FileNotFoundException {

        Source parent = getDocumentSource(parentDocumentId);
        Source child = parent.getChild(displayName);

        try {
            createSource(child, mimeType);
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to create document with name " +
                    displayName + " and documentId " + parentDocumentId);
        }

        String documentId = getDocumentId(parentDocumentId, child);
        Logger.d(Logger.Level.MODULE, "Source created %s ", documentId);
        return documentId;
    }

    /**
     * Creates new source
     * 
     * @param source
     *            the source for creation
     * @param mimeType
     *            the mime type of new source
     */
    protected void createSource(Source source, String mimeType)
            throws IOException {
        if (source instanceof WritableSource) {
            WritableSource writableSource = (WritableSource) source;
            if (Document.MIME_TYPE_DIR.equals(mimeType)) {
                writableSource.createConteiner();
            } else {
                writableSource.create();
            }
        } else {
            throw new IllegalArgumentException("Source must be writeble");
        }

    }

    @Override
    public String renameDocument(String documentId, String displayName)
            throws FileNotFoundException {
        Source source = getDocumentSource(documentId);
        Source newSource = renameSource(source, displayName);
        return getDocumentId(documentId, newSource);
    }

    protected Source renameSource(Source source, String displayName) throws FileNotFoundException {
        RenamedSource renamedSource = (RenamedSource) source;
        try {
            RenamedSource newSource = renamedSource.createRenamedSource(displayName);
            if (!renamedSource.canBeRenamed(newSource)) {
                throw new RuntimeException("Unavalable rename to" + displayName);
            }
            renamedSource.rename(newSource);
            return newSource;
        } catch (RenameException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        Source source = getDocumentSource(documentId);
        if (canDelete(source) && (source instanceof DeletableSource)) {
            DeletableSource deletableSource = (DeletableSource) source;

            if (deletableSource.delete()) {
                return;
            }
        }
        throw new FileNotFoundException("Failed to delete document with id " + documentId);
    }

    /**
     * Add a representation of a source to a cursor.
     *
     * @param result
     *            the cursor to modify
     * @param docId
     *            the document ID representing the desired file (may be null if
     *            given file)
     * @param source
     *            the {@link Source} object representing the desired file (may
     *            be null if given docID)
     * @throws java.io.FileNotFoundException
     */
    private void includeSource(MatrixCursor result, String docId, Source source, String parentId)
            throws FileNotFoundException {
        if (docId == null) {
            docId = getDocumentId(parentId, source);
        } else {
            source = getDocumentSource(docId);
        }

        int flags = 0;

        if (source.isConteiner()) {
            if (canWrite(source)) {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            }
        } else {
            if (canWrite(source)) {
                flags |= Document.FLAG_SUPPORTS_WRITE;
            }
        }

        if (canDelete(source)) {
            flags |= Document.FLAG_SUPPORTS_DELETE;
        }

        if (canRename(source)) {
            flags |= Document.FLAG_SUPPORTS_RENAME;
        }

        final String displayName = source.getName();

        final String mimeType = getMimeType(source);

        if (hasThumbnail(source, mimeType)) {
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, source.getLenght());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_FLAGS, flags);

        row.add(Document.COLUMN_ICON, getIcon(source, mimeType));
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint,
            CancellationSignal signal)
            throws FileNotFoundException {
        try {
            Source source = getDocumentSource(documentId);

            if (source instanceof AssetFileDescriptorSource) {
                AssetFileDescriptorSource assetSource = (AssetFileDescriptorSource) source;
                return assetSource.openAssetFileDescriptor();
            }

            final ParcelFileDescriptor descriptor = open(source, THUMBNAIL_MODE, signal);

            return new AssetFileDescriptor(descriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
        } catch (IOException | IOSourceException e) {
            throw new FileNotFoundException();
        }
    }

    @NonNull
    private String getMimeType(Source source) {
        if (source.isConteiner()) {
            return Document.MIME_TYPE_DIR;
        }
        return getTypeForSource(source);
    }

    /**
     * Checks if source has the thumbnail
     * 
     * @param source
     *            the source
     * @param mimeType
     *            the mime type of source
     */
    protected boolean hasThumbnail(Source source, @NonNull final String mimeType) {
        return mimeType.startsWith(IMAGE_TYPE_PREFIX);
    }

    /**
     * Check if source can be deleted
     * 
     * @param source
     *            the source
     */
    protected boolean canDelete(Source source) {
        if (source instanceof DeletableSource) {
            return ((DeletableSource) source).canBeDeleted();

        }
        return false;
    }

    /**
     * Check if source can be renamed
     * 
     * @param source
     *            the source
     */
    protected boolean canRename(Source source) {
        return source instanceof RenamedSource;
    }

    /**
     * Check if source can be changed
     * 
     * @param source
     *            the source
     */
    protected final boolean canWrite(Source source) {
        return (source instanceof WritableSource)
                && (((WritableSource) source).isWritable());
    }

}
