package org.example.java_cli_app.services;

import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleSheetIssueService implements IssueServiceFacade {

    @Override
    public void createIssue(String description, String parentId) {
        System.out.printf("Created issue: '%s', parent issue ID: %s%n", description, parentId != null ? parentId : "none");
    }

    @Override
    public void updateIssue(String issueId, Status status) {
        System.out.printf("Updating issue %s to status: %s%n", issueId, status);
    }

    @Override
    public void listIssues(Status status) {
        System.out.printf("List of issues with status: %s%n", status);
    }

}
