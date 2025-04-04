package com.allan.openhereplugin.config;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.bean.IWindowRuns;
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
        if (gitBashRuns instanceof IWindowRuns) {
            var runner = (IWindowRuns) gitBashRuns;
            var ans = runner.findPathExe();
            if ("ok".equals(ans.first)) {
                exePathTextField = addSectionHeader("Your git-bash.exe path is:  " + runner.origPathExe(), "You can change it here:");
            } else {
                exePathTextField = addSectionHeader("Can find your git-bash.exe path.", "Custom git-bash.exe path here:");
            }
            mainPanel.add(Box.createVerticalStrut(12));
        }

        addCheckboxRow("Hide options: git status",
                gitStatusCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: git diff",
                gitDiffCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: git log",
                gitLogCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: copy name",
                copyNameCheckBox = new JCheckBox(), true);
        addCheckboxRow("Hide options: copy name no extension",
                copyNameNoExtensionCheckBox = new JCheckBox(), true);

        sperator();
        //warp
        addCheckboxRow("Enable ‘Warp’", warpCheckbox = new JCheckBox(), false);
//        addCheckboxRow("Hide options: warp tab",
//                warpTabCheckBox = new JCheckBox(), true);

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
        return !editSame ||
                gitStatusCheckBox.isSelected() != state.isGitStatusChecked ||
                gitDiffCheckBox.isSelected() != state.isGitDiffChecked ||
                gitLogCheckBox.isSelected() != state.isGitLogChecked ||
               // warpTabCheckBox.isSelected() != state.isWarpTabChecked ||
                copyNameNoExtensionCheckBox.isSelected() != state.isCopyNameNoExChecked ||
                copyNameCheckBox.isSelected() != state.isCopyNameChecked ||
                type != state.gitToolType;
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
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

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
