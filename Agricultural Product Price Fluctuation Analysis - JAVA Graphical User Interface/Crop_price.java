//以Python分析概念為基礎，設計圖形化介面與操作介面並連線資料庫

package less_01;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Crop_price extends JFrame {

    private static final long serialVersionUID = 1L; //序列化ID，用於確保類在不同Java版本中的兼容性
    private JCheckBox[] regionCheckBoxes; //保存地區選擇的CheckBox
    private JComboBox<String> cropComboBox; //農作物名稱下拉選單
    private JPanel chartPanel; //顯示圖表的面板
    private JPanel regionPanel; //放置地區CheckBox的面板

    private int chartWidth = 1140; //圖表寬度
    private int chartHeight = 630; //圖表高度

    private Map<String, String> regionMap = new HashMap<>(); //地區對應的SQL條件反射(Reflection)

    public Crop_price() {
        //初始化地區對應
        initializeRegionMap();

        //設置界面
        setTitle("動態生成圖表"); //設置視窗標題
        setSize(1200, 900); //設置視窗大小
        setDefaultCloseOperation(EXIT_ON_CLOSE); //設置點擊關閉按鈕時的操作
        setLayout(null); //使用自定義佈局

        //地區選擇 CheckBox
        JLabel regionLabel = new JLabel("選擇地區："); //創建標籤
        regionLabel.setBounds(20, 20, 100, 30); //設置標籤的位置和大小
        add(regionLabel); //添加到視窗

        regionPanel = new JPanel(); //初始化地區面板
        regionPanel.setLayout(new GridLayout(5, 3)); //使用GridLayout佈局
        regionPanel.setBounds(120, 20, 400, 100); //設置位置和大小
        add(regionPanel); //添加到視窗

        //初始化地區選擇CheckBox
        regionCheckBoxes = new JCheckBox[regionMap.size()]; //創建CheckBox陣列
        int index = 0;
        for (String region : regionMap.keySet()) { //遍歷(Traversal)地區反射(Reflection)
            JCheckBox checkBox = new JCheckBox(region); //創建CheckBox
            regionCheckBoxes[index] = checkBox; //添加到陣列
            regionPanel.add(checkBox); //添加到面板
            index++;
        }

        //農作物名稱下拉選單
        JLabel cropLabel = new JLabel("農作物名稱："); //創建農作物名稱標籤
        cropLabel.setBounds(20, 140, 100, 30); //設置位置和大小
        add(cropLabel); //添加到視窗

        cropComboBox = new JComboBox<>(); //初始化農作物名稱下拉選單
        cropComboBox.setBounds(120, 140, 200, 30); //設置位置和大小
        cropComboBox.setEditable(true); //設置為可輸入
        add(cropComboBox); //添加到視窗

        //動態初始化農作物名稱
        initializeCropNames();

        //生成圖表按鈕
        JButton generateButton = new JButton("生成圖表"); //創建按鈕
        generateButton.setBounds(680, 140, 100, 30); //設置按鈕位置和大小
        add(generateButton); //添加到視窗

        //圖表顯示區域
        chartPanel = new JPanel(); //初始化圖表面板
        chartPanel.setBounds(20, 200, chartWidth, chartHeight); //設置位置和大小
        add(chartPanel); //添加到視窗

        //事件處理：按下按鈕生成圖表
        generateButton.addActionListener((ActionEvent e) -> createChart()); //設置按鈕事件
    }

    //初始化地區對應
    private void initializeRegionMap() {
        regionMap.put("台北市", "market_code = 104 OR market_code = 105 OR market_code = 109");
        regionMap.put("新北市", "market_code = 220 OR market_code = 241");
        regionMap.put("桃園市", "market_code = 338");
        regionMap.put("宜蘭市", "market_code = 260");
        regionMap.put("台中市", "market_code = 400 OR market_code = 420 OR market_code = 423");
        regionMap.put("彰化縣", "market_code = 512 OR market_code = 514");
        regionMap.put("南投縣", "market_code = 540");
        regionMap.put("雲林縣", "market_code = 648");
        regionMap.put("高雄市", "market_code = 800 OR market_code = 830");
        regionMap.put("台南市", "market_code = 700");
        regionMap.put("嘉義市", "market_code = 600");
        regionMap.put("屏東縣", "market_code = 900");
        regionMap.put("台東縣", "market_code = 930");
        regionMap.put("花蓮縣", "market_code = 950");
    }

    //動態初始化農作物名稱
    private void initializeCropNames() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/world?serverTimezone=Asia/Taipei", "root", "ab901087");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT crop_name FROM produce")) {

            while (rs.next()) {
                cropComboBox.addItem(rs.getString("crop_name")); //將結果添加到下拉選單
            }
        } catch (Exception e) {
            e.printStackTrace(); //錯誤處理
        }
    }

    //生成圖表
    private void createChart() {
        String selectedCrop = (String) cropComboBox.getSelectedItem(); //獲取選擇的農作物名稱
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //創建資料集

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/world?serverTimezone=Asia/Taipei", "root", "ab901087");
             Statement stmt = conn.createStatement()) {

            StringBuilder queryBuilder = new StringBuilder(); //建構SQL查詢
            boolean isFirst = true;
            for (JCheckBox checkBox : regionCheckBoxes) { //遍歷(Traversal)選中的地區
                if (checkBox.isSelected()) {
                    if (!isFirst) {
                        queryBuilder.append(" UNION ");
                    }
                    String regionCondition = regionMap.get(checkBox.getText());
                    queryBuilder.append("SELECT '")
                            .append(checkBox.getText())
                            .append("' AS city, transaction_date, ROUND(AVG(average_price), 2) "
                            		+ "AS average_price ")
                            .append("FROM produce WHERE ")
                            .append("crop_name = '").append(selectedCrop).append("' AND ")
                            .append("(").append(regionCondition).append(") ")
                            .append("GROUP BY transaction_date");
                    isFirst = false;
                }
            }

            queryBuilder.append(" ORDER BY transaction_date"); //按交易日期排序
            ResultSet rs = stmt.executeQuery(queryBuilder.toString()); //執行查詢

            while (rs.next()) {
                dataset.addValue(rs.getDouble("average_price"), rs.getString("city"), rs.getString(""
                		+ "transaction_date")); //添加數據
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "無法從資料庫中取得資料"); //顯示錯誤訊息
            return;
        }

        //創建折線圖
        JFreeChart chart = ChartFactory.createLineChart(
                "農作物平均價格", "交易日期", "平均價", dataset);

        chart.setTitle(new TextTitle("農作物平均價格")); //設置圖表標題
        chart.getLegend().setItemFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14)); //設置圖例字體

        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabel("交易日期"); //設置X軸標題
        domainAxis.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14)); //設置X軸字體
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.
        		createUpRotationLabelPositions(Math.PI / 4.0)); //旋轉標籤
        domainAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12)); //設置標籤字體

        //設置每隔5個顯示一次標籤
        int tickInterval = 5;
        List<?> categories = dataset.getColumnKeys();
        for (int i = 0; i < categories.size(); i++) {
            if (i % tickInterval != 0) {
                String category = categories.get(i).toString();
                domainAxis.setTickLabelPaint(category, new Color(0, 0, 0, 0)); //設置隱藏標籤的顏色
            }
        }

        //設置Y軸
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabel("平均價"); //設置Y軸標題
        rangeAxis.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14)); //設置Y軸字體

        chartPanel.removeAll(); //清空舊圖表
        ChartPanel chartPane = new ChartPanel(chart); //創建新圖表面板
        chartPane.setPreferredSize(new Dimension(chartWidth, chartHeight)); //設置圖表大小
        chartPanel.add(chartPane); //添加到面板
        chartPanel.revalidate();
        chartPanel.repaint(); //重新繪製
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH); //設定語言環境
        SwingUtilities.invokeLater(() -> {
            Crop_price example = new Crop_price(); //創建主程式視窗
            example.setVisible(true); //顯示視窗
        });
    }
}

