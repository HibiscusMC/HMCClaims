package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.FormButton;
import com.hibiscusmc.hmcclaims.form.spec.FormComponent;
import com.hibiscusmc.hmcclaims.form.spec.FormImage;
import com.hibiscusmc.hmcclaims.form.spec.FormSpec;
import com.hibiscusmc.hmcclaims.form.spec.FormValues;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.geysermc.cumulus.util.FormImage.Type;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Translates the plugin's renderer-independent {@link FormSpec}s into Cumulus forms and
 * hands them to Floodgate.
 */
final class FloodgateBridge {

    private final SchedulerUtil scheduler;

    FloodgateBridge(@NotNull SchedulerUtil scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @param uuid The player's unique id.
     * @return {@code true} if this player is connected through Geyser.
     */
    boolean isBedrock(@NotNull UUID uuid) {
        FloodgateApi api = FloodgateApi.getInstance();

        return api != null && api.isFloodgatePlayer(uuid);
    }

    /**
     * Builds and delivers a form.
     *
     * @param uuid The Bedrock player to send it to.
     * @param spec The form to render.
     * @return {@code true} if Floodgate accepted the form.
     */
    boolean send(@NotNull UUID uuid, @NotNull FormSpec spec) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api == null) {
                return false;
            }

            FormBuilder<?, ?, ?> builder = switch (spec) {
                case SimpleFormSpec simple -> build(simple);
                case ModalFormSpec modal -> build(modal);
                case CustomFormSpec custom -> build(custom);
            };

            return api.sendForm(uuid, builder);
        } catch (Exception ex) {
            Logger.error("Failed to send a Bedrock form", ex);
            return false;
        }
    }

    @NotNull
    private SimpleForm.Builder build(@NotNull SimpleFormSpec spec) {
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(spec.title())
                .content(spec.content());

        List<FormButton> buttons = spec.buttons();
        for (FormButton button : buttons) {
            FormImage image = button.image();

            if (image == null) {
                builder.button(button.text());
            } else {
                builder.button(button.text(), image.url() ? Type.URL : Type.PATH, image.data());
            }
        }

        builder.validResultHandler(response -> {
            int clicked = response.clickedButtonId();

            if (clicked >= buttons.size()) {
                return;
            }

            sync(buttons.get(clicked).action());
        });

        Runnable onClose = spec.onClose();
        if (onClose != null) {
            builder.closedOrInvalidResultHandler(() -> sync(onClose));
        }

        return builder;
    }

    @NotNull
    private ModalForm.Builder build(@NotNull ModalFormSpec spec) {
        ModalForm.Builder builder = ModalForm.builder()
                .title(spec.title())
                .content(spec.content())
                .button1(spec.firstButton())
                .button2(spec.secondButton())
                .validResultHandler(response -> sync(response.clickedFirst() ? spec.onFirst() : spec.onSecond()));

        Runnable onClose = spec.onClose();
        if (onClose != null) {
            builder.closedOrInvalidResultHandler(() -> sync(onClose));
        }

        return builder;
    }

    @NotNull
    private CustomForm.Builder build(@NotNull CustomFormSpec spec) {
        CustomForm.Builder builder = CustomForm.builder()
                .title(spec.title());

        List<FormComponent> components = spec.components();
        for (FormComponent component : components) {
            switch (component) {
                case FormComponent.Label label -> builder.label(label.text());
                case FormComponent.Input input ->
                        builder.input(input.label(), input.placeholder(), input.defaultValue());
                case FormComponent.Toggle toggle -> builder.toggle(toggle.label(), toggle.defaultValue());
                case FormComponent.Dropdown dropdown ->
                        builder.dropdown(dropdown.label(), dropdown.options(), dropdown.defaultIndex());
                case FormComponent.Slider slider ->
                        builder.slider(slider.label(), slider.min(), slider.max(), slider.step(), slider.defaultValue());
                case FormComponent.StepSlider stepSlider ->
                        builder.stepSlider(stepSlider.label(), stepSlider.steps(), stepSlider.defaultIndex());
            }
        }

        builder.validResultHandler(response -> {
            Map<String, Object> values = new LinkedHashMap<>();

            response.includeLabels(false);
            response.reset();

            for (FormComponent component : components) {
                if (component instanceof FormComponent.Label) {
                    continue;
                }

                values.put(component.key(), response.next());
            }

            FormValues submitted = new FormValues(values);
            sync(() -> spec.onSubmit().accept(submitted));
        });

        Runnable onClose = spec.onClose();
        if (onClose != null) {
            builder.closedOrInvalidResultHandler(() -> sync(onClose));
        }

        return builder;
    }

    /**
     * Cumulus invokes result handlers off the main server thread; every callback the
     * plugin registers touches Bukkit state, so it has to hop back first.
     */
    private void sync(@NotNull Runnable runnable) {
        scheduler.schedule(runnable);
    }
}