package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.data.QueuedCommand;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ExecuteAndConfirmCommandExecutor implements Callable<CommandExecutorResult>, Runnable {
    private final BuycraftPlugin plugin;
    private final List<QueuedCommand> commandList;

    @Override
    public CommandExecutorResult call() throws Exception {
        // Perform the actual command execution.
        Future<CommandExecutorResult> initialCheck = Bukkit.getScheduler().callSyncMethod(plugin, new CommandExecutor(
                plugin, commandList, false, false));
        CommandExecutorResult result = initialCheck.get();

        if (!result.getDone().isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (QueuedCommand command : result.getDone()) {
                ids.add(command.getId());
            }

            plugin.getApiClient().deleteCommand(ids);
        }

        return result;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to execute commands", e);
        }
    }
}