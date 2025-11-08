<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Bill History</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
</head>
<body>
  <div class="container">
    <h1>Recent Bills</h1>
    <table class="menu">
      <thead><tr><th>Bill No</th><th>Date</th><th>Total</th><th>GST</th><th>Grand</th></tr></thead>
      <tbody>
        <c:forEach var="b" items="${bills}">
          <tr>
            <td>${b.bill_number}</td>
            <td>${b.created_at}</td>
            <td>${b.total}</td>
            <td>${b.gst}</td>
            <td>${b.grand}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div style="margin-top:10px;">
      <a href="${pageContext.request.contextPath}/" class="btn">New Order</a>
    </div>
  </div>
</body>
</html>
