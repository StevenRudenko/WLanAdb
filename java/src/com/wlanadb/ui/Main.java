package com.wlanadb.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.service.ClientManager;
import com.wlanadb.service.LogManager;
import com.wlanadb.service.WLanAdbController;
import com.wlanadb.service.WLanAdbController.OnWLanAdbControllerEventListener;
import com.wlanadb.ui.DevicePanel.IUiSelectionListener;

import java.io.File;

public class Main implements OnWLanAdbControllerEventListener, IUiSelectionListener {

  private final WLanAdbController service;

  private Shell shell;

  private SashForm sashForm;
  private DevicePanel devices;
  private LogPanel log;

  public Main() {
    service = new WLanAdbController();
    service.addEventsListener(this);
  }

  public void show() {
    shell.forceActive();
  }

  /**
   * Open the window.
   */
  public void open() {
    if (!service.start()) {
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        service.stop();
      }
    });

    final Display display = Display.getDefault();
    createContents();
    shell.open();
    shell.layout();

    service.scan();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    service.stop();
    shell.dispose();
  }

  /**
   * Create contents of the window.
   * @wbp.parser.entryPoint
   */
  protected void createContents() {
    shell = new Shell();
    shell.setText("WLanAdb");

    final Image icon24 = new Image(shell.getDisplay(), "res/images/ic_app_24.png");
    final Image icon32 = new Image(shell.getDisplay(), "res/images/ic_app_32.png");
    final Image icon48 = new Image(shell.getDisplay(), "res/images/ic_app_48.png");
    final Image icon64 = new Image(shell.getDisplay(), "res/images/ic_app_64.png");
    final Image icon96 = new Image(shell.getDisplay(), "res/images/ic_app_96.png");
    final Image icon128 = new Image(shell.getDisplay(), "res/images/ic_app_128.png");
    final Image[] images = new Image[] { icon24, icon32, icon48, icon64, icon96, icon128 };
    shell.setImages(images);

    final FillLayout root = new FillLayout(SWT.HORIZONTAL);
    shell.setLayout(root);
    shell.setSize(840, 541);

      prepareMenu(shell);

    sashForm = new SashForm(shell, SWT.NONE);

    devices = new DevicePanel();
    devices.createControl(sashForm);
    devices.addSelectionListener(this);

    log = new LogPanel();
    log.createControl(sashForm);
    log.setInput(service.getLogManager());

    sashForm.setWeights(new int[] {1, 0});
    sashForm.setTouchEnabled(false);
  }

  @Override
  public void onSelectionChanged(Client client) {
    sashForm.setWeights(new int[] {0, 1});
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
}
