package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.CopyCodeUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.io.File;

public class GitRepositoryNameAction extends DumbAwareAction implements CustomComponentAction {
    private String lastFilePath;
    private File lastRepositoryRoot;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (!GitOpenHereSettings.getInstance().getState().isRepositoryNameEnabled) {
            return;
        }
        File root = getRepositoryRoot(event);
        if (root != null && RevealFileAction.isSupported()) {
            RevealFileAction.openDirectory(root);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        if (!GitOpenHereSettings.getInstance().getState().isRepositoryNameEnabled) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }
        File root = getRepositoryRoot(event);
        event.getPresentation().setEnabledAndVisible(root != null);
        if (root != null) {
            event.getPresentation().setText(root.getName());
            event.getPresentation().setDescription(root.getAbsolutePath());
        }
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        JButton button = new JButton(presentation.getText());
        button.setFont(JBUI.Fonts.toolbarFont().deriveFont(JBUI.Fonts.toolbarFont().getSize2D() + 2));
        button.setIcon(AllIcons.Actions.MenuOpen);
        button.setIconTextGap(JBUI.scale(4));
        button.setBorder(JBUI.Borders.empty(2, 4));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.addActionListener(e -> ActionManager.getInstance().tryToExecute(this, null, button, place, true));
        updateCustomComponent(button, presentation);
        return button;
    }

    @Override
    public void updateCustomComponent(@NotNull JComponent component, @NotNull Presentation presentation) {
        JButton button = (JButton) component;
        button.setText(presentation.getText());
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        Insets insets = button.getInsets();
        Icon icon = button.getIcon();
        button.setPreferredSize(new Dimension(
                metrics.stringWidth(button.getText()) + icon.getIconWidth() + button.getIconTextGap() + insets.left + insets.right,
                Math.max(metrics.getHeight(), icon.getIconHeight()) + insets.top + insets.bottom));
        button.setToolTipText(presentation.getDescription());
        button.setEnabled(presentation.isEnabled());
        button.setVisible(presentation.isVisible());
    }

    private File getRepositoryRoot(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null || project.isDisposed()) {
            return null;
        }
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        String path = editor == null ? null : CopyCodeUtil.getFilePath(editor);
        if (path == null) {
            VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
            if (file == null) {
                VirtualFile[] files = FileEditorManager.getInstance(project).getSelectedFiles();
                file = files.length == 0 ? null : files[0];
            }
            path = CopyCodeUtil.getPreviewDiffFilePath(file);
            if (path == null && file != null) {
                path = file.getPath();
            }
        }
        if (path == null) {
            path = project.getBasePath();
        }
        File currentFile = CopyCodeUtil.getLocalFile(path);
        if (currentFile == null) {
            currentFile = CopyCodeUtil.getLocalFile(project.getBasePath());
            if (currentFile == null) {
                return null;
            }
        }
        String filePath = currentFile.getAbsolutePath();
        if (filePath.equals(lastFilePath)) {
            return lastRepositoryRoot;
        }
        lastFilePath = filePath;
        lastRepositoryRoot = null;
        for (File directory = currentFile; directory != null; directory = directory.getParentFile()) {
            if (new File(directory, ".git").exists()) {
                lastRepositoryRoot = directory;
                break;
            }
        }
        return lastRepositoryRoot;
    }
}
