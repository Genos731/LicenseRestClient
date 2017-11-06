<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Validation</title>
</head>
<body>
	Current licence details:
	<br> Licence Details

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
			<td>${currentLicence.id}</td>
			<td>${currentLicence.name}</td>
			<td>${currentLicence.address}</td>
			<td>${currentLicence.number}</td>
			<td>${currentLicence.licenceClass}</td>
			<td>${currentLicence.email}</td>
			<td>${currentLicence.getDate()}</td>
		</tr>
	</table>
	<c:choose>
		<c:when test="${numFail != 0}">
		${errorMessage}
		You have failed validation: ${numFail} times<br>
		</c:when>
		<c:when test = "${numFail == 0}">
		You haven't failed yet<br>
		</c:when>
	</c:choose>
	Update your details:
	<br>
	<form method="POST" action="validate?renewalID=${renewalID}">
		Pre Street:<input type="text" name="preStreet" /><br> Street
		Name:<input type="text" name="streetName" /><br>
		Street Type:<input type="text" name="streetType" /><br>
		Suburb:<input type="text" name="suburb" /><br>
		State:<input type="text" name="state" /><br>
		Email:<input type="text" size="20" maxlength="45" name="newEmail" value="${oldEmail}" /><br>
		<input type="hidden" name="action" value="validate" />
		<input type="submit" value="Confirm" />
	</form>
</body>
</html>