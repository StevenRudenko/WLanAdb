package com.wlanadb.ui;

import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.service.ClientManager;
import com.wlanadb.service.LogManager;
import com.wlanadb.service.WLanAdbController;
import com.wlanadb.service.WLanAdbController.OnWLanAdbControllerEventListener;
import com.wlanadb.ui.DevicePanel.IUiSelectionListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.io.File;

public class Main implements OnWLanAdbControllerEventListener, IUiSelectionListener {

    private final WLanAdbController service;

    private Shell shell;

    private StackLayout stackLayout;
    private DevicePanel devices;
    private LogPanel log;

    public Main() {
        service = new WLanAdbController();
        service.addEventsListener(this);
    }

    public void show() {
//        frame.setState(Frame.NORMAL); // restores minimized windows
//        frame.toFront(); // brings to front without needing to setAlwaysOnTop
//        frame.requestFocus();
        final Display display = shell.getDisplay();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                shell.forceActive();
                shell.forceFocus();
            }
        });
    }

    public void close() {
        service.stop();
        shell.dispose();
    }

    public int prepare() {
        return service.start();
    }

    /**
     * Open the window.
     */
    public void open() {
        final Display display = Display.getDefault();
        shell = createContents();

        shell.open();
        shell.layout();

        if (service.isReady())
            service.scan();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        close();
    }

    /**
     * Create contents of the window.
     *
     * @wbp.parser.entryPoint
     */
    protected Shell createContents() {
        final Shell shell = prepareShell();
        shell.setSize(840, 541);

        prepareMenu(shell);

        stackLayout = new StackLayout();
        shell.setLayout(stackLayout);

        devices = new DevicePanel();
        devices.createControl(shell);
        devices.addSelectionListener(this);

        log = new LogPanel();
        log.createControl(shell);
        log.setInput(service.getLogManager());
        log.getView().setVisible(false);

        stackLayout.topControl = devices.getView();
        return shell;
    }

    @Override
    public void onSelectionChanged(Client client) {
        stackLayout.topControl = log.getView();
        devices.getView().setVisible(false);
        log.getView().setVisible(true);

        service.startLogging(client);
    }

    @Override
    public void onClientListUpdated(ClientManager manager) {
        devices.setInput(manager);
    }

    @Override
    public void onLogUpdated(LogManager manager) {
        log.refresh();
    }

    private void prepareMenu(final Shell shell) {
        final Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);

        final MenuItem mntmDevices = new MenuItem(menu, SWT.CASCADE);
        mntmDevices.setText("Devices");

        final Menu mnctmDevices = new Menu(mntmDevices);
        mntmDevices.setMenu(mnctmDevices);

        final MenuItem mntmRefresh = new MenuItem(mnctmDevices, SWT.NONE);
        mntmRefresh.setAccelerator(SWT.F5);
        mntmRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                service.scan();
            }
        });
        mntmRefresh.setText("Refresh");

        final MenuItem mntmOperations = new MenuItem(menu, SWT.CASCADE);
        mntmOperations.setText("Operations");

        final Menu mnctmOperations = new Menu(mntmOperations);
        mntmOperations.setMenu(mnctmOperations);

        final MenuItem mntmPush = new MenuItem(mnctmOperations, SWT.NONE);
        mntmPush.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                final FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
                fileDialog.setText("Select files or directory to push to device");

                final String path = fileDialog.open();
                if (path != null) {
                    final File root = new File(path).getParentFile();
                    final String[] result = fileDialog.getFileNames();
                    for (String f : result)
                        System.out.println(new File(root, f).getAbsolutePath());
                    //TODO: start push action one by one
                    //TODO: show progress
                }
            }
        });
        mntmPush.setText("Push");
    }

    public void handleError(int error) {
        shell = prepareShell();

        final String message;
        switch (error) {
            case WLanAdbController.ERROR_UDP_ADDRESS_USED:
                message = "It seems you have another application instance launched. Please close it first and try again.";
                break;
            case WLanAdbController.ERROR_NOT_CONNECTED:
                message = "It seems you are not connected to network. Please check you connection and try again.";
                break;
            default:
                return;
        }
        MessageDialog.openWarning(shell, "WLanAdb", message);
    }

    private static Shell prepareShell() {
        final Shell shell = new Shell();
        shell.setText("WLanAdb");

        final Image icon24 = new Image(shell.getDisplay(), "res/images/ic_app_24.png");
        final Image icon32 = new Image(shell.getDisplay(), "res/images/ic_app_32.png");
        final Image icon48 = new Image(shell.getDisplay(), "res/images/ic_app_48.png");
        final Image icon64 = new Image(shell.getDisplay(), "res/images/ic_app_64.png");
        final Image icon96 = new Image(shell.getDisplay(), "res/images/ic_app_96.png");
        final Image icon128 = new Image(shell.getDisplay(), "res/images/ic_app_128.png");
        final Image[] images = new Image[]{icon24, icon32, icon48, icon64, icon96, icon128};
        shell.setImages(images);
        return shell;
    }
}
