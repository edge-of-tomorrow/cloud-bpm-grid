<html>
<body>
  <p>You have been assigned to the new task <b>'${task.name}'</b> with ID <b>${task.id}</b>.</p>

  <p>You can access it in your <a href="${taskInboxUrl}"><b>Task Inbox</b></a>.</p>

  <h2>Task Details</h2>
  <ul>
  <#list taskInputs?keys as key>
  	<li>${key}: ${taskInputs[key]}</li>
  </#list>
  </ul>
</body>
</html>