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

import com.wlanadb.data.LogcatLine;
import com.wlanadb.service.LogManager;
import com.wlanadb.utils.Log;
import com.wlanadb.utils.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A display of both the devices and their clients.
 */
public final class LogPanel extends Panel {
    private static final String TAG = LogPanel.class.getSimpleName();

    private final static int TEXT_MARGIN = 3;

    private final static int COL_PID = 0;
    private final static int COL_TAG = 1;
    private final static int COL_TYPE = 2;
    private final static int COL_TEXT = 3;
    private final static String[] COL_NAMES = new String[]{
            "PID",
            "Tag",
            "T",
            "Message"
    };
    private final static int[] COL_WIDTH = new int[]{
            50, 150, 25, 250
    };


    private final static RGB RED = new RGB(204, 0, 0);
    private final static RGB YELLOW = new RGB(255, 136, 0);
    private final static RGB GREEN = new RGB(102, 153, 0);
    private final static RGB PURPLE = new RGB(153, 51, 204);
    private final static RGB BLUE = new RGB(0, 153, 204);
    private final static RGB[] COLORS = new RGB[]{
            RED,
            YELLOW,
            GREEN,
            PURPLE,
            BLUE
    };

    private final static Set<String> SYSTEM_TAGS = new HashSet<String>();

    static {
        SYSTEM_TAGS.add("dalvikvm");
        SYSTEM_TAGS.add("Process");
        SYSTEM_TAGS.add("ActivityManager");
        SYSTEM_TAGS.add("ActivityThread");
    }

    private Table table;
    private TableViewer viewer;

    /**
     * Creates the {@link LogPanel} object.
     */
    public LogPanel() {
    }

    @Override
    protected Control createControl(Composite parent) {
        final Display display = parent.getDisplay();

        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

        table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
//        table.setLayout(new TableLayout());

        for (int i = 0; i < COL_NAMES.length; ++i) {
            final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(COL_NAMES[i]);
            tableColumn.setWidth(COL_WIDTH[i]);
        }

        viewer.setContentProvider(new LogContentProvider());
        viewer.setLabelProvider(new LogLabelProvider(display));

        final GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        viewer.getControl().setLayoutData(layoutData);
//        OwnerDrawLabelProvider.setUpOwnerDraw(viewer);

        return table;
    }

    @Override
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

    public void setInput(final LogManager manager) {
        exec(new Runnable() {
            @Override
            public void run() {
                viewer.setInput(manager);
            }
        });
    }

    public void refresh() {
        exec(new Runnable() {
            @Override
            public void run() {
                final int selection = table.getVerticalBar().getSelection();
                viewer.refresh();
                table.getVerticalBar().setSelection(selection);
            }
        });
    }

    @Override
    protected void postCreation() {
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

    private class LogContentProvider implements IStructuredContentProvider {
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
            if (inputElement instanceof LogManager) {
                final LogManager manager = (LogManager) inputElement;
                return manager.getLogs();
            }
            return new Object[0];
        }
    }

    private class LogLabelProvider extends OwnerDrawLabelProvider
            implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {

        private final Display display;
        private final HashMap<String, LogcatLineHolder> holders = new HashMap<String, LogcatLineHolder>();

        public LogLabelProvider(Display display) {
            this.display = display;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            final LogcatLine line = (LogcatLine) element;

            if (!line.isValid()) {
                if (COL_TEXT == columnIndex)
                    return line.full;
                return null;
            }


            switch (columnIndex) {
                case COL_PID:
                    return Integer.toString(line.pid);
                case COL_TAG:
                    return line.tag;
                case COL_TYPE:
                    return line.type;
                case COL_TEXT:
                    return line.text;
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

        @Override
        public Color getBackground(Object element, int columnIndex) {
            // pass
            return null;
        }

        @Override
        public Color getForeground(Object element, int columnIndex) {
            final LogcatLine line = (LogcatLine) element;

            if (!line.isValid()) {
                return null;
            }


            switch (columnIndex) {
                case COL_PID:
                    return null;
                case COL_TAG:
                    return null;
                case COL_TYPE:
                    if (line.type.equals(LogcatLine.TYPE_D))
                        return new Color(display, BLUE);
                    else if (line.type.equals(LogcatLine.TYPE_V))
                        return null;
                    else if (line.type.equals(LogcatLine.TYPE_I))
                        return new Color(display, GREEN);
                    else if (line.type.equals(LogcatLine.TYPE_W))
                        return new Color(display, YELLOW);
                    else if (line.type.equals(LogcatLine.TYPE_E))
                        return new Color(display, RED);
                    return null;
                case COL_TEXT:
                    if (line.type.equals(LogcatLine.TYPE_W))
                        return new Color(display, YELLOW);
                    else if (line.type.equals(LogcatLine.TYPE_E))
                        return new Color(display, RED);
                    return null;
            }
            return null;
        }

        @Override
        public Font getFont(Object element, int columnIndex) {
            final LogcatLine line = (LogcatLine) element;
            if (line.isWarning())
                return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
            return null;
        }

        @Override
        protected void measure(Event event, Object element) {
            final String text = getColumnText(element, event.index);
            if (text == null) {
                event.width = viewer.getTable().getColumn(event.index).getWidth();
                event.height = 0;
                return;
            }

            event.width = viewer.getTable().getColumn(event.index).getWidth();
            if (event.index != COL_TEXT) {
                final Point size = StringUtils.getSize(text, event.gc);
                event.height = size.y;
                event.setBounds(new Rectangle(event.x, event.y, event.width, event.height));
                return;
            }

            final int maxWidth = event.width - TEXT_MARGIN * 2;

            event.width = viewer.getTable().getColumn(event.index).getWidth();

            LogcatLineHolder holder = holders.get(text);
            if (holder == null || holder.width != maxWidth) {
                Point size = StringUtils.getSize(text, event.gc);
                if (size.x <= maxWidth) {
                    event.height = size.y;
                    holders.remove(text);

                    event.setBounds(new Rectangle(event.x, event.y, event.width, event.height));
                    return;
                }

                holder = new LogcatLineHolder();
                holder.lines = StringUtils.wrap(text, event.gc, maxWidth);
                holder.width = maxWidth;

                size = StringUtils.getSize(holder.lines.get(0), event.gc);
                holder.height = size.y * holder.lines.size();
                holder.lineHeight = size.y;

                holders.put(text, holder);
            }

            event.height = holder.height;
            event.setBounds(new Rectangle(event.x, event.y, event.width, event.height));
        }

        @Override
        protected void paint(Event event, Object element) {
            final String text = getColumnText(element, event.index);
            if (text == null)
                return;

            if (event.index != COL_TEXT) {
                event.gc.drawText(text, event.x, event.y, true);
                return;
            }

            LogcatLineHolder holder = holders.get(text);
            if (holder == null) {
                event.gc.drawText(text, event.x, event.y, true);
            } else {
                int offset = 0;
                for (String s : holder.lines) {
                    event.gc.drawText(s, event.x, event.y + offset, true);
                    offset += holder.lineHeight;
                }
            }
        }

        private class LogcatLineHolder {
            List<String> lines = null;
            int width = 0;
            int height = 0;
            int lineHeight = 0;
        }
    }

}
