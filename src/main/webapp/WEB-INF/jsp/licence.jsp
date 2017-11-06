<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Licence Details</title>
</head>
<body>
	Licence Details

	<table border="1">
		<tr>
			<th>ID</th>
			<th>Name</th>
			<th>Address</th>
			<th>Number</th>
			<th>Licence Class</th>
			<th>Email</th>
			<th>Expiry Date</th>
		</tr>
		<tr>
			<td>${licence.id}</td>
			<td>${licence.name}</td>
			<td>${licence.address}</td>
			<td>${licence.number}</td>
			<td>${licence.licenceClass}</td>
			<td>${licence.email}</td>
			<td>${licence.getDate()}</td>
		</tr>
	</table>
</body>
</html>