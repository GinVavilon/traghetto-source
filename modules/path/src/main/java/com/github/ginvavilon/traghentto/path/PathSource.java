/**
 * 
 */
package com.github.ginvavilon.traghentto.path;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.ginvavilon.traghentto.BaseWritebleSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.RenamedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public class PathSource extends BaseWritebleSource implements Source, WritableSource {

    private final Path mPath;

    public PathSource(Path path) {
        super();
        mPath = path;
    }

    public PathSource(File file) {
        super();
        mPath = file.toPath();
    }


    @Override
    public boolean delete() {
        try {
            removeRecursive(mPath);
            return true;
        } catch (IOException e) {
            Logger.e(e);
            return false;
        }
    }
    
    public static void removeRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }
        });
    }

    
    @Override
    public RenamedSource createRenamedSource(String name) {
        Path parent = mPath.getParent();
        return new PathSource(parent.resolve(name));
    }
    
    @Override
    public boolean canBeRenamed(RenamedSource source) {
        return source instanceof PathSource;
    }

    @Override
    public void rename(RenamedSource source) throws RenameException {
        if (!(source instanceof PathSource)) {
            throw new RenameException("Source must be" + this.getClass().getName());
        }
        try {
            PathSource pathSource = (PathSource) source;

            Files.move(mPath, pathSource.mPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RenameException(e);
        }

    }

    @Override
    public boolean create() throws IOException {
        Path parent = mPath.getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.createFile(mPath);
        return Files.exists(mPath);

    }

    @Override
    public boolean createConteiner() throws IOException {
        Files.createDirectories(mPath);
        return Files.exists(mPath);
    }

    @Override
    public PathSource getChild(String name) {

        Path child = mPath.resolve(name);
        return new PathSource(child);
    }

    @Override
    protected OutputStream openOutputStream(StreamParams pParams) throws IOException {
        StreamParams params = SourceUtils.getSaflyParams(pParams);
        List<StandardOpenOption> options = new LinkedList<>();
        if (params.getProperty(ParamNames.APPEND, false)) {
            options.add(StandardOpenOption.APPEND);
        }

        if (params.getProperty(ParamNames.CREATE, true)) {
            options.add(StandardOpenOption.CREATE);
        }

        return Files.newOutputStream(mPath,
                options.toArray(new StandardOpenOption[options.size()]));

    }

    @Override
    protected InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException {
        return Files.newInputStream(mPath);
    }

    @Override
    public List<? extends PathSource> getChildren() {
        try {
            return Files
                    .list(mPath)
                    .map(PathSource::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean isConteiner() {
        return Files.isDirectory(mPath);
    }

    @Override
    public String getPath() {
        return mPath.toString();
    }

    @Override
    public String getName() {
        return mPath.getFileName().toString();
    }

    @Override
    public String getUriString() {
        return mPath.toUri().toString();
    }

    @Override
    public boolean exists() {
        return Files.exists(mPath);
    }

    @Override
    public long getLenght() {
        try {
            return Files.size(mPath);
        } catch (IOException e) {
            return UNKNOWN_LENGHT;
        }
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDataAvailable() {
        return Files.isReadable(mPath);
    }

    public Path getNioPath() {
        return mPath;
    }

    @Override
    public String toString() {
        return getUriString();
    }

    public static final SourceCreator<PathSource> CREATOR = new SourceCreator<PathSource>() {

        @Override
        public PathSource create(String pParam) {
            return new PathSource(Paths.get(pParam));
        }

    };

    @Override
    public boolean canBeDeleted() {
        return Files.isWritable(mPath);
    }

    @Override
    public boolean isWritable() {
        return Files.isWritable(mPath);
    }

}
