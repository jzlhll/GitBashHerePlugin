package com.allan.openhereplugin.config;

import com.allan.openhereplugin.Common;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;

public class GitBashOpenHereConfigurable implements Configurable {
    private JPanel mainPanel;
    private JTextField exePathTextField;
    private JCheckBox gitStatusCheckBox;
    private JCheckBox gitDiffCheckBox;
    private JCheckBox gitLogCheckBox;
    private JCheckBox copyNameCheckBox;
    private JCheckBox copyNameNoExtensionCheckBox;
    private JCheckBox customGitToolCheckBox; // 新增复选框

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //第一行：
        addSectionHeader("Your current git tool exe path:");
        var ans = Common.findGitBashPath();
        if ("ok".equals(ans)) {
            addDesc(Common.gitBashPath);
            mainPanel.add(Box.createVerticalStrut(30));
        }

        // 第一行：Git工具路径
        // 修改后（替换为带复选框的行）：
        customGitToolCheckBox = new JCheckBox();
        addCheckboxRow("Use custom git tool exe path:", customGitToolCheckBox, false);
        exePathTextField = createPathTextField();
        mainPanel.add(exePathTextField);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(Box.createVerticalStrut(10));

        // 第二行：git status 选项
        addCheckboxRow("Hide options: git status",
                gitStatusCheckBox = new JCheckBox(), true);

        // 第三行：git diff 选项
        addCheckboxRow("Hide options: git diff",
                gitDiffCheckBox = new JCheckBox(), true);

        // 第三行：git diff 选项
        addCheckboxRow("Hide options: git log",
                gitLogCheckBox = new JCheckBox(), true);

        addCheckboxRow("Hide options: copy name",
                copyNameCheckBox = new JCheckBox(), true);

        addCheckboxRow("Hide options: copy name no extension",
                copyNameNoExtensionCheckBox = new JCheckBox(), true);
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        GitBashOpenHereSettings.State state = GitBashOpenHereSettings.getInstance().getState();
        return !exePathTextField.getText().equals(state.gitToolExePath) ||
                gitStatusCheckBox.isSelected() != state.isGitStatusChecked ||
                gitDiffCheckBox.isSelected() != state.isGitDiffChecked ||
                gitLogCheckBox.isSelected() != state.isGitLogChecked ||
                copyNameNoExtensionCheckBox.isSelected() != state.isCopyNameNoExChecked ||
                copyNameCheckBox.isSelected() != state.isCopyNameChecked ||
                customGitToolCheckBox.isSelected() != state.isCustomGitToolChecked;
    }

    @Override
    public void apply() {
        GitBashOpenHereSettings.State state = GitBashOpenHereSettings.getInstance().getState();
        state.gitToolExePath = exePathTextField.getText().trim();
        state.isGitStatusChecked = gitStatusCheckBox.isSelected();
        state.isGitDiffChecked = gitDiffCheckBox.isSelected();
        state.isGitLogChecked = gitLogCheckBox.isSelected();
        state.isCustomGitToolChecked = customGitToolCheckBox.isSelected();
        state.isCopyNameNoExChecked = copyNameNoExtensionCheckBox.isSelected();
        state.isCopyNameChecked = copyNameCheckBox.isSelected();
    }

    @Override
    public void reset() {
        GitBashOpenHereSettings.State state = GitBashOpenHereSettings.getInstance().getState();
        exePathTextField.setText(state.gitToolExePath);
        gitStatusCheckBox.setSelected(state.isGitStatusChecked);
        gitDiffCheckBox.setSelected(state.isGitDiffChecked);
        gitLogCheckBox.setSelected(state.isGitLogChecked);
        customGitToolCheckBox.setSelected(state.isCustomGitToolChecked);
        copyNameCheckBox.setSelected(state.isCopyNameChecked);
        copyNameNoExtensionCheckBox.setSelected(state.isCopyNameNoExChecked);
    }

    @Override
    public @Nls String getDisplayName() {
        return "GitBashOpenHere";
    }

    private void addSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        mainPanel.add(label);
    }

    private void addDesc(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(14f));
        mainPanel.add(label);
    }

    private void addCheckboxRow(String title, JCheckBox checkBox, Boolean hasVertPadding) {
        // 主容器强制左对齐（关键修复）
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    private JTextField createPathTextField() {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(850, 35));
        field.setToolTipText("Example: C:\\Program Files\\Git\\bin\\git.exe");
        return field;
    }

}
