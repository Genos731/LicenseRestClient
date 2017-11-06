<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Licences Needing Review</title>
</head>
<body>
	<br>Unowned
	<br>
	<table border="1" id="unownedTable">
		<tr>
			<th onclick="sortUnownedTable(0)">ID</th>
			<th onclick="sortUnownedTable(1)">Licence ID</th>
			<th onclick="sortUnownedTable(2)">Address</th>
			<th onclick="sortUnownedTable(3)">Email</th>
			<th onclick="sortUnownedTable(4)">Status</th>
			<th onclick="sortUnownedTable(5)">Owned By</th>
			<th onclick="sortUnownedTable(6)">Payment Amount</th>
		</tr>
		<c:forEach var="notice" items="${unownedNotices}">
			<tr>
				<td>${notice.id}</td>
				<td><a href="licence?id=${notice.licenceId}">${notice.licenceId}</a></td>
				<td>${notice.address}</td>
				<td>${notice.email}</td>
				<td>${notice.status}</td>
				<td>${notice.ownedBy}</td>
				<td>${notice.getPaymentValue()}</td>
				<td>
					<form method="POST" action="review">
					<input type="hidden" name="action" value="own" />
					<input type="hidden" name="renewalID" value=${notice.id} />
					<input type="submit" value="Own" />
					</form>
				</td>
			</tr>
		</c:forEach>
	</table>

	<br>Owned
	<br>
	<table border="1" id="ownedTable">
		<tr>
			<th onclick="sortOwnedTable(0)">ID</th>
			<th onclick="sortOwnedTable(1)">Licence ID</th>
			<th onclick="sortOwnedTable(2)">Address</th>
			<th onclick="sortOwnedTable(3)">Email</th>
			<th onclick="sortOwnedTable(4)">Status</th>
			<th onclick="sortOwnedTable(5)">Owned By</th>
			<th onclick="sortOwnedTable(6)">Payment Amount</th>
		</tr>
		<c:forEach var="notice" items="${ownedNotices}">
			<tr>
				<td>${notice.id}</td>
				<td><a href="licence?id=${notice.licenceId}">${notice.licenceId}</a></td>
				<td>${notice.address}</td>
				<td>${notice.email}</td>
				<td>${notice.status}</td>
				<td>${notice.ownedBy}</td>
				<td>${notice.getPaymentValue()}</td>
				<td>
					<form method="POST" action="review">
					<input type="hidden" name="action" value="accept" />
					<input type="hidden" name="renewalID" value=${notice.id} />
					<input type="submit" value="Accept" />
					</form>
				</td>
				<td>
					<form method="POST" action="review">
					<input type="hidden" name="action" value="deny" />
					<input type="hidden" name="renewalID" value=${notice.id} />
					<input type="submit" value="Deny" />
					</form>
				</td>
				<td>
					<form method="POST" action="review">
					<input type="hidden" name="action" value="disown" />
					<input type="hidden" name="renewalID" value=${notice.id} />
					<input type="submit" value="Disown" />
					</form>
				</td>
			</tr>
		</c:forEach>
	</table>

	<a href="expiring">Expiring</a>
	<br>
	<a href="pending">Pending</a>
	<br>
	<a href="review">Review</a>

<script>
function sortUnownedTable(n) {
  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
  table = document.getElementById("unownedTable");
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

<script>
function sortOwnedTable(n) {
  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
  table = document.getElementById("ownedTable");
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