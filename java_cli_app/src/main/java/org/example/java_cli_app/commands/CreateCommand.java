package org.example.java_cli_app.commands;

import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "create", description = "Creates a new issue at Google Spreadsheet")
public class CreateCommand implements Runnable {

    private final IssueServiceFacade issueServiceFacade;

    public CreateCommand(IssueServiceFacade issueServiceFacade) {
        this.issueServiceFacade = issueServiceFacade;
    }

    @CommandLine.Parameters(index = "0", description = "Issue description")
    private String description;

    @CommandLine.Parameters(index = "1", arity = "0..1", description = "Parent issue ID (Optional)")
    private String parentId;

    @Override
    public void run() {
        issueServiceFacade.createIssue(description, parentId);
    }
}
