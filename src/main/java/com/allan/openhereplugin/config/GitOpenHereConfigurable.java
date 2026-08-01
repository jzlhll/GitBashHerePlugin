package com.allan.openhereplugin.config;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.runs.abs.IWindowGitBashRuns;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;

public class GitOpenHereConfigurable implements Configurable {
    private JPanel mainPanel;
    @Nullable
    private JTextField exePathTextField;

    private JCheckBox gitStatusCheckBox;
    private JCheckBox gitDiffCheckBox;
    private JCheckBox gitLogCheckBox;
    private JCheckBox gitPushCheckBox;
    //private JCheckBox warpTabCheckBox;

    private JCheckBox copyNameCheckBox;
    private JCheckBox copyFullNameCheckBox;
    private JCheckBox copyNameNoExtensionCheckBox;
    private JCheckBox enableGBOHIconCheckBox;
    private JCheckBox gbohCopyFullPathCheckBox;
    private JCheckBox windowCmdCheckBox;
    private JCheckBox windowUsePowerShellCheckBox;

    private JCheckBox gitBashCheckbox;
    private JCheckBox warpCheckbox;

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        addSectionTitle("About Git Bash");
        addCheckboxRow("Enable ‘Git Bash’", gitBashCheckbox = new JCheckBox(), false);
        var gitBashRuns = Common.gitBashRunner;
        if (gitBashRuns instanceof IWindowGitBashRuns) {
            var runner = (IWindowGitBashRuns) gitBashRuns;
            var ans = runner.findPathExe();
            if ("ok".equals(ans.first)) {
                exePathTextField = addSectionHeader("Your git-bash.exe path is:  " + runner.origPathExe(), "You can change it here:");
            } else {
                exePathTextField = addSectionHeader("Can find your git-bash.exe path.", "Custom git-bash.exe path here:");
            }
            mainPanel.add(Box.createVerticalStrut(12));
        }


        addCheckboxRow("Hide options: git status",
                gitStatusCheckBox = new JCheckBox(), true, true);
        addCheckboxRow("Hide options: git diff",
                gitDiffCheckBox = new JCheckBox(), true, true);
        addCheckboxRow("Hide options: git log",
                gitLogCheckBox = new JCheckBox(), true, true);
        addCheckboxRow("Hide options: git push",
                gitPushCheckBox = new JCheckBox(), true, true);

        addSeparator();

        addSectionTitle("About Warp");
        addCheckboxRow("Enable ‘Warp’", warpCheckbox = new JCheckBox(), true);
//        addCheckboxRow("Hide options: warp tab",
//                warpTabCheckBox = new JCheckBox(), true);
        addSeparator();

        addSectionTitle("About other features");
        if (Common.SYSTEM_WINDOWS.equals(Common.supportSystem())) {
            addCheckboxRow("Enable ‘Windows cmd’ (Directly to directory not git root)", windowCmdCheckBox = new JCheckBox(), false);
            addCheckboxRow("Instead with PowerShell", windowUsePowerShellCheckBox = new JCheckBox(), false, true);
        }

        addCheckboxRow("Hide options: copy name",
                copyNameCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: copy name no extension",
                copyNameNoExtensionCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: copy file path",
                copyFullNameCheckBox = new JCheckBox(), true);
        
        addSeparator();

        addGBOHCopySection();
        addCheckboxRow("Enable",
                enableGBOHIconCheckBox = new JCheckBox(), false);
        addCheckboxRow("Use full file path", gbohCopyFullPathCheckBox = new JCheckBox(), true, true);
        addSeparator();

        mainPanel.add(Box.createVerticalStrut(12));

