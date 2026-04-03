package it.consciousdreams;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DynamicMavenAction extends AnAction {

    private static final Icon FALLBACK_ICON = IconLoader.getIcon("/icons/maven_install.svg", DynamicMavenAction.class);

    private final MavenActionConfig config;

    public DynamicMavenAction(MavenActionConfig config) {
        super(config.label, config.label, loadIcon(config.iconPath));
        this.config = config;
    }

    private static Icon loadIcon(String path) {
        if (path == null || path.isEmpty()) return FALLBACK_ICON;
        try {
            return IconLoader.getIcon(path, DynamicMavenAction.class);
        } catch (Exception ex) {
            return FALLBACK_ICON;
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String basePath = project.getBasePath();
        if (basePath == null) return;

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        if (!mavenProjectsManager.isMavenizedProject()) {
            Messages.showWarningDialog(project, "This is not a Maven project.", config.label);
            return;
        }

        List<String> goals = new ArrayList<>();
        Map<String, String> props = new LinkedHashMap<>();

        for (String token : config.goals.trim().split("\\s+")) {
            if (token.startsWith("-D")) {
                String kv = token.substring(2);
                int eq = kv.indexOf('=');
                if (eq >= 0) {
                    props.put(kv.substring(0, eq), kv.substring(eq + 1));
                } else {
                    props.put(kv, "true");
                }
            } else if (!token.isEmpty()) {
                goals.add(token);
            }
        }

        MavenRunnerParameters params = new MavenRunnerParameters(
                true,
                basePath,
                (String) null,
                goals,
                Collections.emptyList()
        );

        MavenRunnerSettings settings = MavenRunner.getInstance(project).getSettings().clone();
        if (!props.isEmpty()) {
            Map<String, String> merged = new LinkedHashMap<>(settings.getMavenProperties());
            merged.putAll(props);
            settings.setMavenProperties(merged);
        }

        MavenRunner.getInstance(project).run(params, settings, null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
