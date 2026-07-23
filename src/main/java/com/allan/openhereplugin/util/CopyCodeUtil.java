package com.allan.openhereplugin.util;

import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Method;
import java.util.List;

public class CopyCodeUtil {
    public static void performCopy(com.intellij.openapi.project.Project project, @NotNull Editor editor) {
        try {
            SelectionModel selectionModel = editor.getSelectionModel();
            String fileName = getFileName(editor);
            Logger.d("[GBOH_DIFF_TRACE] copy code action: fileName=" + fileName + ", hasSelection=" + selectionModel.hasSelection());
            if (!selectionModel.hasSelection() || fileName == null) {
                return;
            }

            Document document = editor.getDocument();
            int startLine = document.getLineNumber(selectionModel.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(selectionModel.getSelectionEnd()) + 1;

            String simpleCopyText = fileName + " " + (startLine == endLine ? "Line" + startLine : "Line" + startLine + "-" + endLine);
            CopyPasteManager.getInstance().setContents(new StringSelection(simpleCopyText));
            Logger.sendNotification("copied success! \n" + simpleCopyText, project, NotificationType.INFORMATION);
        } catch (Exception e) {
            Logger.d("[GBOH_DIFF_TRACE] CopyCodeUtil performCopy error: " + e.getMessage());
            if (project != null && !project.isDisposed()) {
                try {
                    Logger.sendNotification("copy failed: " + e.getMessage(), project, NotificationType.ERROR);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Nullable
    public static String getFileName(@NotNull Editor editor) {
        VirtualFile documentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        VirtualFile editorFile = editor instanceof EditorEx ? ((EditorEx) editor).getVirtualFile() : null;
        Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve start: editor=" + editor.getClass().getSimpleName()
                + ", documentFile=" + getFileInfo(documentFile) + ", editorFile=" + getFileInfo(editorFile));
        if (editor instanceof EditorEx) {
            DiffRequest request = DiffDataKeys.DIFF_REQUEST.getData(((EditorEx) editor).getDataContext());
            Logger.d("[GBOH_DIFF_TRACE] copy fileName diff request=" + (request == null ? "null" : request.getClass().getSimpleName()));
            if (request instanceof ContentDiffRequest) {
                String fallbackFileName = null;
                for (DiffContent content : ((ContentDiffRequest) request).getContents()) {
                    if (content instanceof DocumentContent) {
                        String fileName = content.getUserData(DiffUserDataKeysEx.FILE_NAME);
                        VirtualFile file = ((DocumentContent) content).getHighlightFile();
                        if (fileName == null) {
                            fileName = file == null ? null : file.getName();
                        }
                        boolean documentMatched = ((DocumentContent) content).getDocument() == editor.getDocument();
                        Logger.d("[GBOH_DIFF_TRACE] copy fileName diff content: type=" + content.getClass().getSimpleName()
                                + ", documentMatched=" + documentMatched + ", fileName=" + fileName
                                + ", highlightFile=" + getFileInfo(file));
                        if (documentMatched) {
                            Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=matchedDiffContent, fileName=" + fileName);
                            return fileName;
                        }
                        if (fallbackFileName == null) {
                            fallbackFileName = fileName;
                        }
                    }
                }
                Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=fallbackDiffContent, fileName=" + fallbackFileName);
                return fallbackFileName;
            }
        }

        VirtualFile file = documentFile == null ? editorFile : documentFile;
        String previewFileName = getPreviewDiffFileName(file);
        if (previewFileName != null) {
            Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=previewDiffProducer, fileName=" + previewFileName);
            return previewFileName;
        }
        if (file == null || "TabPreviewDiffVirtualFile".equals(file.getName())
                || "TabPreviewDiffVirtualFile".equals(file.getClass().getSimpleName())) {
            Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=editorFile, fileName=null");
            return null;
        }
        Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=editorFile, fileName=" + file.getName());
        return file.getName();
    }

    @Nullable
    public static String getPreviewDiffFileName(@Nullable VirtualFile file) {
        if (file == null) {
            return null;
        }
        try {
            Class<?> producerFileClass = Class.forName("com.intellij.diff.editor.DiffVirtualFileWithProducers");
            if (!producerFileClass.isInstance(file)) {
                return null;
            }
            Method collectMethod = producerFileClass.getMethod("collectDiffProducers", boolean.class);
            boolean selectedOnly = true;
            Object selection = collectMethod.invoke(file, true);
            if (!(selection instanceof com.intellij.openapi.ListSelection)) {
                return null;
            }
            com.intellij.openapi.ListSelection<?> producers = (com.intellij.openapi.ListSelection<?>) selection;
            if (producers.isEmpty()) {
                selectedOnly = false;
                selection = collectMethod.invoke(file, false);
                if (!(selection instanceof com.intellij.openapi.ListSelection)) {
                    return null;
                }
                producers = (com.intellij.openapi.ListSelection<?>) selection;
            }
            int selectedIndex = producers.getSelectedIndex();
            List<?> producerList = producers.getList();
            if (selectedIndex < 0 || selectedIndex >= producerList.size()) {
                return null;
            }
            Object producer = producerList.get(selectedIndex);
            if (!(producer instanceof com.intellij.diff.chains.DiffRequestProducer)) {
                return null;
            }
            String path = ((com.intellij.diff.chains.DiffRequestProducer) producer).getName();
            int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            String fileName = separator < 0 ? path : path.substring(separator + 1);
            Logger.d("[GBOH_DIFF_TRACE] preview diff producer: type=" + producer.getClass().getSimpleName()
                    + ", selectedOnly=" + selectedOnly + ", path=" + path + ", fileName=" + fileName);
            return fileName;
        } catch (Exception e) {
            Logger.d("[GBOH_DIFF_TRACE] preview diff producer resolve error: " + e.getMessage());
            return null;
        }
    }

    private static String getFileInfo(@Nullable VirtualFile file) {
        return file == null ? "null" : file.getClass().getSimpleName() + ":" + file.getName();
    }
}
