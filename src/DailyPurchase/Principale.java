/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DailyPurchase;

import Connection.ConnectDB;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author hajar
 */
public final class Principale extends javax.swing.JFrame {

    private ResultSet rs;

    /**
     * Creates new form Principale
     */
    public Principale() {
        initComponents();
        getConnection();
        Refresh();

    }
    String ImgPath = null;
    int pos = 0;

    // Prepared connection to manipulate data
    public Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost/gestion_de_produit", "root", "");
            return con;
        } catch (SQLException e) {
            Logger.getLogger(Principale.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(null, "Not Connected");
            return null;
        }
    }

    // Resizing image and show it in labelpic
    public ImageIcon ResizeImage(String imagePath, byte[] pic) {
        ImageIcon myImage = null;
        if (imagePath != null) {
            myImage = new ImageIcon(imagePath);
        } else {
            myImage = new ImageIcon(pic);
        }
        Image img = myImage.getImage();
        Image img2 = img.getScaledInstance(labelPic.getWidth(), labelPic.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(img2);
        return image;
    }
    //Create Function to choose and load image

    public void ChooseImage() {
        JFileChooser file = new JFileChooser();
        file.setCurrentDirectory(new File(System.getProperty("user.home")));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images", "jpg", "png", "jpeg");
        file.addChoosableFileFilter(filter);
        int result = file.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = file.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            labelPic.setIcon(ResizeImage(path, null));
            ImgPath = path;
        } else {
            System.out.println("No file Selected");
        }
    }

    //Create Function to insert data to database
    public void InsertFunction() {
        if (checkInputs() && ImgPath != null) {
            try {
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO products(name, price, addDate, picture) " + " values(?,?,?,?)");
                ps.setString(1, txtNAME.getText());
                ps.setString(2, txtPRICE.getText());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String addDate = dateFormat.format(jDateChooser1.getDate());
                ps.setString(3, addDate);
                InputStream img = new FileInputStream(new File(ImgPath));
                ps.setBlob(4, img);
                ps.executeUpdate();
                ShowAllDataTable();
                //Refresh();
                JOptionPane.showMessageDialog(null, "Data Inserted");
            } catch (HeadlessException | FileNotFoundException | SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());

            }
        } else {
            JOptionPane.showMessageDialog(null, "One or more fileds are empty");
        }
    }

    /**
     * Update Function with two statement, once with image and the last without
     * picture
     */
    public void UpdateFunction() {
        if (checkInputs() && txtID != null) {
            String UpdateQuery = null;
            PreparedStatement ps = null;
            Connection con = getConnection();
            //Update withour image
            if (ImgPath == null) {
                try {
                    UpdateQuery = "UPDATE products SET name = ?, price = ?, addDate = ? WHERE id = ?";
                    ps = con.prepareStatement(UpdateQuery);
                    ps.setString(1, txtNAME.getText());
                    ps.setString(2, txtPRICE.getText());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String addDate = dateFormat.format(jDateChooser1.getDate());
                    ps.setString(3, addDate);
                    ps.setInt(4, Integer.parseInt(txtID.getText()));
                    ps.executeUpdate();
                    Refresh();
                    JOptionPane.showMessageDialog(null, "Data Updated");
                } catch (SQLException e) {
                    //Logger.getLogger(Principale.class.getName()).log(Level.SEVERE, null, e);
                    JOptionPane.showMessageDialog(null, e.getMessage());

                }
            } else {
                //Update with Image
                try {
                    UpdateQuery = "UPDATE products SET name = ?, price = ?, addDate = ?, picture = ? WHERE id = ?";
                    ps = con.prepareStatement(UpdateQuery);
                    ps.setString(1, txtNAME.getText());
                    ps.setString(2, txtPRICE.getText());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String addDate = dateFormat.format(jDateChooser1.getDate());
                    ps.setString(3, addDate);
                    InputStream img = new FileInputStream(new File(ImgPath));
                    ps.setBlob(4, img);
                    ps.setInt(5, Integer.parseInt(txtID.getText()));
                    ps.executeUpdate();
                    Refresh();
                    JOptionPane.showMessageDialog(null, "Data Updated");

                } catch (FileNotFoundException | NumberFormatException | SQLException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "One or more fileds are empty or Wrong");
        }
    }

    /**
     * Delete data by id
     */
    public void DeleteFunction() {
        if (!txtID.getText().equals("")) {
            try {
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id = ?");
                int id = Integer.parseInt(txtID.getText());
                ps.setInt(1, id);
                ps.executeUpdate();
                Refresh();
                JOptionPane.showMessageDialog(null, "Data deleted");
            } catch (NumberFormatException | SQLException e) {
                JOptionPane.showMessageDialog(null, "Data not deleted" + e.getMessage());
            }
        }
    }

    /**
     * Retrieve and print all data in the TableDataProducts
     */
    public void PrintFunction() {
        java.text.MessageFormat head = new java.text.MessageFormat("All shopping you made");
        java.text.MessageFormat end = new java.text.MessageFormat("Page {0,number,integer}");
        try {
            TableDataProducts.print(JTable.PrintMode.FIT_WIDTH, head, end);
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this, "There is an error: " + e.getMessage());
        }
    }

    /**
     * Show a message before closing the apps window
     */
    public void CloseAppFunction() {
        if (JOptionPane.showConfirmDialog(this, "Would you like to quit this app", "be carefull !", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new Authentification().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "There is an error !");
        }
    }

    /**
     * Function to clear al field and refresh TableDataProducts
     */
    public void Refresh() {
        txtID.setText("");
        txtNAME.setText("");
        txtPRICE.setText("");
        jDateChooser1.setCalendar(null);
        labelPic.setIcon(null);
        ShowAllDataTable();

    }

    /**
     * Check if field txtPrice is fill in correct input value to double, float
     */
    public void PriceFieldCheck() {
        String ChampCodeDoss = txtPRICE.getText();
        int CCD = ChampCodeDoss.length();
        int valeur = 10;
        if (ChampCodeDoss.matches("[0-9,.]*")) {
            if (CCD > valeur) {
                ChampCodeDoss = ChampCodeDoss.substring(0, CCD - 1);
                JOptionPane.showMessageDialog(null, "This field support 10 characters only, Thank you !");
            }
        } else {
            ChampCodeDoss = ChampCodeDoss.substring(0, CCD - 1);
            JOptionPane.showMessageDialog(null, "Please, use entier number or decimal, Thank you !!");
        }
        txtPRICE.setText(ChampCodeDoss);
    }

    /**
     * Check if field txName is fill in correct input
     */
    public void NameFieldCheck() {
        String ChampCodeDoss = txtNAME.getText();
        int CCD = ChampCodeDoss.length();
        int valeur = 100;
        if (txtNAME.getText().matches("[A-Z,a-z,0-9,', ]*")) {
            if (CCD > valeur) {
                ChampCodeDoss = ChampCodeDoss.substring(0, CCD - 1);
                JOptionPane.showMessageDialog(null, "This field support 100 characters only, Thank you !");
            }
            txtNAME.setText(ChampCodeDoss);
        } else {
            ChampCodeDoss = ChampCodeDoss.substring(0, CCD - 1);
            txtNAME.setText(ChampCodeDoss);
            JOptionPane.showMessageDialog(null, "Please, Type Alphabet Characters Without Accent, Thank you !");
        }
    }

    /**
     *
     * Function to print sum of price in TableDataPoducts in to txtSomme
     */
    public void printSum() {
        int rowsCount = TableDataProducts.getRowCount();
        double sum = 0;
        for (int i = 0; i < rowsCount; i++) {
            double val = Double.valueOf(TableDataProducts.getValueAt(i, 2).toString());
            sum += val;
        }
        txtSomme.setText(Double.toString(sum));
    }
