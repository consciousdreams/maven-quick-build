package it.consciousdreams;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MavenQuickBuildActionGroup extends ActionGroup {

    private AnAction[] cachedActions = AnAction.EMPTY_ARRAY;
    private List<MavenActionConfig> lastConfigs = Collections.emptyList();

    public MavenQuickBuildActionGroup() {
        super();
        setPopup(false);
    }

    @Override
    public @NotNull AnAction[] getChildren(@Nullable AnActionEvent e) {
        MavenQuickBuildSettings settings = MavenQuickBuildSettings.getInstance();
        if (settings == null) return AnAction.EMPTY_ARRAY;

        List<MavenActionConfig> configs = settings.getActions();
        if (!configs.equals(lastConfigs)) {
            lastConfigs = new ArrayList<>(configs);
            cachedActions = configs.stream()
                    .map(DynamicMavenAction::new)
                    .toArray(AnAction[]::new);
        }
        return cachedActions;
    }
}
