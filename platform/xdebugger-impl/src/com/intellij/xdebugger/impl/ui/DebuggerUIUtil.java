/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.xdebugger.impl.ui;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * User: lex
 * Date: Sep 20, 2003
 * Time: 11:26:44 PM
 */
public class DebuggerUIUtil {
  private DebuggerUIUtil() {
  }

  public static void enableEditorOnCheck(final JCheckBox checkbox, final JComponent textfield) {
    checkbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean selected = checkbox.isSelected();
        textfield.setEnabled(selected);
      }
    });
    textfield.setEnabled(checkbox.isSelected());
  }

  public static void focusEditorOnCheck(final JCheckBox checkbox, final JComponent component) {
    final Runnable runnable = new Runnable() {
      public void run() {
        component.requestFocus();
      }
    };
    checkbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (checkbox.isSelected()) {
          SwingUtilities.invokeLater(runnable);
        }
      }
    });
  }

  public static void invokeLater(final Runnable runnable) {
    ApplicationManager.getApplication().invokeLater(runnable);
  }

  public static void invokeOnEventDispatch(final Runnable runnable) {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeLater(runnable);
    }
  }

  public static RelativePoint calcPopupLocation(Editor editor, final int line) {
    Point p = editor.logicalPositionToXY(new LogicalPosition(line + 1, 0));

    final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
    if (!visibleArea.contains(p)) {
      p = new Point((visibleArea.x + visibleArea.width) / 2, (visibleArea.y + visibleArea.height) / 2);
    }
    return new RelativePoint(editor.getContentComponent(), p);
  }

  public static void showValuePopup(@NotNull XFullValueEvaluator text, @NotNull MouseEvent event, @NotNull Project project) {
    final JTextArea textArea = new JTextArea("Evaluating...");
    textArea.setEditable(false);
    textArea.setBackground(HintUtil.INFORMATION_COLOR);
    textArea.setLineWrap(false);

    final JScrollPane component = ScrollPaneFactory.createScrollPane(textArea);
    final Dimension frameSize = WindowManager.getInstance().getFrame(project).getSize();
    final Dimension size = new Dimension(frameSize.width / 2, frameSize.height / 2);
    component.setPreferredSize(size);
    component.setBorder(null);

    final JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(component, null)
      .setResizable(true)
      .setMovable(true)
      .setDimensionServiceKey(project, "XDebugger.FullValuePopup", false)
      .setRequestFocus(false)
      .createPopup();

    text.startEvaluation(new FullValueEvaluationCallbackImpl(popup, textArea));

    final Component parentComponent = event.getComponent();
    RelativePoint point = new RelativePoint(parentComponent, new Point(event.getX()-size.width, event.getY()-size.height));
    popup.show(point);
  }

  private static class FullValueEvaluationCallbackImpl implements XFullValueEvaluator.XFullValueEvaluationCallback {
    private final JBPopup myPopup;
    private final JTextArea myTextArea;

    public FullValueEvaluationCallbackImpl(final JBPopup popup, final JTextArea textArea) {
      myPopup = popup;
      myTextArea = textArea;
    }

    public void evaluated(@NotNull final String fullValue) {
      invokeOnEventDispatch(new Runnable() {
        public void run() {
          myTextArea.setText(fullValue);
          myTextArea.setCaretPosition(0);
        }
      });
    }

    public void errorOccurred(@NotNull final String errorMessage) {
      invokeOnEventDispatch(new Runnable() {
        public void run() {
          myTextArea.setForeground(XDebuggerUIConstants.ERROR_MESSAGE_ATTRIBUTES.getFgColor());
          myTextArea.setText(errorMessage);
        }
      });
    }

    public boolean isObsolete() {
      return myPopup.isDisposed();
    }
  }
}
