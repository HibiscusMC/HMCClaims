package com.hibiscusmc.hmcclaims.config.gui;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimListConfig extends BaseListGuiConfig {

    private String title = "Your claims";

    private int rows = 5;
}