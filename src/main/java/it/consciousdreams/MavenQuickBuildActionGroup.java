package it.consciousdreams;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MavenQuickBuildActionGroup extends ActionGroup {

    public MavenQuickBuildActionGroup() {
        super();
        setPopup(false);
    }

    @Override
    public @NotNull AnAction[] getChildren(@Nullable AnActionEvent e) {
        ActionManager am = ActionManager.getInstance();
        return MavenQuickBuildSettings.getInstance().getActions().stream()
                .map(config -> {
                    String id = MavenActionsRegistrar.PREFIX + config.id;
                    AnAction action = am.getAction(id);
                    if (action == null) {
                        // Fallback: register on first access if startup sync hasn't run yet
                        action = new DynamicMavenAction(config);
                        am.registerAction(id, action);
                    }
                    return action;
                })
                .toArray(AnAction[]::new);
    }
}
