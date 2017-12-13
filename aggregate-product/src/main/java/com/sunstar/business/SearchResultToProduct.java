package com.sunstar.business;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * @Author 黄昌焕
 * @Date 2017-12-06  4:38 PM
 */
public class SearchResultToProduct {
    private static final Logger logger = Logger.getLogger(SearchResultToProduct.class);


    public static long importData( ){
        String url = "jdbc:mysql://192.168.0.183:3306/couponsdb?user=root&password=" +
                "Sunstar123!&useUnicode=true&characterEncoding=UTF-8&useCursorFetch=true";
        String sql = "SELECT a.classid,a.spname,a.sppic,0,0,1,b.value_id,a.id FROM " +
                "ss_hj_search_result a LEFT JOIN ss_hj_value b ON (a.brand_name = b.value_name " +
                "AND b.quantity_id = 1) WHERE a.id not in (select autoid from ss_hj_product_relation)";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        long allStart = System.currentTimeMillis();
        long count =0;
        Connection con = null;
        Connection con1 = null;
        PreparedStatement psHJSearchResult = null;
        PreparedStatement psHJProduct = null;
        PreparedStatement psHJProductPicture = null;
        PreparedStatement psHJProductRelation = null;
        ResultSet rs = null;
        int productId=572457;
        int pictureId=1711669;
        int relationId=252381;
        try {
            //查询操作
            con = DriverManager.getConnection(url);
            con.setAutoCommit(false);
            //查询ss_hj_search_result合格数据
            psHJSearchResult = con.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            psHJSearchResult.setFetchSize(1);
            //psHJSearchResult.setFetchDirection(ResultSet.FETCH_REVERSE);
            rs = psHJSearchResult.executeQuery();
            // 写入操作
            con1 = DriverManager.getConnection(url);
            con1.setAutoCommit(false);
            //插入ss_hj_product
            psHJProduct = con1.prepareStatement("insert into ss_hj_product" +
                    "(PRODUCT_ID,CLS_ID,PRODUCT_NAME,MAIN_PICTURE,SALES_VOLUME," +
                    "COMMENTS_NUMBER,STS,PRIORITY,BRAND_ID) values(?,?,?,?,?,?,?,?,?)");
            //插入ss_hj_product_picture
            psHJProductPicture = con1.prepareStatement("insert into ss_hj_product_picture" +
                    "(PRODUCT_ID,PRODUCT_URL,PRODUCT_VIEW_URL,PICTURE_ID) values(?,?,?,?)");
            //插入ss_hj_product_relation
            psHJProductRelation = con1.prepareStatement("insert into ss_hj_product_relation" +
                    "(PRODUCT_ID,AUTOID,RELATION_ID) values(?,?,?)");
            while (rs.next()) {
                    //聚合商品
                    psHJProduct.setInt(1,productId);
                    psHJProduct.setInt(2,rs.getInt("classid"));
                    psHJProduct.setString(3,rs.getString("spname"));
                    psHJProduct.setString(4,rs.getString("sppic"));
                    psHJProduct.setInt(5,0);
                    psHJProduct.setInt(6,0);
                    psHJProduct.setInt(7,1);
                    psHJProduct.setInt(8,productId);
                    psHJProduct.setInt(9,rs.getInt("value_id"));
                    psHJProduct.addBatch();
                    //图片
                    psHJProductPicture.setInt(1,productId);
                    psHJProductPicture.setString(2,rs.getString("sppic"));
                    psHJProductPicture.setString(3,rs.getString("sppic"));
                    psHJProductPicture.setInt(4,pictureId);
                    psHJProductPicture.addBatch();
                    //关系
                    psHJProductRelation.setInt(1,productId);
                    psHJProductRelation.setInt(2,rs.getInt("id"));
                    psHJProductRelation.setInt(3,relationId);
                    psHJProductRelation.addBatch();
                    //id增加
                    productId++;
                    pictureId++;
                    relationId++;
                count++;
                if(count%100000==0) {//提交
                    psHJProduct.executeBatch();
                    psHJProductPicture.executeBatch();
                    psHJProductRelation.executeBatch();
                    con1.commit();
                }
            }
            //最后一批没有提交处理
            psHJProduct.executeBatch();
            psHJProductPicture.executeBatch();
            psHJProductRelation.executeBatch();
            con1.commit();
        } catch (Exception e) {
            logger.error("异常");
            e.printStackTrace();
        } finally {
            logger.error("=productId:"+productId);
            logger.error("=pictureId:"+pictureId);
            logger.error("=relationId:"+relationId);
            try {
                if(rs!=null){
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(psHJSearchResult!=null){
                    psHJSearchResult.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(con!=null){
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public static void main(String[] args) throws InterruptedException {

        String sql = "select * from test.bigTable ";
        //System.out.println("Disney's Frozen, \"Frozen Day\" Tapestry Throw - by The Northwest Company, 48-inches by 60-inches".replaceAll("'","’"));
        importData();

    }
}
