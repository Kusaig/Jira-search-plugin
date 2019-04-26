package com.example.plugins.tutorial;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.bc.issue.search.*;
import com.atlassian.jira.issue.search.*;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.bean.PagerFilter;


import java.util.HashMap;
import java.util.Map;


public class DueDateIndicator extends AbstractJiraContextProvider {

    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        String customFieldId = "customfield_10000";
        Map<String, Object> contextMap = new HashMap<>();
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        /* Timestamp dueDate = currentIssue.getDueDate();
        if (dueDate != null) {
            LocalDate currentTimeInDays = LocalDate.now();
            LocalDate dueDateTimeInDays = dueDate.toLocalDateTime().toLocalDate();
            long daysAwayFromDueDateCalc = DAYS.between(currentTimeInDays, dueDateTimeInDays);
            contextMap.put("daysAwayFromDueDate", daysAwayFromDueDateCalc);
        } */
        
        // My code
        CustomFieldManager customFieldManager = com.atlassian.jira.component.ComponentAccessor.getCustomFieldManager();
        CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
        Object obj = currentIssue.getCustomFieldValue(customField);
        
        if(obj!=null){
            contextMap.put("myCustomFieldValue", obj.toString());
        }

            String jqlSearch = String.format("cf[10000]~ %s order by created desc", obj.toString());
            SearchService searchService = com.atlassian.jira.component.ComponentAccessor.getComponent(SearchService.class);
            UserUtil userUtil = com.atlassian.jira.component.ComponentAccessor.getUserUtil();
            ApplicationUser user = com.atlassian.jira.component.ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            IssueManager issueManager = com.atlassian.jira.component.ComponentAccessor.getIssueManager();
            
            /* if (!user) {
                user = userUtil.getUserObject('jira_bot');
            } */
            
            java.util.List<Issue> issues = null;
            String baseurl = com.atlassian.jira.component.ComponentAccessor.getApplicationProperties().getString("jira.baseurl") + "/browse/";


            SearchService.ParseResult parseResult =  searchService.parseQuery(user, jqlSearch);
            if (parseResult.isValid()) {
                try {
                    com.atlassian.jira.issue.search.SearchResults searchResult = searchService.search(user, parseResult.getQuery(), new PagerFilter(10));
                    issues = searchResult.getIssues();

                    StringBuilder str = new StringBuilder();
                    for (Issue issue : issues){
                        // issue.getStatus().toString();
                        str.append(String.format("<a href='%s'>%s</a></ br>", baseurl + issue.getKey(), issue.getSummary()));
                    }

                    contextMap.put("totalSearchCount", String.format("Total issues: %s", issues.size()));
                } catch (SearchException e) {
                    contextMap.put("totalSearchCount", String.format("Error: %s", e.getMessage()));
                }
            } else {
                contextMap.put("totalSearchCount", "Invalid Search: " + jqlSearch);
            }

        return contextMap;
        }
}