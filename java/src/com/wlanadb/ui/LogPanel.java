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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.wlanadb.data.LogcatLine;
import com.wlanadb.service.LogManager;
import com.wlanadb.utils.Log;

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
  private final static String[] COL_NAMES = new String[] {
    "PID",
    "Tag",
    "T",
    "Message"
  };
  private final static int[] COL_WIDTH = new int[] {
    50, 150, 25, 150
  };


  private final static RGB RED = new RGB(204, 0, 0);
  private final static RGB YELLOW = new RGB(255, 136, 0);
  private final static RGB GREEN = new RGB(102, 153, 0);
  private final static RGB PURPLE = new RGB(153, 51, 204);
  private final static RGB BLUE = new RGB(0, 153, 204);
  private final static RGB[] COLORS = new RGB[] {
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
    final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);

    viewer = new TableViewer(scrolledComposite, SWT.BORDER | SWT.FULL_SELECTION); 

    table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    for (int i=0; i<COL_NAMES.length; ++i) {
      final TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
      tableColumn.setText(COL_NAMES[i]);
      tableColumn.setWidth(COL_WIDTH[i]);
    }

    scrolledComposite.setContent(table);
    scrolledComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    // set up the content and label providers.
    viewer.setContentProvider(new LogContentProvider());
    viewer.setLabelProvider(new LogLabelProvider(display));

    OwnerDrawLabelProvider.setUpOwnerDraw(viewer);

    return scrolledComposite;
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

  private class LogLabelProvider extends OwnerDrawLabelProvider implements ITableLabelProvider, 
  ITableFontProvider, ITableColorProvider {

    private final Display display;

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
      final LogcatLine line = (LogcatLine) element;
      final Point size = event.gc.textExtent(line.text);
      event.width = viewer.getTable().getColumn(event.index).getWidth();
      final int lines = size.x / event.width + 1;
      event.height = size.y * lines;
    }

    @Override
    protected void paint(Event event, Object element) {
      final LogcatLine line = (LogcatLine) element;
      event.gc.drawText(line.text, event.x, event.y, true);
    }
  }

}
