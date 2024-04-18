package com.allan.openhereplugin;

public class GitBashCopyNameNoExtensionAction extends GitBashCopyNameAction {
    @Override
    public String changeName(String name) {
        if (name != null && name.contains(".") && name.length() >= 2) {
            return name.substring(0, name.lastIndexOf("."));
        }
        return name;
    }
}