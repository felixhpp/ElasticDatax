package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONReader;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.entity.RestResult;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.ResultUtil;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.jobs.analysis.CaseRecodrXmlBean;
import com.example.demo.jobs.analysis.CaseRecordXmlAnaly;
import com.example.demo.jobs.hbase.HBaseBulkProcessor;
import com.example.demo.service.DefaultDicMapService;
import com.example.demo.service.ElasticBulkService;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class DemoTestController {
    private int TOTAL = 10000;
    private boolean isDev = false;

    @Autowired
    private DefaultDicMapService defaultDicMapService;

    @Autowired
    private ElasticBulkService elasticBulkService;

//    @Autowired
//    private HBaseBulkProcessor hBaseBulkProcessor;

    @GetMapping("bulk/patient")
    public RestResult bulkPatient() throws Exception {
        List<Map<String, Object>> maps = buildPatientData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.PATIENT);

        exeBulk(maps, ElasticTypeEnum.PATIENT);
        return ResultUtil.success(reList);
    }

    @GetMapping("convert/patient")
    public RestResult convertPatient() throws Exception {
        List<Map<String, Object>> maps = buildPatientData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.PATIENT);
        return ResultUtil.success(reList);
    }

    @GetMapping("convert/medicalrecord")
    public RestResult convertAdm() throws Exception {
        List<Map<String, Object>> maps = buildAdmData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.MEDICAL_RECORD);
        return ResultUtil.success(reList);
    }

    @GetMapping("bulk/medicalrecord")
    public RestResult bulkAdm() throws Exception {
        List<Map<String, Object>> maps = buildAdmData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.MEDICAL_RECORD);
        exeBulk(maps, ElasticTypeEnum.MEDICAL_RECORD);
        return ResultUtil.success(reList);
    }

    /**
     * 数据转换测试
     *
     * @return
     */
    @GetMapping("convert/diagnose")
    public RestResult convertDiagnose() throws Exception {
        List<Map<String, Object>> maps = buildDiagnoseData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.DIAGNOSE);
        return ResultUtil.success(reList);
    }

    @GetMapping("bulk/diagnose")
    public RestResult bulkDiagnose() throws Exception {
        List<Map<String, Object>> maps = buildDiagnoseData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.DIAGNOSE);
        exeBulk(maps, ElasticTypeEnum.DIAGNOSE);
        return ResultUtil.success(reList);
    }

    @GetMapping("convert/orditem")
    public RestResult convertOrdItem() throws Exception {
        List<Map<String, Object>> maps = buildOrdItemData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.ORDITEM);


        return ResultUtil.success(reList);
    }

    @GetMapping("convert/suggestion")
    public RestResult suggestion() {
        BulkResponseBody result = elasticBulkService.bulkSuggestion();
        return ResultUtil.success(result);
    }

    @GetMapping("bulk/orditem")
    public RestResult bulkOrdItem() throws Exception {
        List<Map<String, Object>> maps = buildOrdItemData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.ORDITEM);

        BulkResponseBody responseBody = exeBulk(maps, ElasticTypeEnum.ORDITEM);
        return ResultUtil.success(responseBody);
    }

    @GetMapping("convert/lisitem")
    public RestResult convertLisItem() throws Exception {
        List<Map<String, Object>> maps = buildLisitemData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.Lisitem);
        return ResultUtil.success(reList);
    }

    @GetMapping("bulk/lisitem")
    public RestResult bulkLisItem() throws Exception {
        List<Map<String, Object>> maps = buildLisitemData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.Lisitem);
        exeBulk(maps, ElasticTypeEnum.Lisitem);
        return ResultUtil.success(reList);
    }

    @GetMapping("convert/ordris")
    public RestResult convertOrdRis() throws Exception {
        List<Map<String, Object>> maps = buildOrdRisData();
        List<Object> reList = exeConvert(maps, ElasticTypeEnum.OrdRis);
        return ResultUtil.success(reList);
    }

    @GetMapping("convert/case/{fileName}")
    public RestResult convertCaseRecord(@PathVariable String fileName) throws Exception {
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        String curfileName = "ryjl-all.xml";
        ElasticTypeEnum typeEnum = ElasticTypeEnum.Residentadmitnote;
        switch (fileName){
            case "ryjl":
            case "ryjl.xml":
                curfileName = "ryjl-all.xml";
                typeEnum = ElasticTypeEnum.Residentadmitnote;
                break;
            case "basy":
            case "basy.xml":
            case "basy1":
                curfileName = "bnsy1.xml";
                typeEnum = ElasticTypeEnum.MedicalRecordHomePage;
                break;
            case "basy2":
            case "basy2.xml":
                curfileName = "bnsy2.xml";
                typeEnum = ElasticTypeEnum.MedicalRecordHomePage;
                break;
            default:break;
        }
        File file = new File(System.getProperty("user.dir") + "/" + curfileName);
        if (!file.exists()) {
            file = new File(ResourceUtils.getURL("classpath:" + fileName).getPath());
        }

        Document document = reader.read(file);
        CaseRecodrXmlBean bean = CaseRecordXmlAnaly.analyCaseRecordXml(document, typeEnum);
        Map<String, Object> maps = bean.getAnalyResult();
        maps.put("documentid", "11111");
        ESBulkModel bulkMode = ConvertPipeline
                .convertToBulkModel(typeEnum, maps, true);
        //elasticBulkService.bulkCase(bodys);
        return ResultUtil.success(bulkMode);
    }

    @GetMapping("bulk/case/{fileName}")
    public RestResult bulkCaseRecord(@PathVariable String fileName) throws Exception {
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        String curfileName = "ryjl-all.xml";
        ElasticTypeEnum typeEnum = ElasticTypeEnum.Residentadmitnote;
        switch (fileName){
            case "ryjl":
            case "ryjl.xml":
                curfileName = "ryjl-all.xml";
                typeEnum = ElasticTypeEnum.Residentadmitnote;
                break;
            case "basy":
            case "basy.xml":
            case "basy1":
                curfileName = "bnsy1.xml";
                typeEnum = ElasticTypeEnum.MedicalRecordHomePage;
                break;
            case "basy2":
            case "basy2.xml":
                curfileName = "bnsy2.xml";
                typeEnum = ElasticTypeEnum.MedicalRecordHomePage;
                break;
            default:break;
        }
        File file = new File(System.getProperty("user.dir") + File.separator + curfileName);
        if (!file.exists()) {
            file = new File(ResourceUtils.getURL("classpath:" + fileName).getPath());
        }

        Document document = reader.read(file);
        CaseRecodrXmlBean bean = CaseRecordXmlAnaly.analyCaseRecordXml(document, typeEnum);
        Map<String, Object> maps = bean.getAnalyResult();
        maps.put("documentid", "11111");
        ESBulkModel bulkMode = ConvertPipeline
                .convertToBulkModel(typeEnum, maps, true);
        elasticBulkService.bulkCaseTest(bean, typeEnum);
        return ResultUtil.success(bulkMode);
    }

    private List<Map<String, Object>> buildDiagnoseData() {
        List<Map<String, Object>> maps = new ArrayList<>();
        int total = TOTAL;
        String[] diagtype = {"M", "PRE"};
        String[] diagnose = {"1", "2", "3", "", "", ""};
        String[] diagnoseName = {"上感", "支气管炎", "哮喘 AR", "外阴瘙痒", "", "", ""};
        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("id", "123" + i);
            object.put("routing", "1234" + i);
            object.put("diag_id", "1234567" + i);
            object.put("diag_admid", "1234" + i);
            object.put("diag_regno", "1234" + i);
            object.put("diag_date", "2019-01-01 00:00:00");
            object.put("diag_time", "11:01:01");
            if (!isDev) {
                object.put("diag_type_code", getRandom(diagtype));
                object.put("diag_code", getRandom(diagnose));
                object.put("diag_name", getRandom(diagnoseName));
            }
            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> buildPatientData() {
        int total = TOTAL;
        List<Map<String, Object>> maps = new ArrayList<>();
        String[] sexCode = {"F", "M", "I"};
        String[] nationCode = {"01", "02", "03", "04", "04"};
        String[] maritalCode = {"1", "2", "5", "3", "4"};
        String[] birthday = {"", null, "2018-01-01", "2018-10-3 11:11:00", "2018-01-01 11:11:00.0"};
        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("id", "123" + i);
            object.put("routing", "1234" + i);
            object.put("pat_regno", "1234" + i);
            object.put("pat_recordno", "12314" + i);
            object.put("pat_name", "测试姓名" + i);
            object.put("pat_idcard", "141122199309090101" + i);
            if (!isDev) {
                //object.put("pat_gender_code", getRandom(sexCode));
                object.put("pat_gender_code", "");
//                object.put("pat_nation_code", getRandom(nationCode));
                object.put("pat_nation_code", null);
                object.put("pat_marital_code", getRandom(maritalCode));
            }

            object.put("pat_birthday", getRandom(birthday));
//            object.put("pat_birthday", null);

            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> buildAdmData() {
        int total = TOTAL;
        List<Map<String, Object>> maps = new ArrayList<>();
        String[] birthday = {"1990-01-02", "1980-01-02", "1995-11-02", ""};
        String[] hospitalCode = {"HXEY", "1001", "1002"};
        String[] admType = {"O", "E", "I", "H", ""};
        String[] admState = {"A", "C", "D", "P", ""};
        String[] deptCode = {"03071074-IP07400-HX07401B", "03071074-IP07400-HX07403G",
                "03071075-IP07500-HX07501B", "03071075-IP07500-JJ07503G"};

        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("mr_admid", "1234" + i);
            object.put("mr_regno", "12314" + i);

            object.put("mr_admage", "测试姓名" + i);
            object.put("mr_admdate", "2010-01-01 00:00:00");
            object.put("mr_admtime", "10:20:20");
            object.put("mr_dischdate", "2010-11-01 00:0:00");
            object.put("mr_dischtime", "10:20:20");

            if (!isDev) {
                object.put("mr_admhospital_code", getRandom(hospitalCode));
                object.put("mr_admtype_code", getRandom(admType));
                object.put("mr_admdept_code", getRandom(deptCode));
                object.put("mr_dischdept_code", getRandom(deptCode));
                object.put("mr_visitstatus_code", getRandom(admState));
                object.put("mr_birthday", getRandom(birthday));
            }

            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> buildOrdItemData() {
        int total = TOTAL;
        List<Map<String, Object>> maps = new ArrayList<>();
        String[] ordName = {"JZ015", "010101011400001", "11020000117",
                "11020000118", "030205010007", "010702020000001"};
        String[] ord_type = {"S", "OUT", "NORM"};
        String[] ord_status = {"V", "U", "H", "D", "P"};
        String[] ord_cate = {"01", "02", "03", "04"};
        String[] freq_code = {"Tid", "Bid", "Qd", "Qid"};
        String[] ord_duration_code = {"1", "2", "3", "4"};

        String[] deptCode = {"03071074-IP07400-HX07401B", "03071074-IP07400-HX07403G",
                "03071075-IP07500-HX07501B", "03071075-IP07500-JJ07503G"};
        String[] ord_usage = {"口服", "含服", "静脉注射", ""};
        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("ord_id", "123" + i);
            object.put("ord_admno", "1234" + i);
            object.put("ord_regno", "121234" + i);
            object.put("ord_startdate", "2010-01-01 00:00:00");
            object.put("ord_starttime", "10:20:60");
            object.put("ord_enddate", "2010-11-01 00:00:00");
            object.put("ord_endtime", "10:21:60");
            object.put("ord_doseqty", "1");
            if (!isDev) {
                object.put("ord_code", getRandom(ordName));
                object.put("ord_type_code", getRandom(ord_type));
                object.put("ord_status_code", getRandom(ord_status));
                object.put("ord_cate_code", getRandom(ord_cate));
                object.put("ord_freq_code", getRandom(freq_code));
                object.put("ord_duration_code", getRandom(ord_duration_code));
                object.put("ord_dept_code", getRandom(deptCode));
                object.put("ord_execdept_code", getRandom(deptCode));
                object.put("ord_usage_code", getRandom(ord_usage));
            }

            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> buildLisitemData() {
        int total = TOTAL;
        List<Map<String, Object>> maps = new ArrayList<>();
        String[] ordName = {"JZ015", "010101011400001", "11020000117",
                "11020000118", "030205010007", "010702020000001"};
        String[] lisdate = {"20180514", "20180514", "20180514",
                "20180608", "20180618", "20180617"};
        String[] listime = {"021257", "092310", "092309",
                "092310", "092310", "092317"};
        String[] lisValue = {"未见", "查见", "2+","4.8",
                "11.50", "0-3", "-"};
        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("inspection_id", "123"+ i);
            object.put("test_item_id", "a");
            object.put("lis_id", "123" + i);
            object.put("lis_admno", "1234" + i);
            object.put("lis_regno", "121234" + i);
            object.put("lis_name", "性激素基础水平测定" + i);
            object.put("lis_value", getRandom(lisValue));
            object.put("lis_result", "");
            object.put("lis_code", "abc");
            object.put("lis_date", getRandom(lisdate));
            object.put("lis_time", getRandom(listime));
            object.put("lis_ordcode", getRandom(ordName));
            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> buildOrdRisData() {
        int total = TOTAL;
        List<Map<String, Object>> maps = new ArrayList<>();
        String[] ordName = {"JZ015", "010101011400001", "11020000117",
                "11020000118", "030205010007", "010702020000001"};
        for (int i = 0; i < total; i++) {
            Map<String, Object> object = new HashMap<>();
            object.put("or_id", "123" + i);
            object.put("or_admno", "1234" + i);
            object.put("or_regno", "121234" + i);
            object.put("check_date", "2018-01-01 10:12:23");
            object.put("or_name", "测试11" + i);
            object.put("or_ordcode", getRandom(ordName));
            object.put("or_desc", "abc");
            object.put("or_result", "sdadadxxxxx");
            maps.add(object);
        }

        return maps;
    }

    private List<Map<String, Object>> strToMap(String dataJsonStr) {
        long startTime = System.currentTimeMillis();   //获取开始时间
        JSONReader reader = new JSONReader(new StringReader(dataJsonStr));//已流的方式处理，这里很快
        reader.startArray();
        List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        int i = 0;
        while (reader.hasNext()) {
            i++;
            reader.startObject();//这边反序列化也是极速
            map = new HashMap<String, Object>();
            while (reader.hasNext()) {
                String arrayListItemKey = reader.readString();
                String arrayListItemValue = reader.readObject().toString();
                map.put(arrayListItemKey, arrayListItemValue);
            }
            rsList.add(map);
            reader.endObject();
        }
        reader.endArray();
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("strToMap耗时：" + (endTime - startTime) + "ms");

        return rsList;
    }

    private List<Map<String, Object>> strToMapBy(String dataJsonStr) {
        long startTime = System.currentTimeMillis();   //获取开始时间

        List<Map<String, Object>> listObjectFir = JSONArray.parseObject(dataJsonStr, List.class);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("strToMap JSON 耗时：" + (endTime - startTime) + "ms");

        return listObjectFir;
    }

    private List<Object> exeConvert(List<Map<String, Object>> maps, ElasticTypeEnum typeEnum) throws Exception {

        List<Object> reList = new ArrayList<>();
        long startTime = System.currentTimeMillis();   //获取开始时间
        List<ESBulkModel> bulkModels = defaultDicMapService.test(maps, typeEnum);

        long start = System.currentTimeMillis();
        // 进行hbase测试
//        for (ESBulkModel m : bulkModels){
//            hBaseBulkProcessor.add(m);
//        }

        long end = System.currentTimeMillis();
        if (bulkModels.size() > 10) {
            reList.addAll(bulkModels.subList(0, 10));
        } else {
            reList.addAll(bulkModels.subList(0, bulkModels.size()));
        }

        if (typeEnum.equals(ElasticTypeEnum.ORDITEM)) {
            int i = 0;
            // 转换药物
            // 转换用药
            for (ESBulkModel bulkModel : bulkModels) {
                ESBulkModel otherModel = ConvertPipeline.convertToBulkModel(ElasticTypeEnum.Medicine,
                        bulkModel.getMapData(), true);
                if (i < 10) {
                    reList.add(otherModel);
                }
                i++;
            }
        }

        long endTime = System.currentTimeMillis(); //获取结束时间
        Map<String, String> m = new HashMap<>();
        m.put("totla", "共有" + bulkModels.size() + "条");
        m.put("exeTime", "执行时间：" + (endTime - startTime) + "ms");
        reList.add(m);

        return reList;
    }

    private BulkResponseBody exeBulk(List<Map<String, Object>> reList, ElasticTypeEnum typeEnum) {
        String dataStr = JSON.toJSONString(reList);
        long startTime = System.currentTimeMillis();   //获取开始时间
        String theme = typeEnum.getTheme();
        BulkResponseBody responseBody = elasticBulkService.bulk(theme, dataStr);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("bulk es耗时：" + (endTime - startTime) + "ms");
        return responseBody;
    }

    private String getRandom(String[] arr) {
        int index = (int) (Math.random() * arr.length);

        return arr[index];
    }
}
