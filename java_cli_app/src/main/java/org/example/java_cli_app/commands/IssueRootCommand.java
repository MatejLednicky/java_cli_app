package org.example.java_cli_app.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(
        name = "issuep",
        description = "Issue tracker CLI",
        subcommands = {
                CreateCommand.class,
                UpdateCommand.class,
                ListCommand.class
        },
        mixinStandardHelpOptions = true
)
public class IssueRootCommand implements Runnable{
    @Override
    public void run() {
        System.out.println("Use commands: create, update or list");
    }
}
