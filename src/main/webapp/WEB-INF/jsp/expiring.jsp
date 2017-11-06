<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Expiring Licences</title>
</head>
<body>

	<form method="post" action="expiring">
		<table border="1" id="table">
			<tr>
				<th onclick="sortTable(0)">ID</th>
				<th onclick="sortTable(1)">Name</th>
				<th onclick="sortTable(2)">Address</th>
				<th onclick="sortTable(3)">Number</th>
				<th onclick="sortTable(4)">Licence Class</th>
				<th onclick="sortTable(5)">Email</th>
				<th onclick="sortTable(6)">Expiry Date</th>
				<th>Renew</th>
			</tr>
			<c:forEach var="licence" items="${licenceList}">
				<tr>
					<td><a href="licence?id=${licence.id}">${licence.id}</a></td>
					<td>${licence.name}</td>
					<td>${licence.address}</td>
					<td>${licence.number}</td>
					<td>${licence.licenceClass}</td>
					<td>${licence.email}</td>
					<td>${licence.getSortableDate()}</td>
					<td><input type="checkbox" name="renewSelection"
						value=${licence.id } checked></td>
				</tr>
			</c:forEach>
		</table>


		<input type="hidden" name="action" value="email" />
		<button name="Email Selected" type="submit">Email Selected</button>
	</form>

	<a href="expiring">Expiring</a>
	<br>
	<a href="pending">Pending</a>
	<br>
	<a href="review">Review</a>
	
<script>
function sortTable(n) {
  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
  table = document.getElementById("table");
  switching = true;
  //Set the sorting direction to ascending:
  dir = "asc"; 
  /*Make a loop that will continue until
  no switching has been done:*/
  while (switching) {
    //start by saying: no switching is done:
    switching = false;
    rows = table.getElementsByTagName("TR");
    /*Loop through all table rows (except the
    first, which contains table headers):*/
    for (i = 1; i < (rows.length - 1); i++) {
      //start by saying there should be no switching:
      shouldSwitch = false;
      /*Get the two elements you want to compare,
      one from current row and one from the next:*/
      x = rows[i].getElementsByTagName("TD")[n];
      y = rows[i + 1].getElementsByTagName("TD")[n];
      /*check if the two rows should switch place,
      based on the direction, asc or desc:*/
      if (dir == "asc") {
        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch= true;
          break;
        }
      } else if (dir == "desc") {
        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch= true;
          break;
        }
      }
    }
    if (shouldSwitch) {
      /*If a switch has been marked, make the switch
      and mark that a switch has been done:*/
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
      //Each time a switch is done, increase this count by 1:
      switchcount ++;      
    } else {
      /*If no switching has been done AND the direction is "asc",
      set the direction to "desc" and run the while loop again.*/
      if (switchcount == 0 && dir == "asc") {
        dir = "desc";
        switching = true;
      }
    }
  }
}
</script>
</body>
</html>