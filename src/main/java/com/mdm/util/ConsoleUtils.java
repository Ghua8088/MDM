package com.mdm.util;

import picocli.CommandLine.Help.Ansi;

public class ConsoleUtils {

    public static void printHeader(String title) {
        System.out.println(Ansi.AUTO.string("@|bold,cyan === " + title.toUpperCase() + " ===|@"));
    }

    public static void info(String message) {
        System.out.println(Ansi.AUTO.string("@|blue [INFO]|@ " + message));
    }

    public static void success(String message) {
        System.out.println(Ansi.AUTO.string("@|green [SUCCESS]|@ " + message));
    }

    public static void warn(String message) {
        System.out.println(Ansi.AUTO.string("@|yellow [WARN]|@ " + message));
    }

    public static void error(String message) {
        System.err.println(Ansi.AUTO.string("@|red [ERROR]|@ " + message));
    }
    
    // Simple table row formatter
    public static void printRow(String f1, String f2, String f3, String f4) {
        // Use standard formatting
        System.out.printf("%-30s %-30s %-20s %-10s%n", f1, f2, f3, f4);
    }
    
    public static void printDivider() {
        System.out.println("------------------------------------------------------------------------------------------------");
    }
}
