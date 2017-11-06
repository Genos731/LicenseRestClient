<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Request Extension</title>
</head>
<body>
<h1>Request Extension?</h1>

<form method="POST" action ="extensionrequest">
<input type="hidden" name="action" value="requestExtension" />
<input type="submit" value="Yes" />
</form>

<form method="POST" action ="extensionrequest">
<input type="hidden" name="action" value="pay" />
<input type="submit" value="No" />
</form>
</body>
</body>
</html>