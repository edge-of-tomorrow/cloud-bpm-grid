<html>
<body>
  <p>You are one of the persons who can work on the task <b>'${task.name}'</b> with ID <b>${task.id}</b>.
     Decide with your colleagues below who will claim and complete the task in their
     <a href="${taskInboxUrl}"><b>Task Inbox</b></a>.</p>

  <h2>Task Details</h2>
  <ul>
  <#list taskInputs?keys as key>
  	<li>${key}: ${taskInputs[key]}</li>
  </#list>
  </ul>

  <h2>Potential Owners of the Task '${task.name}'</h2>
  <ul>
  <#list colleagues as colleague>
  	<li><b>${colleague}</b></li>
  </#list>
  </ul>

  <a target="_blank" href="${processDiagramUrl}">Show the current process diagram</a>
</body>
</html>
