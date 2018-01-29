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
import java.util.concurrent.Executors;

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

import org.apache.commons.codec.digest.Crypt;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.WritableSource;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.LogHandler;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamUtils.ICopyListener;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.CryptoSourceCreator;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.se.SourceFactory;

/**
 * @author vbaraznovsky
 *
 */
public class ExampleMain {
	static {
		try {
			SourceFactory.registerPath("sfile", CryptoSourceCreator.create(FileSource.CREATOR,"testKey8Exampl56"));
			
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

    /**
     * @author vbaraznovsky
     *
     */
    private final class LogListner implements ICopyListener {
        private final StringBuilder mLogBuilder;
        private final Source mIn;

        private LogListner(StringBuilder pLog, Source pOut) {
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
        public void onProgress(long pRadedByte) {
            long lenght = mIn.getLenght();
            mLogBuilder.append(String.format("Progress %s/%s", pRadedByte, lenght));
            mLogBuilder.append("\n");
            log();
            int size = (int) (100 * pRadedByte / lenght);
            mProgressBar.setValue(size);

        }

        @Override
        public void onFail(Throwable pE) {
            mLogBuilder.append("Fail: ");
            mLogBuilder.append(pE.getMessage());
            mLogBuilder.append("\n");
            log();

        }

        public void log() {
            mLog.setText(mLogBuilder.toString());
        }

        @Override
        public void onCompite() {
            mLogBuilder.append("Complite");
            log();

        }
    }

    private JFrame mframe;
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
            public void run() {
                try {
                    ExampleMain window = new ExampleMain();
                    window.mframe.setVisible(true);
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
        mframe = new JFrame();
        mframe.setBounds(100, 100, 800, 525);
        mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel_1 = new JPanel();
        mframe.getContentPane().add(panel_1, BorderLayout.CENTER);
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
            public void actionPerformed(ActionEvent arg0) {
                mStatus.setText("Copy");
                StringBuilder log = new StringBuilder();
                mLog.setText(log.toString());

                String in = inEdit.getText();
                String out = outEdit.getText();
                try {
                    Source inSource = SourceFactory.createFromUrl(in);
                    Source outSource = SourceFactory.createFromUrl(out);

                    LogListner listener = new LogListner(log, inSource);

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
        mframe.getContentPane().add(panel, BorderLayout.SOUTH);
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
