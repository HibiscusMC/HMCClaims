package com.hibiscusmc.hmcclaims.config.form;

import java.util.List;
import java.util.Map;

public abstract class ClaimManageFormConfig extends FormTemplate {

    public abstract Title title();

    public abstract List<String> content();

    public abstract Button renameButton();

    public abstract Button lockButton();

    public abstract Button unlockButton();

    public abstract Button bannedButton();

    public abstract Button resizeButton();

    public abstract Button deleteButton();

    public abstract Navigation nav();

    public abstract Button backButton();

    public abstract Map<String, ActionButton> extraButtons();

    public abstract List<String> order();
}