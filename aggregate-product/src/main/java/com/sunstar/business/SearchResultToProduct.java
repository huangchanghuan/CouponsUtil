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
        String url = "jdbc:mysql://192.168.0.183:3306/couponsdb?user=root&password=Sunstar123!";
        String sql = "SELECT a.classid,a.spname,a.sppic,0,0,1,b.value_id,a.id FROM ss_hj_search_result a LEFT JOIN ss_hj_value b ON (a.brand_name = b.value_name AND b.quantity_id = 1) WHERE a.id not in (select autoid from ss_hj_product_relation)";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        long allStart = System.currentTimeMillis();
        long count =0;
        Connection con = null;
        PreparedStatement psHJSearchResult = null;
        PreparedStatement psHJProduct = null;
        PreparedStatement psHJProductPicture = null;
        PreparedStatement psHJProductRelation = null;
        ResultSet rs = null;
        int productId=572457;
        int pictureId=1711669;
        int relationId=252381;
        try {
            con = DriverManager.getConnection(url);
            // Set auto-commit to false
            con.setAutoCommit(false);
            //查询ss_hj_search_result合格数据
            psHJSearchResult = con.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            psHJSearchResult.setFetchSize(Integer.MIN_VALUE);
            psHJSearchResult.setFetchDirection(ResultSet.FETCH_REVERSE);
            //插入ss_hj_product
            psHJProduct = con.prepareStatement("insert into ss_hj_product(PRODUCT_ID,CLS_ID,PRODUCT_NAME,MAIN_PICTURE,SALES_VOLUME,COMMENTS_NUMBER,STS,PRIORITY,BRAND_ID) values(?,?,?,?,?,?,?,?,?)");
            //插入ss_hj_product_picture
            psHJProductPicture = con.prepareStatement("insert into ss_hj_product_picture(PRODUCT_ID,PRODUCT_URL,PRODUCT_VIEW_URL,PICTURE_ID) values(?,?,?,?)");
            //插入ss_hj_product_relation
            psHJProductRelation = con.prepareStatement("insert into ss_hj_product_relation(PRODUCT_ID,AUTOID,RELATION_ID) values(?,?,?)");
            rs = psHJSearchResult.executeQuery();

            while (rs.next()) {
                //此处处理业务逻辑
                count++;
                if(rs.getInt("value_id")==0)continue;//品牌为空不导入
                //聚合商品
                logger.info("insert into ss_hj_product(PRODUCT_ID,CLS_ID,PRODUCT_NAME,MAIN_PICTURE,SALES_VOLUME,COMMENTS_NUMBER,STS,PRIORITY,BRAND_ID) values("+productId+","+rs.getInt("classid")+",'"+rs.getString("spname").replaceAll("'","’")+"','"+rs.getString("sppic")+"',0,0,1,"+productId+","+rs.getInt("value_id")+");");
                //图片
                logger.info("insert into ss_hj_product_picture(PRODUCT_ID,PRODUCT_URL,PRODUCT_VIEW_URL,PICTURE_ID) values("+productId+",'"+rs.getString("sppic")+"','"+rs.getString("sppic")+"',"+pictureId+");");
                //关系表
                logger.info("insert into ss_hj_product_relation(PRODUCT_ID,AUTOID,RELATION_ID) values("+productId+","+rs.getInt("id")+","+relationId+");");
                //id增加
                productId++;
                pictureId++;
                relationId++;
            }
        } catch (SQLException e) {
            logger.error("productId:"+productId);
            logger.error("pictureId:"+pictureId);
            logger.error("relationId:"+relationId);
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
