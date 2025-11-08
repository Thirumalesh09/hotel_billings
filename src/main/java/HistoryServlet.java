package com.militaryhotel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name="HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sql = "SELECT id, bill_number, total, gst_amount, grand_total, created_at FROM bills ORDER BY created_at DESC LIMIT 100";
        List<Map<String,Object>> bills = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("bill_number", rs.getString("bill_number"));
                m.put("total", rs.getBigDecimal("total"));
                m.put("gst", rs.getBigDecimal("gst_amount"));
                m.put("grand", rs.getBigDecimal("grand_total"));
                m.put("created_at", rs.getTimestamp("created_at"));
                bills.add(m);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("bills", bills);
        req.getRequestDispatcher("/WEB-INF/history.jsp").forward(req, resp);
    }
}
