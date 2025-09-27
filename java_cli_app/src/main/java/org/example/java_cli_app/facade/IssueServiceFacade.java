package org.example.java_cli_app.facade;

import org.example.java_cli_app.enums.Status;

public interface IssueServiceFacade {

    void createIssue(String description, String parentId);
    void updateIssue(String issueId, Status status);
    void listIssues(Status status);

}