// Check inputs with correct charachter

    public boolean checkInputs() {
        if (txtNAME.getText() == null || txtPRICE.getText() == null || jDateChooser1.getDate() == null) {
            return false;

        } else {
            try {
                Float.parseFloat(txtPRICE.getText());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
//Fill ArrayList with all DATA

    public ArrayList<Product> getProductList() {
        ArrayList<Product> productList = new ArrayList<>();
        Connection con = getConnection();
        String query = "SELECT * FROM products";
        Statement st;
        ResultSet rs;

        try {
            st = con.createStatement();
            rs = st.executeQuery(query);
            Product product;
            while (rs.next()) {
                product = new Product(rs.getInt("id"), rs.getString("name"), Float.parseFloat(rs.getString("price")), rs.getString("addDate"), rs.getBytes("picture"));
                productList.add(product);
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(Principale.class.getName()).log(Level.SEVERE, null, e);

        }
        return productList;
    }
// Create a function to find all data or specific data in databaee

    public void SearchFunction() {
        try {
            String champ = txtSearch.getText();
            ConnectDB c = new ConnectDB();
            DefaultTableModel model = new DefaultTableModel();

            model.addColumn("ID");
            model.addColumn("NAME");
            model.addColumn("PRICE");
            model.addColumn("DATE");

            TableDataProducts.setModel(model);
            rs = c.executeQuery("SELECT * FROM products where id like '%" + champ + "%' or name like '%" + champ + "%' or price like '%" + champ + "%' or addDate like '%" + champ + "%'");

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String price = rs.getString("price");
                String addDate = rs.getString("addDate");

                Object[] products = {id, name, price, addDate};
                model.addRow(products);
            }
        } catch (Exception ex) {
            Logger.getLogger(Principale.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Show all data to TableProducts
    public void ShowAllDataTable() {
        ArrayList<Product> list = getProductList();
        DefaultTableModel model = (DefaultTableModel) TableDataProducts.getModel();
        model.setRowCount(0);
        Object[] row = new Object[4];
        for (int i = 0; i < list.size(); i++) {
            row[0] = list.get(i).getId();
            row[1] = list.get(i).getName();
            row[2] = list.get(i).getPrice();
            row[3] = list.get(i).getAddDate();

            model.addRow(row);
            printSum();
        }
    }
    // Show all item 

    public void ShowItem(int index) {
        txtID.setText(Integer.toString(getProductList().get(index).getId()));
        txtNAME.setText(getProductList().get(index).getName());
        txtPRICE.setText(Float.toString(getProductList().get(index).getPrice()));
        try {
            Date addDate = null;
            addDate = new SimpleDateFormat("yyyy-MM-dd").parse((String) getProductList().get(index).getAddDate());
            jDateChooser1.setDate(addDate);
        } catch (ParseException e) {
            Logger.getLogger(Principale.class.getName()).log(Level.SEVERE, null, e);
        }
        labelPic.setIcon(ResizeImage(null, getProductList().get(index).getPicture()));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        kGradientPanel1 = new keeptoo.KGradientPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        txtNAME = new javax.swing.JTextField();
        txtPRICE = new javax.swing.JTextField();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        labelPic = new javax.swing.JLabel();
        btnChooseImage = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnInsert = new keeptoo.KButton();
        btnUpdate = new keeptoo.KButton();
        btnDelete = new keeptoo.KButton();
        btnRefresh = new keeptoo.KButton();
        kButton5 = new keeptoo.KButton();
        btnGraphe = new keeptoo.KButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        btnFirst = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableDataProducts = new javax.swing.JTable();
        txtSomme = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        btnExportDB = new javax.swing.JMenuItem();
        btnExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        btnHelp = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        btnAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(853, 743));
        setUndecorated(true);

        kGradientPanel1.setkEndColor(new java.awt.Color(0, 157, 54));
        kGradientPanel1.setkGradientFocus(10);
        kGradientPanel1.setkStartColor(new java.awt.Color(255, 255, 255));
        kGradientPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true), "Operations"));
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("PRICE");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 80, 20));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("NAME");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 80, -1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("ID");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 26, 80, 20));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("DATE");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 80, -1));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("PICTURE");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 80, -1));

        txtID.setBackground(new java.awt.Color(204, 51, 255));
        txtID.setEnabled(false);
        txtID.setOpaque(false);
        jPanel1.add(txtID, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 230, 30));

        txtNAME.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNAMEKeyReleased(evt);
            }
        });
        jPanel1.add(txtNAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 230, 30));

        txtPRICE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPRICEKeyReleased(evt);
            }
        });
        jPanel1.add(txtPRICE, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 100, 230, 30));

        jDateChooser1.setDateFormatString("yyyy-MM-dd");
        jPanel1.add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 150, 230, 30));

        labelPic.setBackground(new java.awt.Color(204, 255, 204));
        labelPic.setForeground(new java.awt.Color(255, 255, 102));
        labelPic.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelPic.setOpaque(true);
        jPanel1.add(labelPic, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 190, 230, 130));

        btnChooseImage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnChooseImage.setText("choose image ...");
        btnChooseImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnChooseImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseImageActionPerformed(evt);
            }
        });
        jPanel1.add(btnChooseImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 330, 230, 30));

        kGradientPanel1.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 370, 390));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true), "Operations"));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnInsert.setText("insert");
        btnInsert.setkBorderRadius(20);
        btnInsert.setkEndColor(new java.awt.Color(0, 255, 255));
        btnInsert.setkStartColor(new java.awt.Color(0, 51, 255));
        btnInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertActionPerformed(evt);
            }
        });
        jPanel2.add(btnInsert, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        btnUpdate.setText("update");
        btnUpdate.setkBorderRadius(20);
        btnUpdate.setkEndColor(new java.awt.Color(255, 153, 0));
        btnUpdate.setkIndicatorThickness(0);
        btnUpdate.setkStartColor(new java.awt.Color(255, 153, 0));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        btnDelete.setText("delete");
        btnDelete.setkBorderRadius(20);
        btnDelete.setkEndColor(new java.awt.Color(255, 0, 0));
        btnDelete.setkIndicatorThickness(0);
        btnDelete.setkStartColor(new java.awt.Color(255, 51, 51));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        btnRefresh.setText("refresh");
        btnRefresh.setkBorderRadius(20);
        btnRefresh.setkIndicatorThickness(0);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        jPanel2.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, -1, -1));

        kButton5.setText("print");
        kButton5.setkBorderRadius(20);
        kButton5.setkEndColor(new java.awt.Color(51, 51, 0));
        kButton5.setkIndicatorThickness(0);
        kButton5.setkStartColor(new java.awt.Color(0, 102, 204));
        kButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(kButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        btnGraphe.setText("resume graph");
        btnGraphe.setkBorderRadius(20);
        btnGraphe.setkEndColor(new java.awt.Color(51, 0, 51));
        btnGraphe.setkIndicatorThickness(0);
        btnGraphe.setkStartColor(new java.awt.Color(51, 255, 255));
        jPanel2.add(btnGraphe, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, -1));

        kGradientPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 10, 220, 390));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true), "Graph Statistic"));
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        kGradientPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 10, 550, 390));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true), "All Data Saved"));
        jPanel4.setOpaque(false);
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/first_20px.png"))); // NOI18N
        btnFirst.setText("First");
        btnFirst.setOpaque(false);
        btnFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstActionPerformed(evt);
            }
        });
        jPanel4.add(btnFirst, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 90, 30));

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/next_20px.png"))); // NOI18N
        btnNext.setText("Next");
        btnNext.setOpaque(false);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        jPanel4.add(btnNext, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 90, 30));

        btnPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/previous_20px.png"))); // NOI18N
        btnPrevious.setText("Previous");
        btnPrevious.setOpaque(false);
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });
        jPanel4.add(btnPrevious, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 110, 30));

        btnLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/last_20px.png"))); // NOI18N
        btnLast.setText("Last");
        btnLast.setOpaque(false);
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });
        jPanel4.add(btnLast, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 30, 80, 30));

        TableDataProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAME", "PRICE", "DATE"
            }
        ));
        TableDataProducts.setOpaque(false);
        TableDataProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableDataProductsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(TableDataProducts);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 1140, 310));

        txtSomme.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtSomme.setForeground(new java.awt.Color(255, 255, 0));
        txtSomme.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtSomme.setText("0.0");
        txtSomme.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), "Total Price €", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N
        txtSomme.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel4.add(txtSomme, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 20, 270, 40));

        txtSearch.setBackground(new java.awt.Color(0, 221, 155));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), "Search", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N
        txtSearch.setMaximumSize(new java.awt.Dimension(10, 39));
        txtSearch.setOpaque(false);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSearchKeyTyped(evt);
            }
        });
        jPanel4.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, 430, 40));

        kGradientPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 1160, 390));

        jMenuBar1.setBackground(new java.awt.Color(204, 204, 255));
        jMenuBar1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 255), 2, true));
        jMenuBar1.setMaximumSize(new java.awt.Dimension(66, 26));
        jMenuBar1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jMenuBar1MouseDragged(evt);
            }
        });
        jMenuBar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuBar1MousePressed(evt);
            }
        });

        jMenu1.setText("File");

        btnExportDB.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        btnExportDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/realtime_database_20px.png"))); // NOI18N
        btnExportDB.setText("Export BD");
        jMenu1.add(btnExportDB);

        btnExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        btnExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/cancel_20px.png"))); // NOI18N
        btnExit.setText("Exit");
        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExitMouseClicked(evt);
            }
        });
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        jMenu1.add(btnExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/look_20px.png"))); // NOI18N
        jMenuItem2.setText("Change Look");
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        btnHelp.setText("Help");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/help_20px.png"))); // NOI18N
        jMenuItem1.setText("Help Contents");
        btnHelp.add(jMenuItem1);

        btnAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_MASK));
        btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/info_20px.png"))); // NOI18N
        btnAbout.setText("About");
        btnHelp.add(btnAbout);

        jMenuBar1.add(btnHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(kGradientPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(kGradientPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 810, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnChooseImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseImageActionPerformed
        ChooseImage();
    }//GEN-LAST:event_btnChooseImageActionPerformed

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
        InsertFunction();
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        UpdateFunction();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        DeleteFunction();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        Refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void TableDataProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableDataProductsMouseClicked
        int index = TableDataProducts.getSelectedRow();
        ShowItem(index);
    }//GEN-LAST:event_TableDataProductsMouseClicked

    private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
        pos = 0;
        ShowItem(pos);
    }//GEN-LAST:event_btnFirstActionPerformed

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
        pos = getProductList().size() - 1;
        ShowItem(pos);
    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        pos++;
        if (pos >= getProductList().size()) {
            pos = getProductList().size() - 1;
        }
        ShowItem(pos);
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        pos--;
        if (pos < 0) {
            pos = 0;
        }
        ShowItem(pos);
    }//GEN-LAST:event_btnPreviousActionPerformed

    private void txtNAMEKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNAMEKeyReleased
        NameFieldCheck();
    }//GEN-LAST:event_txtNAMEKeyReleased

    private void txtPRICEKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPRICEKeyReleased
        PriceFieldCheck();
    }//GEN-LAST:event_txtPRICEKeyReleased

    private void btnExitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExitMouseClicked
        CloseAppFunction();
    }//GEN-LAST:event_btnExitMouseClicked

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        CloseAppFunction();
    }//GEN-LAST:event_btnExitActionPerformed

    private void kButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kButton5ActionPerformed
        PrintFunction();
    }//GEN-LAST:event_kButton5ActionPerformed
    int xx, xy;

    private void jMenuBar1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuBar1MousePressed
        xx = evt.getX();
        xy = evt.getY();
    }//GEN-LAST:event_jMenuBar1MousePressed

    private void jMenuBar1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuBar1MouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xx, y - xy);
    }//GEN-LAST:event_jMenuBar1MouseDragged

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        SearchFunction();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void txtSearchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyTyped
        SearchFunction();
    }//GEN-LAST:event_txtSearchKeyTyped

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principale().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TableDataProducts;
    private javax.swing.JMenuItem btnAbout;
    private javax.swing.JButton btnChooseImage;
    private keeptoo.KButton btnDelete;
    private javax.swing.JMenuItem btnExit;
    private javax.swing.JMenuItem btnExportDB;
    private javax.swing.JButton btnFirst;
    private keeptoo.KButton btnGraphe;
    private javax.swing.JMenu btnHelp;
    private keeptoo.KButton btnInsert;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private keeptoo.KButton btnRefresh;
    private keeptoo.KButton btnUpdate;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private keeptoo.KButton kButton5;
    private keeptoo.KGradientPanel kGradientPanel1;
    private javax.swing.JLabel labelPic;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtNAME;
    private javax.swing.JTextField txtPRICE;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JLabel txtSomme;
    // End of variables declaration//GEN-END:variables
}
