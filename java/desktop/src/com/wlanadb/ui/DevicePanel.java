/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wlanadb.ui;

import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.service.ClientManager;
import com.wlanadb.utils.Log;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A display of both the devices and their clients.
 */
public final class DevicePanel extends Panel {
    private static final String TAG = DevicePanel.class.getSimpleName();

    /**
     * Classes which implement this interface provide methods that deals
     * with {@link IDevice} and {@link Client} selection changes coming from the ui.
     */
    public interface IUiSelectionListener {
        /**
         * Sent when a new {@link IDevice} and {@link Client} are selected.
         *
         * @param selectedClient The selected client. If null, no clients are selected.
         */
        public void onSelectionChanged(Client selectedClient);
    }

    private final static int COL_ID = 0;
    private final static int COL_NAME = 1;
    private final static int COL_MODEL = 2;
    private final static int COL_FIRMWARE = 3;
    private final static String[] COL_NAMES = new String[]{
            "Serial number",
            "Name",
            "Model",
            "Firmware"
    };
    private final static int[] COL_WIDTH = new int[]{
            150, 75, 75, 75
    };

    public final static String ICON_LOCK = "res/images/ic_lock.png"; //$NON-NLS-1$

    private Client selectedClient;

    private Table table;
    private TableViewer viewer;

    private Image icLock;

    private final Set<IUiSelectionListener> mListeners = new HashSet<IUiSelectionListener>();

    /**
     * Creates the {@link DevicePanel} object.
     *
     * @param loader
     */
    public DevicePanel() {
    }

    public void addSelectionListener(IUiSelectionListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeSelectionListener(IUiSelectionListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    @Override
    protected Control createControl(Composite parent) {
        loadImages(parent.getDisplay());

        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);

        table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        for (int i = 0; i < COL_NAMES.length; ++i) {
            final TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(COL_NAMES[i]);
            tableColumn.setWidth(COL_WIDTH[i]);
        }

        // set up the content and label providers.
        viewer.setContentProvider(new ClientContentProvider());
        viewer.setLabelProvider(new ClientLabelProvider());

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent ev) {
                final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                final Client client = (Client) selection.getFirstElement();
                notifyListeners(client);
            }
        });

        return table;
    }

    public Control getView() {
        return viewer.getControl();
    }

    /**
     * Sets the focus to the proper control inside the panel.
     */
    @Override
    public void setFocus() {
        table.setFocus();
    }

    public void setInput(final ClientManager manager) {
        exec(new Runnable() {
            @Override
            public void run() {
                viewer.setInput(manager);
            }
        });
    }

    /**
     * Returns the selected {@link Client}. May be null.
     */
    public Client getSelectedClient() {
        return selectedClient;
    }

    @Override
    protected void postCreation() {
    }

    private void notifyListeners(Client client) {
        if (client == null)
            return;

        Log.d(TAG, "Client selected: " + client.getId());
        synchronized (mListeners) {
            for (IUiSelectionListener listener : mListeners) {
                listener.onSelectionChanged(client);
            }
        }
    }

    private void loadImages(Display display) {
        icLock = new Image(display, ICON_LOCK);
    }

    /**
     * Executes the {@link Runnable} in the UI thread.
     *
     * @param runnable the runnable to execute.
     */
    private void exec(Runnable runnable) {
        try {
            Display display = table.getDisplay();
            display.asyncExec(runnable);
        } catch (SWTException e) {
            Log.e(TAG, "Failed to run on UI thread", e);
        }
    }

    /**
     * A Content provider for the {@link TreeViewer}.
     * <p/>
     * The input is a {@link AndroidDebugBridge}. First level elements are {@link IDevice} objects,
     * and second level elements are {@link Client} object.
     */
    private class ClientContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
            // pass
        }

        @Override
        public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
            // pass
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ClientManager) {
                final ClientManager manager = (ClientManager) inputElement;
                return manager.getClients().toArray();
            }
            return new Object[0];
        }

    }

    /**
     * A Label Provider for the {@link TreeViewer} in {@link DevicePanel}. It provides
     * labels and images for {@link IDevice} and {@link Client} objects.
     */
    private class ClientLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof Client) {
                Client client = (Client) element;
                switch (columnIndex) {
                    case COL_ID:
                        if (client.getUsePin()) {
                            return icLock;
                        }
                        return null;
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof Client) {
                final Client client = (Client) element;

                switch (columnIndex) {
                    case COL_ID:
                        return client.getId();
                    case COL_NAME:
                        return client.getName();
                    case COL_MODEL:
                        return client.getModel();
                    case COL_FIRMWARE:
                        return client.getFirmware();
                }
            }
            return null;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // pass
        }

        @Override
        public void dispose() {
            // pass
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            // pass
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // pass
        }
    }

}
