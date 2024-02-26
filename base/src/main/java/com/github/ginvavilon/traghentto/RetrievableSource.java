package com.github.ginvavilon.traghentto;

public interface RetrievableSource extends Source {

    Status getStatus();

    Controller getController();

    Source getReadySource();

    interface Controller {

        void fetch();

        void cancel();

        int getFullByteSize();

        int getReadyByteSize();

        void registerListener(Listener listener);

        void unregisterListener(Listener listener);
    }

    enum Status {
        READY,
        AVAILABLE,
        FETCHING,
        PAUSED,
        ERROR,
        UNKNOWN,
    }

    interface Listener {

        void onStatusUpdate(Status status);

        void onProgress(long readyBytes, long fullBytes);

        void onError(Throwable throwable);
    }
}
