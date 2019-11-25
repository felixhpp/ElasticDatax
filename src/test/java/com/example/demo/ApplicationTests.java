package com.example.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.utils.DateFormatUtil;
import com.example.demo.jobs.Pipeline;

import com.example.demo.jobs.hbase.LisSynTest;
import com.example.demo.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TaskService taskService;
    @Test
    public void test() throws Exception {
        logger.info("输出info log42");
        logger.debug("输出debug log42");
        logger.error("输出error log42");
    }
    @Test
    public void queryLisitem() throws SQLException, IOException, ClassNotFoundException {
        String startDate = "20080101";
        String endDate = "20191231";
        String sql = "SELECT oe_orditem.oeori_itmmast_dr || '_' || lis_inspection_result.english_name as lis_catcode, lis_inspection_result.lis_id AS lis_id, lis_inspection_result.test_item_id AS test_item_id, lis_inspection_result.group_id AS group_id, lis_inspection_result.inspection_date AS lis_date, lis_inspection_result.inspection_time AS lis_time , lis_inspection_result.sample_number AS lis_sample_number, lis_inspection_result.english_name AS lis_code, lis_inspection_result.chinese_name AS lis_name, lis_inspection_result.quantitative_result AS lis_value, lis_inspection_result.qualitative_result AS lis_result , lis_inspection_result.test_item_unit AS lis_unit, lis_inspection_result.dhcchis_id AS his_id, oe_orditem.oeori_rowid AS oeori_rowid, oe_orditem.oeori_itmmast_dr AS lis_ordcode, oe_order.oeord_date AS oeord_date , pa_adm.paadm_rowid AS lis_admno, pa_adm.paadm_admno AS paadm_admno, pa_adm.paadm_papmi_dr AS paadm_papmi_dr, pa_patmas.papmi_no AS lis_regno FROM( select a.inspection_id || a.test_item_id as lis_id, replace(a.his_id,'@','||') as dhcchis_id, * from db002_dsn0013_lis_dbo.lis_inspection_result a where a.inspection_date >= "
                + startDate + " and a.inspection_date <= " + endDate
                + ") lis_inspection_result LEFT JOIN db002_dsn0001_sqluser.oe_orditem oe_orditem ON oe_orditem.oeori_rowid = lis_inspection_result.dhcchis_id LEFT JOIN db002_dsn0001_sqluser.oe_order oe_order ON oe_order.oeord_rowid = oe_orditem.oeori_oeord_parref LEFT JOIN db002_dsn0001_sqluser.pa_adm pa_adm ON pa_adm.paadm_rowid = oe_order.oeord_adm_dr "
                +" LEFT JOIN db002_dsn0001_sqluser.pa_patmas pa_patmas ON pa_patmas.papmi_rowid = pa_adm.paadm_papmi_dr";
        //String all_sql = "SELECT oe_orditem.oeori_itmmast_dr || '_' || lis_inspection_result.english_name as lis_catcode, lis_inspection_result.lis_id AS lis_id, lis_inspection_result.test_item_id AS test_item_id, lis_inspection_result.group_id AS group_id, lis_inspection_result.inspection_date AS lis_date, lis_inspection_result.inspection_time AS lis_time , lis_inspection_result.sample_number AS lis_sample_number, lis_inspection_result.english_name AS lis_code, lis_inspection_result.chinese_name AS lis_name, lis_inspection_result.quantitative_result AS lis_value, lis_inspection_result.qualitative_result AS lis_result , lis_inspection_result.test_item_unit AS lis_unit, lis_inspection_result.dhcchis_id AS his_id, oe_orditem.oeori_rowid AS oeori_rowid, oe_orditem.oeori_itmmast_dr AS lis_ordcode, oe_order.oeord_date AS oeord_date , pa_adm.paadm_rowid AS lis_admno, pa_adm.paadm_admno AS paadm_admno, pa_adm.paadm_papmi_dr AS paadm_papmi_dr, pa_patmas.papmi_no AS lis_regno FROM( select a.inspection_id || a.test_item_id as lis_id, replace(a.his_id,'@','||') as dhcchis_id, * from db002_dsn0013_lis_dbo.lis_inspection_result a ) lis_inspection_result LEFT JOIN db002_dsn0001_sqluser.oe_orditem oe_orditem ON oe_orditem.oeori_rowid = lis_inspection_result.dhcchis_id LEFT JOIN db002_dsn0001_sqluser.oe_order oe_order ON oe_order.oeord_rowid = oe_orditem.oeori_oeord_parref LEFT JOIN db002_dsn0001_sqluser.pa_adm pa_adm ON pa_adm.paadm_rowid = oe_order.oeord_adm_dr LEFT JOIN db002_dsn0001_sqluser.pa_patmas pa_patmas ON pa_patmas.papmi_rowid = pa_adm.paadm_papmi_dr";
        LisSynTest lisSynTest = LisSynTest.getInstance();

        //lisSynTest.testQuery(sql);
    }

    /**
     * 通过对es lisitem 聚合 整理检验项和医嘱大类 关联关系
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
//    @Test
//    public void getSearchItem() throws SQLException, ClassNotFoundException, IOException {
//        SearchItemConfig searchItemConfig = new SearchItemConfig();
//
//        //searchItemConfig.Query();
//    }

    /**
     * 从数据表中导入es 推荐词
     */