        JBScrollPane scrollPane = new JBScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除默认边框，保持原有的视觉效果
        return scrollPane;
    }

    @Override
    public boolean isModified() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        var editSame = exePathTextField == null || exePathTextField.getText().equals(state.gitBashCustomPath);
        boolean isWindowCmdSame = isWindowCmdSame(state);

        return !editSame ||
                gitStatusCheckBox.isSelected() != state.isGitStatusChecked ||
                gitDiffCheckBox.isSelected() != state.isGitDiffChecked ||
                gitLogCheckBox.isSelected() != state.isGitLogChecked ||
                gitPushCheckBox.isSelected() != state.isGitPushChecked ||
               // warpTabCheckBox.isSelected() != state.isWarpTabChecked ||
                copyNameNoExtensionCheckBox.isSelected() != state.isCopyNameNoExChecked ||
                copyFullNameCheckBox.isSelected() != state.isCopyFullNameChecked ||
                enableGBOHIconCheckBox.isSelected() != state.isGBOHFloatingIconEnabled ||
                gbohCopyFullPathCheckBox.isSelected() != state.isGBOHCopyFullPathEnabled ||
                !isWindowCmdSame ||
                copyNameCheckBox.isSelected() != state.isCopyNameChecked ||
                getSelectedGitToolType() != state.gitToolType;
    }

    private boolean isWindowCmdSame(GitOpenHereSettings.State state) {
        return windowCmdCheckBox == null || getSelectedWindowCmdType() == state.windowCmdType;
    }

    @Override
    public void apply() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        if(exePathTextField != null) state.gitBashCustomPath = exePathTextField.getText().trim();

        state.isGitStatusChecked = gitStatusCheckBox.isSelected();
        state.isGitDiffChecked = gitDiffCheckBox.isSelected();
        state.isGitLogChecked = gitLogCheckBox.isSelected();
        state.isGitPushChecked = gitPushCheckBox.isSelected();
      //  state.isWarpTabChecked = warpTabCheckBox.isSelected();
        state.isCopyNameNoExChecked = copyNameNoExtensionCheckBox.isSelected();
        state.isCopyFullNameChecked = copyFullNameCheckBox.isSelected();
        state.isGBOHFloatingIconEnabled = enableGBOHIconCheckBox.isSelected();
        state.isGBOHCopyFullPathEnabled = gbohCopyFullPathCheckBox.isSelected();
        if (windowCmdCheckBox != null && windowUsePowerShellCheckBox != null) {
            state.windowCmdType = getSelectedWindowCmdType();
        }
        state.isCopyNameChecked = copyNameCheckBox.isSelected();
        state.gitToolType = getSelectedGitToolType();
    }

    @Override
    public void reset() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        if(exePathTextField != null) exePathTextField.setText(state.gitBashCustomPath);

        gitStatusCheckBox.setSelected(state.isGitStatusChecked);
        gitDiffCheckBox.setSelected(state.isGitDiffChecked);
        gitLogCheckBox.setSelected(state.isGitLogChecked);
        gitPushCheckBox.setSelected(state.isGitPushChecked);
      //  warpTabCheckBox.setSelected(state.isWarpTabChecked);
        copyNameCheckBox.setSelected(state.isCopyNameChecked);
        copyFullNameCheckBox.setSelected(state.isCopyFullNameChecked);
        copyNameNoExtensionCheckBox.setSelected(state.isCopyNameNoExChecked);
        enableGBOHIconCheckBox.setSelected(state.isGBOHFloatingIconEnabled);
        gbohCopyFullPathCheckBox.setSelected(state.isGBOHCopyFullPathEnabled);
        if(windowCmdCheckBox != null) windowCmdCheckBox.setSelected(state.windowCmdType > 0);
        if(windowUsePowerShellCheckBox != null) windowUsePowerShellCheckBox.setSelected(state.windowCmdType == GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD);

        gitBashCheckbox.setSelected(state.gitToolType == 0 || state.gitToolType == 2);
        warpCheckbox.setSelected(state.gitToolType == 1 || state.gitToolType == 2);
    }

    @Override
    public @Nls String getDisplayName() {
        return "GitBashOpenHere";
    }

    private int getSelectedGitToolType() {
        boolean gitBashEnabled = gitBashCheckbox.isSelected();
        boolean warpEnabled = warpCheckbox.isSelected();
        if (gitBashEnabled && warpEnabled) {
            return GitOpenHereSettings.GIT_TOOL_TYPE_BASH_AND_WARP;
        }
        if (gitBashEnabled) {
            return GitOpenHereSettings.GIT_TOOL_TYPE_BASH;
        }
        return warpEnabled ? GitOpenHereSettings.GIT_TOOL_TYPE_WARP : -1;
    }

    private int getSelectedWindowCmdType() {
        if (!windowCmdCheckBox.isSelected()) {
            return GitOpenHereSettings.WINDOW_CMD_TYPE_NO;
        }
        return windowUsePowerShellCheckBox.isSelected()
                ? GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD
                : GitOpenHereSettings.WINDOW_CMD_TYPE_CMD;
    }

    private JTextField addSectionHeader(String text, String desc) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 16, 8, 0));

        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label2 = new JLabel(desc);
        label2.setAlignmentX(Component.LEFT_ALIGNMENT);
        label2.setForeground(Color.GRAY);
        panel.add(label);
        panel.add(Box.createVerticalStrut(2));
        panel.add(label2);
        panel.add(Box.createVerticalStrut(4));

        JTextField field = new JTextField();
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        field.setToolTipText("Example: C:\\Program Files\\Git\\bin\\git.exe");
        panel.add(field);

        mainPanel.add(panel);

        return field;
    }

    private void addGBOHCopySection() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));

        panel.add(createSectionTitle("About Floating Button ‘GBOH’"));
        panel.add(Box.createVerticalStrut(2));

        JLabel descriptionLabel = new JLabel("After selecting code, it puts the file reference and selected line range on the clipboard.");
        descriptionLabel.setForeground(Color.GRAY);
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 15));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descriptionLabel);
        mainPanel.add(panel);
    }

    private void addSectionTitle(String title) {
        mainPanel.add(createSectionTitle(title));
    }

    private TitledSeparator createSectionTitle(String title) {
        TitledSeparator separator = new TitledSeparator(title);
        separator.setTitleFont(separator.getTitleFont().deriveFont(Font.BOLD, 16));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, separator.getPreferredSize().height));
        return separator;
    }

    private void addCheckboxRow(String title, JCheckBox checkBox, boolean hasVertPadding) {
        addCheckboxRow(title, checkBox, hasVertPadding, false);
    }

    private void addCheckboxRow(String title, JCheckBox checkBox, boolean hasVertPadding, boolean needLeftPadding) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(0, needLeftPadding ? 16 : 0, 0, 0));

        checkBox.setText(title);
        checkBox.setOpaque(false);
        panel.add(checkBox);
        mainPanel.add(panel);
        if (hasVertPadding) mainPanel.add(Box.createVerticalStrut(2));
    }

    private void addSeparator() {
        mainPanel.add(Box.createVerticalStrut(6));
        var sp = new JSeparator(SwingConstants.HORIZONTAL);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        mainPanel.add(sp);
        mainPanel.add(Box.createVerticalStrut(6));
    }
}
