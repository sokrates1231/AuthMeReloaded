package fr.xephi.authme.command.executable.authme.debug;

import com.google.common.collect.ImmutableSet;
import fr.xephi.authme.command.ExecutableCommand;
import fr.xephi.authme.initialization.factory.Factory;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Debug command main.
 */
public class DebugCommand implements ExecutableCommand {

    @Inject
    private Factory<DebugSection> debugSectionFactory;

    private Set<Class<? extends DebugSection>> sectionClasses =
        ImmutableSet.of(PermissionGroups.class, TestEmailSender.class, OldTestEmailSender.class);

    private Map<String, DebugSection> sections;

    @Override
    public void executeCommand(CommandSender sender, List<String> arguments) {
        if (arguments.isEmpty()) {
            sender.sendMessage("Available sections:");
            getSections().values()
                .forEach(e -> sender.sendMessage("- " + e.getName() + ": " + e.getDescription()));
        } else {
            DebugSection debugSection = getSections().get(arguments.get(0).toLowerCase());
            if (debugSection == null) {
                sender.sendMessage("Unknown subcommand");
            } else {
                debugSection.execute(sender, arguments.subList(1, arguments.size()));
            }
        }
    }

    // Lazy getter
    private Map<String, DebugSection> getSections() {
        if (sections == null) {
            sections = sectionClasses.stream()
                .map(debugSectionFactory::newInstance)
                .collect(Collectors.toMap(DebugSection::getName, Function.identity()));
        }
        return sections;
    }
}
