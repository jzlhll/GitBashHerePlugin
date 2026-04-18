package com.allan.openhereplugin.util;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.util.Logger;
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
            if (!selectionModel.hasSelection()) {
                return;
            }

            var bean = Common.findClosestGitRoot(vf);
            String path = "";
            if (bean != null) {
                if (bean instanceof PathInfo) {
                    path = ((PathInfo) bean).relativePath;
                } else {
                    path = bean.path;
                }
            } else {
                if (vf != null) {
                    path = vf.toString().replace("file://", "");
                }
            }

            if (path == null || path.isEmpty()) {
                return;
            }

            Document document = editor.getDocument();
            int startLine = document.getLineNumber(selectionModel.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(selectionModel.getSelectionEnd()) + 1;

            String lineStr = startLine == endLine ? "Line" + startLine + ":" : "Line" + startLine + "-" + endLine + ":";
            
            boolean isSimpleCopy = com.allan.openhereplugin.config.GitOpenHereSettings.getInstance().getState().isGBOHSimpleCopyChecked;
            if (isSimpleCopy) {
                String fileName = vf != null ? vf.getName() : new java.io.File(path).getName();
                String simpleCopyText = fileName + " " + (startLine == endLine ? "Line" + startLine : "Line" + startLine + "-Line" + endLine);
                CopyPasteManager.getInstance().setContents(new StringSelection(simpleCopyText));
                Logger.sendNotification("copied success! \n" + simpleCopyText, project, NotificationType.INFORMATION);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(path).append("\n");
            sb.append(lineStr);

            String selectedText = selectionModel.getSelectedText();
            if (selectedText == null) {
                selectedText = "";
            }

            int lineCount = endLine - startLine + 1;
            String toastText;
            String trimmedText = selectedText.trim();
            if (lineCount <= 3) {
                sb.append("\n").append(selectedText);
                
                // Toast logic for <= 3 lines
                int firstSpaceIndex = trimmedText.indexOf(' ');
                int secondSpaceIndex = firstSpaceIndex != -1 ? trimmedText.indexOf(' ', firstSpaceIndex + 1) : -1;
                String startWords = secondSpaceIndex != -1 ? trimmedText.substring(0, secondSpaceIndex) : trimmedText;
                if (startWords.length() < 15 && trimmedText.length() >= 15) {
                    startWords = trimmedText.substring(0, 15).replaceAll("[\\r\\n\\t]+", " ").trim();
                }
                toastText = lineStr + " " + startWords + "......";
            } else {
                // > 3 lines logic
                int firstSpaceIndex = trimmedText.indexOf(' ');
                int secondSpaceIndex = firstSpaceIndex != -1 ? trimmedText.indexOf(' ', firstSpaceIndex + 1) : -1;
                String startWords = secondSpaceIndex != -1 ? trimmedText.substring(0, secondSpaceIndex) : trimmedText;
                if (startWords.length() < 15 && trimmedText.length() >= 15) {
                    startWords = trimmedText.substring(0, 15).replaceAll("[\\r\\n\\t]+", " ").trim();
                }
                
                int lastSpaceIndex = trimmedText.lastIndexOf(' ');
                int secondLastSpaceIndex = lastSpaceIndex != -1 ? trimmedText.lastIndexOf(' ', lastSpaceIndex - 1) : -1;
                String endWords = secondLastSpaceIndex != -1 ? trimmedText.substring(secondLastSpaceIndex + 1) : trimmedText;
                if (endWords.length() < 15 && trimmedText.length() >= 15) {
                    endWords = trimmedText.substring(trimmedText.length() - 15).replaceAll("[\\r\\n\\t]+", " ").trim();
                }
                
                String ellipseText = "code is from Line" + startLine + " \"" + startWords + "...\" to Line" + endLine + " \"..." + endWords + "\"";
                sb.append("\n").append(ellipseText);
                
                toastText = lineStr + " " + startWords + "......";
            }

            String result = sb.toString();
            CopyPasteManager.getInstance().setContents(new StringSelection(result));

            Logger.sendNotification("copied success! \n" + toastText, project, NotificationType.INFORMATION);
        } catch (Exception e) {
            // ignore
        }
    }
}
