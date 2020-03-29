/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladimir Baraznovsky
 *
 * @param <T>
 */
public abstract class BaseSourceFactory<T> {

    private final Map<String, Data> mCreators = new HashMap<>();

    private Data mDefault;

    public BaseSourceFactory<T> register(String protocol, SourceCreator<?> creator) {
        mCreators.put(protocol, new Data(false, creator));
        return this;
    }

    public BaseSourceFactory<T> registerPath(String protocol, SourceCreator<?> creator) {
        mCreators.put(protocol, new Data(true, creator));
        return this;
    }

    public BaseSourceFactory<T> setDefault(SourceCreator<?> creator) {
        mDefault = new Data(false, creator);
        return this;
    }

    public BaseSourceFactory<T> setDefaultPath(SourceCreator<?> creator) {
        mDefault = new Data(true, creator);
        return this;
    }

    public Source createFromUri(String uri) {
        T raw = parse(uri);
        return create(raw);
    }

    protected Source createBase(T raw) {
        String protocol = extractProtocol(raw);

        Data data = mCreators.get(protocol);
        if (data == null) {
            data = mDefault;
        }
        String param = data.isPath() ? toPath(raw) : toUri(raw);
        return data.getCreator().create(param);
    }

    public Source create(T raw) {

        String child = extractChild(raw);
        Source source = createBase(raw);
        if (child != null) {
            return source.getChild(child);
        }
        return source;
    }

    public Source createChild(Source pParent, String pUri) throws MalformedURLException {
        T raw = parse(pUri);
        Source source;
        if (!isAbsolute(raw)) {
            source = pParent.getChild(pUri);
        } else {
            source = create(raw);
        }
        return source;
    }

    protected abstract String extractChild(T raw);

    protected abstract boolean isAbsolute(T raw);

    protected abstract String extractProtocol(T raw);

    protected abstract String toPath(T raw);

    protected abstract String toUri(T raw);

    protected abstract T parse(String uri);

    private static class Data {
        private final boolean path;
        private final SourceCreator<?> creator;

        public Data(boolean pPath, SourceCreator<?> pCreator) {
            super();

            path = pPath;
            creator = pCreator;
        }

        public boolean isPath() {
            return path;
        }

        public SourceCreator<?> getCreator() {
            return creator;
        }
    }

    public static class Builder<T> {

        private Extractor<T> mUriExtractor;
        private Extractor<T> mPathExtractor;
        private Extractor<T> mProtocolExtractor;
        private Parser<T> mParser;
        private Prediction<T> mIsAbsolute;
        private Extractor<T> mChildGetter;

        public Builder<T> uri(Extractor<T> uriExtractor) {
            mUriExtractor = uriExtractor;
            return this;
        }

        public Builder<T> checkAbsolute(Prediction<T> isAbsolute) {
            mIsAbsolute = isAbsolute;
            return this;
        }

        public Builder<T> path(Extractor<T> pathExtractor) {
            mPathExtractor = pathExtractor;
            return this;
        }

        public Builder<T> protocol(BaseSourceFactory.Extractor<T> protocolExtractor) {
            mProtocolExtractor = protocolExtractor;
            return this;
        }

        public Builder<T> parser(BaseSourceFactory.Parser<T> parser) {
            mParser = parser;
            return this;
        }

        public Builder<T> childGetter(Extractor<T> childGetter) {
            mChildGetter = childGetter;
            return this;
        }

        public BaseSourceFactory<T> build() {
            return new BuilderSourceFactory<T>(mParser, mProtocolExtractor, mPathExtractor,
                    mUriExtractor, mIsAbsolute, mChildGetter);
        }
    }

    public static interface Parser<T> {
        T parseString(String string);
    }

    public static interface Extractor<T> {
        String extract(T data);
    }

    public static interface Prediction<T> {
        boolean test(T data);
    }

}