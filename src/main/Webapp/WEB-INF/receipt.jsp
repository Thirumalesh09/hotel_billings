<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*,com.militaryhotel.BillingServlet" %>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Bill Receipt</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
</head>
<body>
  <div class="container receipt">
    <h2>Military Hotel</h2>
    <p><strong>Bill Number:</strong> ${billNumber}</p>
    <p><strong>Date:</strong> ${pageContext.request.time}</p>
    <table class="menu">
      <thead><tr><th>Item</th><th>Qty</th><th>Unit</th><th>Amount</th></tr></thead>
      <tbody>
        <c:forEach var="line" items="${lines}">
          <tr>
            <td>${line.itemName}</td>
            <td>${line.qty}</td>
            <td>${line.unitPrice}</td>
            <td>${line.amount}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div class="totals">
      <p>Total: <strong>${total}</strong></p>
      <p>GST (18%): <strong>${gst}</strong></p>
      <p>Grand Total: <strong>${grand}</strong></p>
    </div>
    <div style="margin-top:10px;">
      <button onclick="window.print()" class="btn">Print</button>
      <a href="${pageContext.request.contextPath}/" class="btn alt">New Order</a>
      <a href="${pageContext.request.contextPath}/${billFileName}" class="btn alt" target="_blank">View Saved Bill File</a>
    </div>
  </div>
</body>
</html>
