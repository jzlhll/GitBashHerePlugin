package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.CopyCodeUtil;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

public class GitBashCopyNameAction extends AnAction {

    public String changeName(String name) {
        return name;
    }

    protected String getCopyText(@NotNull AnActionEvent event, String name, VirtualFile file, Editor editor, DiffRequest diffRequest) {
        return changeName(name);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            String name = editor == null ? null : CopyCodeUtil.getFileName(editor);
            VirtualFile virtualFile = getVirtualFile(event, editor);
            DiffRequest diffRequest = event.getData(DiffDataKeys.DIFF_REQUEST);
            Logger.d("[GBOH_DIFF_TRACE] copy name action: editor=" + (editor == null ? "null" : editor.getClass().getSimpleName())
                    + ", eventDiffRequest=" + (diffRequest == null ? "null" : diffRequest.getClass().getSimpleName())
                    + ", resolvedFileName=" + name);
            if (diffRequest instanceof ContentDiffRequest) {
                if (editor == null) {
                    Logger.d("[GBOH_DIFF_TRACE] copy name action result: fileName=null, reason=missingEditor");
                    return;
                }
                String fallbackFileName = null;
                for (DiffContent content : ((ContentDiffRequest) diffRequest).getContents()) {
                    if (content instanceof DocumentContent) {
                        VirtualFile file = ((DocumentContent) content).getHighlightFile();
                        name = content.getUserData(DiffUserDataKeysEx.FILE_NAME);
                        if (name == null) {
                            name = file == null ? null : file.getName();
                        }
                        if (((DocumentContent) content).getDocument() == editor.getDocument()) {
                            virtualFile = file;
                            break;
                        }
                        if (fallbackFileName == null) {
                            fallbackFileName = name;
                        }
                        name = null;
                    }
                }
                if (name == null) {
                    name = fallbackFileName;
                }
                if (name == null) {
                    Logger.d("[GBOH_DIFF_TRACE] copy name action result: fileName=null, reason=missingDiffContentName");
                    return;
                }
            }
            if (name == null) {
                if (virtualFile == null) {
                    Logger.d("[GBOH_DIFF_TRACE] copy name action result: fileName=null, reason=missingVirtualFile");
                    return;
                }
                name = CopyCodeUtil.getPreviewDiffFileName(virtualFile);
                if (name != null) {
                    Logger.d("[GBOH_DIFF_TRACE] copy name action result: source=previewDiffProducer, fileName=" + name);
                } else {
                    name = virtualFile.getName();
                }
                if ("TabPreviewDiffVirtualFile".equals(name)) {
                    Logger.d("[GBOH_DIFF_TRACE] copy name action result: fileName=null, reason=previewVirtualFile");
                    return;
                }
            }
            name = getCopyText(event, name, virtualFile, editor, diffRequest);
            Logger.d("[GBOH_DIFF_TRACE] copy name action result: fileName=" + name);
            CopyPasteManager.getInstance().setContents(new StringSelection(name));

            Logger.sendNotification("copied success! " + name, event, NotificationType.INFORMATION);
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }

    @Override
    public final void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(isNeedShow());
    }

    protected boolean isNeedShow() {
        return !GitOpenHereSettings.getInstance().getState().isCopyNameChecked;
    }

    private VirtualFile getVirtualFile(@NotNull AnActionEvent event, Editor editor) {
        VirtualFile file = editor == null ? null : FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null && editor instanceof EditorEx) {
            file = ((EditorEx) editor).getVirtualFile();
        }
        return file == null ? event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE) : file;
    }

    public static class FullName extends GitBashCopyNameAction {
        @Override
        protected String getCopyText(@NotNull AnActionEvent event, String name, VirtualFile file, Editor editor, DiffRequest diffRequest) {
            String filePath = CopyCodeUtil.getFilePath(event, diffRequest, editor, file);
            return filePath == null ? name : filePath;
        }

        @Override
        protected boolean isNeedShow() {
            return !GitOpenHereSettings.getInstance().getState().isCopyFullNameChecked;
        }
    }
}
