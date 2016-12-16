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
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Optional task input variables:
 *  - mail_cc = userId, email2@domain.com, email3@domain.com, userId4
 */
public class NotifyOwnersByEmailTaskListener extends DefaultTaskEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotifyOwnersByEmailTaskListener.class);

    private static final String MAIL_HOST = "localhost";
    private static final String MAIL_PORT = "25";
    private static final String MAIL_FROM = System.getProperty("org.kie.mail.from", "noreply@bpm.grid");
    private static final String MAIL_SUBJECT = "[TASK] \'%s\' is awaiting your action";
    private static final String MAIL_OWNER_TEMPLATE = "EmailNewTaskToOwner.ftlh";
    private static final String MAIL_POTOWNER_TEMPLATE = "EmailNewTaskToPotentialOwners.ftlh";
    private static final String TASK_INBOX_URL = System.getProperty("org.kie.task.inbox.url", "http://localhost:8080/business-central");
    private static final String MAIL_CC = "mail_cc";
    private static final String MAIL_CC_GROUP = System.getProperty("org.kie.mail.cc.tasks.group", "CCTasks");
    
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
                LOG.error("Template " + MAIL_OWNER_TEMPLATE + " wasn't loaded.", ex);
            }
        } else {
            recipients = getPotentialOwners(task, userInfo);
            if (recipients.isEmpty()) {
                return;
            }
            List<String> colleagues = new ArrayList<>();
            for (User user : recipients) {
                String name = userInfo.getDisplayName(user);
                if (name == null || name.isEmpty()) {
                    LOG.warn("User " + user.getId() + " doesn't have a name set in UserInfo (KeyCloak).");
                    colleagues.add(user.getId());
                } else {
                    colleagues.add(name);
                }
            }
            try {
                temp = cfg.getTemplate(MAIL_POTOWNER_TEMPLATE);
                dataModel.put("colleagues", colleagues);
            } catch (Exception ex) {
                LOG.error("Template " + MAIL_POTOWNER_TEMPLATE + " wasn't loaded.", ex);
                return;
            }
        }
        
        try {
            StringWriter sw = new StringWriter();
            temp.process(dataModel, sw);
            body = sw.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        List<String> ccs = getCCRecipients(task, userInfo);

        Email email = buildEmail(subject, body, recipients, ccs, userInfo);
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
    
    private List<String> getCCRecipients(Task task, UserInfo userInfo) {
        String mailCC = (String) task.getTaskData().getTaskInputVariables().get(MAIL_CC);
        List<String> ccs = new ArrayList<>();
        if (mailCC != null && !mailCC.isEmpty()) {
            String[] ccsarray = mailCC.split(",");
            for (String ca : ccsarray) {
                ccs.add(ca);
            }
        }
        Iterator<OrganizationalEntity> ccMembers = userInfo.getMembersForGroup(new GroupImpl(MAIL_CC_GROUP));
        while (ccMembers.hasNext()) {
            OrganizationalEntity ccUser = ccMembers.next();
            if (ccUser instanceof User) {
                ccs.add(ccUser.getId());
            }
        }
        return ccs;
    }
    
    private Email buildEmail(String subject, String body, List<User> recipients, List<String> ccs, UserInfo userInfo) {
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
            } else {
                LOG.warn("User " + user.getId() + " doesn't have an email set in UserInfo (KeyCloak).");
            }
        }
        
        if (recipients.isEmpty()) {
            LOG.warn("Couldn't send new task notification email due to no recipients.");
            return null;
        }
        
        for (String cc : ccs) {
            Recipient recipient = new Recipient();
            recipient.setType( "Cc" );
            String e = null;
            if (cc.contains("@")) {
                e = cc;
            } else {
                e = userInfo.getEmailForEntity(new UserImpl(cc));
            }
            if (e != null) {
                recipient.setEmail(e);
                recs.addRecipient(recipient);
            }
        }
        
        message.setRecipients(recs);
        message.setSubject(subject);
        message.setBody(body);
        
        email.setMessage(message);
        email.setConnection(connection);
        return email;
    }

}
