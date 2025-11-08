package com.militaryhotel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(name = "MenuServlet", urlPatterns = {"/"})
public class MenuServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Map<String,Object>> menu = new ArrayList<>();
        String sql = "SELECT id, name, category, price FROM menu_items ORDER BY category, name";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("name", rs.getString("name"));
                m.put("category", rs.getString("category"));
                m.put("price", rs.getBigDecimal("price"));
                menu.add(m);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("menu", menu);
        req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
    }
}
