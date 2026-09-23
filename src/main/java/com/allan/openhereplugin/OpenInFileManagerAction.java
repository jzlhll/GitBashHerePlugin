package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.CopyCodeUtil;
import com.allan.openhereplugin.util.Logger;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vcs.VcsDataKeys;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class OpenInFileManagerAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (!GitOpenHereSettings.getInstance().getState().isDiffOpenInFileManagerEnabled) {
            return;
        }
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        DiffRequest request = event.getData(DiffDataKeys.DIFF_REQUEST);
        if (request == null && editor instanceof EditorEx) {
            request = DiffDataKeys.DIFF_REQUEST.getData(((EditorEx) editor).getDataContext());
        }
        File file = CopyCodeUtil.getLocalFile(CopyCodeUtil.getFilePath(
                event, request, editor, event.getData(CommonDataKeys.VIRTUAL_FILE)));
        if (file == null) {
            Logger.sendNotification("File location is unavailable", event, NotificationType.WARNING);
            return;
        }
        if (file.exists()) {
            RevealFileAction.openFile(file);
            return;
        }
        File directory = file.getParentFile();
        while (directory != null && !directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        if (directory == null) {
            Logger.sendNotification("File location is unavailable", event, NotificationType.WARNING);
            return;
        }
        RevealFileAction.openDirectory(directory);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        if (!GitOpenHereSettings.getInstance().getState().isDiffOpenInFileManagerEnabled) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        boolean isDiff = event.getData(DiffDataKeys.DIFF_REQUEST) != null
                || event.getData(VcsDataKeys.CURRENT_CHANGE) != null
                || event.getData(VcsDataKeys.CHANGES) != null
                || (editor instanceof EditorEx
                && DiffDataKeys.DIFF_REQUEST.getData(((EditorEx) editor).getDataContext()) != null)
                || CopyCodeUtil.getPreviewDiffFilePath(event.getData(CommonDataKeys.VIRTUAL_FILE)) != null;
        event.getPresentation().setEnabledAndVisible(isDiff && RevealFileAction.isSupported());
        String system = Common.supportSystem();
        event.getPresentation().setText(Common.SYSTEM_MAC.equals(system) ? "Open in Finder"
                : Common.SYSTEM_WINDOWS.equals(system) ? "Open in File Explorer" : "Open in File Manager");
    }
}
