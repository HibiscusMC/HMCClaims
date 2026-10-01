package com.hibiscusmc.hmcclaims.config.gui;

import lombok.Getter;
import team.hypox.config.core.annotation.Config;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimListConfig extends BaseListGuiConfig {

    private String title = "Your claims";

    private int rows = 5;
}