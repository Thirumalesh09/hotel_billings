package com.militaryhotel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet(name = "BillingServlet", urlPatterns = {"/bill"})
public class BillingServlet extends HttpServlet {

    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18%

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Expect parameters like qty_<id> for menu item id
        Map<Integer, Integer> order = new LinkedHashMap<>();
        Enumeration<String> en = req.getParameterNames();
        while (en.hasMoreElements()) {
            String param = en.nextElement();
            if (param.startsWith("qty_")) {
                try {
                    int id = Integer.parseInt(param.substring(4));
                    String val = req.getParameter(param);
                    int qty = val == null || val.isEmpty() ? 0 : Integer.parseInt(val);
                    if (qty > 0) order.put(id, qty);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (order.isEmpty()) {
            req.setAttribute("message", "No items selected. Please select items.");
            req.getRequestDispatcher("/").forward(req, resp);
            return;
        }

        // Fetch item details for IDs
        String inClause = String.join(",", Collections.nCopies(order.size(), "?"));
        String sqlFetch = "SELECT id, name, price FROM menu_items WHERE id IN (" + inClause + ")";
        Map<Integer, Item> items = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFetch)) {
            int idx = 1;
            for (Integer id : order.keySet()) ps.setInt(idx++, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.put(rs.getInt("id"), new Item(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price")));
            }

            // Calculate totals
            BigDecimal total = BigDecimal.ZERO;
            List<BillLine> lines = new ArrayList<>();
            for (Map.Entry<Integer,Integer> e : order.entrySet()) {
                Item it = items.get(e.getKey());
                if (it == null) continue;
                int qty = e.getValue();
                BigDecimal amount = it.price.multiply(new BigDecimal(qty));
                total = total.add(amount);
                lines.add(new BillLine(it.id, it.name, qty, it.price, amount));
            }
            BigDecimal gst = total.multiply(GST_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal grand = total.add(gst).setScale(2, BigDecimal.ROUND_HALF_UP);

            // Save bill
            conn.setAutoCommit(false);
            String billNumber = "MH" + System.currentTimeMillis();
            String insertBill = "INSERT INTO bills (bill_number, total, gst_amount, grand_total) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psBill = conn.prepareStatement(insertBill, Statement.RETURN_GENERATED_KEYS)) {
                psBill.setString(1, billNumber);
                psBill.setBigDecimal(2, total);
                psBill.setBigDecimal(3, gst);
                psBill.setBigDecimal(4, grand);
                psBill.executeUpdate();
                ResultSet gk = psBill.getGeneratedKeys();
                if (!gk.next()) throw new SQLException("Failed to insert bill");
                int billId = gk.getInt(1);

                String insertItem = "INSERT INTO bill_items (bill_id, menu_item_id, item_name, qty, unit_price, amount) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psItem = conn.prepareStatement(insertItem)) {
                    for (BillLine bl : lines) {
                        psItem.setInt(1, billId);
                        psItem.setInt(2, bl.menuItemId);
                        psItem.setString(3, bl.itemName);
                        psItem.setInt(4, bl.qty);
                        psItem.setBigDecimal(5, bl.unitPrice);
                        psItem.setBigDecimal(6, bl.amount);
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                conn.commit();

                // create bill text file under webapp/bills
                String billsDirPath = getServletContext().getRealPath("/bills");
                File billsDir = new File(billsDirPath);
                if (!billsDir.exists()) billsDir.mkdirs();

                String filename = "bill_" + billNumber + ".txt";
                File billFile = new File(billsDir, filename);
                try (PrintWriter pw = new PrintWriter(new FileWriter(billFile))) {
                    pw.println("Military Hotel");
                    pw.println("Bill Number: " + billNumber);
                    pw.println("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    pw.println("--------------------------------------------------");
                    for (BillLine bl : lines) {
                        pw.printf("%s x%d @ %.2f = %.2f%n", bl.itemName, bl.qty, bl.unitPrice, bl.amount);
                    }
                    pw.println("--------------------------------------------------");
                    pw.printf("Total: %.2f%n", total);
                    pw.printf("GST (18%%): %.2f%n", gst);
                    pw.printf("Grand Total: %.2f%n", grand);
                } catch (IOException ioe) {
                    // log but don't fail
                    ioe.printStackTrace();
                }

                // Forward to receipt page
                req.setAttribute("billNumber", billNumber);
                req.setAttribute("total", total);
                req.setAttribute("gst", gst);
                req.setAttribute("grand", grand);
                req.setAttribute("lines", lines);
                req.setAttribute("billFileName", "bills/" + filename);
                req.getRequestDispatcher("/WEB-INF/receipt.jsp").forward(req, resp);
                return;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static class Item {
        int id; String name; BigDecimal price;
        Item(int id,String name,BigDecimal price){this.id=id;this.name=name;this.price=price;}
    }

    public static class BillLine {
        public int menuItemId;
        public String itemName;
        public int qty;
        public BigDecimal unitPrice;
        public BigDecimal amount;
        public BillLine(int menuItemId,String itemName,int qty,BigDecimal unitPrice,BigDecimal amount){
            this.menuItemId = menuItemId;
            this.itemName = itemName;
            this.qty = qty;
            this.unitPrice = unitPrice;
            this.amount = amount;
        }
    }
}
