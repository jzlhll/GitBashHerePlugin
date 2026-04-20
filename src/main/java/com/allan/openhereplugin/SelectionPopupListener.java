package com.allan.openhereplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
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
import java.util.HashMap;
import java.util.Map;

public class SelectionPopupListener implements SelectionListener {

    private static final SelectionPopupListener INSTANCE = new SelectionPopupListener();
    private boolean initialized = false;

    // Cache the UI components so we only create them ONCE per IDE session, not per editor/selection.
    private JPanel cachedPanel;
    private JBPopup currentPopup;
    private Editor currentPopupEditor;
    private final Map<Editor, VisibleAreaListener> visibleAreaListeners = new HashMap<>();
    private boolean windowFocusListenerRegistered = false;

    public static SelectionPopupListener getInstance() {
        return INSTANCE;
    }

    public synchronized void init() {
        if (initialized) return;
        initialized = true;
        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(this, ApplicationManager.getApplication());
        registerWindowFocusListener();
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

    private final Map<Editor, Alarm> alarms = new HashMap<>();

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        if (editor.getProject() == null || editor.getDocument() == null) {
            return;
        }
        
        // 过滤掉非代码主编辑器的弹窗（例如：搜索框、终端、日志控制台、Diff 视图等）
        if (editor.isOneLineMode()) {
            return;
        }
        
        VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (vf == null || !vf.isInLocalFileSystem()) {
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

        Alarm alarm = alarms.computeIfAbsent(editor, e -> new Alarm(Alarm.ThreadToUse.POOLED_THREAD, com.intellij.openapi.project.ProjectManager.getInstance().getDefaultProject()));

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

        hideCurrentPopup();
        
        if (cachedPanel == null) {
            // 全局只创建一次核心 UI
            cachedPanel = new JPanel(new BorderLayout());
            cachedPanel.setBackground(new JBColor(new Color(245, 245, 245), new Color(60, 63, 65)));
            cachedPanel.setBorder(BorderFactory.createLineBorder(new JBColor(Color.LIGHT_GRAY, Color.DARK_GRAY), 1));
            
            JLabel label = new JLabel("<html><i><font color='#B19CD9'>GB</font><font color='#90EE90'>O</font><font color='#B19CD9'>H</font></i></html>");
            label.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            cachedPanel.add(label, BorderLayout.CENTER);

            // 点击事件：这里因为要复用 panel，我们在内部动态获取当前的焦点 editor
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentPopup != null && !currentPopup.isDisposed()) {
                        currentPopup.cancel();
                    }
                    
                    com.intellij.openapi.project.Project[] openProjects = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects();
                    if (openProjects.length == 0) return;
                    
                    // 获取当前处于活动状态的 editor
                    Editor activeEditor = null;
                    for (Project p : openProjects) {
                        activeEditor = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(p).getSelectedTextEditor();
                        if (activeEditor != null) break;
                    }
                    
                    if (activeEditor != null) {
                        Project project = activeEditor.getProject();
                        VirtualFile vf = FileDocumentManager.getInstance().getFile(activeEditor.getDocument());
                        com.allan.openhereplugin.util.CopyCodeUtil.performCopy(project, activeEditor, vf);
                    }
                }
            });
        }

        // 每次显示时，用缓存的 panel 创建一个新的 popup，因为 popup 销毁后不能复用
        currentPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(cachedPanel, null)
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
                hideCurrentPopup();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(
                listener,
                AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK
        );
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
        if (point.y < minAllowedY || point.y > maxAllowedY) {
            return null;
        }

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

    private void hideCurrentPopupImmediately() {
        if (currentPopup != null && !currentPopup.isDisposed()) {
            currentPopup.cancel();
        }
        currentPopup = null;
        currentPopupEditor = null;
    }
}
