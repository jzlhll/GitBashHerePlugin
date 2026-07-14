package com.allan.openhereplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.AWTEvent;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionPopupListener implements SelectionListener {

    private static final SelectionPopupListener INSTANCE = new SelectionPopupListener();
    private volatile boolean initialized = false;

    // Cache the UI components so we only create them ONCE per IDE session, not per editor/selection.
    private JBPopup currentPopup;
    private Editor currentPopupEditor;
    private final Map<Editor, VisibleAreaListener> visibleAreaListeners = new ConcurrentHashMap<>();
    private volatile boolean windowFocusListenerRegistered = false;
    private volatile boolean activeWindowListenerRegistered = false;

    public static SelectionPopupListener getInstance() {
        return INSTANCE;
    }

    public synchronized void init() {
        if (initialized) return;
        initialized = true;
        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(this, ApplicationManager.getApplication());
        // 监听 editor 释放事件，及时清理 alarms 与 visibleAreaListeners，避免长时间运行后内存泄漏与状态异常
        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            public void editorReleased(@NotNull EditorFactoryEvent event) {
                Editor released = event.getEditor();
                Alarm alarm = alarms.remove(released);
                if (alarm != null) {
                    alarm.cancelAllRequests();
                }
                VisibleAreaListener listener = visibleAreaListeners.remove(released);
                if (listener != null && !released.isDisposed()) {
                    try {
                        released.getScrollingModel().removeVisibleAreaListener(listener);
                    } catch (Exception ignored) {
                    }
                }
                if (currentPopupEditor == released) {
                    hideCurrentPopup();
                }
            }
        }, ApplicationManager.getApplication());
        registerWindowFocusListener();
        registerActiveWindowListener();
    }

    public void init(@NotNull Project project) {
        init();
        project.getMessageBus().connect(project).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        hideCurrentPopup();
                    }
                }
        );
    }

    private final Map<Editor, Alarm> alarms = new ConcurrentHashMap<>();

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        if (editor.getProject() == null || editor.getDocument() == null) {
            return;
        }
        
        // 过滤掉单行输入框，保留可绑定实际文件的 Diff 编辑器。
        if (editor.isOneLineMode()) {
            return;
        }
        
        if (getEditorFile(editor) == null) {
            return;
        }

        ensureEditorListeners(editor);
        handleSelectionChanged(editor);
    }

    private void handleSelectionChanged(Editor editor) {
        if (!com.allan.openhereplugin.config.GitOpenHereSettings.getInstance().getState().isGBOHFloatingIconEnabled) {
            hideCurrentPopup();
            return;
        }

        Project project = editor.getProject();
        if (project == null || project.isDisposed()) {
            return;
        }

        Alarm alarm = alarms.computeIfAbsent(editor, e -> new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project));

        alarm.cancelAllRequests();

        // 隐藏当前可能存在的 popup (如果当前正显示着，先隐藏)
        hideCurrentPopup();

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            return;
        }

        alarm.addRequest(() -> {
            // 这里是子线程逻辑 (POOLED_THREAD)
            ApplicationManager.getApplication().runReadAction(() -> {
                if (editor.isDisposed() || !selectionModel.hasSelection()) {
                    return;
                }
                
                String selectedText = selectionModel.getSelectedText();
                if (selectedText == null || selectedText.trim().isEmpty()) {
                    return;
                }

                // 处理完子线程逻辑后，切换回主线程显示 UI
                ApplicationManager.getApplication().invokeLater(() -> {
                    showPopup(editor);
                });
            });
        }, 600);
    }

    private void showPopup(Editor editor) {
        if (editor.isDisposed() || !editor.getSelectionModel().hasSelection()) {
            return;
        }
        if (!isEditorWindowActive(editor)) {
            return;
        }

        hideCurrentPopup();

        // 每次显示时新建 panel，避免跨 popup 复用组件导致的 parent/listener 链异常
        JPanel panel = createPopupPanel(editor);

        // 每次显示时，创建一个新的 popup，因为 popup 销毁后不能复用
        currentPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setCancelOnClickOutside(true)
                .setFocusable(false)
                .setRequestFocus(false)
                .createPopup();
        currentPopupEditor = editor;
        Point point = calculatePopupScreenPoint(editor);
        if (point == null) {
            hideCurrentPopupImmediately();
            return;
        }
        currentPopup.showInScreenCoordinates(editor.getContentComponent(), point);
    }

    private JPanel createPopupPanel(Editor capturedEditor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new JBColor(new Color(245, 245, 245), new Color(60, 63, 65)));
        panel.setBorder(BorderFactory.createLineBorder(new JBColor(Color.LIGHT_GRAY, Color.DARK_GRAY), 1));

        JLabel label = new JLabel("<html><i><font color='#B19CD9'>GB</font><font color='#90EE90'>O</font><font color='#B19CD9'>H</font></i></html>");
        label.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.add(label, BorderLayout.CENTER);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentPopup != null && !currentPopup.isDisposed()) {
                    currentPopup.cancel();
                }

                // 直接使用 capture 的 editor，避免全局查找导致的不一致
                if (capturedEditor.isDisposed()) {
                    return;
                }

                Project project = capturedEditor.getProject();
                if (project == null || project.isDisposed()) {
                    return;
                }

                VirtualFile vf = getEditorFile(capturedEditor);
                com.allan.openhereplugin.util.CopyCodeUtil.performCopy(project, capturedEditor, vf);
            }
        });

        return panel;
    }

    private VirtualFile getEditorFile(Editor editor) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null && editor instanceof EditorEx) {
            file = ((EditorEx) editor).getVirtualFile();
        }
        return file;
    }

    private void ensureEditorListeners(Editor editor) {
        if (editor.isDisposed() || visibleAreaListeners.containsKey(editor)) {
            return;
        }

        VisibleAreaListener listener = new VisibleAreaListener() {
            @Override
            public void visibleAreaChanged(@NotNull VisibleAreaEvent e) {
                if (!e.getOldRectangle().equals(e.getNewRectangle())) {
                    repositionCurrentPopupForEditor(editor);
                }
            }
        };
        editor.getScrollingModel().addVisibleAreaListener(listener);
        visibleAreaListeners.put(editor, listener);
    }

    private synchronized void registerWindowFocusListener() {
        if (windowFocusListenerRegistered) {
            return;
        }
        windowFocusListenerRegistered = true;

        AWTEventListener listener = event -> {
            if (!(event instanceof java.awt.event.WindowEvent)) {
                return;
            }

            var windowEvent = (java.awt.event.WindowEvent) event;
            int eventId = windowEvent.getID();
            if (eventId == java.awt.event.WindowEvent.WINDOW_DEACTIVATED
                    || eventId == java.awt.event.WindowEvent.WINDOW_LOST_FOCUS) {
                hideCurrentPopupAndCancelRequests();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(
                listener,
                AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK
        );
    }

    private synchronized void registerActiveWindowListener() {
        if (activeWindowListenerRegistered) {
            return;
        }
        activeWindowListenerRegistered = true;

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", this::handleActiveWindowChanged);
    }

    private void handleActiveWindowChanged(PropertyChangeEvent event) {
        if (event.getNewValue() == null) {
            hideCurrentPopupAndCancelRequests();
        }
    }

    private boolean isEditorWindowActive(Editor editor) {
        Window editorWindow = SwingUtilities.getWindowAncestor(editor.getContentComponent());
        Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return editorWindow != null && activeWindow != null && (editorWindow == activeWindow || activeWindow.getOwner() == editorWindow);
    }

    private Point calculatePopupScreenPoint(Editor editor) {
        if (editor.isDisposed() || !editor.getSelectionModel().hasSelection()) {
            return null;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.getSelectionEnd();

        int startLine = editor.getDocument().getLineNumber(selectionStart);
        int endLine = editor.getDocument().getLineNumber(selectionEnd);
        Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();

        int firstVisibleLine = editor.xyToLogicalPosition(new Point(visibleArea.x, visibleArea.y)).line;
        int lastVisibleY = visibleArea.y + Math.max(visibleArea.height - editor.getLineHeight(), 0);
        int lastVisibleLine = editor.xyToLogicalPosition(new Point(visibleArea.x, lastVisibleY)).line;

        int visibleSelectionTopLine = Math.max(startLine, firstVisibleLine);
        int visibleSelectionBottomLine = Math.min(endLine, lastVisibleLine);
        if (visibleSelectionTopLine > visibleSelectionBottomLine) {
            return null;
        }

        int topRightOffset;
        if (startLine == endLine) {
            topRightOffset = selectionEnd;
        } else if (visibleSelectionTopLine == endLine) {
            topRightOffset = selectionEnd;
        } else {
            topRightOffset = editor.getDocument().getLineEndOffset(visibleSelectionTopLine);
        }

        Point point = editor.visualPositionToXY(editor.offsetToVisualPosition(topRightOffset));

        // 限制弹出的 X 坐标（限制在编辑器视口可视区域宽度的 25% 到 30% 之间）
        int visibleWidth = visibleArea.width;
        int scrollX = visibleArea.x;

        int minAllowedX = scrollX + (int) (visibleWidth * 0.25);
        int maxAllowedX = scrollX + (int) (visibleWidth * 0.30);

        if (point.x < minAllowedX) {
            point.x = minAllowedX;
        } else if (point.x > maxAllowedX) {
            point.x = maxAllowedX;
        }

        int minAllowedY = visibleArea.y;
        int maxAllowedY = lastVisibleY;
        point.y = Math.max(minAllowedY, Math.min(point.y, maxAllowedY));

        SwingUtilities.convertPointToScreen(point, editor.getContentComponent());
        point.translate(5, 0);
        return point;
    }

    private void repositionCurrentPopupForEditor(Editor editor) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (currentPopupEditor != editor) {
                return;
            }
            if (currentPopup == null || currentPopup.isDisposed()) {
                currentPopup = null;
                currentPopupEditor = null;
                return;
            }

            Point point = calculatePopupScreenPoint(editor);
            if (point == null) {
                hideCurrentPopupImmediately();
                return;
            }
            currentPopup.setLocation(point);
        });
    }

    private void hideCurrentPopup() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            hideCurrentPopupImmediately();
            return;
        }
        ApplicationManager.getApplication().invokeLater(this::hideCurrentPopupImmediately);
    }

    private void hideCurrentPopupAndCancelRequests() {
        alarms.values().forEach(Alarm::cancelAllRequests);
        hideCurrentPopup();
    }

    private void hideCurrentPopupImmediately() {
        if (currentPopup != null && !currentPopup.isDisposed()) {
            currentPopup.cancel();
        }
        currentPopup = null;
        currentPopupEditor = null;
    }
}
