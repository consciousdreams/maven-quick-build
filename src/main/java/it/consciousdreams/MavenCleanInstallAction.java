package it.consciousdreams;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MavenCleanInstallAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        if (!mavenProjectsManager.isMavenizedProject()) {
            Messages.showWarningDialog(
                    project,
                    "This is not a Maven project.",
                    "Maven Clean Install Skipping Tests"
            );
            return;
        }

        // Disambiguate constructor: (boolean, String, String pomFile, List<String>, Collection<String>)
        MavenRunnerParameters params = new MavenRunnerParameters(
                true,
                basePath,
                (String) null,
                Arrays.asList("clean", "install"),
                Collections.emptyList()
        );

        // Clone current runner settings and add -Dmaven.test.skip=true
        MavenRunnerSettings settings = MavenRunner.getInstance(project).getSettings().clone();
        Map<String, String> props = new LinkedHashMap<>(settings.getMavenProperties());
        props.put("maven.test.skip", "true");
        settings.setMavenProperties(props);

        MavenRunner.getInstance(project).run(params, settings, null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
