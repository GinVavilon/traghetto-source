/**
 * 
 */
package com.github.ginvavilon.traghentt.example;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.LogHandler;
import com.github.ginvavilon.traghentto.ResourceSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.CryptoConfiguration;
import com.github.ginvavilon.traghentto.crypto.CryptoSourceCreator;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.se.SourceFactory;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ExampleMain {

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    static {
        try {
            Source privateKeySource = ResourceSource.CREATOR.create("private.key");
            Source publicKeySource = ResourceSource.CREATOR.create("public.key");

            CryptoConfiguration configuration = CryptoConfiguration.builder()
                    .setAlgorithm(Algorithm.RSA)
                    .loadPrivateKey(privateKeySource)
                    .loadPublicKey(publicKeySource)
                    .build();

            SourceFactory.registerPath("sfile",
                    CryptoSourceCreator.createByPassword(FileSource.CREATOR,
                            "testKey8Exampl56"));

            SourceFactory.registerPath("rsa-file",
                    CryptoSourceCreator.create(FileSource.CREATOR,
                            configuration));

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author Vladimir Baraznovsky
     *
     */
    private final class LogListener implements SourceUtils.ICopyListener {
        private final StringBuilder mLogBuilder;
        private final Source mIn;
        private Future<?> mTask;

        private LogListener(StringBuilder pLog, Source pOut) {
            mLogBuilder = pLog;
            mIn = pOut;
        }

        @Override
        public void onStart() {
            mLogBuilder.append("Start\n");
            mProgressBar.setValue(0);
            log();

        }

        @Override
        public void onProgress(long pReadBytes) {
            sentToLog(new Runnable() {

                @Override
                public void run() {

                    long length = mIn.getLength();
                    mLogBuilder.append(String.format("Progress %s/%s", pReadBytes, length));
                    mLogBuilder.append("\n");
                    log();
                    int size = (int) (100 * pReadBytes / length);
                    mProgressBar.setValue(size);

                }
            });

        }

        protected void sentToLog(Runnable task) {
            if (mTask != null) {
                mTask.cancel(false);
            }
            mTask = mExecutor.submit(task);
        }

        @Override
        public void onFail(Throwable pE) {
            sentToLog(new Runnable() {

                @Override
                public void run() {
                    mLogBuilder.append("Fail: ");
                    mLogBuilder.append(pE.getMessage());
                    mLogBuilder.append("\n");
                    log();

                }
            });

            pE.printStackTrace();

        }

        public void log() {
            mLog.setText(mLogBuilder.toString());
        }

        @Override
        public void onComplete() {
            sentToLog(new Runnable() {

                @Override
                public void run() {
                    mLogBuilder.append("Complete");
                    mProgressBar.setValue(100);
                    log();
                }
            });

        }
    }

    private JFrame mFrame;
    private JTextField inEdit;
    private JTextField outEdit;
    private JLabel mStatus;
    private JTextPane mLog;
    private JProgressBar mProgressBar;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        Logger.register(new LogHandler() {

            @Override
            public void i(int pType, String pMessage, Object[] pArgs) {
                System.out.println(String.format(pMessage, pArgs));

            }

            @Override
            public void e(int pType, String pMessage, Object[] pArgs, Throwable pThrowable) {
                System.err.println(String.format(pMessage, pArgs));
                if (pThrowable != null) {
                    pThrowable.printStackTrace();
                }

            }

            @Override
            public void e(int pType, Throwable pE) {
                if (pE != null) {
                    pE.printStackTrace();
                }
            }

            @Override
            public void d(int pType, String pMessage, Object[] pArgs) {

                System.out.println(String.format(pMessage, pArgs));

            }

        });

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ExampleMain window = new ExampleMain();
                    window.mFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ExampleMain() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mFrame = new JFrame();
        mFrame.setBounds(100, 100, 800, 525);
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel_1 = new JPanel();
        mFrame.getContentPane().add(panel_1, BorderLayout.CENTER);
        SpringLayout sl_panel_1 = new SpringLayout();
        panel_1.setLayout(sl_panel_1);

        JLabel inLabel = new JLabel("In");
        sl_panel_1.putConstraint(SpringLayout.NORTH, inLabel, 10, SpringLayout.NORTH, panel_1);
        sl_panel_1.putConstraint(SpringLayout.WEST, inLabel, 10, SpringLayout.WEST, panel_1);
        sl_panel_1.putConstraint(SpringLayout.EAST, inLabel, 60, SpringLayout.WEST, panel_1);
        panel_1.add(inLabel);

        inEdit = new JTextField();
        inEdit.setText("files/text.txt");
        inLabel.setLabelFor(inEdit);
        sl_panel_1.putConstraint(SpringLayout.NORTH, inEdit, 8, SpringLayout.NORTH, panel_1);
        sl_panel_1.putConstraint(SpringLayout.WEST, inEdit, 65, SpringLayout.WEST, panel_1);
        sl_panel_1.putConstraint(SpringLayout.EAST, inEdit, -5, SpringLayout.EAST, panel_1);
        panel_1.add(inEdit);
        inEdit.setColumns(10);

        JLabel outLabel = new JLabel("Out");
        sl_panel_1.putConstraint(SpringLayout.NORTH, outLabel, 6, SpringLayout.SOUTH, inLabel);
        sl_panel_1.putConstraint(SpringLayout.WEST, outLabel, 0, SpringLayout.WEST, inLabel);
        panel_1.add(outLabel);

        outEdit = new JTextField();
        outEdit.setText("out/");
        outLabel.setLabelFor(outEdit);
        sl_panel_1.putConstraint(SpringLayout.NORTH, outEdit, 6, SpringLayout.SOUTH, inEdit);
        sl_panel_1.putConstraint(SpringLayout.WEST, outEdit, 29, SpringLayout.EAST, outLabel);
        sl_panel_1.putConstraint(SpringLayout.EAST, outEdit, 0, SpringLayout.EAST, inEdit);
        panel_1.add(outEdit);
        outEdit.setColumns(10);

        JButton btnRun = new JButton("Run");
        sl_panel_1.putConstraint(SpringLayout.NORTH, btnRun, 6, SpringLayout.SOUTH, outEdit);
        sl_panel_1.putConstraint(SpringLayout.WEST, btnRun, 65, SpringLayout.WEST, panel_1);
        btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mStatus.setText("Copy");
                StringBuilder log = new StringBuilder();
                mLog.setText(log.toString());

                String in = inEdit.getText();
                String out = outEdit.getText();
                try {
                    Source inSource = SourceFactory.createFromUrl(in);
                    Source outSource = SourceFactory.createFromUrl(out);

                    LogListener listener = new LogListener(log, inSource);

                    log.append(inSource.getUriString());
                    log.append("\n");
                    log.append(outSource.getUriString());
                    log.append("\n");
                    mLog.setText(log.toString());
                    Executors.newSingleThreadExecutor().execute(new Runnable() {

                        @Override
                        public void run() {

                            try {
                                SourceUtils.replace(inSource, (WritableSource) outSource,
                                        listener);
                            } catch (IOSourceException | IOException e) {
                                listener.onFail(e);
                            }
                        }
                    });

                } catch (IOException e) {
                    Logger.e(e);
                    mStatus.setText(e.getMessage());
                }
            }
        });
        panel_1.add(btnRun);

        mLog = new JTextPane();
        mLog.setEditable(false);
        sl_panel_1.putConstraint(SpringLayout.NORTH, mLog, 5, SpringLayout.SOUTH, btnRun);
        sl_panel_1.putConstraint(SpringLayout.WEST, mLog, 0, SpringLayout.WEST, inEdit);
        sl_panel_1.putConstraint(SpringLayout.SOUTH, mLog, -5, SpringLayout.SOUTH, panel_1);
        sl_panel_1.putConstraint(SpringLayout.EAST, mLog, 0, SpringLayout.EAST, inEdit);
        panel_1.add(mLog);

        JPanel panel = new JPanel();
        panel.setBorder(new CompoundBorder());
        mFrame.getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        mProgressBar = new JProgressBar();
        mProgressBar.setStringPainted(true);
        mProgressBar.setValue(50);
        panel.add(mProgressBar);

        mStatus = new JLabel("Status");
        mStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(mStatus);
    }
}
