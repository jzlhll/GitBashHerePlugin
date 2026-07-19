package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            VirtualFile vf = editor == null ? null : FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (vf == null && editor instanceof EditorEx) {
                vf = ((EditorEx) editor).getVirtualFile();
            }
            if (vf == null) {
                vf = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
            }
            if (vf == null) {
                return;
            }

            var name = vf.getName();
            name = changeName(name);
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
}
