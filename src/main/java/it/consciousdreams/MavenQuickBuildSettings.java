package it.consciousdreams;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@State(name = "MavenQuickBuildSettings", storages = @Storage("MavenQuickBuild.xml"))
@Service(Service.Level.APP)
public final class MavenQuickBuildSettings implements PersistentStateComponent<MavenQuickBuildSettings.State> {

    public static class State {
        public List<MavenActionConfig> actions = new ArrayList<>();
    }

    private State state = createDefaultState();

    public static MavenQuickBuildSettings getInstance() {
        return ApplicationManager.getApplication().getService(MavenQuickBuildSettings.class);
    }

    private static State createDefaultState() {
        State s = new State();
        s.actions.add(new MavenActionConfig(
                UUID.randomUUID().toString(),
                "Maven Clean Install (skip tests)",
                "clean install -Dmaven.test.skip=true",
                "/icons/maven_install.svg"
        ));
        s.actions.add(new MavenActionConfig(
                UUID.randomUUID().toString(),
                "Maven Clean Install",
                "clean install",
                "/icons/maven_install_with_tests.svg"
        ));
        return s;
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public List<MavenActionConfig> getActions() {
        return state.actions;
    }

    public void setActions(List<MavenActionConfig> actions) {
        state.actions = new ArrayList<>(actions);
    }
}
