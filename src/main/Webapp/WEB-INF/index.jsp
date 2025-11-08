 <%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Military Hotel - Billing</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
</head>
<body>
  <div class="container">
    <h1>Military Hotel - Take Order</h1>
    <form method="post" action="${pageContext.request.contextPath}/bill">
      <table class="menu">
        <thead>
          <tr><th>Item</th><th>Category</th><th>Price</th><th>Qty</th></tr>
        </thead>
        <tbody>
          <c:forEach var="item" items="${menu}">
            <tr>
              <td>${item.name}</td>
              <td>${item.category}</td>
              <td>${item.price}</td>
              <td>
                <input type="number" min="0" name="qty_${item.id}" value="0" style="width:60px;">
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
      <div style="margin-top:10px;">
        <button type="submit" class="btn">Generate Bill</button>
        <a href="${pageContext.request.contextPath}/history" class="btn alt">Bill History</a>
      </div>
    </form>
  </div>
</body>
</html>
