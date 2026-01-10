package com.mdm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "mdm", mixinStandardHelpOptions = true, version = "mdm 1.0",
        description = "Maven Dependency Manager CLI - Manage your dependencies with ease.",
        subcommands = { 
            com.mdm.commands.AddCommand.class,
            com.mdm.commands.UndoCommand.class,
            com.mdm.commands.SearchCommand.class,
            com.mdm.commands.RemoveCommand.class,
            com.mdm.commands.ListCommand.class,
            com.mdm.commands.AuditCommand.class,
            com.mdm.commands.InitCommand.class,
            com.mdm.commands.BuildCommand.class,
            com.mdm.commands.RunCommand.class
        })
public class Main implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Default behavior if no subcommand is specified
        System.out.println(picocli.CommandLine.Help.Ansi.AUTO.string("@|bold,cyan " +
            "  __  __ _____  __  __ \n" +
            " |  \\/  |  __ \\|  \\/  |\n" +
            " | \\  / | |  | | \\  / |\n" +
            " | |\\/| | |  | | |\\/| |\n" +
            " | |  | | |__| | |  | |\n" +
            " |_|  |_|_____/|_|  |_|  v1.0 (LTS)|@"));
        System.out.println("\nWelcome to MDM-CLI - The Modern Dependency Manager.");
        System.out.println("Use --help to see available commands.");
        return 0;
    }
}
