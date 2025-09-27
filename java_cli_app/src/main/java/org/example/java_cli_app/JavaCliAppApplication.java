package org.example.java_cli_app;

import org.example.java_cli_app.commands.IssueRootCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class JavaCliAppApplication implements CommandLineRunner, ExitCodeGenerator {

    private CommandLine.IFactory factory;
    private IssueRootCommand issueRootCommand;
    private int exitCode;

    public JavaCliAppApplication(CommandLine.IFactory factory, IssueRootCommand issueRootCommand) {
        this.factory = factory;
        this.issueRootCommand = issueRootCommand;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(JavaCliAppApplication.class, args)));
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(issueRootCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