//    @Test
//    public void importSuggestIndex() throws SQLException, ClassNotFoundException {
//        String sql = "SELECT a.ID AS ID, a.Name AS Name FROM SuggestDics a WHERE a.UpdateTime = 0";
//
//        Suggest suggest = new Suggest();
//        suggest.bulkToES(sql);
//
//        System.out.println("sucess");
//    }

    @Test
    public void dateTest() throws Exception{
        long start = System.currentTimeMillis();
        String date1 = "2019-2-1 00:00:00";
        String bron = "2016-1-2";
        Date sDate = DateFormatUtil.parseDateString(date1, "yyyy-MM-dd");
        Date eDate = DateFormatUtil.parseDateString(bron, "yyyy-MM-dd");
        long d = DateFormatUtil.differentYears(sDate, eDate);
        long end2 = System.currentTimeMillis();
        System.out.println("tool2: " + (end2-start) + "ms");
        long d1 = DateFormatUtil.differentYears(sDate, eDate);
        long end3 = System.currentTimeMillis();
        System.out.println("tool: " + (end3-start) + "ms" + " 间隔" + d);
    }

    @Test
    public void convert() throws Exception {
        Integer i = 100;
        Pipeline mapper = Pipeline.getInstance(ElasticTypeEnum.PATIENT, true);
        for(Integer j = 0; j<i; j++){
            JSONObject object = new JSONObject();
            object.put("id","123" + j);
            object.put("routing", "1234" + j);
            object.put("code1", "dept" + j);
            object.put("name1", "name" + j);

            ESBulkModel model = mapper.mapper(object);
            System.out.println(JSON.toJSON(model));
        }
    }

    /**
     * 没有返回值测试
     */
    @Test
    public void testVoid() {
        for (int i = 0; i < 20; i++) {
            taskService.excutVoidTask(i);
        }
        System.out.println("========主线程执行完毕=========");
    }
    @Test
    public void testReturn() throws InterruptedException, ExecutionException {
        List<Future<String>> lstFuture = new ArrayList<>();// 存放所有的线程，用于获取结果
        for (int i = 0; i < 100; i++) {
            while (true) {
                try {
                    // 线程池超过最大线程数时，会抛出TaskRejectedException，则等待1s，直到不抛出异常为止
                    Future<String> stringFuture = taskService.excuteValueTask(i);
                    lstFuture.add(stringFuture);
                    break;
                } catch (TaskRejectedException e) {
                    System.out.println("线程池满，等待1S。");
                    Thread.sleep(1000);
                }
            }
        }

        // 获取值.get是阻塞式，等待当前线程完成才返回值
        for (Future<String> future : lstFuture) {
            System.out.println(future.get());
        }

        System.out.println("========主线程执行完毕=========");
    }
}
