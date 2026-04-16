package com.allan.openhereplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
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

    public static SelectionPopupListener getInstance() {
        return INSTANCE;
    }

    public synchronized void init() {
        if (initialized) return;
        initialized = true;
        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(this, ApplicationManager.getApplication());
    }

    private final Map<Editor, Alarm> alarms = new HashMap<>();

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        if (editor.getProject() == null || editor.getDocument() == null) {
            return;
        }
        handleSelectionChanged(editor);
    }

    private void handleSelectionChanged(Editor editor) {
        if (!com.allan.openhereplugin.config.GitOpenHereSettings.getInstance().getState().isEnableGBOHIcon) {
            return;
        }

        Alarm alarm = alarms.computeIfAbsent(editor, e -> new Alarm(Alarm.ThreadToUse.POOLED_THREAD, com.intellij.openapi.project.ProjectManager.getInstance().getDefaultProject()));

        alarm.cancelAllRequests();

        // 隐藏当前可能存在的 popup (如果当前正显示着，先隐藏)
        ApplicationManager.getApplication().invokeLater(() -> {
            if (currentPopup != null && !currentPopup.isDisposed()) {
                currentPopup.cancel();
            }
        });

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
        }, 800);
    }

    private void showPopup(Editor editor) {
        if (editor.isDisposed() || !editor.getSelectionModel().hasSelection()) {
            return;
        }

        if (currentPopup != null && !currentPopup.isDisposed()) {
            currentPopup.cancel();
        }
        
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

        // 计算显示位置（选中文本的右上角）
        SelectionModel selectionModel = editor.getSelectionModel();
        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.getSelectionEnd();
        
        int startLine = editor.getDocument().getLineNumber(selectionStart);
        int endLine = editor.getDocument().getLineNumber(selectionEnd);
        
        int topRightOffset;
        if (startLine == endLine) {
            topRightOffset = selectionEnd;
        } else {
            topRightOffset = Math.min(editor.getDocument().getLineEndOffset(startLine), selectionEnd);
        }

        Point point = editor.visualPositionToXY(editor.offsetToVisualPosition(topRightOffset));
        
        // 限制弹出的 X 坐标（限制在编辑器视口可视区域宽度的 30% 到 60% 之间）
        int visibleWidth = editor.getScrollingModel().getVisibleArea().width;
        int scrollX = editor.getScrollingModel().getVisibleArea().x; // 当前横向滚动条的偏移量
        
        int minAllowedX = scrollX + (int) (visibleWidth * 0.3);
        int maxAllowedX = scrollX + (int) (visibleWidth * 0.6);
        
        if (point.x < minAllowedX) {
            point.x = minAllowedX;
        } else if (point.x > maxAllowedX) {
            point.x = maxAllowedX;
        }
        
        // 将编辑器坐标转换为屏幕坐标
        SwingUtilities.convertPointToScreen(point, editor.getContentComponent());
        
        // 将图标定位到右上角：向右偏移一点，向上偏移一个行高避免遮挡第一行代码
        point.translate(5, -20);

        currentPopup.showInScreenCoordinates(editor.getContentComponent(), point);
    }
}
