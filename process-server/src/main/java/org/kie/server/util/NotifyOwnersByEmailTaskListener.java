package org.kie.server.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbpm.process.workitem.email.Connection;
import org.jbpm.process.workitem.email.Email;
import org.jbpm.process.workitem.email.Message;
import org.jbpm.process.workitem.email.Recipient;
import org.jbpm.process.workitem.email.Recipients;
import org.jbpm.process.workitem.email.SendHtml;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.UserInfo;

public class NotifyOwnersByEmailTaskListener extends DefaultTaskEventListener {

    private static final String MAIL_HOST = "localhost";
    private static final String MAIL_PORT = "25";
    private static final String MAIL_FROM = "noreply@bpm.grid";
    private static final String MAIL_SUBJECT = "[TASK] \'%s\' is awaiting your action";
    private static final String MAIL_BODY_OWNER = "You have been assigned to the task \'%s\' with ID %d. You can access it in your <a href=\"%s\">Task Inbox</a>.";
    private static final String MAIL_BODY_POTOWNER = "You are one of the persons who can work on the task \'%s\' with ID %d. Decide with your colleagues %s who will claim and complete the task in their <a href=\"%s\">Task Inbox</a>.";
    private static final String TASK_INBOX_URL = System.getProperty("org.kie.task.inbox.url", "http://localhost:8080/business-central");
    
    private Connection connection = null;
    
    public NotifyOwnersByEmailTaskListener() {
        
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        Task task = event.getTask();
        String subject = String.format(MAIL_SUBJECT, task.getName());
        UserInfo userInfo = (UserInfo) ((TaskContext)event.getTaskContext()).get(EnvironmentName.TASK_USER_INFO);
        
        User taskOwner = task.getTaskData().getActualOwner();
        List<User> recipients = null;
        String body = null;
        if (taskOwner != null) {
            recipients = new ArrayList<>();
            recipients.add(taskOwner);
            body = String.format(MAIL_BODY_OWNER, task.getName(), task.getId(), TASK_INBOX_URL);
        } else {
            recipients = getPotentialOwners(task, userInfo);
            if (recipients.isEmpty()) {
                return;
            }
            String names = "";
            for (User user : recipients) {
                names += userInfo.getDisplayName(user) + ", ";
            }
            names = names.substring(0, names.length() - 2);
            body = String.format(MAIL_BODY_POTOWNER, task.getName(), task.getId(), names, TASK_INBOX_URL);
        }
        
        Email email = buildEmail(subject, body, recipients, userInfo);
        if (email != null) {
            SendHtml.sendHtml(email);
        }
    }
    
    private List<User> getPotentialOwners(Task task, UserInfo userInfo) {
        List<User> recipients = new ArrayList<>();
        
        List<OrganizationalEntity> potOwners = task.getPeopleAssignments().getPotentialOwners();
        for (OrganizationalEntity oe : potOwners) {
            if (oe instanceof User) {
                recipients.add((User) oe);
            } else if (oe instanceof Group) {
                Iterator<OrganizationalEntity> mems = userInfo.getMembersForGroup((Group) oe);
                while (mems.hasNext()) {
                    recipients.add((User) mems.next());
                }
            }
        }
        
        return recipients;
    }
    
    private Email buildEmail(String subject, String body, List<User> recipients, UserInfo userInfo) {
        if (connection == null) {
            connection = new Connection();
            connection.setHost(MAIL_HOST);
            connection.setPort(MAIL_PORT);
        }
        
        Email email = new Email();
        Message message = new Message();
        message.setFrom(MAIL_FROM);
        Recipients recs = new Recipients();
        
        for (User user : recipients) {
            String e = userInfo.getEmailForEntity(user);
            if (e != null) {
                Recipient recipient = new Recipient();
                recipient.setEmail(e);
                recipient.setType( "To" );
                recs.addRecipient(recipient);
            }
        }
        
        if (recipients.isEmpty()) {
            return null;
        }
        
        message.setRecipients(recs);
        message.setSubject(subject);
        message.setBody(body);
        
        email.setMessage(message);
        email.setConnection(connection);
        return email;
    }

}
