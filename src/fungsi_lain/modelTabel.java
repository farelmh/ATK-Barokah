package fungsi_lain;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class modelTabel extends DefaultTableCellRenderer {

    public static void setModel(JTable table) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                table.setRowHeight(30);
                table.setGridColor(new Color(220, 220, 220));
                table.setShowGrid(true);
                table.setIntercellSpacing(new Dimension(1, 1));

                JTableHeader header = table.getTableHeader();
                header.setFont(new Font("Segoe UI", Font.BOLD, 14));
                header.setBackground(new Color(230, 230, 250));
                header.setForeground(new Color(50, 50, 50));

                TableColumnModel colModel = table.getColumnModel();

                for (int i = 0; i < colModel.getColumnCount(); i++) {
                    Class<?> colClass = table.getModel().getColumnClass(i);

                    // Renderer untuk semua kolom (termasuk Boolean)
                    TableCellRenderer renderer = new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {

                            Component c;

                            if (colClass == Boolean.class) {
                                JCheckBox checkBox = new JCheckBox();
                                checkBox.setSelected(value != null && (Boolean) value);
                                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                                checkBox.setEnabled(table.isCellEditable(row, column));

                                // Jika sedang diselect, atur background khusus
                                if (isSelected) {
                                    checkBox.setBackground(new Color(63, 114, 175));
                                }

                                // Tambahkan ini juga untuk menonaktifkan efek opaque default
                                checkBox.setOpaque(true);

                                c = checkBox;

                            } else {
                                c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            }

                            if (!isSelected) {
                                c.setBackground((row % 2 == 0) ? new Color(245, 245, 245) : new Color(204, 204, 255));
                            } else {
                                c.setBackground(new Color(63, 114, 175));
                                // c.setForeground(Color.WHITE);
                            }

                            return c;
                        }
                    };

                    colModel.getColumn(i).setCellRenderer(renderer);
                }

                // Di akhir method setModel, tambahkan:
                for (int column = 0; column < table.getColumnCount(); column++) {
                    TableColumn tableColumn = table.getColumnModel().getColumn(column);
                    int preferredWidth = tableColumn.getMinWidth();
                    int maxWidth = tableColumn.getMaxWidth();

                    // Periksa header
                    TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
                    if (headerRenderer == null) {
                        headerRenderer = table.getTableHeader().getDefaultRenderer();
                    }
                    Object headerValue = tableColumn.getHeaderValue();
                    Component headerComp = headerRenderer.getTableCellRendererComponent(
                            table, headerValue, false, false, 0, column);
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width);

                    // Periksa data di setiap baris (terbatas pada 20 baris untuk performa)
                    for (int row = 0; row < Math.min(20, table.getRowCount()); row++) {
                        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                        Component comp = table.prepareRenderer(cellRenderer, row, column);
                        int width = comp.getPreferredSize().width + table.getIntercellSpacing().width;
                        preferredWidth = Math.max(preferredWidth, width);

                        // Batasi ke ukuran maksimum
                        if (preferredWidth >= maxWidth) {
                            preferredWidth = maxWidth;
                            break;
                        }
                    }

                    tableColumn.setPreferredWidth(preferredWidth + 10); // Tambahkan sedikit padding
                }

                // ScrollPane check (parent scroll)
                if (table.getParent() instanceof JViewport) {
                    Component parent = table.getParent().getParent();
                    if (parent instanceof JScrollPane) {
                        JScrollPane scroll = (JScrollPane) parent;
                        // Scrollbar vertikal & horizontal
                        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
                        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
                    }
                }
            }
        });
    }

    // Class inner ScrollBar UI
    public static class ModernScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(130, 160, 210);
            trackColor = new Color(240, 240, 240);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color = isDragging ? new Color(90, 130, 200)
                    : isThumbRollover() ? new Color(110, 150, 220)
                            : new Color(150, 180, 230);
            g2.setPaint(color);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    public static void setTransparan(JTable table, int index) {
        table.getColumnModel().getColumn(index).setMaxWidth(0);
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setWidth(0);
    }

}
