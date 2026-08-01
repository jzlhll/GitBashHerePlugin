package com.allan.openhereplugin.util;

import com.intellij.diff.DiffVcsDataKeys;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Method;
import java.util.List;

public class CopyCodeUtil {
    public static void performCopy(com.intellij.openapi.project.Project project, @NotNull Editor editor) {
        performCopy(project, editor, false);
    }

    public static void performCopy(com.intellij.openapi.project.Project project, @NotNull Editor editor, boolean copyFullPath) {
        try {
            SelectionModel selectionModel = editor.getSelectionModel();
            String fileInfo = copyFullPath ? getFilePath(editor) : getFileName(editor);
            Logger.d("[GBOH_DIFF_TRACE] copy code action: fileInfo=" + fileInfo + ", hasSelection=" + selectionModel.hasSelection());
            if (!selectionModel.hasSelection() || fileInfo == null) {
                return;
            }

            Document document = editor.getDocument();
            int startLine = document.getLineNumber(selectionModel.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(selectionModel.getSelectionEnd()) + 1;

            String simpleCopyText = fileInfo + " " + (startLine == endLine ? "Line" + startLine : "Line" + startLine + "-" + endLine);
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
    public static String getFilePath(@NotNull Editor editor) {
        DiffRequest request = editor instanceof EditorEx
                ? DiffDataKeys.DIFF_REQUEST.getData(((EditorEx) editor).getDataContext())
                : null;
        return getFilePath(request, editor);
    }

    @Nullable
    public static String getFilePath(
            @NotNull AnActionEvent event,
            @Nullable DiffRequest request,
            @Nullable Editor editor,
            @Nullable VirtualFile virtualFile
    ) {
        String filePath = getChangePath(event.getData(VcsDataKeys.CURRENT_CHANGE));
        if (filePath == null) {
            Change[] changes = event.getData(VcsDataKeys.CHANGES);
            if (changes != null && changes.length > 0) {
                filePath = getChangePath(changes[0]);
            }
        }
        if (filePath != null) {
            return filePath;
        }
        if (editor != null) {
            filePath = getFilePath(request, editor);
            if (filePath != null) {
                return filePath;
            }
        }
        String previewFilePath = getPreviewDiffFilePath(virtualFile);
        if (previewFilePath != null) {
            return previewFilePath;
        }
        return isPreviewDiffFile(virtualFile) ? null : virtualFile == null ? null : shortenHomePath(virtualFile.getPath());
    }

    @Nullable
    public static String getFilePath(@Nullable DiffRequest request, @NotNull Editor editor) {
        VirtualFile documentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        VirtualFile editorFile = editor instanceof EditorEx ? ((EditorEx) editor).getVirtualFile() : null;
        if (request instanceof ContentDiffRequest) {
            String fallbackPath = null;
            for (DiffContent content : ((ContentDiffRequest) request).getContents()) {
                if (!(content instanceof DocumentContent)) {
                    continue;
                }
                String path = getDiffContentPath(content, (DocumentContent) content);
                if (((DocumentContent) content).getDocument() == editor.getDocument() && path != null) {
                    return shortenHomePath(path);
                }
                if (fallbackPath == null) {
                    fallbackPath = path;
                }
            }
            if (fallbackPath != null) {
                return shortenHomePath(fallbackPath);
            }
        }

        String changePath = getChangePath(request);
        if (changePath != null) {
            return shortenHomePath(changePath);
        }

        VirtualFile file = documentFile == null ? editorFile : documentFile;
        String previewPath = getPreviewDiffFilePath(file);
        if (previewPath != null) {
            return shortenHomePath(previewPath);
        }
        if (isPreviewDiffFile(file)) {
            return null;
        }
        return shortenHomePath(file.getPath());
    }

    @Nullable
    private static String getDiffContentPath(@NotNull DiffContent content, @NotNull DocumentContent documentContent) {
        Pair<FilePath, VcsRevisionNumber> revisionInfo = content.getUserData(DiffVcsDataKeys.REVISION_INFO);
        if (revisionInfo != null) {
            return revisionInfo.first.getPath();
        }
        VirtualFile file = documentContent.getHighlightFile();
        return isPreviewDiffFile(file) ? null : file == null ? null : file.getPath();
    }

    @Nullable
    private static String getChangePath(@Nullable DiffRequest request) {
        if (request == null) {
            return null;
        }
        Change change = request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY);
        return getChangePath(change);
    }

    @Nullable
    public static String getChangePath(@Nullable Change change) {
        return change == null ? null : ChangesUtil.getFilePath(change).getPath();
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
        if (isPreviewDiffFile(file)) {
            Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=editorFile, fileName=null");
            return null;
        }
        Logger.d("[GBOH_DIFF_TRACE] copy fileName resolve result: source=editorFile, fileName=" + file.getName());
        return file.getName();
    }

    @Nullable
    public static String getPreviewDiffFileName(@Nullable VirtualFile file) {
        String path = getPreviewDiffFilePath(file);
        if (path == null) {
            return null;
        }
        int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return separator < 0 ? path : path.substring(separator + 1);
    }

    @Nullable
    public static String getPreviewDiffFilePath(@Nullable VirtualFile file) {
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
            Logger.d("[GBOH_DIFF_TRACE] preview diff producer: type=" + producer.getClass().getSimpleName()
                    + ", selectedOnly=" + selectedOnly + ", path=" + path);
            return path;
        } catch (Exception e) {
            Logger.d("[GBOH_DIFF_TRACE] preview diff producer resolve error: " + e.getMessage());
            return null;
        }
    }

    private static String shortenHomePath(String path) {
        String homePath = System.getProperty("user.home");
        if (path.equals(homePath)) {
            return "~";
        }
        return path.startsWith(homePath + "/") ? "~" + path.substring(homePath.length()) : path;
    }

    private static boolean isPreviewDiffFile(@Nullable VirtualFile file) {
        return file == null || "TabPreviewDiffVirtualFile".equals(file.getName())
                || "TabPreviewDiffVirtualFile".equals(file.getClass().getSimpleName());
    }

    private static String getFileInfo(@Nullable VirtualFile file) {
        return file == null ? "null" : file.getClass().getSimpleName() + ":" + file.getName();
    }
}
