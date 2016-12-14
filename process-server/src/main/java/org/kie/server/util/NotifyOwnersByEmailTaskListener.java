package org.kie.server.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import freemarker.template.Configuration;
import freemarker.template.Template;

public class NotifyOwnersByEmailTaskListener extends DefaultTaskEventListener {

    private static final String MAIL_HOST = "localhost";
    private static final String MAIL_PORT = "25";
    private static final String MAIL_FROM = System.getProperty("org.kie.mail.from", "noreply@bpm.grid");
    private static final String MAIL_SUBJECT = "[TASK] \'%s\' is awaiting your action";
    private static final String MAIL_OWNER_TEMPLATE = "EmailNewTaskToOwner.ftlh";
    private static final String MAIL_POTOWNER_TEMPLATE = "EmailNewTaskToPotentialOwners.ftlh";
    private static final String TASK_INBOX_URL = System.getProperty("org.kie.task.inbox.url", "http://localhost:8080/business-central");
    
    private Connection connection = null;
    
    public NotifyOwnersByEmailTaskListener() {
        
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        Task task = event.getTask();
        event.getTaskContext().loadTaskVariables(task);
        String subject = String.format(MAIL_SUBJECT, task.getName());
        UserInfo userInfo = (UserInfo) ((TaskContext)event.getTaskContext()).get(EnvironmentName.TASK_USER_INFO);
        Configuration cfg = TemplateConfiguration.getInstance();
        
        User taskOwner = task.getTaskData().getActualOwner();
        List<User> recipients = null;
        String body = null;
        Template temp = null;
        Map<String, Object> dataModel = new HashMap<>(); // data-model for the template system
        dataModel.put("task", task);
        dataModel.put("taskInboxUrl", TASK_INBOX_URL);
        dataModel.put("taskInputs", task.getTaskData().getTaskInputVariables());
        if (taskOwner != null) {
            recipients = new ArrayList<>();
            recipients.add(taskOwner);
            try {
                temp = cfg.getTemplate(MAIL_OWNER_TEMPLATE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            recipients = getPotentialOwners(task, userInfo);
            if (recipients.isEmpty()) {
                return;
            }
            List<String> colleagues = new ArrayList<>();
            for (User user : recipients) {
                colleagues.add(userInfo.getDisplayName(user));
            }
            try {
                temp = cfg.getTemplate(MAIL_POTOWNER_TEMPLATE);
                dataModel.put("colleagues", colleagues);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (temp == null) {
            return;
        }
        
        try {
            StringWriter sw = new StringWriter();
            temp.process(dataModel, sw);
            body = sw.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
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
