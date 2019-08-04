/**
 * 
 */
package com.github.ginvavilon.traghentto;

class BuilderSourceFactory<T> extends BaseSourceFactory<T> {

    private final Parser<T> mParser;
    private final Extractor<T> mProtocolExtractor;
    private final Extractor<T> mPathExtractor;
    private final Extractor<T> mUriExtractor;
    private final Extractor<T> mChildExtractor;
    private final Prediction<T> mIsAbsolute;

    public BuilderSourceFactory(BaseSourceFactory.Parser<T> parser, Extractor<T> protocolExtractor,
            Extractor<T> pathExtractor, Extractor<T> uriExtractor, Prediction<T> pIsAbsolute,
            Extractor<T> childExtractor) {
        super();
        mParser = parser;
        mProtocolExtractor = protocolExtractor;
        mPathExtractor = pathExtractor;
        mUriExtractor = uriExtractor;
        mIsAbsolute = pIsAbsolute;
        mChildExtractor = childExtractor;
    }

    @Override
    protected T parse(String uri) {
        T raw = mParser.parseString(uri);
        return raw;
    }

    @Override
    protected String toUri(T raw) {
        return mUriExtractor.extract(raw);
    }

    @Override
    protected String toPath(T raw) {
        return mPathExtractor.extract(raw);
    }

    @Override
    protected String extractProtocol(T raw) {
        return mProtocolExtractor.extract(raw);
    }

    @Override
    protected boolean isAbsolute(T raw) {
        return mIsAbsolute.test(raw);
    }

    @Override
    protected String extractChild(T raw) {
        return mChildExtractor.extract(raw);
    }

}
