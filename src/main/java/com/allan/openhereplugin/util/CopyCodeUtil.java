package com.allan.openhereplugin.util;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

public class CopyCodeUtil {
    public static void performCopy(com.intellij.openapi.project.Project project, @NotNull Editor editor, @org.jetbrains.annotations.Nullable com.intellij.openapi.vfs.VirtualFile vf) {
        try {
            SelectionModel selectionModel = editor.getSelectionModel();
            if (!selectionModel.hasSelection() || vf == null) {
                return;
            }

            Document document = editor.getDocument();
            int startLine = document.getLineNumber(selectionModel.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(selectionModel.getSelectionEnd()) + 1;

            // Diff 编辑器绑定的是实际文件时直接取其名称，不解析 Repository Diff 等界面标题。
            String fileName = vf.getName();
            String simpleCopyText = fileName + " " + (startLine == endLine ? "Line" + startLine : "Line" + startLine + "-" + endLine);
            CopyPasteManager.getInstance().setContents(new StringSelection(simpleCopyText));
            Logger.sendNotification("copied success! \n" + simpleCopyText, project, NotificationType.INFORMATION);
        } catch (Exception e) {
            Logger.d("CopyCodeUtil performCopy error: " + e.getMessage());
            if (project != null && !project.isDisposed()) {
                try {
                    Logger.sendNotification("copy failed: " + e.getMessage(), project, NotificationType.ERROR);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
