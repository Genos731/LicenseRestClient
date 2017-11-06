<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Set Fee</title>
</head>
<body>
	Set Fee:
	<br>
	<form method="POST" action="setFee">
		$<input type="text" name="newValue" value=${payment.amount} /><br>
		<input type="hidden" name="action" value="setNewFee" /> <input
			type="submit" value="Set Fee" />
	</form>
</body>
</html>