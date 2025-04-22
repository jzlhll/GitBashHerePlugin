package com.allan.openhereplugin.config;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.runs.abs.IWindowGitBashRuns;
import com.intellij.openapi.options.Configurable;
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
    //private JCheckBox warpTabCheckBox;

    private JCheckBox copyNameCheckBox;
    private JCheckBox copyNameNoExtensionCheckBox;
    private JCheckBox windowCmdCheckBox;
    private JCheckBox windowUsePowerShellCheckBox;

    private JCheckBox gitBashCheckbox;
    private JCheckBox warpCheckbox;

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
       // mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //git bash 
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
        sperator();

        //warp
        addCheckboxRow("Enable ‘Warp’", warpCheckbox = new JCheckBox(), true);
//        addCheckboxRow("Hide options: warp tab",
//                warpTabCheckBox = new JCheckBox(), true);
        sperator();

        //window cmd
        if (Common.SYSTEM_WINDOWS.equals(Common.supportSystem())) {
            addCheckboxRow("Enable ‘Windows cmd’ (Directly to directory not git root)", windowCmdCheckBox = new JCheckBox(), false);
            addCheckboxRow("Instead with PowerShell", windowUsePowerShellCheckBox = new JCheckBox(), false, true);
            sperator();
        }

        //copy options
        addCheckboxRow("Hide options: copy name",
                copyNameCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: copy name no extension",
                copyNameNoExtensionCheckBox = new JCheckBox(), true);
        sperator();

        mainPanel.add(Box.createVerticalStrut(12));
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        var enable1 = gitBashCheckbox.isSelected();
        var enable2 = warpCheckbox.isSelected();
        int type;
        if (enable1 && enable2) {
            type = 2;
        } else if (enable1) {
            type = 0;
        } else if (enable2) {
            type = 1;
        } else {
            type = -1;
        }

        var editSame = exePathTextField == null || exePathTextField.getText().equals(state.gitBashCustomPath);
        var isWindowCmdSame = isIsWindowCmdSame(state);

        return !editSame ||
                gitStatusCheckBox.isSelected() != state.isGitStatusChecked ||
                gitDiffCheckBox.isSelected() != state.isGitDiffChecked ||
                gitLogCheckBox.isSelected() != state.isGitLogChecked ||
               // warpTabCheckBox.isSelected() != state.isWarpTabChecked ||
                copyNameNoExtensionCheckBox.isSelected() != state.isCopyNameNoExChecked ||
                !isWindowCmdSame ||
                copyNameCheckBox.isSelected() != state.isCopyNameChecked ||
                type != state.gitToolType;
    }

    private boolean isIsWindowCmdSame(GitOpenHereSettings.State state) {
        var isWindowCmdSame = true;
        if (windowCmdCheckBox != null) {
            int windowCmdType;
            if (windowCmdCheckBox.isSelected()) {
                windowCmdType = windowUsePowerShellCheckBox.isSelected() ? GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD : GitOpenHereSettings.WINDOW_CMD_TYPE_CMD;
            } else {
                windowCmdType = GitOpenHereSettings.WINDOW_CMD_TYPE_NO;
            }

            isWindowCmdSame = windowCmdType == state.windowCmdType;
        }
        return isWindowCmdSame;
    }

    @Override
    public void apply() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        if(exePathTextField != null) state.gitBashCustomPath = exePathTextField.getText().trim();

        state.isGitStatusChecked = gitStatusCheckBox.isSelected();
        state.isGitDiffChecked = gitDiffCheckBox.isSelected();
        state.isGitLogChecked = gitLogCheckBox.isSelected();
      //  state.isWarpTabChecked = warpTabCheckBox.isSelected();
        state.isCopyNameNoExChecked = copyNameNoExtensionCheckBox.isSelected();
        if (windowCmdCheckBox != null && windowUsePowerShellCheckBox != null) {
            int windowCmdType;
            if (windowCmdCheckBox.isSelected()) {
                windowCmdType = windowUsePowerShellCheckBox.isSelected() ? GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD : GitOpenHereSettings.WINDOW_CMD_TYPE_CMD;
            } else {
                windowCmdType = GitOpenHereSettings.WINDOW_CMD_TYPE_NO;
            }
            state.windowCmdType = windowCmdType;
        }
        state.isCopyNameChecked = copyNameCheckBox.isSelected();

        var enable1 = gitBashCheckbox.isSelected();
        var enable2 = warpCheckbox.isSelected();
        int type;
        if (enable1 && enable2) {
            type = 2;
        } else if (enable1) {
            type = 0;
        } else if (enable2) {
            type = 1;
        } else {
            type = -1;
        }
        state.gitToolType = type;
    }

    @Override
    public void reset() {
        GitOpenHereSettings.State state = GitOpenHereSettings.getInstance().getState();
        if(exePathTextField != null) exePathTextField.setText(state.gitBashCustomPath);

        gitStatusCheckBox.setSelected(state.isGitStatusChecked);
        gitDiffCheckBox.setSelected(state.isGitDiffChecked);
        gitLogCheckBox.setSelected(state.isGitLogChecked);
      //  warpTabCheckBox.setSelected(state.isWarpTabChecked);
        copyNameCheckBox.setSelected(state.isCopyNameChecked);
        copyNameNoExtensionCheckBox.setSelected(state.isCopyNameNoExChecked);
        if(windowCmdCheckBox != null) windowCmdCheckBox.setSelected(state.windowCmdType > 0);
        if(windowUsePowerShellCheckBox != null) windowUsePowerShellCheckBox.setSelected(state.windowCmdType == GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD);

        gitBashCheckbox.setSelected(state.gitToolType == 0 || state.gitToolType == 2);
        warpCheckbox.setSelected(state.gitToolType == 1 || state.gitToolType == 2);
    }

    @Override
    public @Nls String getDisplayName() {
        return "GitBashOpenHere";
    }

    private JTextField addSectionHeader(String text, String desc) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel label2 = new JLabel(desc);
        label2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.add(label, BorderLayout.NORTH);
        panel.add(label2, BorderLayout.CENTER);

        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(850, 35));
        field.setToolTipText("Example: C:\\Program Files\\Git\\bin\\git.exe");
        panel.add(field, BorderLayout.SOUTH);

        mainPanel.add(panel);

        return field;
    }

    private void addCheckboxRow(String title, JCheckBox checkBox, Boolean hasVertPadding) {
        addCheckboxRow(title, checkBox, hasVertPadding, false);
    }

    private void addCheckboxRow(String title, JCheckBox checkBox, Boolean hasVertPadding, Boolean needLeftPadding) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        if (needLeftPadding) {
            panel.add(Box.createHorizontalStrut(16)); // 16像素的左侧间距，可根据需要调整
        }

        // 复选框对齐优化
        checkBox.setAlignmentY(Component.CENTER_ALIGNMENT); // 避免垂直居中
        panel.add(checkBox);
        panel.add(Box.createHorizontalStrut(12));

        // 标签强制左对齐
        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setAlignmentY(Component.CENTER_ALIGNMENT); // 避免垂直居中
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        // 组合控件
        panel.add(titleLabel);
        panel.add(Box.createHorizontalGlue());

        // 父容器添加时二次确认对齐
        mainPanel.add(panel);
        if(hasVertPadding) mainPanel.add(Box.createVerticalStrut(8)); // 建议添加行间距
    }

    private void sperator() {
        var sp = new JSeparator(SwingConstants.HORIZONTAL);
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
        mainPanel.add(sp);
    }
}
