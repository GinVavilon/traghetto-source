package com.github.ginvavilon.traghentto;

public interface RetrievableSource extends Source {

    Status getStatus();

    Controller getController();

    interface Controller {

        void fetch();

        void cancel();

        Progress getProgress();

        void registerListener(Listener listener);

        void unregisterListener(Listener listener);
    }

    enum Status {
        READY,
        PENDING,
        FETCHING,
        PAUSED,
        ERROR,
        UNKNOWN,
    }

    interface Listener {

        void onStatusUpdate(RetrievableSource source, Status status);

        void onProgress(RetrievableSource source, long readyBytes, long fullBytes);

        void onError(RetrievableSource source, Throwable throwable);
    }

    interface Progress {
        long getFullByteSize();

        long getReadyByteSize();
    }
}
